/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.postgres;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Service to execute JDBC statements.
 * Exception handling and commits are done by this service.
 *
 * Provides method for execution of liquibase changelog.
 */
public class JdbcExecutorService {

   private static final String CHANGELOG_FILE = "server/receiver/postgres/db.changelog-master.xml";

   private Supplier<Connection> connectionSupplier;

   public JdbcExecutorService( Supplier<Connection> connectionSuplier ) {
      this.connectionSupplier = connectionSuplier;
   }

   /**
    * The given runner will be executed by this method by passing a valid jdbc connection.
    * The caller does not have handle connection related actions.
    * Commit, close and exception handling is done by this method.
    *
    * @param runner executes the given runner
    */
   public void execute( ExecutorCommand<Connection> runner ) {
      Connection connection = null;
      try {
         connection = getConnection();
         connection.setAutoCommit( false );
         runner.accept( connection );
         connection.commit();
      } catch ( Exception ex ) {
         throw new RuntimeException( "Data access failed.", ex );
      } finally {
         try {
            if ( connection != null ) {
               connection.close();
            }
         } catch ( SQLException e ) {
            //ignore
         }
      }
   }

   private Connection getConnection() throws SQLException {
      return connectionSupplier.get();
   }

   @FunctionalInterface
   public interface ExecutorCommand<T> {
      void accept( T t ) throws Exception;
   }

   /**
    * Executes the database changelog using liquibase.
    */
   void executeChangeLog() {
      try {
         Class.forName( "org.postgresql.Driver" );
      } catch ( ClassNotFoundException e ) {
         throw new RuntimeException( "No driver for postgres found", e );
      }
      execute( connection -> {
         Database database = DatabaseFactory.getInstance()
                                            .findCorrectDatabaseImplementation( new JdbcConnection( connection ) );
         Liquibase liquibase = new liquibase.Liquibase( CHANGELOG_FILE, new ClassLoaderResourceAccessor(), database );
         liquibase.update( new Contexts(), new LabelExpression() );
      } );
   }

}
