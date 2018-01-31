/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

import io.vertx.core.json.JsonObject;
import server.receiver.Receiver;
import server.receiver.ReceiverFactory;

/**
 *  Provides methods to create needed dependencies.
 */
public class DependencyProvider {

   private JsonObject config;

   public DependencyProvider( JsonObject config ) {
      this.config = config;
   }

   public Receiver getReceiver() {
      return createReceiver( config );
   }

   /**
    * Creates a {@link Receiver} based on the property "persistence.system".
    *
    * If the property is postgres - a postgres receiver will returned.
    * Default receiver is the influxDb receiver.
    *
    * @param config - the config object
    * @return a receiver instance
    */
   private Receiver createReceiver( JsonObject config ) {
      String persistence = config.getString( "persistence.system", "influxdb" );
      if ( persistence.equals( "postgres" ) ) {
         return ReceiverFactory.createPostgresReceiver( getConnection() );
      }
      return ReceiverFactory.createInfluxBbReceiver( config );
   }

   /**
    * Returns a connection supplier. The supplier returns new Connection by every {@link Supplier#get()}
    *
    * @return Supplier which creates new connection when {@link Supplier#get()} is called.
    */
   public Supplier<Connection> getConnection() {
      String user = config.getString( "postgresDb.user" );
      String url = config.getString( "postgresDb.url" );
      String password = config.getString( "postgresDb.password" );
      return () -> {
         try {
            return DriverManager.getConnection( url, user, password );
         } catch ( SQLException e ) {
            throw new RuntimeException( "Failed to get connection", e );
         }
      };
   }
}
