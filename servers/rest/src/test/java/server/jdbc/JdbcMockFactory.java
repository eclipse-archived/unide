/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.jdbc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.ParameterReference;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * Provides the possibility to mock and stub JDBC statements.
 * Currently only INSERT statements are supported.
 *
 * Statements are delegated to a http server. The http server can provide stub data based on the received statement.
 * This can be archived for instance using WireMock.
 */
public class JdbcMockFactory {

   public static Connection getMockConnection( String stubUrl ) {
      return (Connection) Proxy.newProxyInstance( Connection.class.getClassLoader(), new Class[] { Connection.class },
            new ConnectionInvocationHandler( stubUrl, new MockConnection() ) );
   }

   private static class BaseMethodInvocationHandler implements InvocationHandler {
      private final Object target;
      private final Map<MethodKey, Method> methods;

      BaseMethodInvocationHandler( Object target ) {
         this.target = target;
         this.methods = getMethods( target.getClass() );
      }

      public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
         if ( args == null || args.length == 0 ) {
            return methods.get( new MethodKey( method.getName(), method.getParameterTypes() ) ).invoke( target );
         }
         return methods.get( new MethodKey( method.getName(), method.getParameterTypes() ) )
                       .invoke( target, args );
      }

      private static Map<MethodKey, Method> getMethods( Class<?> clazz ) {
         Method[] methods = clazz.getMethods();
         return Arrays.stream( methods ).collect(
               Collectors
                     .toMap( entry -> new MethodKey( entry.getName(), entry.getParameterTypes() ), entry -> entry ) );
      }

      private static class MethodKey {
         final String methodName;
         final Class<?>[] parameterTypes;

         MethodKey( String methodName, Class[] parameterTypes ) {
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
         }

         @Override
         public boolean equals( Object o ) {
            if ( this == o ) {
               return true;
            }
            if ( !(o instanceof MethodKey) ) {
               return false;
            }
            MethodKey methodKey = (MethodKey) o;
            if ( !methodName.equals( methodKey.methodName ) ) {
               return false;
            }
            return Arrays.equals( parameterTypes, methodKey.parameterTypes );
         }

         @Override
         public int hashCode() {
            int result = methodName.hashCode();
            result = 31 * result + Arrays.hashCode( parameterTypes );
            return result;
         }
      }
   }

   private static class ConnectionInvocationHandler extends BaseMethodInvocationHandler {

      private final Connection connection;
      private final Map<String, Method> methods;
      private final String stubUrl;

      ConnectionInvocationHandler( String stubUrl, Connection connection ) {
         super( connection );
         this.stubUrl = stubUrl;
         this.connection = connection;
         this.methods = new HashMap<>();
         Arrays.stream( connection.getClass().getMethods() )
               .forEach( method -> this.methods.put( method.getName(), method ) );
      }

      @Override
      public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
         if ( "prepareStatement".equals( method.getName() ) ) {
            String sqlStatement = (String) args[0];
            return Proxy.newProxyInstance( PreparedStatement.class.getClassLoader(),
                  new Class[] { PreparedStatement.class },
                  new MockPreparedStatementHandler( stubUrl, new MockPreparedStatement( connection, sqlStatement ) ) );
         }
         return super.invoke( proxy, method, args );
      }

   }

   private static class MockPreparedStatementHandler extends BaseMethodInvocationHandler {

      private final MockPreparedStatement mockPreparedStatement;
      private final String stubUrl;
      private final MediaType RESPONSE_MEDIA_TYPE = MediaType.parse( "application/text" );

      MockPreparedStatementHandler( String stubUrl, MockPreparedStatement mockPreparedStatement ) {
         super( mockPreparedStatement );
         this.stubUrl = stubUrl;
         this.mockPreparedStatement = mockPreparedStatement;
      }

      @Override
      public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
         if ( "executeUpdate".equals( method.getName() ) ) {
            String responseBody = sendRequest();
            return parseToUpdateResult( responseBody );
         }
         return super.invoke( proxy, method, args );
      }

      private static int parseToUpdateResult( String responseBody ) {
         try {
            return Integer.parseInt( responseBody );
         } catch ( NumberFormatException e ) {
            throw new RuntimeException( "For executeUpdate only Number are possible. Value was: '" + responseBody + "'",
                  e );
         }
      }

      private String sendRequest() throws IOException {
         Request.Builder post = new Request.Builder()
               .url( stubUrl )
               .headers( Headers.of( getIndexedParameters() ) )
               .post( RequestBody.create( RESPONSE_MEDIA_TYPE, mockPreparedStatement.getSQL() ) );
         OkHttpClient okHttpClient = new OkHttpClient();
         Request request = post.build();
         Response response = okHttpClient.newCall( request ).execute();
         if ( response.isSuccessful() ) {
            return new String( response.body().bytes(), StandardCharsets.UTF_8 );
         } else {
            throw new RuntimeException(
                  "No stub for url: '" + request.url() + "' body: '" + bodyToString( request ) + "' headers: '"
                        + request.headers() + "' found." );
         }
      }

      private static String bodyToString( final Request request ) {
         if ( request.body() == null ) {
            return "";
         }
         try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo( buffer );
            return buffer.readUtf8();
         } catch ( final IOException e ) {
            return "Failed to extract body from request";
         }
      }

      private Map<String, String> getIndexedParameters() {
         Set<Map.Entry<ParameterReference, Object>> entries = mockPreparedStatement
               .getIndexedParameterMap().entrySet();

         if ( entries.isEmpty() ) {
            return new HashMap<>();
         }
         return entries
               .stream().collect( Collectors.toMap( entry -> String.valueOf( getIndex( entry.getKey() ) ),
                     entry -> entry.getValue() != null ? entry.getValue().toString() : "null" ) );
      }

      private static String getIndex( ParameterReference parameterReference ) {
         String indexValue = parameterReference.toString()
                                               .replace( "ParameterIndex{index=", "" )
                                               .replace( "}", "" );
         return String.valueOf( Integer.parseInt( indexValue ) );
      }
   }

}
