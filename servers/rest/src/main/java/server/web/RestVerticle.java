/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.web;

import java.net.HttpURLConnection;
import java.util.function.Predicate;

import javax.xml.bind.ValidationException;

import org.apache.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import server.ppmp.PpmpEvent;
import server.ppmp.PpmpEventCodec;
import server.ppmp.PpmpType;
import server.ppmp.PpmpValidator;

public class RestVerticle extends AbstractVerticle {

   private static final Logger LOG = Logger.getLogger( RestVerticle.class );

   /**
    * Method starts router for all web
    */
   @Override
   public void start() {
      vertx.eventBus().registerDefaultCodec( PpmpEvent.class, new PpmpEventCodec() );
      Router router = Router.router( vertx );
      router.route().handler( BodyHandler.create() );
      router.post( "/rest/v2/" ).handler( this::handlePpmpRequest );
      router.post( "/rest/v2/validate" ).handler( this::handleValidationRequest );
      router.post( "/rest/v2/message" ).handler( this::handlePpmpMessageRequest );
      router.post( "/rest/v2/measurement" ).handler( this::handlePpmpMeasurementRequest );
      router.post( "/rest/v2/process" ).handler( this::handlePpmpProcessRequest );
      router.get( "/*" ).handler( StaticHandler.create().setIndexPage( "documentation.html" ) );
      vertx.createHttpServer().requestHandler( router::accept ).listen( config().getInteger( "http.port", 80 ) );
   }

   private void handleRequest( RoutingContext routingContext, Predicate<PpmpType> condition ) {
      String ppmpPayload = routingContext.getBodyAsString();
      try {
         PpmpType ppmpType = PpmpValidator.getPpmpType( ppmpPayload );
         if ( condition.test( ppmpType ) ) {
            PpmpEvent ppmpEvent = PpmpValidator.validate( ppmpPayload );
            LOG.info( "Going to process ppmp message for type '" + ppmpEvent.getPpmpType() + "'." );
            sendPpmpEvent( routingContext, ppmpEvent );
         } else {
            throw new ValidationException( "Type of message does not match this route." );
         }
      } catch ( ValidationException ex ) {
         //If validation fails, HTTP-Response 400 is sent
         if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Failed to process message: '" + ppmpPayload + "'", ex );
         } else {
            LOG.info( "Failed to process message due to validation failure. Validation Message is: '" + ex.getMessage()
                  + "'." );
         }
         //If validation fails, HTTP-Response 400 is sent
         routingContext.response().setStatusCode( HttpURLConnection.HTTP_BAD_REQUEST )
                       .putHeader( "content-type", "text/plain" )
                       .end( ex.getMessage() );
      } catch ( Exception ex ) {
         LOG.error( "Failed to process '" + ppmpPayload + "'", ex );
         routingContext.response().setStatusCode( HttpURLConnection.HTTP_INTERNAL_ERROR )
                       .putHeader( "content-type", "text/plain" )
                       .end( "Something went wrong." );
      }
   }

   /**
    * Handle all PPMP messages. This can be done as the message type (content-spec) attribute is present in the payload.
    *
    * @param routingContext - the routing context
    */
   private void handlePpmpRequest( RoutingContext routingContext ) {
      handleRequest( routingContext, ppmpType -> true );
   }

   /**
    * Handle all PPMP Messages of type {@link PpmpType#PROCESS}
    *
    * @param routingContext - the routing context
    */
   private void handlePpmpProcessRequest( RoutingContext routingContext ) {
      handleRequest( routingContext, ppmpType -> ppmpType.equals( PpmpType.PROCESS ) );
   }

   /**
    * Handle all PPMP Messages of type {@link PpmpType#MEASUREMENT}
    *
    * @param routingContext - the routing context
    */
   private void handlePpmpMeasurementRequest( RoutingContext routingContext ) {
      handleRequest( routingContext, ppmpType -> ppmpType.equals( PpmpType.MEASUREMENT ) );
   }

   /**
    * Handle all PPMP Messages of type {@link PpmpType#MESSAGE}
    *
    * @param routingContext - the routing context
    */
   private void handlePpmpMessageRequest( RoutingContext routingContext ) {
      handleRequest( routingContext, ppmpType -> ppmpType.equals( PpmpType.MESSAGE ) );
   }

   /**
    * Validates the payload. Data will not persisted. This endpoint can be used to only validate a PPMP Message.
    *
    * @param routingContext - the routing context
    */
   private void handleValidationRequest( RoutingContext routingContext ) {
      String ppmpPayload = routingContext.getBodyAsString();
      try {
         PpmpEvent ppmpEvent = PpmpValidator.validate( ppmpPayload );
         routingContext.response().setStatusCode( 200 ).putHeader( "content-type", "text/plain" )
                       .end( "Ppmp Message of type '" + ppmpEvent.getPpmpType() + "' is valid" );
         LOG.info( "Message of type '" + ppmpEvent.getPpmpType() + "' successful validated." );
      } catch ( ValidationException ex ) {
         //If validation fails, HTTP-Response 400 is sent
         if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Failed to process message: '" + ppmpPayload + "'", ex );
         } else {
            LOG.info( "Failed to process message due to validation failure. Validation Message is: '" + ex.getMessage()
                  + "'" );
         }

         routingContext.response().setStatusCode( 400 ).putHeader( "content-type", "text/plain" )
                       .end( ex.getMessage() );
      }
   }

   /**
    * Sends the PPMP Message over the EventBus. Consumers on the used EventBus address can process further.
    *
    * @param routingContext - the routing context
    * @param ppmpEvent - the PPMP Event to send over eventbus
    */
   private void sendPpmpEvent( RoutingContext routingContext, PpmpEvent ppmpEvent ) {
      vertx.eventBus().send( Addresses.SEND_PPMP_EVENT, ppmpEvent, (Handler<AsyncResult<Message<String>>>) event -> {
         if ( event.succeeded() ) {
            LOG.info( "Message of type '" + ppmpEvent.getPpmpType() + "' successful processed." );
            routingContext.response().putHeader( "content-type", "application/json" ).end();
         } else {
            Throwable throwable = event.cause();
            LOG.error( "Failed to process message: '" + ppmpEvent.getPayload() + "'", throwable );
            routingContext.response().setStatusCode( HttpURLConnection.HTTP_INTERNAL_ERROR )
                          .putHeader( "content-type", "text/plain" )
                          .end( "Failed to process " + ppmpEvent.toString() );

         }
      } );
   }
}
