/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.influxdb;

import java.util.function.Consumer;

import org.eclipse.iot.unide.ppmp.messages.MessagesWrapper;
import org.influxdb.InfluxDB;

/**
 * Base class for influxDb {@link Consumer}
 *
 * @param <T> - Type of Object to consume
 */
abstract class AbstractInfluxDbConsumer<T> implements Consumer<T> {
   private InfluxDB influxDB;
   private String databaseName;

   /**
    * @param influxDB - the influxDb instance
    * @param databaseName - the name of the database to use for this {@link Consumer}
    */
   AbstractInfluxDbConsumer( InfluxDB influxDB, String databaseName ) {
      this.influxDB = influxDB;
      this.databaseName = databaseName;
   }

   /**
    * @return the database name
    */
   String getDatabaseName() {
      return databaseName;
   }

   /**
    * @return the influxDb instanz
    */
   InfluxDB getInfluxDb() {
      return influxDB;
   }

   /**
    * @param object - object to check
    * @return true when given object is not null
    */
   static boolean isNotNull( Object object ) {
      return null != object;
   }
}
