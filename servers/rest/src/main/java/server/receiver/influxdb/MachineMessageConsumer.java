/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.influxdb;

import java.util.concurrent.TimeUnit;

import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.messages.Message;
import org.eclipse.iot.unide.ppmp.messages.MessagesWrapper;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

/**
 * Consumer class for Message
 */
class MachineMessageConsumer extends AbstractInfluxDbConsumer<MessagesWrapper> {

   private static final String MEASUREMENT_NAME = "ppmp_messages";

   MachineMessageConsumer( InfluxDB connection, String databaseName ) {
      super( connection, databaseName );
   }

   /**
    * Inserts a single Message-Array with device information
    *
    * @param message Message-Array as PPMP java binding object
    * @param device Device as PPMP java binding object
    */
   private void insert( Message message, Device device ) {
      BatchPoints batchPoints = BatchPoints
            .database( getDatabaseName() )
            .consistency( InfluxDB.ConsistencyLevel.ALL )
            .build();
      Builder builder = Point.measurement( MEASUREMENT_NAME )
                             .time( message.getTimestamp().toInstant().toEpochMilli(), TimeUnit.MILLISECONDS );

      setTags( builder, message, device );
      setFields( builder, message );

      Point point = builder.build();
      batchPoints.point( point );
      getInfluxDb().write( batchPoints );
   }

   private void setTags( Builder builder, Message message, Device device ) {
      builder.tag( "deviceId", device.getDeviceID() );
      builder.tag( "code", message.getCode() );
   }

   @Override
   public void accept( MessagesWrapper data ) {
      Device device = data.getDevice();
      data.getMessages().forEach( message -> insert( message, device ) );
   }

   /**
    * Sets the message information for the DB-pointer
    *
    * @param builder - the builder
    * @param message Message as PPMP java binding object
    */
   private void setFields( Builder builder, Message message ) {
      if ( isNotNull( message.getOrigin() ) ) {
         builder.addField( "origin", message.getOrigin() );
      }

      if ( isNotNull( message.getSeverity() ) ) {
         builder.addField( "severity", message.getSeverity().name() );
      }

      if ( isNotNull( message.getTitle() ) ) {
         builder.addField( "title", message.getTitle() );
      }

      if ( isNotNull( message.getDescription() ) ) {
         builder.addField( "description", message.getDescription() );
      }

      if ( isNotNull( message.getHint() ) ) {
         builder.addField( "hint", message.getHint() );
      }

      if ( isNotNull( message.getType() ) ) {
         builder.addField( "type", message.getType().name() );
      }
   }

}
