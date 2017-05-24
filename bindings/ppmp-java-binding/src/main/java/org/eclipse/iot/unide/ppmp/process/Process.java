/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp.process;

import java.time.OffsetDateTime;

import org.eclipse.iot.unide.ppmp.commons.MetaData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Process {
	
	public enum Result {
		OK, NOK, UNKNOWN
	};

	@JsonProperty("ts")
	private OffsetDateTime timestamp;

	@JsonProperty("result")
	private Result result; // optional
	
	@JsonProperty("externalProcessId")
	private String externalProcessId; // optional

	@JsonProperty("shutoffPhase")
	private String shutoffPhase; // optional
	
	@JsonProperty("program")
	private Program program; // optional

	@JsonProperty("shutoffValues")
	private ShutOffValuesMap shutOffValuesMap; // optional
	
	@JsonProperty("metaData")
	private MetaData metaData; // optional

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public String getExternalProcessId() {
		return externalProcessId;
	}

	public void setExternalProcessId(String externalProcessId) {
		this.externalProcessId = externalProcessId;
	}
	
	public String getShutoffPhase() {
		return shutoffPhase;
	}

	public void setShutoffPhase(String shutoffPhase) {
		this.shutoffPhase = shutoffPhase;
	}

	public ShutOffValuesMap getShutOffValuesMap() {
		return shutOffValuesMap;
	}

	public void setShutOffValuesMap(ShutOffValuesMap shutOffValues) {
		this.shutOffValuesMap = shutOffValues;
	}
	
	public Program getProgram() {
		return program;
	}

	public void setProgram(Program program) {
		this.program = program;
	}
	
	public MetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}
}
