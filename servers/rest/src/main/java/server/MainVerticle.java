/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import server.receiver.Receiver;
import server.receiver.ReceiverFactory;
import server.receiver.ReceiverVerticle;
import server.web.RestVerticle;

public class MainVerticle extends AbstractVerticle {

   private static final Logger LOG = LoggerFactory.getLogger( MainVerticle.class );

   private DependencyProvider dependencyProvider;

   public MainVerticle() {
   }

   public MainVerticle( DependencyProvider dependencyProvider ) {
      this.dependencyProvider = dependencyProvider;
   }

   @Override
   public void start( Future<Void> startFuture ) throws Exception {
      if ( dependencyProvider == null ) {
         dependencyProvider = new DependencyProvider( config() );
      }

      boolean enablePersistence = config().getBoolean( "enable.persistence", true );
      if ( enablePersistence ) {
         CompositeFuture deployFuture = CompositeFuture.all( deployRestVerticle(), deployReceiverVerticle() );
         deployFuture.setHandler( deploymentHandler( startFuture ) );
      } else {
         Future<String> future = deployRestVerticle();
         future.setHandler( deploymentHandler( startFuture ) );
      }
   }

   private <T> Handler<AsyncResult<T>> deploymentHandler( Future<Void> startFuture ) {
      return handler -> {
         if ( handler.failed() ) {
            LOG.error( "Failed to start application.", handler.cause() );
            startFuture.fail( handler.cause() );
         }
         LOG.info( "Application started successful." );
         startFuture.complete();
      };
   }

   private DeploymentOptions getDeploymentOptions() {
      return new DeploymentOptions().setConfig( config() );
   }

   private Future<String> deployRestVerticle() {
      Future<String> future = Future.future();
      vertx.deployVerticle( new RestVerticle(), getDeploymentOptions(), future.completer() );
      return future;
   }

   private Future<String> deployReceiverVerticle() {
      Future<String> future = Future.future();
      Receiver receiver = dependencyProvider.getReceiver();
      vertx.deployVerticle( new ReceiverVerticle( receiver ),
            getDeploymentOptions().setWorker( true ), future.completer() );
      return future;
   }

}
