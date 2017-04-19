/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.endpoints;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.everit.json.schema.ValidationException;

import server.json.ExceptionMapper;
import server.json.Validator;
import server.endpoints.receivers.IMachineMessageReceiver;

/**
 * Provides HTTP-endpoints for Machine Messages for storaging in a database
 * Stores incoming data in a database
 */
@Path("/v2/message") 
public class MachineMessage {
	
	/** 
	* The logger. 
	*/
	private static final Logger LOG = Logger.getLogger( MachineMessage.class );
	
	/**
	 * Provides an endpoint for a POST-request 
	 * Validation is an optional parameter
	 */
	@POST
	@Consumes()
	@Produces(MediaType.APPLICATION_JSON)
	public Response postMessage(String incomingData
			, @QueryParam("validate") boolean validation) {
		
		try{	
		
			if (validation) {
				
				try {
					Validator.getInstance().validate(incomingData);				       

					for(IMachineMessageReceiver receiver : DatabaseStoraging.getMachineMessageReceivers()){
						receiver.receive(incomingData);
					}			
					
					return Response
							.ok()						
							.header("Access-Control-Allow-Origin", "*")
	    		            .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
	    		            .header("Access-Control-Allow-Credentials", "true")
	    		            .header("Access-Control-Allow-Methods", "POST")
	    		            .build();
				}				
				
				catch (ValidationException e) {
					
					LOG.debug(e.getMessage());
					return new ExceptionMapper().toResponse(e);
					
				}
					
			}
			else{
				
				for(IMachineMessageReceiver receiver : DatabaseStoraging.getMachineMessageReceivers()){
					receiver.receive(incomingData);
				}
				
				return Response
						.ok()
						.header("Access-Control-Allow-Origin", "*")
    		            .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
    		            .header("Access-Control-Allow-Credentials", "true")
    		            .header("Access-Control-Allow-Methods", "POST")
    		            .build();
				
			}
			
						
		} catch (IOException ex) {
			
			LOG.debug("Failed to receive message", ex);
			return new ExceptionMapper().toResponse(ex);
			
		}
	}
	
}