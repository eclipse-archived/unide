/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Thrown indicates that a JSON-string is incorrect to the schema 
 */
class ExceptionJson extends IllegalArgumentException{

	private static final long serialVersionUID = 1L;

	@JsonProperty("message")
    private String message;
	
	@JsonProperty("type")
    private String type;
    
    protected ExceptionJson(Exception e){
    	this.message = e.getMessage();
    	this.type = e.getClass().getName();
    }
    
    /**
     * Gets the exception message
     */
	public String getMessage() {
		return message;
	}
	
	/**
     * Sets the exception message
     */
	protected void setMessage(String message) {
		this.message = message;
	}
	
	/**
     * Gets the exception type
     */
	protected String getType() {
		return type;
	}
	
	/**
     * sets the exception type
     */
	protected void setType(String type) {
		this.type = type;
	}
    
    
}
