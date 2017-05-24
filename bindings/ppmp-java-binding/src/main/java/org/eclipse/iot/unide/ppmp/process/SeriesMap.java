/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class SeriesMap {
	private Map<String, List<Number>> series = new HashMap<>();

	public SeriesMap(){
		
	}
	
	@JsonAnyGetter
	public Map<String, List<Number>> getSeries() {
		return series;
	}

	/**
	 * To represents the time sequence of a measurement, add an entry with
	 * "$_time" as a key. Its values are relative time-offsets to the given
	 * timestamp. Please refer to the documentation of the interface for
	 * details.
	 */
	@JsonAnySetter
	public void setSeriesValue(String name, List<Number> value) {
		series.put(name, value);
	}

	public void setSeries(Map<String, List<Number>> series) {
		this.series = series;
	}
}
