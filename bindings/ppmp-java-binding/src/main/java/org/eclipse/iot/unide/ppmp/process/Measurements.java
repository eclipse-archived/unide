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

public class Measurements {
	public enum Result {
		OK, NOK, UNKNOWN
	};

	@JsonProperty("ts")
	private OffsetDateTime timestamp;

	@JsonProperty("phase")
	private String phase;	
	
	@JsonProperty("name")
	private String name;		
	
	@JsonProperty("result")
	private Result result; // optional

	@JsonProperty("code")
	private String code; // optional

	@JsonProperty("limits")
	private LimitsMap limitsMap; // optional

	@JsonProperty("series")
	private SeriesMap seriesMap;
	
	@JsonProperty("specialValues")
	private SpecialValueMap specialValues;

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
	
	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LimitsMap getLimitsMap() {
		return limitsMap;
	}

	public void setLimitsMap(LimitsMap limits) {
		this.limitsMap = limits;
	}

	public SeriesMap getSeriesMap() {
		return seriesMap;
	}

	public void setSeriesMap(SeriesMap series) {
		this.seriesMap = series;
	}
	
	public SpecialValueMap getSpecialValues() {
		return specialValues;
	}

	public void setSpecialValues(SpecialValueMap specialValues) {
		this.specialValues = specialValues;
	}
}
