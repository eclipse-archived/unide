/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver;

import java.sql.Connection;
import java.util.function.Supplier;

import io.vertx.core.json.JsonObject;
import server.receiver.influxdb.InfluxDbReceiver;
import server.receiver.postgres.PostgresDbReceiver;

/**
 * Factory class for creation of {@link Receiver}
 */
public final class ReceiverFactory {

   /**
    * Creates an {@link Receiver} backed by InfluxDB as persistence.
    *
    * @param config the InfluxDB configs
    * @return a {@link Receiver} backed by InfluxDB as persistence
    */
   public static Receiver createInfluxBbReceiver( JsonObject config ) {
      return new InfluxDbReceiver( config );
   }

   /**
    * Creates an {@link Receiver} backed by PostgresDB as persistence.
    *
    * @param connectionSupplier a supplier for retrieving the db connection
    * @return a {@link Receiver} backed by PostgresDB as persistence
    */
   public static Receiver createPostgresReceiver( Supplier<Connection> connectionSupplier ) {
      return new PostgresDbReceiver( connectionSupplier );
   }

}
