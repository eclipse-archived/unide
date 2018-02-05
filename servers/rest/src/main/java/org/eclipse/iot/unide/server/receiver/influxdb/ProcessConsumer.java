/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.eclipse.iot.unide.server.receiver.influxdb;

import java.util.concurrent.TimeUnit;

import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.process.Process;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;
import org.eclipse.iot.unide.server.receiver.util.PpmpHelper;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

/**
 * Consumer class for Process
 */
class ProcessConsumer extends AbstractInfluxDbConsumer<ProcessWrapper> {

   private static final String MEASUREMENT_NAME = "ppmp_processes";
   private static final String PROGRAM_ID = "program";
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

      String programId = getProgramId( processWrapper.getProcess() );
      if ( isNotNull( programId ) ) {
         pointBuilder.tag( PROGRAM_ID, programId );
      }

      pointBuilder.addField( PAYLOAD, PpmpHelper.toJson( processWrapper ) );
      batchPointsProcess.point( pointBuilder.build() );
      getInfluxDb().write( batchPointsProcess );
   }

   private static String getProgramId( Process process ) {
      if ( process == null || process.getProgram() == null ) {
         return null;
      }
      return process.getProgram().getId();
   }

   @Override
   public void accept( ProcessWrapper data ) {
      insertProcessData( data );
   }
}
