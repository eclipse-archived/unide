/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.commons.MetaData;
import org.eclipse.iot.unide.ppmp.process.Limits;
import org.eclipse.iot.unide.ppmp.process.LimitsMap;
import org.eclipse.iot.unide.ppmp.process.Measurements;
import org.eclipse.iot.unide.ppmp.process.Part;
import org.eclipse.iot.unide.ppmp.process.Part.Type;
import org.eclipse.iot.unide.ppmp.process.Process;
import org.eclipse.iot.unide.ppmp.process.Process.Result;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;
import org.eclipse.iot.unide.ppmp.process.Program;
import org.eclipse.iot.unide.ppmp.process.SeriesMap;
import org.eclipse.iot.unide.ppmp.process.ShutOffValues;
import org.eclipse.iot.unide.ppmp.process.ShutOffValuesMap;
import org.eclipse.iot.unide.ppmp.process.SpecialValue;
import org.eclipse.iot.unide.ppmp.process.SpecialValueMap;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Testclass for the process message java binding
 * @author FIH1IMB
 *
 */
public class ProcessTest {

	private static final String TEST_FILE_NAME = "process.json";
	private static String PROCESS_TEST_MESSAGE;

	@BeforeClass
	public static void readFileContent() throws IOException {
		ClassLoader classLoader = ProcessTest.class.getClassLoader();
		PROCESS_TEST_MESSAGE = IOUtils.toString(classLoader.getResourceAsStream(TEST_FILE_NAME),"UTF-8");
	}


	@Test
	public void convertFullMessagesExampleToJson() throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		PPMPPackager packager = new PPMPPackager();
		ProcessWrapper wrapper = createProcessWrapperWithFullData();
		String result = packager.getMessage(wrapper, true);

		System.out.println(result);

		JSONAssert.assertEquals(PROCESS_TEST_MESSAGE, result, false);
	}
	
	@Test
	public void convertMeasurementsJsonToJava() throws JsonParseException, JsonMappingException, IOException {
		PPMPPackager packager = new PPMPPackager();

		ProcessWrapper wrapperResult = packager.getProcessesBean(PROCESS_TEST_MESSAGE);

		assertNotNull(wrapperResult);

		Process process = wrapperResult.getProcess();
		assertEquals("b4927dad-58d4-4580-b460-79cefd56775b",  process.getExternalProcessId());

	}

	/**
	 * Creates the ProcessWrapper object with a setup of all parameters
	 * @return
	 */
	private ProcessWrapper createProcessWrapperWithFullData() {
		ProcessWrapper wrapper = new ProcessWrapper();
		wrapper.setDevice(createDevice());
		wrapper.setProcess(createProcessExample());
		wrapper.setPart(createPart());
		wrapper.setMeasurements(Arrays.asList(createMeasurementsExample1(),createMeasurementsExample2()));

		return wrapper;
	}

	/**
	 * Creates the process object
	 * @return
	 */
	private Process createProcessExample() {
		Process process = new Process();
		process.setTimestamp(OffsetDateTime.parse("2002-05-30T09:30:10.123+02:00"));
		process.setExternalProcessId("b4927dad-58d4-4580-b460-79cefd56775b");
		process.setResult(Result.NOK);
		process.setShutoffPhase("phase 1");
		
		Program program = new Program();
		program.setName("Programm 1");
		program.setId("1");
		program.setLastChangedDate("2002-05-30T09:30:10.123+02:00");
		
		MetaData metaData = new MetaData();
		metaData.setMetaDataValue("name", "Getriebedeckel verschrauben");
		
		process.setMetaData(metaData);
		process.setShutOffValuesMap(createShutOffValues());
		process.setProgram(program);	
		
		return process;
	}

	/**
	 * Creates an example of a device object
	 * @return
	 */
	private Device createDevice() {
		Device device = new Device();
		device.setDeviceID("a4927dad-58d4-4580-b460-79cefd56775b");
		device.setOperationalStatus("normal");
		MetaData metaData = new MetaData();
		metaData.setMetaDataValue("swVersion", "2.0.3.13");
		metaData.setMetaDataValue("swBuildId", "41535");
		device.setMetaData(metaData);
		return device;
	}
	
	/**
	 * Creates an example of a part object
	 * @return
	 */
	private Part createPart() {
		Part part = new Part();
		part.setType(Type.SINGLE);
		part.setCode("HUH289");
		part.setPartID("420003844");
		part.setPartTypeID("F00VH07328");
		part.setResult(org.eclipse.iot.unide.ppmp.process.Part.Result.NOK);
		MetaData metaData = new MetaData();
		metaData.setMetaDataValue("toolId", "32324-432143");
		part.setMetaData(metaData);
		return part;
	}
	
	/**
	 * Creates an example of a ShutOffValuesMap
	 * @return
	 */
	private ShutOffValuesMap createShutOffValues() {
		ShutOffValuesMap shutOffValuesMap = new ShutOffValuesMap();
		
		ShutOffValues shutOffValuesForce = new ShutOffValues();
		shutOffValuesForce.setTimestamp(OffsetDateTime.parse("2002-05-30T09:30:11.123+02:00"));
		shutOffValuesForce.setValue(24);
		shutOffValuesForce.setLowerError(22);
		shutOffValuesForce.setUpperError(26);
		
		ShutOffValues shutOffValuesPressure = new ShutOffValues();
		shutOffValuesPressure.setValue(50);
		shutOffValuesPressure.setLowerError(48);
		shutOffValuesPressure.setUpperError(52);
		
		shutOffValuesMap.setShutOffValue("force", shutOffValuesForce);
		shutOffValuesMap.setShutOffValue("pressure", shutOffValuesPressure);
		
		return shutOffValuesMap;
	}

	/**
	 * Creates an example for a measurement object
	 * @return
	 */
	private Measurements createMeasurementsExample1() {
		Measurements measurements = new Measurements();
		
		measurements.setPhase("phasen name 2");
		measurements.setName("500 Grad links drehen");
		measurements.setTimestamp(OffsetDateTime.parse("2002-05-30T09:30:10.123+02:00"));
		measurements.setResult(Measurements.Result.NOK);
		measurements.setCode("0000 EE01");

		Limits limitsTemperature = new Limits();
		limitsTemperature.setUpperErrorSingleValue(4444);
		limitsTemperature.setLowerErrorSingleValue(44);
		limitsTemperature.setUpperWarnSingleValue(2222);
		limitsTemperature.setLowerWarnSingleValue(46);
		limitsTemperature.setTargetSingleValue(35);
		LimitsMap limitsMapTemperature = new LimitsMap();
		limitsMapTemperature.setLimitsValue("temperature", limitsTemperature);
		measurements.setLimitsMap(limitsMapTemperature);
		
		SpecialValue specialValuePressure = new SpecialValue();
		specialValuePressure.setTime(24);
		specialValuePressure.setValue(44.2432);		
		SpecialValue specialValueForce = new SpecialValue();
		specialValueForce.setTime(24);
		specialValueForce.setValue(24);
		SpecialValueMap specialValueMap = new SpecialValueMap();
		specialValueMap.setSpecialValue("pressure", specialValuePressure);
		specialValueMap.setSpecialValue("force", specialValueForce);
		measurements.setSpecialValues(specialValueMap);

		
		SeriesMap seriesMap1 = new SeriesMap();
		seriesMap1.setSeriesValue("time", Arrays.asList(0, 23, 24));
		seriesMap1.setSeriesValue("temperature", Arrays.asList(45.4243, 46.42342, 44.2432));
		seriesMap1.setSeriesValue("force", Arrays.asList(26, 23, 24));
		seriesMap1.setSeriesValue("pressure", Arrays.asList(52.4, 46.32, 44.2432));
		measurements.setSeriesMap(seriesMap1);
		return measurements;
	}
	
	/**
	 * Creates an example for a measurement object
	 * @return
	 */
	private Measurements createMeasurementsExample2() {
		Measurements measurements = new Measurements();
		
		measurements.setPhase("phasen name");
		measurements.setTimestamp(OffsetDateTime.parse("2002-05-30T09:30:10.123+02:00"));
		measurements.setResult(Measurements.Result.OK);

		Limits limitsForce = new Limits();
		limitsForce.setUpperError(Arrays.asList(27, 24, 25));
		limitsForce.setLowerError(Arrays.asList(25, 22, 23));		
		Limits limitsPressure = new Limits();
		limitsPressure.setUpperError(Arrays.asList(54, 48, 46));
		limitsPressure.setLowerError(Arrays.asList(50, 44, 42));		
		LimitsMap limitsMap = new LimitsMap();
		limitsMap.setLimitsValue("force", limitsForce);
		limitsMap.setLimitsValue("pressure", limitsPressure);
		measurements.setLimitsMap(limitsMap);
		
		SpecialValue specialValuePressure = new SpecialValue();
		specialValuePressure.setValue(24);		
		SpecialValue specialValueForce = new SpecialValue();
		specialValueForce.setValue(50);
		SpecialValueMap specialValueMap = new SpecialValueMap();
		specialValueMap.setSpecialValue("pressure", specialValuePressure);
		specialValueMap.setSpecialValue("force", specialValueForce);
		measurements.setSpecialValues(specialValueMap);

		SeriesMap seriesMap = new SeriesMap();
		seriesMap.setSeriesValue("time", Arrays.asList(30, 36, 42));
		seriesMap.setSeriesValue("temperature", Arrays.asList(45.4243, 46.42342, 44.2432));
		seriesMap.setSeriesValue("force", Arrays.asList(26, 23, 24));
		seriesMap.setSeriesValue("pressure", Arrays.asList(52.4, 46.32, 44.2432));
		measurements.setSeriesMap(seriesMap);
		
		return measurements;
	}

}
