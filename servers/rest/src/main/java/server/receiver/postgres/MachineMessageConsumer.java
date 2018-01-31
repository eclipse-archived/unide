/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.postgres;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.messages.Message;
import org.eclipse.iot.unide.ppmp.messages.MessagesWrapper;
import org.influxdb.dto.Point.Builder;

/**
 * Consumer class for Message
 */
class MachineMessageConsumer extends AbstractPostgresConsumer<MessagesWrapper> {

   private static final String INSERT_MESSAGE =
         "INSERT INTO ppmp_messages(time, deviceid, code, severity, title, description, hint, type) VALUES(?,?,?,?,?,?,?,?)";

   /**
    * @param executorService - the connection
    */
   MachineMessageConsumer( JdbcExecutorService executorService ) {
      super( executorService );
   }

   @Override
   public void accept( MessagesWrapper data ) {
      Device device = data.getDevice();
      data.getMessages().forEach( message -> insert( message, device ) );
   }

   /**
    * Inserts a single Message-Array with device information
    *
    * @param message Message-Array as PPMP java binding object
    * @param device Device as PPMP java binding object
    */
   private void insert( Message message, Device device ) {
      getExecutorService().execute( connection -> {
         PreparedStatement insertMessage = connection.prepareStatement( INSERT_MESSAGE );
         long time = message.getTimestamp().toInstant().toEpochMilli();
         insertMessage.setTimestamp( 1, new Timestamp( time ) );
         insertMessage.setString( 2, device.getDeviceID() );
         insertMessage.setString( 3, message.getCode() );
         insertMessage.setString( 4, getSeverityName( message.getSeverity() ) );
         insertMessage.setString( 5, message.getTitle() );
         insertMessage.setString( 6, message.getDescription() );
         insertMessage.setString( 7, message.getHint() );
         insertMessage.setString( 8, getType( message.getType() ) );
         insertMessage.executeUpdate();
      } );
   }

   private String getSeverityName( Message.MessageSeverity messageSeverity ) {
      return isNotNull( messageSeverity ) ? messageSeverity.name() : null;
   }

   private String getType( Message.MessageType messageType ) {
      return isNotNull( messageType ) ? messageType.name() : null;
   }
}
