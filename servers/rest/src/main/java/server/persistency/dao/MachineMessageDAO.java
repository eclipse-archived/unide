/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.persistency.dao;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.messages.Message;
import org.eclipse.iot.unide.ppmp.messages.MessagesWrapper;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import server.endpoints.receivers.IMachineMessageReceiver;
import server.persistency.db.ConnectionFactory;

/**
 * DAO class for Measurement objects of InfluxDB
 *
 */
public class MachineMessageDAO implements IMachineMessageReceiver{
	
	private InfluxDB connection;
	
	public MachineMessageDAO(){
		
	}
	
	/**
	 * Inserts the whole MeasurementWrapper object to the database
	 * 
	 * @param measurements MeasurementWrapper as PPMP java binding object
	 * @throws IOException 
	 */
	private void insert(MessagesWrapper messages) throws IOException {
		
		
		for(Message message : messages.getMessages())
		{
			insert(message, messages.getDevice());
		}
		
	}	
	
	/**
	 * Inserts a PPMP-Message as JSON-string
	 * 
	 * @param jsonStringMessage Message in JSON format
	 * @throws IOException
	 */
	public void insert(String jsonStringMessage) throws IOException {
		
		PPMPPackager pckg = new PPMPPackager();
		MessagesWrapper w = pckg.getMessagesBean(jsonStringMessage.replace("content_spec", "content-spec"));	

		insert(w);		
		
	}	
		
	/**
	 * Inserts a single Message-Array with device information
	 * 
	 * @param message Message-Array as PPMP java binding object 
	 * @param device Device as PPMP java binding object 
	 * @throws IOException 
	 */
	private void insert(Message message, Device device) throws IOException{

		connection = ConnectionFactory.getConnection();
			
		BatchPoints batchPoints = BatchPoints
                .database(ConnectionFactory.getDatabasenameMessages())
                .tag("async", "true")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

		Builder builder = Point.measurement(device.getDeviceID())
	         .time(Timestamp.valueOf(message.getTimestamp().atZoneSameInstant(ZoneOffset.ofHours(1)).toLocalDateTime()).getTime(),TimeUnit.MILLISECONDS);		         
	         
         setDeviceInfoForPointer(builder, device);
         
         setFieldsForPointer(builder, message);
         
         Point point = builder.build();  
		
		batchPoints.point(point); 	    		
    			
        connection.write(batchPoints);
	        		
	}
	
	/**
	 * Sets the device information for the DB-pointer
	 *  
	 * @param pointBuilder InfluxDB PointBuilder
	 * @param device Device as PPMP java binding object
	 */
	private void setDeviceInfoForPointer(Builder pointBuilder, Device device) {
		
		assert !device.getDeviceID().isEmpty();
		
		pointBuilder.tag("DeviceID", device.getDeviceID());

		if (device.getOperationalStatus() != null){
			pointBuilder.tag("DeviceOperationalStatus",device.getOperationalStatus());
		}
			
		if (device.getMetaData() != null){
			pointBuilder.tag("DeviceMetaData",device.getMetaData().toString());
		}
			
	}
	
	/**
	 * Sets the message information for the DB-pointer
	 * 
	 * @param pointBuilder
	 * @param message Message as PPMP java binding object
	 */	
	private void setFieldsForPointer(Builder pointBuilder, Message message){
		
		assert !message.getCode().isEmpty();
		
		pointBuilder.addField("code",message.getCode());

		if (message.getOrigin() != null){
			pointBuilder.addField("origin",message.getOrigin());
		}
			
		if (message.getSeverity() != null){
			pointBuilder.addField("severity",message.getSeverity().toString());
		}
			
		if (message.getTitle() != null){
			pointBuilder.addField("title",message.getTitle());
		}
			
		if (message.getDescription() != null){
			pointBuilder.addField("description",message.getDescription());
		}
			
		if (message.getHint() != null){
			pointBuilder.addField("hint",message.getHint());
		}
			
		if (message.getMetaData() != null){
			pointBuilder.addField("metadata",message.getMetaData().toString());
		}
			

	}

	@Override
	public void receive(String data) throws IOException {		
			
		insert(data);			
		
	}
}
