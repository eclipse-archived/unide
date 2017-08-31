/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.persistency.dao;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.measurements.Measurements;
import org.eclipse.iot.unide.ppmp.measurements.MeasurementsWrapper;
import org.eclipse.iot.unide.ppmp.measurements.Part;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import server.endpoints.receivers.IMeasurementMessageReceiver;
import server.persistency.db.ConnectionFactory;

/**
 * DAO class for Measurement objects of InfluxDB
 *
 */
public class MeasurementDAO implements IMeasurementMessageReceiver {

	private InfluxDB connection;

	public MeasurementDAO() { 
		
	}
	
	/**
	 * Inserts a PPMP-Message as JSON-string
	 * 
	 * @param JsonStringMessage
	 * @throws IOException
	 */
	public void insert(String JsonStringMessage) throws IOException{
		
		PPMPPackager pckg = new PPMPPackager();
		MeasurementsWrapper w = pckg.getMeasurementsBean(JsonStringMessage.replace("content_spec", "content-spec"));	

		insertMeasurements(w);		
		
	}	
	
	/**
	 * Inserts the whole MeasurementWrapper object to the database
	 * 
	 * @param measurements MeasurementWrapper as PPMP java binding object
	 * @throws IOException 
	 */
	private void insertMeasurements(MeasurementsWrapper measurements) throws IOException {
				
		for(Measurements meas : measurements.getMeasurements())
		{
			insertSingleMeasurement(meas, measurements.getDevice(), measurements.getPart());
		}
		
	}	
	
	/**
	 * Inserts a single Measurment-Array with device information
	 * 
	 * @param measurement Measurment-Array as PPMP java binding object 
	 * @param device Device as PPMP java binding object 
	 * @param part Part as PPMP java binding object
	 * @throws IOException 
	 */
	private void insertSingleMeasurement(Measurements measurement, Device device, Part part) throws IOException {

		connection = ConnectionFactory.getConnection();
		
			BatchPoints batchPoints = BatchPoints
	                .database(ConnectionFactory.getDatabasenameMeasurements())
	                .consistency(InfluxDB.ConsistencyLevel.ALL)
	                .build();
						
			Iterator<Entry<String, List<Number>>> it = measurement.getSeriesMap().getSeries().entrySet().iterator();
			
			List<Number> time_iterator = measurement.getSeriesMap().getSeries().get("$_time");
			long starttime = measurement.getTimestamp().toInstant().toEpochMilli();
			
		    while (it.hasNext()) {		    	
		    	Map.Entry<String, List<Number>> pair = (Map.Entry<String, List<Number>>)it.next();
		    	if (pair.getKey() != "$_time")
		    	{
		    		for (int i = 0; i < pair.getValue().size(); i++ ){
		    			Builder pointBuilder = Point.measurement(device.getDeviceID())
					         .time((long)(starttime + time_iterator.get(i).longValue()), TimeUnit.MILLISECONDS)
					         .addField(pair.getKey(), pair.getValue().get(i).doubleValue());

		    			setDeviceInfoForPointer(pointBuilder,device);		    				

		    			if (part != null)
		    			{
		    				setPartInfoForPointer(pointBuilder,part);		    				
		    			}
		    				
		    			
		    			Point point = pointBuilder.build();
		    			
		    			batchPoints.point(point);
		    		}
			        
		    	}
		            
		    }
			
	        connection.write(batchPoints);
	
	}
	
	/**
	 * Sets the part information for the DB-pointer
	 * 
	 * @param pointBuilder
	 * @param part Part as PPMP java binding object
	 */
	private void setPartInfoForPointer(Builder pointBuilder, Part part){
		
		assert !part.getPartID().isEmpty();
		
		pointBuilder.tag("PartID",part.getPartID());
		
		if (part.getPartTypeID() != null){
			pointBuilder.tag("PartTypeID",part.getPartTypeID());
		}
			
		if (part.getCode() != null){
			pointBuilder.tag("PartCode",part.getCode());
		}
			
		if (part.getResult() != null){
			pointBuilder.tag("PartResult",part.getResult().toString());
		}
			
		if (part.getMetaData() != null){
			pointBuilder.tag("PartMetaData",part.getMetaData().toString());
		}
			
	}
	
	/**
	 * Sets the device information for the DB-pointer
	 *  
	 * @param pointBuilder
	 * @param device Device as PPMP java binding object
	 */
	private void setDeviceInfoForPointer(Builder pointBuilder, Device device){
		
		assert !device.getDeviceID().isEmpty();
			
		pointBuilder.tag("DeviceID", device.getDeviceID());

		if (device.getOperationalStatus() != null){
			pointBuilder.tag("DeviceOperationalStatus",device.getOperationalStatus());
		}
			
		if (device.getMetaData() != null){
			pointBuilder.tag("DeviceMetaData",device.getMetaData().toString());
		}
			
	}

	@Override
	public void receive(String data) throws IOException {
			
		insert(data);	
	
	}
}
