/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver;

import org.apache.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import server.ppmp.PpmpEvent;
import server.web.Addresses;
import server.web.RestVerticle;

/**
 * Verticle which subscribes to {@link Addresses#SEND_PPMP_EVENT} and processes {@link PpmpEvent}
 */
public class ReceiverVerticle extends AbstractVerticle {
   private static final Logger LOG = Logger.getLogger( RestVerticle.class );

   private final Receiver receiver;


   public ReceiverVerticle( Receiver receiver ) {
      this.receiver = receiver;
   }

   @Override
   public void start( Future<Void> startFuture ) throws Exception {
      try {
         receiver.init();
         registerEventPpmpMessageConsumer();
         startFuture.complete();
      } catch ( Exception e ) {
         startFuture.fail( e );
         throw new RuntimeException( "Failed to start " + ReceiverVerticle.class.getName(), e );
      }
   }

   private void registerEventPpmpMessageConsumer() {
      vertx.eventBus().consumer( Addresses.SEND_PPMP_EVENT, ( Message<PpmpEvent> message ) -> {
         PpmpEvent ppmpEvent = message.body();
         try {
            receiver.handle( ppmpEvent );
            message.reply( "Received" );
         } catch ( ReceiverException e ) {
            LOG.error( "Failed to handle message with type '" + ppmpEvent.getPpmpType() + "'", e);
            message.fail( 500, e.getMessage() );
         }
      } );
   }
}
