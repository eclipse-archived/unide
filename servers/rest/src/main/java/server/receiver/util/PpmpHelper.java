/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.measurements.Measurements;

/**
 * Helper class for Ppmp related logic.
 */
public class PpmpHelper {

   private static final String TIME_FIELD = "$_time";

   /**
    * Converts the given object to Json.
    *
    * For the convert {@link PPMPPackager} is used.
    *
    * @param object - the object to convert
    * @return Json representation of given object
    */
   public static String toJson( Object object ) {
      try {
         PPMPPackager ppmpPackager = new PPMPPackager();
         return ppmpPackager.getMessage( object );
      } catch ( Exception ex ) {
         throw new RuntimeException( "Failed to decode process to json", ex );
      }
   }

   /**
    * A single Measurement message can consist of multiple measurement-value entries.
    * For instance a single Measurement could have 10 temperature measurements.
    * This method extract's all measurement-value entries from the given measurement.
    *
    * @param measurement - the measurement to get measurement-value entries from
    * @return list of all measurement-value entries from given measurement
    */
   public static List<MeasurementValue> getMeasurementValues( Measurements measurement ) {
      Stream<Map.Entry<String, List<Number>>> seriesIterator = measurement.getSeriesMap().getSeries().entrySet()
                                                                          .stream();
      long startTime = measurement.getTimestamp().toInstant().toEpochMilli();
      Map<String, List<Number>> values = seriesIterator.filter( entry -> !TIME_FIELD.equals( entry.getKey() ) )
                                                       .collect( Collectors
                                                             .toMap( Map.Entry::getKey, Map.Entry::getValue ) );
      List<Number> timeLists = measurement.getSeriesMap().getSeries().get( TIME_FIELD );
      return values.entrySet().stream().map( entry ->
            IntStream.range( 0, timeLists.size() ).mapToObj( i -> {
               Number measurementValue = entry.getValue().get( i );
               Number timeOffset = timeLists.get( i );
               long time = startTime + timeOffset.longValue();
               return new MeasurementValue( entry.getKey(), time, measurementValue.doubleValue() );
            } ) ).flatMap( stream -> stream )
                   .collect( Collectors.toList() );
   }

   /**
    * Represents a single measurement-value entry.
    */
   public static class MeasurementValue {
      private String name;
      private long time;
      private double value;

      MeasurementValue( String name, long time, double value ) {
         this.name = name;
         this.time = time;
         this.value = value;
      }

      public String getName() {
         return name;
      }

      public long getTime() {
         return time;
      }

      public double getValue() {
         return value;
      }
   }

}
