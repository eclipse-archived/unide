/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.measurements.Measurements;
import org.eclipse.iot.unide.ppmp.measurements.MeasurementsWrapper;
import org.eclipse.iot.unide.ppmp.measurements.SeriesMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PpmpSimulator {

   public static void main( String[] args ) throws IOException, InterruptedException {

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable( DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY );
      objectMapper.registerModule( new JavaTimeModule() );

      objectMapper.configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );
      objectMapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );

      OkHttpClient okHttpClient = new OkHttpClient();
      int count = 0;
      while ( true ) {
         count = count + 1;
         System.out.println( count + " .Going to send request" );
         MeasurementsWrapper measurements = new MeasurementsWrapper();
         Device device = new Device();
         device.setDeviceID( "33a58b-8350-4592-23121-755194497d" );
         measurements.setDevice( device );
         List<Measurements> measurementsList = new ArrayList<>();
         measurementsList.add( generateMeasurements() );
         measurements.setMeasurements( measurementsList );

         String body = objectMapper.writeValueAsString( measurements );

         Call call = okHttpClient.newCall(
               new Request.Builder().url( new URL( "http://localhost:8090/rest/v2/" ) )
                                    .post( RequestBody.create( MediaType.parse( "application/json" ), body ) )
                                    .build() );
         Response response = call.execute();
         int responseCode = response.code();
         System.out.println( count + " Response send. Code: " + responseCode );
         Thread.sleep( 3000 );
      }

   }

   private static Measurements generateMeasurements() {
      Measurements measurements = new Measurements();

      measurements.setCode( "TEST" );
      measurements.setTimestamp( OffsetDateTime.now() );

      SeriesMap seriesMap = new SeriesMap();
      seriesMap.setSeriesValue( "$_time", randomValues( 0, 1, 10, 90 ) );
      seriesMap.setSeriesValue( "temprature", randomValues( 0, 1, 30,50 ) );
      seriesMap.setSeriesValue( "pressure", randomValues( 0, 1, 40,80 ) );
      measurements.setSeriesMap( seriesMap );

      return measurements;
   }

   private static List<Number> randomValues( int begin, int end, int minValue, int maxValue ) {
      return IntStream.range( begin, end )
                      .map( val -> ThreadLocalRandom.current().nextInt( minValue, maxValue + 1 ) )
                      .boxed().collect( Collectors.toList() );

   }
}
