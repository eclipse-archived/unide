/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.eclipse.iot.unide.server.receiver;

import javax.sql.DataSource;

import org.eclipse.iot.unide.server.receiver.influxdb.InfluxDbReceiver;
import org.eclipse.iot.unide.server.receiver.sql.SqlDbReceiver;

import io.vertx.core.json.JsonObject;

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
    * Creates an {@link Receiver} backed by SQL DB as persistence.
    *
    * @param dataSourceSupplier a supplier for retrieving the db parameter
    * @return a {@link Receiver} backed by SQL DB as persistence
    */
   public static Receiver createSqlReceiver( DataSource dataSource ) {
      return new SqlDbReceiver( dataSource );
   }

}
