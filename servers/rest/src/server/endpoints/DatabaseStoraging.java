/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.endpoints;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import server.endpoints.receivers.IMachineMessageReceiver;
import server.endpoints.receivers.IMeasurementMessageReceiver;
import server.json.ExceptionMapper;
import server.persistency.dao.MachineMessageDAO;
import server.persistency.dao.MeasurementDAO;

/**
 * Provides HTTP-endpoints for activate or deactivate storaging incoming data in a database
 */
@Path("/influxdb") 
public class DatabaseStoraging {
	
	/** 
	* The logger. 
	*/
	private static final Logger LOG = Logger.getLogger( DatabaseStoraging.class );
	
	private static Set<IMachineMessageReceiver> machineMessageReceivers = new HashSet<IMachineMessageReceiver>();
	private static Set<IMeasurementMessageReceiver> measurementMessageReceivers = new HashSet<IMeasurementMessageReceiver>();
	
	static
	{
		addMachineMessageReceiver(new MachineMessageDAO());
		addMeasurementMessageReceiver(new MeasurementDAO());
	}
	
	
	
	private static void addMachineMessageReceiver(IMachineMessageReceiver receiver) {
		if (!machineMessageReceivers.contains(receiver)){
			machineMessageReceivers.add(receiver);
		}		
	}

	private static void removeMachineMessageReceiver(IMachineMessageReceiver receiver) {
		machineMessageReceivers.remove(receiver);
	}
	
	private static void addMeasurementMessageReceiver(IMeasurementMessageReceiver receiver) {
		if (!measurementMessageReceivers.contains(receiver)){
			measurementMessageReceivers.add(receiver);
		}
	}

	private static void removeMeasurementMessageReceiver(IMeasurementMessageReceiver receiver) {
		measurementMessageReceivers.remove(receiver);
	}	
	
	public static Set<IMachineMessageReceiver> getMachineMessageReceivers(){
		return machineMessageReceivers;
	}
	
	public static Set<IMeasurementMessageReceiver> getMeasurementMessageReceivers(){
		return measurementMessageReceivers;
	}
	
	/**
	 * Provides an endpoint for a POST-request 
	 * Validation is an optional parameter
	 */
	@POST
	@Consumes()
	@Produces(MediaType.APPLICATION_JSON)
	public Response postMessage(@QueryParam("active") boolean active) throws Exception{
		
		try{	
		
			if (active) {
				
				addMachineMessageReceiver(new MachineMessageDAO());
				addMeasurementMessageReceiver(new MeasurementDAO());
				
				return Response
						.ok()						
						.header("Access-Control-Allow-Origin", "*")
    		            .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
    		            .header("Access-Control-Allow-Credentials", "true")
    		            .header("Access-Control-Allow-Methods", "POST")
    		            .build();
					
			}
			else{
				
				removeMachineMessageReceiver(new MachineMessageDAO());
				removeMeasurementMessageReceiver(new MeasurementDAO());
				
				return Response
						.ok()
						.header("Access-Control-Allow-Origin", "*")
    		            .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
    		            .header("Access-Control-Allow-Credentials", "true")
    		            .header("Access-Control-Allow-Methods", "POST")
    		            .build();
				
			}
			
						
		} catch (Exception e) {
			
			LOG.debug("Failed to receive message. ", e);
			return new ExceptionMapper().toResponse(e);
			
		}
	}
	
}