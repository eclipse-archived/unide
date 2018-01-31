/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.postgres;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.measurements.MeasurementsWrapper;
import org.eclipse.iot.unide.ppmp.messages.MessagesWrapper;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;

import io.vertx.core.json.JsonObject;
import server.receiver.PpmpEventReceiver;
import server.receiver.ReceiverException;

/**
 * This receiver is backed by a Postgres Database.
 * Received {@link server.ppmp.PpmpEvent}'s are persisted to postgres.
 */
public class PostgresDbReceiver extends PpmpEventReceiver {

   private JdbcExecutorService executorService;

   public PostgresDbReceiver( Supplier<Connection> connectionSupplier ) {
      super( new PPMPPackager() );
      this.executorService = new JdbcExecutorService( connectionSupplier );
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