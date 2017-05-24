/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp.process;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShutOffValues {
	@JsonProperty("upperError")
	private Number upperError; // optional

	@JsonProperty("lowerError")
	private Number lowerError; // optional

	@JsonProperty("value")
	private Number value; // optional
	
	@JsonProperty("ts")
	private OffsetDateTime timestamp; // optional

	public Number getUpperError() {
		return upperError;
	}

	public void setUpperError(Number upperError) {
		this.upperError = upperError;
	}

	public Number getLowerError() {
		return lowerError;
	}

	public void setLowerError(Number lowerError) {
		this.lowerError = lowerError;
	}
	
	public Number getValue() {
		return value;
	}

	public void setValue(Number value) {
		this.value = value;
	}
	
	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}

}
