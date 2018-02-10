/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.eclipse.iot.unide.server.receiver.influxdb;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.measurements.Measurements;
import org.eclipse.iot.unide.ppmp.measurements.MeasurementsWrapper;
import org.eclipse.iot.unide.ppmp.process.Process;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;
import org.eclipse.iot.unide.server.receiver.util.PpmpHelper;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

/**
 * Consumer class for Measurement objects
 */
class MeasurementConsumer extends AbstractInfluxDbConsumer<MeasurementsWrapper> {

	private static final String MEASUREMENT_NAME = "ppmp_measurements";

	private static final String DEVICE_ID = "deviceId";
	private static final String MEASUREMENT_POINT = "measurementPoint";
	private static final String MEASUREMENT_VALUE = "value";

	MeasurementConsumer(InfluxDB influxDB, String databaseName) {
		super(influxDB, databaseName);
	}

	private void insertMeasurements(Measurements measurements, Device device) {
		BatchPoints batchPoints = BatchPoints.database(getDatabaseName()).consistency(InfluxDB.ConsistencyLevel.ALL)
				.build();
		List<PpmpHelper.MeasurementValue> measurementValues = PpmpHelper.getMeasurementValues(measurements);
		measurementValues.forEach(measurement -> {
			Builder pointBuilder = Point.measurement(MEASUREMENT_NAME).time(measurement.getTime(),
					TimeUnit.MILLISECONDS);
			setTags(pointBuilder, measurement, device);
			setFields(pointBuilder, measurement);
			batchPoints.point(pointBuilder.build());
		});
		getInfluxDb().write(batchPoints);
	}

	/**
	 * Sets the tag / index information in the db
	 *
	 * @param builder
	 *            a pointer to the database
	 * @param measurement
	 *            a single measurement
	 * @param device
	 *            the device object of the payload
	 */
	private void setTags(Builder builder, PpmpHelper.MeasurementValue measurement, Device device) {
		builder.tag(MEASUREMENT_POINT, measurement.getName());
		builder.tag(DEVICE_ID, device.getDeviceID());
	}

	/**
	 * Sets other, non-indexed information in the db
	 *
	 * @param builder
	 *            a pointer to the database
	 * @param measurement
	 *            a single measurement
	 */
	private void setFields(Builder builder, PpmpHelper.MeasurementValue measurement) {
		builder.addField(MEASUREMENT_VALUE, measurement.getValue());
	}

	@Override
	public void accept(MeasurementsWrapper data) {
		Device device = data.getDevice();
		data.getMeasurements().forEach(measurement -> insertMeasurements(measurement, device));
	}
}
