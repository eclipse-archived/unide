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

public class LimitsMap {
	private Map<String, Limits> limits = new HashMap<>();

	@JsonAnyGetter
	public Map<String, Limits> getLimits() {
		return limits;
	}

	@JsonAnySetter
	public void setLimitsValue(String name, Limits value) {
		limits.put(name, value);
	}

	public void setLimits(Map<String, Limits> limits) {
		this.limits = limits;
	}
}
