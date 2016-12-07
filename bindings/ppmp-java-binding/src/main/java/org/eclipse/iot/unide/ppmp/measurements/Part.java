/*
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp.measurements;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.iot.unide.ppmp.commons.MetaData;
import org.eclipse.iot.unide.ppmp.measurements.Measurements.Result;

public class Part {
	@JsonProperty("partTypeID")
	private String partTypeID; // optional

	@JsonProperty("partID")
	private String partID; // optional

	@JsonProperty("result")
	private Result result; // optional

	@JsonProperty("code")
	private String code; // optional

	@JsonProperty("metaData")
	private MetaData metaData; // optional

	public String getPartTypeID() {
		return partTypeID;
	}

	public void setPartTypeID(String partTypeID) {
		this.partTypeID = partTypeID;
	}

	public String getPartID() {
		return partID;
	}

	public void setPartID(String partID) {
		this.partID = partID;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public MetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}
}
