/*
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp.commons;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class MetaData {
	private Map<String, String> metaData = new HashMap<>();

	@JsonAnyGetter
	public Map<String, String> getMetaData() {
		return metaData;
	}

	@JsonAnySetter
	public void setMetaDataValue(String name, String value) {
		metaData.put(name, value);
	}

	public void setMetaData(Map<String, String> metaData) {
		this.metaData = metaData;
	}
}
