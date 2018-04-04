/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.eclipse.iot.unide.server.receiver.sql;

import java.util.function.Consumer;

import javax.sql.DataSource;

import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.measurements.MeasurementsWrapper;
import org.eclipse.iot.unide.ppmp.messages.MessagesWrapper;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;
import org.eclipse.iot.unide.server.receiver.PpmpEventReceiver;
import org.eclipse.iot.unide.server.receiver.ReceiverException;

/**
 * This receiver is backed by a sql Database.
 * Received {@link org.eclipse.iot.unide.server.ppmp.PpmpEvent}'s are persisted to sql.
 */
public class SqlDbReceiver extends PpmpEventReceiver {

   private JdbcExecutorService executorService;

   public SqlDbReceiver( DataSource dataSource ) {
      super( new PPMPPackager() );
      this.executorService = new JdbcExecutorService( dataSource );
   }

   @Override
   public void init() throws ReceiverException {
      try {
         executorService.executeChangeLog();
      } catch ( Exception e ) {
         throw new ReceiverException( "Failed to execute changeset", e );
      }
   }

   @Override
   protected Consumer<MessagesWrapper> getMessagesConsumer() {
      return new MachineMessageConsumer( executorService );
   }

   @Override
   protected Consumer<MeasurementsWrapper> getMeasurementsConsumer() {
      return new MeasurementConsumer( executorService );
   }

   @Override
   protected Consumer<ProcessWrapper> getProcessesConsumer() {
      return new ProcessConsumer( executorService );
   }

}