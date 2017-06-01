/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.endpoints;

import java.io.IOException;

import javax.xml.bind.ValidationException;

import org.apache.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import server.endpoints.receivers.ReceiverController;
import server.endpoints.receivers.IMachineMessageReceiver;
import server.endpoints.receivers.IMeasurementMessageReceiver;
import server.endpoints.receivers.IProcessMessageReceiver;
import server.json.ExceptionMapper;
import server.json.Validator;

public class RestEndpoints extends AbstractVerticle {
		
	/** 
	* The logger. 
	*/
	private static final Logger LOG = Logger.getLogger( RestEndpoints.class );
	
	/**
	 * Method starts router for all endpoints
	 * @throws IOException 
	 */
	@Override
	public void start() throws IOException {

	    Router router = Router.router(vertx);
	    
	    router.route().handler(BodyHandler.create());
	    
	    router.post("/rest/*").handler(this::checkValidation);	    
	    router.post("/rest/v2/message").handler(this::handleMachineMessage);
	    router.post("/rest/v2/measurement").handler(this::handleMeasurementMessage);
	    router.post("/rest/v2/process").handler(this::handleProcessMessage);
	    router.post("/influxdb").handler(this::handleInfluxDbActivation);
	    
	    router.get("/*").handler(this::handleGet);
	    	    
	    router.route().handler(StaticHandler.create()); 
	    
	    vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 8080));

	}	
	
	/**
	 * Method for handling all GET-Requests
	 * @param routingContext
	 */
	private void handleGet(RoutingContext routingContext) {		
	    routingContext.response().putHeader("content-type", "text/html").end("<h1>This is the service for validating PPMP-messages</h1>");
	  }
	
	/**
	 * Method for checking validation if parameter is set
	 * @param routingContext
	 */
	private void checkValidation(RoutingContext routingContext) {
		
		try {
					
			String validate = routingContext.request().getParam("validate");

			if (validate.toLowerCase().equals("true")){
				Validator.getInstance().validate(routingContext.getBodyAsString());	
			}		
			
			routingContext.next();
			
		} catch (NullPointerException e) {
			
			//Validation parameter not set. No validation necessary
			routingContext.next();			
			
		} catch (ValidationException e) {
			
			//If validation fails, HTTP-Response 400 is sent
			LOG.debug(e.getMessage());
			routingContext.response().setStatusCode(400).putHeader("content-type", "application/json").end(new ExceptionMapper().toResponse(e));
			
		} catch (IOException e) {
		
			LOG.debug(e.getMessage());
			respondText(routingContext,400,e.getMessage());
			
		}	
			    
	  }	
	
	/**
	 * Method for handling machine messages and forwarding them to the specific receiver if this exists
	 * @param routingContext
	 */
	private void handleMachineMessage(RoutingContext routingContext) {					       
			
		for(IMachineMessageReceiver receiver : ReceiverController.getMachineMessageReceivers()){
			receiver.receive(routingContext.getBodyAsString());
		}
		
		routingContext.response().putHeader("content-type", "application/json").end();
		    
	  }
	
	/**
	 * Method for handling measurement messages and forwarding them to the specific receiver if this exists
	 * @param routingContext
	 */
	private void handleMeasurementMessage(RoutingContext routingContext) {
		
		for(IMeasurementMessageReceiver receiver : ReceiverController.getMeasurementMessageReceivers()){
			receiver.receive(routingContext.getBodyAsString());
		}
		
	    routingContext.response().putHeader("content-type", "application/json").end();
	    
	  }
	
	/**
	 * Method for handling process messages and forwarding them to the specific receiver if this exists
	 * @param routingContext
	 */
	private void handleProcessMessage(RoutingContext routingContext) {
		
		for(IProcessMessageReceiver receiver : ReceiverController.getProcessMessageReceivers()){
			receiver.receive(routingContext.getBodyAsString());
		}
		
	    routingContext.response().putHeader("content-type", "application/json").end("");
	    
	  }
	
	/**
	 * Method for enabling or disabling the database connection to an influxDB
	 * @param routingContext
	 */
	private void handleInfluxDbActivation(RoutingContext routingContext) {
		
		try {
			
			String active = routingContext.request().getParam("active");

			if (active.toLowerCase().equals("true")){
				
				ReceiverController.addAllReceivers();
				
				respondText(routingContext,200,"InfluxDB activated");
				
			} else if (active.toLowerCase().equals("false")){
				
				ReceiverController.removeAllReceivers();
				
				respondText(routingContext,200,"InfluxDB deactivated");	
				
			}			
			
		} catch (NullPointerException e) {
			
			respondText(routingContext,400,"Parameter 'active' is missing");	
			
		}
	    
	  }
	
	/**
	 * Method to respond a plain text message
	 * @param routingContext
	 * @param statusCode
	 * @param responseMessage
	 */
	private void respondText(RoutingContext routingContext, int statusCode, String responseMessage){
		routingContext.response().putHeader("content-type", "text/plain").setStatusCode(statusCode).end(responseMessage);	
	}
}
