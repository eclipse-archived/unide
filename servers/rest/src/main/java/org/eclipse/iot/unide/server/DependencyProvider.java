/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.eclipse.iot.unide.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.function.Supplier;

import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.iot.unide.server.receiver.Receiver;
import org.eclipse.iot.unide.server.receiver.ReceiverFactory;

import io.vertx.core.json.JsonObject;


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
    * If the property is sql - a sql receiver will returned.
    * Default receiver is the influxDb receiver.
    *
    * @param config - the config object
    * @return a receiver instance
    */
   private Receiver createReceiver( JsonObject config ) {
      String persistence = config.getString( "persistence.system", "influxdb" );
      if ( persistence.equals( "sql" ) ) {
         return ReceiverFactory.createSqlReceiver( getDataSource() );
      }
      return ReceiverFactory.createInfluxBbReceiver( config );
   }

   /**
    * Returns a data source. The data source returns a Connection on every {@link DataSource#getConnection()}
    *
    * @return DataSource which produces a connection when {@link DataSource#getConnection()} is called.
    */
   public Supplier<Connection> getConnection() {
      DataSource ds = getDataSource();
      return () -> {
    	 // https://docs.oracle.com/javase/tutorial/jdbc/basics/sqldatasources.html
    	 // DataSource is the preferred way to get connections over DriverManager
    	 try {
    		 return ds.getConnection();
    	 } catch ( SQLException e ) {
             throw new RuntimeException( "Failed to get connection", e );
    	 }
      };
   }

   public DataSource getDataSource() {
	   String user = config.getString( "sqlDb.user" );
	   String url = config.getString( "sqlDb.url" );
	   String driver = config.getString( "sqlDb.driver" );
	   String password = config.getString( "sqlDb.password" );
	   BasicDataSource dataSource = new BasicDataSource();
	   if(driver == null) {
		   driver = "org.postgresql.Driver";
	   }  
	   try {
		   Class.forName( driver );
       } catch ( ClassNotFoundException e ) {
          throw new RuntimeException( "No driver for database found", e );
       }
	   dataSource.setDriverClassName( driver );
	   dataSource.setUrl( url );
       dataSource.setUsername( user );
       dataSource.setPassword( password );
       dataSource.setDefaultAutoCommit( false );
       return dataSource;
   }
}
