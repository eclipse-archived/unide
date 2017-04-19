/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.json;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.everit.json.schema.ValidationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Mapper for Parsing an exception to a JSON repsonse
 */
@Provider
public class ExceptionMapper
{
	/** 
	* The logger. 
	*/
	private static final Logger LOG = Logger.getLogger( ExceptionMapper.class );
	
	private static final transient ObjectMapper objectMapper = new ObjectMapper(); 

	/**
	* Building the Response including the header
	*/
	public Response toResponse(final Exception exception)
	{
		ResponseBuilder builder = Response
				.status(Status.BAD_REQUEST)
				.entity(getJson(exception))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "POST")
				.type(MediaType.APPLICATION_JSON);
		return builder.build();
	}
	
	/**
	 * Parsing the exception content to a JSON Format
	 */
	private String getJson(final Exception exception)
	{
		try
		{
				if (exception.getClass() == ValidationException.class)
					return ((ValidationException) exception).toJSON().toString();
				else
				{
					ExceptionJson errorInfo = new ExceptionJson(exception);
		
					return objectMapper.writeValueAsString(errorInfo);
				}		
				
		}
		catch (JsonProcessingException e)
		{
			LOG.info(e.getMessage());
			LOG.debug(e.getStackTrace());
			return "{\"message\":\"An internal error occurred\"}";
		}
	}
}