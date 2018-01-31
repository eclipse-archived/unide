/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.postgres;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;
import org.eclipse.iot.unide.ppmp.process.Program;
import org.postgresql.util.PGobject;

import server.receiver.util.PpmpHelper;

/**
 * Consumer class for Process
 */
class ProcessConsumer extends AbstractPostgresConsumer<ProcessWrapper> {

   private static final String INSERT_PROCESS =
         "INSERT INTO ppmp_processes(time, deviceid, programname, payload) VALUES(?,?,?,to_json(?::json))";

   /**
    * @param executorService - the connection
    */
   ProcessConsumer( JdbcExecutorService executorService ) {
      super( executorService );
   }

   @Override
   public void accept( ProcessWrapper data ) {
      insertProcessData( data );
   }

   /**
    * Inserts the process data
    *
    * @param processWrapper The whole process message
    */
   private void insertProcessData( ProcessWrapper processWrapper ) {
      getExecutorService().execute( connection -> {
         Device device = processWrapper.getDevice();
         String programName = getProgramName( processWrapper.getProcess().getProgram() );
         long time = processWrapper.getProcess().getTimestamp().toInstant().toEpochMilli();

         PreparedStatement insertProcess = connection.prepareStatement( INSERT_PROCESS );
         insertProcess.setTimestamp( 1, new Timestamp( time ) );
         insertProcess.setString( 2, device.getDeviceID() );
         insertProcess.setString( 3, programName );
         insertProcess.setString( 4, PpmpHelper.toJson( processWrapper ) );
         insertProcess.executeUpdate();
      } );
   }

   private static String getProgramName( Program program ) {
      return isNotNull( program ) ? program.getName() : null;
   }

}
