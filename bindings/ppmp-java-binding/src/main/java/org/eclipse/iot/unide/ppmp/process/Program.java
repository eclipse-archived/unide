/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp.process;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Program {
	@JsonProperty("name")
	private String name; // optional

	@JsonProperty("id")
	private String id; // optional
	
	@JsonProperty("lastChangedDate")
	private String lastChangedDate; // optional

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getLastChangedDate() {
		return lastChangedDate;
	}

	public void setLastChangedDate(String lastChangedDate) {
		this.lastChangedDate = lastChangedDate;
	}
	
}
