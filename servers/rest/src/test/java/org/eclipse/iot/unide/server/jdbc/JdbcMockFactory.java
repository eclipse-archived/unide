/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.eclipse.iot.unide.server.jdbc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.util.EntityUtils;


/**
 * Provides the possibility to mock and stub JDBC statements. Currently only
 * INSERT statements are supported.
 *
 * Statements are delegated to a http server. The http server can provide stub
 * data based on the received statement. This can be archived for instance using
 * WireMock.
 */
public class JdbcMockFactory {

	public static Connection getMockConnection(String stubUrl) {
		return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] { Connection.class },
				new ConnectionInvocationHandler(stubUrl, new MockConnection()));
	}

	private static class BaseMethodInvocationHandler implements InvocationHandler {
		private final Object target;
		private final Map<MethodKey, Method> methods;

		BaseMethodInvocationHandler(Object target) {
			this.target = target;
			this.methods = getMethods(target.getClass());
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (args == null || args.length == 0) {
				return methods.get(new MethodKey(method.getName(), method.getParameterTypes())).invoke(target);
			}
			return methods.get(new MethodKey(method.getName(), method.getParameterTypes())).invoke(target, args);
		}

		private static Map<MethodKey, Method> getMethods(Class<?> clazz) {
			Method[] methods = clazz.getMethods();
			return Arrays.stream(methods).collect(Collectors
					.toMap(entry -> new MethodKey(entry.getName(), entry.getParameterTypes()), entry -> entry));
		}

		private static class MethodKey {
			final String methodName;
			final Class<?>[] parameterTypes;

			MethodKey(String methodName, Class[] parameterTypes) {
				this.methodName = methodName;
				this.parameterTypes = parameterTypes;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) {
					return true;
				}
				if (!(o instanceof MethodKey)) {
					return false;
				}
				MethodKey methodKey = (MethodKey) o;
				if (!methodName.equals(methodKey.methodName)) {
					return false;
				}
				return Arrays.equals(parameterTypes, methodKey.parameterTypes);
			}

			@Override
			public int hashCode() {
				int result = methodName.hashCode();
				result = 31 * result + Arrays.hashCode(parameterTypes);
				return result;
			}
		}
	}

	private static class ConnectionInvocationHandler extends BaseMethodInvocationHandler {

		private final Connection connection;
		private final Map<String, Method> methods;
		private final String stubUrl;

		ConnectionInvocationHandler(String stubUrl, Connection connection) {
			super(connection);
			this.stubUrl = stubUrl;
			this.connection = connection;
			this.methods = new HashMap<>();
			Arrays.stream(connection.getClass().getMethods())
					.forEach(method -> this.methods.put(method.getName(), method));
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("prepareStatement".equals(method.getName())) {
				String sqlStatement = (String) args[0];
				return Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
						new Class[] { PreparedStatement.class },
						new MockPreparedStatementHandler(stubUrl, new MockPreparedStatement(connection, sqlStatement)));
			}
			return super.invoke(proxy, method, args);
		}

	}

	private static class MockPreparedStatementHandler extends BaseMethodInvocationHandler {

		private final MockPreparedStatement mockPreparedStatement;
		private final String stubUrl;

		MockPreparedStatementHandler(String stubUrl, MockPreparedStatement mockPreparedStatement) {
			super(mockPreparedStatement);
			this.stubUrl = stubUrl;
			this.mockPreparedStatement = mockPreparedStatement;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("executeUpdate".equals(method.getName())) {
				String responseBody = sendRequest();
				return parseToUpdateResult(responseBody);
			}
			return super.invoke(proxy, method, args);
		}

		private static int parseToUpdateResult(String responseBody) {
			try {
				return Integer.parseInt(responseBody);
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						"For executeUpdate only Number are possible. Value was: '" + responseBody + "'", e);
			}
		}

		private String sendRequest() throws IOException {
			HttpClient client = HttpClientBuilder.create()
					.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE)
					.build();
			HttpPost post = new HttpPost(stubUrl);
			// post.addHeader(arg0, arg1);
			Map<String, String> headers = getIndexedParameters();
			for (String name : headers.keySet()) {
				post.addHeader(name, headers.get(name));
			}
			post.setEntity(new StringEntity( mockPreparedStatement.getSQL() ));
			ResponseHandler<String> handler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					HttpEntity entity = response.getEntity();
					String body = null;
					if( entity != null ) {
						body = EntityUtils.toString(entity);
					}
					if( status != 200 ) {
						throw new RuntimeException("No stub for url: '" + stubUrl + "' body: '" + body + "' found.");
					}
					return body;
				}
			};
			return client.execute(post, handler);
		}

		private Map<String, String> getIndexedParameters() {
			Set<Map.Entry<ParameterReference, Object>> entries = mockPreparedStatement.getIndexedParameterMap()
					.entrySet();

			if (entries.isEmpty()) {
				return new HashMap<>();
			}
			return entries.stream().collect(Collectors.toMap(entry -> String.valueOf(getIndex(entry.getKey())),
					entry -> entry.getValue() != null ? entry.getValue().toString() : "null"));
		}

		private static String getIndex(ParameterReference parameterReference) {
			String indexValue = parameterReference.toString().replace("ParameterIndex{index=", "").replace("}", "");
			return String.valueOf(Integer.parseInt(indexValue));
		}
	}

}
