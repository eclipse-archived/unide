/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.endpoints;

import java.io.IOException;
import java.util.Base64;

import javax.xml.bind.ValidationException;

import org.apache.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import server.endpoints.receivers.ReceiverController;
import server.endpoints.receivers.IMachineMessageReceiver;
import server.endpoints.receivers.IMeasurementMessageReceiver;
import server.endpoints.receivers.IProcessMessageReceiver;
import server.json.ExceptionMapper;
import server.json.Validator;
import server.persistency.db.ConnectionFactory;

public class RestEndpoints extends AbstractVerticle {
		
	private Router router;
	
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

		try {
						
			router = Router.router(vertx);
	    
		    router.route().handler(BodyHandler.create());
		    
		    router.post("/rest/basicauth/*").handler(this::handleAuthentication);
		    router.post("/rest/*").handler(this::checkValidation);		    
		    
		    router.post("/rest/v2/message").handler(this::handleMachineMessage);
		    router.post("/rest/v2/measurement").handler(this::handleMeasurementMessage);
		    router.post("/rest/v2/process").handler(this::handleProcessMessage);
		    router.post("/ppm/v2/measurement").handler(this::handleMeasurementMessage);
		    	
		    router.post("/rest/basicauth/v2/message").handler(this::handleMachineMessage);
		    router.post("/rest/basicauth/v2/measurement").handler(this::handleMeasurementMessage);
		    router.post("/rest/basicauth/v2/process").handler(this::handleProcessMessage);
		    
		    router.post("/influxdb").handler(this::handleInfluxDbActivation); 
		    
		    router.get("/*").handler(this::handleGet);	 
		    
		    LOG.debug("Initializing DB-configuration");
		    ConnectionFactory.getInstance().setConfiguration(config().getJsonObject("database"));
		    
		    vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 80));
		} catch (IOException e) {
			LOG.error(e.getMessage());
			throw e;
		}
	    

	}	
	
	/**
	 * Method for handling all GET-Requests
	 * @param routingContext
	 */
	private void handleGet(RoutingContext routingContext) {		
	    routingContext.response().putHeader("content-type", "text/html").end("<h1>This is the service for validating PPMP-messages</h1>"+
"<h2>The following endpoints are available</h2>"+
"<p>"+
"<ul>"+
"<li><a>http://[host]:[port]/rest/v2/message?validate=true</a></li>"+
"<li><a>http://[host]:[port]/rest/v2/measurement?validate=true</a></li>"+
"<li><a>http://[host]:[port]/rest/v2/process?validate=true</a></li>"+
"<li><a>http://[host]:[port]/rest/basicauth/v2/message?validate=true</a></li>"+
"<li><a>http://[host]:[port]/rest/basicauth/v2/measurement?validate=true</a></li>"+
"<li><a>http://[host]:[port]/rest/basicauth/v2/process?validate=true</a></li>"+
"</ul>" +
"</p>");
	  }

	/**
	 * Method for handling authentication
	 * @param routingContext
	 */
	private void handleAuthentication(RoutingContext routingContext) {		
		//Getting authentication information from the header
		String[] authHeader = routingContext.request().getHeader("Authorization").split(" ");
		
		//Expected credentials are user: unide and password: unide
		String expectedHash = Base64.getEncoder().encodeToString("unide:unide".getBytes());
		
		//Supported authentication mechanism is basic authentication
		if (authHeader[0].equals("Basic")){
			
			if (authHeader[1].equals(expectedHash)){
				routingContext.next();
			}
			else{
				routingContext.response()
				.putHeader("content-type", "text/html")
				.setStatusCode(401)
				.end("Unauthorized");
				LOG.debug("HTTP-Status: " + routingContext.response().getStatusCode());
			}	
			
		} else	{			
			routingContext.response()
			.putHeader("content-type", "text/html")
			.setStatusCode(401)
			.end("Unauthorized");				
			LOG.debug("HTTP-Status: " + routingContext.response().getStatusCode());			
		}			    
		
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
			LOG.debug("HTTP-Status: " + routingContext.response().getStatusCode());
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
		
		try {
			for(IMachineMessageReceiver receiver : ReceiverController.getMachineMessageReceivers()){			
				receiver.receive(routingContext.getBodyAsString());			
			}
		} catch (IOException e) {
			respondText(routingContext,400,"Message not valid to store to database. Please check validation by adding parameter validate=\"true\" to your request! \n Error: " + e.getMessage());
			LOG.info("Error when handling Process Message", e);
		}
		
		routingContext.response().putHeader("content-type", "application/json").end();
		    
	  }
	
	/**
	 * Method for handling measurement messages and forwarding them to the specific receiver if this exists
	 * @param routingContext
	 */
	private void handleMeasurementMessage(RoutingContext routingContext) {
		
		try {			
			for(IMeasurementMessageReceiver receiver : ReceiverController.getMeasurementMessageReceivers()){			
				receiver.receive(routingContext.getBodyAsString());			
			}			
		} catch (IOException e) {
			respondText(routingContext,400,"Message not valid to store to database. Please check validation by adding parameter validate=\"true\" to your request! \n Error: " + e.getMessage());
			LOG.info("Error when handling Process Message", e);
		}
		
	    routingContext.response().putHeader("content-type", "application/json").end();
	    
	  }
	
	/**
	 * Method for handling process messages and forwarding them to the specific receiver if this exists
	 * @param routingContext
	 * @throws IOException 
	 */
	private void handleProcessMessage(RoutingContext routingContext) {
		
		try {			
			for(IProcessMessageReceiver receiver : ReceiverController.getProcessMessageReceivers()){				
				receiver.receive(routingContext.getBodyAsString());				
			}		
		} catch (IOException e) {				
			respondText(routingContext,400,"Message not valid to store to database. Please check validation by adding parameter validate=\"true\" to your request! \nError: " + e.getMessage());
			LOG.info("Error when handling Process Message", e);
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
