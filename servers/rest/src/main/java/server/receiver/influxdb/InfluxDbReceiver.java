/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.influxdb;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.measurements.MeasurementsWrapper;
import org.eclipse.iot.unide.ppmp.messages.MessagesWrapper;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import io.vertx.core.json.JsonObject;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.Buffer;
import server.receiver.PpmpEventReceiver;
import server.receiver.ReceiverException;

/**
 * Manages connections to InfluxDB
 */
public final class InfluxDbReceiver extends PpmpEventReceiver {

   private InfluxDB influxDb;

   public InfluxDbReceiver( JsonObject jsonObject ) {
      super( new PPMPPackager() );
      InfluxDbProperties influxDbProperties = new InfluxDbProperties( jsonObject );
      this.influxDb = InfluxDBFactory
            .connect( influxDbProperties.url, influxDbProperties.user, influxDbProperties.password,
                  new OkHttpClientFactory().createClient() );
   }

   @Override
   public void init() throws ReceiverException {
      influxDb.createDatabase( InfluxDbProperties.MESSAGES_DB_NAME );
      influxDb.createDatabase( InfluxDbProperties.MEASUREMENTS_DB_NAME );
      influxDb.createDatabase( InfluxDbProperties.PROCESSES_DB_NAME );
   }

   @Override
   protected Consumer<MessagesWrapper> getMessagesConsumer() {
      return new MachineMessageConsumer( influxDb, InfluxDbProperties.MESSAGES_DB_NAME );
   }

   @Override
   protected Consumer<MeasurementsWrapper> getMeasurementsConsumer() {
      return new MeasurementConsumer( influxDb, InfluxDbProperties.MEASUREMENTS_DB_NAME );
   }

   @Override
   protected Consumer<ProcessWrapper> getProcessesConsumer() {
      return new ProcessConsumer( influxDb, InfluxDbProperties.PROCESSES_DB_NAME );
   }

   private static class OkHttpClientFactory {
      private static final Logger LOG = Logger.getLogger( InfluxDbReceiver.class );

      OkHttpClient.Builder createClient() {
         return new OkHttpClient.Builder().addInterceptor( getRequestLoggingInterceptor() );
      }

      private Interceptor getRequestLoggingInterceptor() {
         return chain -> {
            if ( LOG.isDebugEnabled() ) {
               Request request = chain.request();
               String body = bodyToString( request );
               LOG.debug( "Url: '" + request.url() + "' Headers: '" + request.headers() + "' Body: '" + body + "'" );
            }
            return chain.proceed( chain.request() );
         };
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
   }

}