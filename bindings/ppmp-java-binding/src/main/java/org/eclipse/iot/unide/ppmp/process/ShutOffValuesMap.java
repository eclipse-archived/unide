/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp.process;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class ShutOffValuesMap {
	private Map<String, ShutOffValues> shutOffValues = new HashMap<>();

	@JsonAnyGetter
	public Map<String, ShutOffValues> getShutOffValues() {
		return shutOffValues;
	}

	@JsonAnySetter
	public void setShutOffValue(String name, ShutOffValues value) {
		shutOffValues.put(name, value);
	}

	public void setShutOffValues(Map<String, ShutOffValues> shutOffValues) {
		this.shutOffValues = shutOffValues;
	}
}
