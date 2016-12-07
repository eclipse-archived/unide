/*
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp.measurements;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.iot.unide.ppmp.commons.Device;

public class MeasurementsWrapper {
	@JsonProperty("content-spec")
	private final String contentSpec = "urn:spec://eclipse.org/unide/measurement-message#v2";

	@JsonProperty("device")
	private Device device;

	@JsonProperty("part")
	private Part part; // optional

	@JsonProperty("measurements")
	private List<Measurements> measurements;

	public String getContentSpec() {
		return contentSpec;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Part getPart() {
		return part;
	}

	public void setPart(Part part) {
		this.part = part;
	}

	public List<Measurements> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<Measurements> measurements) {
		this.measurements = measurements;
	}
}
