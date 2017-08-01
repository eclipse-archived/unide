/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp.process;

import java.util.Arrays;
import java.util.List;

import org.eclipse.iot.unide.ppmp.mapper.SingleValueAsArrayUnwrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Limits {
	
	@JsonProperty("upperError")
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonSerialize(using = SingleValueAsArrayUnwrapper.class)
	private List<Number> upperError; // optional
	
	@JsonProperty("lowerError")
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonSerialize(using = SingleValueAsArrayUnwrapper.class)
	private List<Number> lowerError; // optional

	@JsonProperty("upperWarn")
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonSerialize(using = SingleValueAsArrayUnwrapper.class)
	private List<Number> upperWarn; // optional

	@JsonProperty("lowerWarn")
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonSerialize(using = SingleValueAsArrayUnwrapper.class)
	private List<Number> lowerWarn; // optional
	
	@JsonProperty("target")
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonSerialize(using = SingleValueAsArrayUnwrapper.class)
	private List<Number> target; // optional

	public List<Number> getUpperError() {
		return upperError;
	}

	public void setUpperError(List<Number> upperError) {
		this.upperError = upperError;
	}
	
	public void setUpperErrorSingleValue(Number upperError) {
		this.upperError = Arrays.asList(upperError);
	}

	public List<Number> getLowerError() {
		return lowerError;
	}

	public void setLowerError(List<Number> lowerError) {
		this.lowerError = lowerError;
	}
	
	public void setLowerErrorSingleValue(Number lowerError) {
		this.lowerError = Arrays.asList(lowerError);
	}


	public List<Number> getUpperWarn() {
		return upperWarn;
	}

	public void setUpperWarn(List<Number> upperWarn) {
		this.upperWarn = upperWarn;
	}
	
	public void setUpperWarnSingleValue(Number upperWarn) {
		this.upperWarn = Arrays.asList(upperWarn);
	}


	public List<Number> getLowerWarn() {
		return lowerWarn;
	}

	public void setLowerWarn(List<Number> lowerWarn) {
		this.lowerWarn = lowerWarn;
	}
	
	public void setLowerWarnSingleValue(Number lowerWarn) {
		this.lowerWarn = Arrays.asList(lowerWarn);
	}
	
	public List<Number> getTarget() {
		return target;
	}

	public void setTarget(List<Number> target) {
		this.target = target;
	}
	
	public void setTargetSingleValue(Number target) {
		this.target = Arrays.asList(target);
	}

}
