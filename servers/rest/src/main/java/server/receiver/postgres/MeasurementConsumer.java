/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.postgres;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.measurements.Measurements;
import org.eclipse.iot.unide.ppmp.measurements.MeasurementsWrapper;

import server.receiver.util.PpmpHelper;

/**
 * Consumer class for Measurement objects
 */
class MeasurementConsumer extends AbstractPostgresConsumer<MeasurementsWrapper> {

   private static final String INSERT_MEASUREMENT =
         "INSERT INTO ppmp_measurements(time, deviceid, measurementpoint, value) VALUES(?,?,?,?)";

   MeasurementConsumer( JdbcExecutorService executorService ) {
      super( executorService );
   }

   @Override
   public void accept( MeasurementsWrapper data ) {
      Device device = data.getDevice();
      data.getMeasurements().forEach( measurement -> insertSingleMeasurement( measurement, device ) );
   }

   private void insertSingleMeasurement( Measurements measurements, Device device ) {
      List<PpmpHelper.MeasurementValue> measurementValues = PpmpHelper.getMeasurementValues( measurements );
      final String deviceID = device.getDeviceID();
      getExecutorService().execute( connection ->
            measurementValues.forEach( measurement -> {
                     try {
                        PreparedStatement statement = connection.prepareStatement( INSERT_MEASUREMENT );
                        statement.setTimestamp( 1, toTimeStamp( measurement.getTime() ) );
                        statement.setString( 2, deviceID );
                        statement.setString( 3, measurement.getName() );
                        statement.setDouble( 4, measurement.getValue() );
                        statement.executeUpdate();
                     } catch ( SQLException e ) {
                        throw new RuntimeException( e );
                     }
                  }
            )
      );
   }

   private static Timestamp toTimeStamp( long timestamp ) {
      return new Timestamp( timestamp );
   }

}
