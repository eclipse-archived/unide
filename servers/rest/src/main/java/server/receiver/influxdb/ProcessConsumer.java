/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.influxdb;

import java.util.concurrent.TimeUnit;

import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.process.Process;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import server.receiver.util.PpmpHelper;

/**
 * Consumer class for Process
 */
class ProcessConsumer extends AbstractInfluxDbConsumer<ProcessWrapper> {

   private static final String MEASUREMENT_NAME = "ppmp_processes";
   private static final String PROGRAMM_NAME = "programm";
   private static final String PAYLOAD = "payload";

   ProcessConsumer( InfluxDB influxDB, String databaseName ) {
      super( influxDB, databaseName );
   }

   /**
    * Inserts the process data
    *
    * @param processWrapper The whole process message
    */
   private void insertProcessData( ProcessWrapper processWrapper ) {

      BatchPoints batchPointsProcess = BatchPoints
            .database( getDatabaseName() )
            .consistency( InfluxDB.ConsistencyLevel.ALL )
            .build();

      long startTime = processWrapper.getProcess().getTimestamp().toInstant().toEpochMilli();
      Builder pointBuilder = Point.measurement( MEASUREMENT_NAME )
                                  .time( startTime, TimeUnit.MILLISECONDS );

      String programName = getProgrammName( processWrapper.getProcess() );
      if ( isNotNull( programName ) ) {
         pointBuilder.tag( PROGRAMM_NAME, programName );
      }

      pointBuilder.addField( PAYLOAD, PpmpHelper.toJson( processWrapper ) );
      batchPointsProcess.point( pointBuilder.build() );
      getInfluxDb().write( batchPointsProcess );
   }

   private static String getProgrammName( Process process ) {
      if ( process == null || process.getProgram() == null ) {
         return null;
      }
      return process.getProgram().getName();
   }

   @Override
   public void accept( ProcessWrapper data ) {
      insertProcessData( data );
   }
}
