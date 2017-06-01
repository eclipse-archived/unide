/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.json;

import javax.xml.bind.ValidationException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Mapper for Parsing an exception to a JSON repsonse
 */
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
	public String toResponse(final Exception exception)
	{
		return getJson(exception);
	}
	
	/**
	 * Parsing the exception content to a JSON Format
	 */
	private String getJson(final Exception exception)
	{
		try
		{
			if (exception.getClass() == ValidationException.class)
				return ((ValidationException) exception).toString();
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