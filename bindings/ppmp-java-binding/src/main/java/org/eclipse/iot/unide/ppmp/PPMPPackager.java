/*
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.iot.unide.ppmp;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.eclipse.iot.unide.ppmp.measurements.MeasurementsWrapper;
import org.eclipse.iot.unide.ppmp.messages.MessagesWrapper;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;

import java.io.IOException;

public class PPMPPackager {

    private ObjectMapper mapper;

    public PPMPPackager() {
        mapper = new ObjectMapper();
        
        mapper.enable(DeserializationFeature. ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.registerModule(new JavaTimeModule());

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    public String getMessage(Object jsonBean) throws IOException {
        String json = mapper.writeValueAsString(jsonBean);
        return json;
    }

    public String getMessage(Object jsonBean, boolean indentOutput) throws JsonProcessingException {
        mapper = setJsonFormat(mapper, indentOutput);
        String json = mapper.writeValueAsString(jsonBean);
        return json;
    }

    private ObjectMapper setJsonFormat(ObjectMapper mapper, boolean indentOutput) {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, indentOutput);
        return mapper;
    }

    public MessagesWrapper getMessagesBean(String jsonString) throws IOException {
        MessagesWrapper bean = null;
        bean = mapper.readValue(jsonString, MessagesWrapper.class);
        return bean;
    }

    public MeasurementsWrapper getMeasurementsBean(String jsonString) throws IOException {
        MeasurementsWrapper bean = null;
        bean = mapper.readValue(jsonString, MeasurementsWrapper.class);
        return bean;
    }
    
    public ProcessWrapper getProcessesBean(String jsonString) throws IOException {
    	ProcessWrapper bean = null;
        bean = mapper.readValue(jsonString, ProcessWrapper.class);
        return bean;
    }
}

