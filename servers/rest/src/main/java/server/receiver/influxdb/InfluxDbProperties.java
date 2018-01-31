/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.influxdb;

import io.vertx.core.json.JsonObject;

/**
 * Represents influxDb properties
 */
class InfluxDbProperties {
   static final String MESSAGES_DB_NAME = "Messages";
   static final String MEASUREMENTS_DB_NAME = "Measurements";
   static final String PROCESSES_DB_NAME = "Processes";

   final String url;
   final String user;
   final String password;

   InfluxDbProperties( JsonObject configuration ) {
      this.url = configuration.getString( "influxDb.url" );
      this.user = configuration.getString( "influxDb.user" );
      this.password = configuration.getString( "influxDb.password" );
   }

}
