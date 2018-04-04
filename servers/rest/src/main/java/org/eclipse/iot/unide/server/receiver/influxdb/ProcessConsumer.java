/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.eclipse.iot.unide.server.receiver.influxdb;

import java.util.concurrent.TimeUnit;

import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.process.Process;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;
import org.eclipse.iot.unide.server.receiver.util.PpmpHelper;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

/**
 * Consumer class for Process
 */
class ProcessConsumer extends AbstractInfluxDbConsumer<ProcessWrapper> {

	private static final String DEVICE_ID = "deviceId";
	private static final String MEASUREMENT_NAME = "ppmp_processes";
	private static final String PROGRAM_ID = "program";
	private static final String PAYLOAD = "payload";

	ProcessConsumer(InfluxDB influxDB, String databaseName) {
		super(influxDB, databaseName);
	}

	/**
	 * Inserts the process data
	 *
	 * @param processWrapper
	 *            The whole process message
	 */
	private void insert(ProcessWrapper processWrapper) {

		BatchPoints batchPointsProcess = BatchPoints.database(getDatabaseName())
				.consistency(InfluxDB.ConsistencyLevel.ALL).build();

		long startTime = processWrapper.getProcess().getTimestamp().toInstant().toEpochMilli();
		Builder pointBuilder = Point.measurement(MEASUREMENT_NAME).time(startTime, TimeUnit.MILLISECONDS);

		setTags(pointBuilder, processWrapper.getProcess(), processWrapper.getDevice());
		setFields(pointBuilder, processWrapper);

		batchPointsProcess.point(pointBuilder.build());
		getInfluxDb().write(batchPointsProcess);
	}

	/**
	 * Sets the tag / index information in the db
	 *
	 * @param builder
	 *            a pointer to the database
	 * @param process
	 *            the process data
	 * @param device
	 *            the device object of the payload
	 */
	private void setTags(Builder builder, Process process, Device device) {
		String programId = null;
		if (isNotNull(process) && isNotNull(process.getProgram())) {
			programId = process.getProgram().getId();
			if (isNotNull(programId)) {
				builder.tag(PROGRAM_ID, programId);
			}
		}
		builder.tag(DEVICE_ID, device.getDeviceID());
	}

	/**
	 * Sets other, non-indexed information in the db
	 *
	 * @param builder
	 *            a pointer to the database
	 * @param processWrapper
	 *            the complete payload of the process Message
	 */
	private void setFields(Builder builder, ProcessWrapper processWrapper) {
		builder.addField(PAYLOAD, PpmpHelper.toJson(processWrapper));
	}

	@Override
	public void accept(ProcessWrapper data) {
		insert(data);
	}
}
