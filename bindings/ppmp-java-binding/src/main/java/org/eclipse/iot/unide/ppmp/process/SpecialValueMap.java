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

public class SpecialValueMap {
	private Map<String, SpecialValue> specialValues = new HashMap<>();

	@JsonAnyGetter
	public Map<String, SpecialValue> getSpecialValue() {
		return specialValues;
	}

	@JsonAnySetter
	public void setSpecialValue(String name, SpecialValue value) {
		specialValues.put(name, value);
	}

	public void setSpecialValue(Map<String, SpecialValue> specialValues) {
		this.specialValues = specialValues;
	}
}
