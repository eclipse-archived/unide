/*
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.commons.MetaData;
import org.eclipse.iot.unide.ppmp.messages.Message;
import org.eclipse.iot.unide.ppmp.messages.MessagesWrapper;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;



public class MessagesTest {


	private static final String TEST_FILE_NAME = "message.json";
	private static String MEASUREMENT_TEST_MESSAGE;

	@BeforeClass
	public static void readFileContent() throws IOException {
		ClassLoader classLoader = MeasurementsTest.class.getClassLoader();
		MEASUREMENT_TEST_MESSAGE = IOUtils.toString(classLoader.getResourceAsStream(TEST_FILE_NAME),Charset.defaultCharset());
	}


	@Test
	public void convertMessagesJsonToJava() throws JsonParseException, JsonMappingException, IOException {
		PPMPPackager packager = new PPMPPackager();
		MessagesWrapper wrapperResult = packager.getMessagesBean(MEASUREMENT_TEST_MESSAGE);

		assertNotNull(wrapperResult);

		List<Message> messages = wrapperResult.getMessages();
		assertEquals(2, messages.size());
		assertEquals("190ABT", messages.get(0).getCode());
		assertEquals("33-02", messages.get(1).getCode());
	}

	@Test
	public void convertFullMessagesExampleToJson() throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		PPMPPackager packager = new PPMPPackager();
		MessagesWrapper wrapper = createMessagesWrapperWithFullData();
		String result = packager.getMessage(wrapper, true);

		System.out.println(result);

		JSONAssert.assertEquals(MEASUREMENT_TEST_MESSAGE, result, false);
	}

	private MessagesWrapper createMessagesWrapperWithFullData() {
		MessagesWrapper wrapper = new MessagesWrapper();
		wrapper.setDevice(createDevice());
		wrapper.setMessages(createMessages());

		return wrapper;
	}

	private List<Message> createMessages() {
		List<Message> messages = new LinkedList<>();

		Message message1 = createFullMessageExample();
		Message message2 = createShortMessageExample();

		messages.add(message1);
		messages.add(message2);

		return messages;
	}

	private Message createShortMessageExample() {
		Message message = new Message();
		message.setTimestamp(OffsetDateTime.parse("2002-05-30T09:30:10.125+02:00"));
		message.setType(Message.MessageType.TECHNICAL_INFO);
		message.setSeverity(Message.MessageSeverity.HIGH);
		message.setCode("33-02");
		message.setTitle("Disk size limit reached");
		message.setDescription("Disk size has reached limit. Unable to write log files.");
		return message;
	}

	private Message createFullMessageExample() {
		Message message = new Message();
		message.setOrigin("sensor-id-992.2393.22");
		message.setTimestamp(OffsetDateTime.parse("2002-05-30T09:30:10.123+02:00"));
		message.setType(Message.MessageType.DEVICE);
		message.setSeverity(Message.MessageSeverity.HIGH);
		message.setCode("190ABT");
		message.setTitle("control board damaged");
		message.setDescription("Electronic control board or its electrical connections are damaged");
		message.setHint("Check the control board");
		MetaData metaData = new MetaData();
		metaData.setMetaDataValue("firmware", "20130304_22.020");
		message.setMetaData(metaData);
		return message;
	}

	private Device createDevice() {
		Device device = new Device();
		device.setDeviceID("2ca5158b-8350-4592-bff9-755194497d4e");
		device.setOperationalStatus("normal");
		MetaData metaData = new MetaData();
		metaData.setMetaDataValue("swVersion", "2.0.3.13");
		metaData.setMetaDataValue("swBuildID", "41535");
		device.setMetaData(metaData);
		return device;
	}

}
