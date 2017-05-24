/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp.process;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpecialValue {
	
	@JsonProperty("time")
	private Number time; // optional

	@JsonProperty("value")
	private Number value; // optional

	public Number getTime() {
		return time;
	}

	public void setTime(Number time) {
		this.time = time;
	}
	
	public Number getValue() {
		return value;
	}

	public void setValue(Number value) {
		this.value = value;
	}

}
