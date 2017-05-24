/*
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp;



import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.apache.commons.io.IOUtils;
import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.commons.MetaData;
import org.eclipse.iot.unide.ppmp.measurements.*;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MeasurementsTest {

	private static final String TEST_FILE_NAME = "measurement.json";
	private static String MEASUREMENT_TEST_MESSAGE;

	@BeforeClass
	public static void readFileContent() throws IOException {
		ClassLoader classLoader = MeasurementsTest.class.getClassLoader();
		MEASUREMENT_TEST_MESSAGE = IOUtils.toString(classLoader.getResourceAsStream(TEST_FILE_NAME), Charset.defaultCharset());
	}

	@Test
	public void convertFullMeasurementsExampleToJson() throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		PPMPPackager packager = new PPMPPackager();
		MeasurementsWrapper wrapper = createMeasurementsWrapperWithFullData();
		String result = packager.getMessage(wrapper, true);

		JSONAssert.assertEquals(MEASUREMENT_TEST_MESSAGE, result, false);
	}

	@Test
	public void convertMeasurementsJsonToJava() throws JsonParseException, JsonMappingException, IOException {
		PPMPPackager packager = new PPMPPackager();

		MeasurementsWrapper wrapperResult = packager.getMeasurementsBean(MEASUREMENT_TEST_MESSAGE);

		assertNotNull(wrapperResult);

		List<Measurements> measurements = wrapperResult.getMeasurements();
		assertEquals(2, measurements.size());
		assertEquals(23, measurements.get(0).getSeriesMap().getSeries().get("$_time").get(1));
		assertEquals(44.2432, measurements.get(1).getSeriesMap().getSeries().get("pressure").get(2));

	}

	private MeasurementsWrapper createMeasurementsWrapperWithFullData() {
		MeasurementsWrapper wrapper = new MeasurementsWrapper();
		wrapper.setDevice(createDevice());
		wrapper.setPart(createPart());
		wrapper.setMeasurements(createMeasurements());
		return wrapper;
	}

	private List<Measurements> createMeasurements() {
		List<Measurements> measurements = new LinkedList<>();
		Measurements measurements1 = createMeasurementsExample1();
		Measurements measurements2 = createMeasurementsExample2();
		measurements.add(measurements1);
		measurements.add(measurements2);
		return measurements;
	}

	private Measurements createMeasurementsExample2() {
		Measurements measurements2 = new Measurements();
		measurements2.setTimestamp(OffsetDateTime.parse("2002-05-30T09:30:10.123+02:00"));
		measurements2.setResult(Measurements.Result.OK);

		LimitsMap limitsMap2 = new LimitsMap();
		limitsMap2.setLimitsValue("force", createErrorLimits(25, 20));
		limitsMap2.setLimitsValue("pressure", createErrorLimits(60, 40.4));
		measurements2.setLimitsMap(limitsMap2);

		SeriesMap seriesMap2 = new SeriesMap();
		seriesMap2.setSeriesValue("force", Arrays.asList(26, 23, 24));
		seriesMap2.setSeriesValue("pressure", Arrays.asList(52.4, 46.32, 44.2432));
		measurements2.setSeriesMap(seriesMap2);
		return measurements2;
	}

	private Measurements createMeasurementsExample1() {
		Measurements measurements1 = new Measurements();
		measurements1.setTimestamp(OffsetDateTime.parse("2002-05-30T09:30:10.123+02:00"));
		measurements1.setResult(Measurements.Result.NOK);
		measurements1.setCode("0000 EE01");

		LimitsMap limitsMap1 = new LimitsMap();
		limitsMap1.setLimitsValue("temperature", createFullLimits(4444, 44, 2222, 46));
		measurements1.setLimitsMap(limitsMap1);

		SeriesMap seriesMap1 = new SeriesMap();
		seriesMap1.setSeriesValue("$_time", Arrays.asList(0, 23, 24));
		seriesMap1.setSeriesValue("temperature", Arrays.asList(45.4243, 46.42342, 44.2432));
		measurements1.setSeriesMap(seriesMap1);
		return measurements1;
	}

	private MetaData createMetaData(String key1, String value1, String key2, String value2) {
		MetaData metaData = new MetaData();
		metaData.setMetaDataValue(key1, value1);
		metaData.setMetaDataValue(key2, value2);
		return metaData;
	}

	private Limits createFullLimits(Number upperError, Number lowerError, Number upperWarn, Number lowerWarn) {
		Limits limits = createErrorLimits(upperError, lowerError);
		limits.setUpperWarn(upperWarn);
		limits.setLowerWarn(lowerWarn);
		return limits;
	}

	private Limits createErrorLimits(Number upper, Number lower) {
		Limits limits = new Limits();
		limits.setUpperError(upper);
		limits.setLowerError(lower);
		return limits;
	}

	private Part createPart() {
		Part part = new Part();
		part.setPartTypeID("F00VH07328");
		part.setPartID("420003844");
		part.setResult(Measurements.Result.NOK);
		part.setCode("HUH289");
		MetaData metaData = createMetaData("chargeID", "845849", "toolID", "32324-432143");
		part.setMetaData(metaData);
		return part;
	}

	private Device createDevice() {
		Device device = new Device();
		device.setDeviceID("a4927dad-58d4-4580-b460-79cefd56775b");
		device.setOperationalStatus("normal");
		MetaData metaData = createMetaData("swVersion", "2.0.3.13", "swBuildID", "41535");
		device.setMetaData(metaData);
		return device;
	}
}
