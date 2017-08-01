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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.process.Measurements;
import org.eclipse.iot.unide.ppmp.process.Part;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;
import org.eclipse.iot.unide.ppmp.process.Process;
import org.eclipse.iot.unide.ppmp.process.ShutOffValues;
import org.eclipse.iot.unide.ppmp.process.SpecialValue;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import server.endpoints.receivers.IProcessMessageReceiver;
import server.persistency.db.ConnectionFactory;

/**
 * DAO class for Measurement objects of InfluxDB
 *
 */
public class ProcessDAO implements IProcessMessageReceiver {

	private InfluxDB connection;
	private PPMPPackager packager;

	public ProcessDAO() { 
		packager = new PPMPPackager();
	}
	
	/**
	 * Inserts a PPMP-Message as JSON-string
	 * 
	 * @param JsonStringMessage
	 * @throws IOException
	 */
	public void insert(String JsonStringMessage) throws IOException{
		
		PPMPPackager packager = new PPMPPackager();
		ProcessWrapper processWrapper = packager.getProcessesBean(JsonStringMessage.replace("content_spec", "content-spec"));	

		insertProcess(processWrapper);		
		
	}	
	
	/**
	 * Inserts the whole ProcessWrapper object to the database
	 * 
	 * @param processWrapper ProcessWrapper as PPMP java binding object
	 * @throws IOException 
	 */
	private void insertProcess(ProcessWrapper processWrapper) throws IOException {
		
		insertProcessData(processWrapper);
		
		for(Measurements measurement : processWrapper.getMeasurements())
		{
			if (measurement.getSpecialValues() != null){
				insertSpecialValues(measurement, processWrapper.getDevice(), processWrapper.getPart());
			}			
		}			
		
	}	
	
	/**
	 * Inserts the process data
	 * 
	 * @param processWrapper The whole process message
	 * @throws IOException 
	 */
	private void insertProcessData(ProcessWrapper processWrapper) throws IOException {

		connection = ConnectionFactory.getConnection();
		
		BatchPoints batchPointsProcess = BatchPoints
                .database(ConnectionFactory.getDatabasenameProcesses())
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
				
		long starttime = Timestamp.valueOf(processWrapper.getProcess().getTimestamp().atZoneSameInstant(ZoneOffset.ofHours(1)).toLocalDateTime()).getTime();
		
		Builder pointBuilder = Point.measurement(processWrapper.getDevice().getDeviceID())
		         .time((long)starttime, TimeUnit.MILLISECONDS);	
		
		if (processWrapper.getProcess().getExternalProcessId() != null){
			pointBuilder.addField("ExternalProcessId", processWrapper.getProcess().getExternalProcessId());
		}
		
		if (processWrapper.getProcess().getResult() != null){
			pointBuilder.addField("Result", processWrapper.getProcess().getResult().toString());
		}
		
		if (processWrapper.getProcess().getShutoffPhase() != null){
			pointBuilder.addField("ShutoffPhase", processWrapper.getProcess().getShutoffPhase());
		}
		
		if (processWrapper.getProcess().getMetaData() != null){
			pointBuilder.addField("ProcessMetaData", processWrapper.getProcess().getMetaData().toString());
		}
		
		if (processWrapper.getMeasurements() != null){
			pointBuilder.addField("Measurements", packager.getMessage(processWrapper.getMeasurements()));	
		}		
		
		if (processWrapper.getProcess().getShutOffValuesMap() != null){
			pointBuilder.addField("ShutOffValues", packager.getMessage(processWrapper.getProcess().getShutOffValuesMap()));	
		}
		
		if (processWrapper.getProcess().getProgram() != null){
			if (processWrapper.getProcess().getProgram().getId() != null){
				pointBuilder.addField("Program_ID", packager.getMessage(processWrapper.getProcess().getProgram().getId()));	
			}
			if (processWrapper.getProcess().getProgram().getId() != null){
				pointBuilder.addField("Program_Name", packager.getMessage(processWrapper.getProcess().getProgram().getName()));	
			}
			if (processWrapper.getProcess().getProgram().getId() != null){
				pointBuilder.addField("Program_LastChangedDate", packager.getMessage(processWrapper.getProcess().getProgram().getLastChangedDate()));	
			}
		}		
	    
		setDeviceInfoForPointer(pointBuilder,processWrapper.getDevice());		    				

		if (processWrapper.getPart() != null)
		{
			setPartInfoForPointer(pointBuilder,processWrapper.getPart());		    				
		}	
		
		Point point = pointBuilder.build();
		
		batchPointsProcess.point(point);
		
        connection.write(batchPointsProcess);
        
		if (processWrapper.getProcess().getShutOffValuesMap() != null){
			insertShutoffValues(processWrapper.getProcess(), processWrapper.getDevice(), processWrapper.getPart());
		}
	
	}	
	
	/**
	 * Inserts the shutoff values of a process with device information
	 * 
	 * @param measurement Measurement-Array as PPMP java binding object 
	 * @param device Device as PPMP java binding object 
	 * @param part Part as PPMP java binding object
	 * @throws IOException 
	 */
	private void insertShutoffValues(Process process, Device device, Part part) throws IOException {

		connection = ConnectionFactory.getConnection();
		
		BatchPoints batchPointsMeasurements = BatchPoints
                .database(ConnectionFactory.getDatabasenameMeasurements())
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
								
		Iterator<Entry<String, ShutOffValues>> iterator = process.getShutOffValuesMap().getShutOffValues().entrySet().iterator();
		
		long starttime = Timestamp.valueOf(process.getTimestamp().atZoneSameInstant(ZoneOffset.ofHours(1)).toLocalDateTime()).getTime();
		
	    while (iterator.hasNext()) {

	    	Entry<String, ShutOffValues> currentValue = iterator.next();
	    	 	
			Builder pointBuilder = Point.measurement(device.getDeviceID())
		         .time((long)starttime, TimeUnit.MILLISECONDS)
		         .addField(currentValue.getKey() + "_value", currentValue.getValue().getValue().doubleValue())
		         .addField(currentValue.getKey() + "_upperError", currentValue.getValue().getUpperError().doubleValue())
		         .addField(currentValue.getKey() + "_lowerError", currentValue.getValue().getLowerError().doubleValue());				    				
						
			setDeviceInfoForPointer(pointBuilder,device);		    				

			if (part != null)
			{
				setPartInfoForPointer(pointBuilder,part);		    				
			}	
			
			Point point = pointBuilder.build();
			
			batchPointsMeasurements.point(point);
	            
	    }
		
        connection.write(batchPointsMeasurements);
	
	}	
	
	/**
	 * Inserts the special values of a process with device information
	 * 
	 * @param measurement Measurement-Array as PPMP java binding object 
	 * @param device Device as PPMP java binding object 
	 * @param part Part as PPMP java binding object
	 * @throws IOException 
	 */
	private void insertSpecialValues(Measurements measurement, Device device, Part part) throws IOException {

		connection = ConnectionFactory.getConnection();
		
		BatchPoints batchPointsMeasurements = BatchPoints
                .database(ConnectionFactory.getDatabasenameMeasurements())
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
								
		Iterator<Entry<String, SpecialValue>> iterator = measurement.getSpecialValues().getSpecialValue().entrySet().iterator();
		long starttime = Timestamp.valueOf(measurement.getTimestamp().atZoneSameInstant(ZoneOffset.ofHours(1)).toLocalDateTime()).getTime();
		
	    while (iterator.hasNext()) {

	    	Entry<String, SpecialValue> currentValue = iterator.next();
	    	
			Builder pointBuilder = Point.measurement(device.getDeviceID())
		         .time((long)starttime + currentValue.getValue().getValue().longValue(), TimeUnit.MILLISECONDS)
		         .addField(currentValue.getKey(), currentValue.getValue().getValue().doubleValue())
		         .tag("SpecialValue", "true");				    				
			
			if (measurement.getPhase() != null){
				pointBuilder.tag("Phase",measurement.getPhase());
			}
			
			if (measurement.getName() != null){
				pointBuilder.tag("Name",measurement.getName());
			}

			if (measurement.getResult() != null){
				pointBuilder.tag("Result",measurement.getResult().toString());
			}
			
			if (measurement.getCode() != null){
				pointBuilder.tag("Code",measurement.getCode().toString());
			}
			
			setDeviceInfoForPointer(pointBuilder,device);		    				

			if (part != null)
			{
				setPartInfoForPointer(pointBuilder,part);		    				
			}	
			
			Point point = pointBuilder.build();
			
			batchPointsMeasurements.point(point);
	            
	    }
		
        connection.write(batchPointsMeasurements);
	
	}
	
	/**
	 * Inserts a single Measurement-Array with device information
	 * 
	 * @param measurement Measurement-Array as PPMP java binding object 
	 * @param device Device as PPMP java binding object 
	 * @param part Part as PPMP java binding object
	 * @throws IOException 
	 */
	private void insertSingleMeasurement(Measurements measurement, Device device, Part part) throws IOException {

		connection = ConnectionFactory.getConnection();
		
			BatchPoints batchPointsMeasurements = BatchPoints
	                .database(ConnectionFactory.getDatabasenameMeasurements())
	                .consistency(InfluxDB.ConsistencyLevel.ALL)
	                .build();
						
			Iterator<Entry<String, List<Number>>> it = measurement.getSeriesMap().getSeries().entrySet().iterator();
			
			List<Number> time_iterator = measurement.getSeriesMap().getSeries().get("time");
			
			if (time_iterator != null)
				
			{
				long starttime = Timestamp.valueOf(measurement.getTimestamp().atZoneSameInstant(ZoneOffset.ofHours(1)).toLocalDateTime()).getTime();
			
			    while (it.hasNext()) {		    	
			    	Map.Entry<String, List<Number>> pair = (Map.Entry<String, List<Number>>)it.next();
			    	if (pair.getKey() != "time")
			    	{
			    		for (int i = 0; i < pair.getValue().size(); i++ ){
			    			Builder pointBuilder = Point.measurement(device.getDeviceID())
						         .time((long)(starttime + time_iterator.get(i).doubleValue()*1000), TimeUnit.MILLISECONDS)
						         .addField(pair.getKey(), pair.getValue().get(i).doubleValue());
	
			    			setDeviceInfoForPointer(pointBuilder,device);		    				
	
			    			if (part != null)
			    			{
			    				setPartInfoForPointer(pointBuilder,part);		    				
			    			}		    				
			    			
			    			Point point = pointBuilder.build();
			    			
			    			batchPointsMeasurements.point(point);
			    		}
				        
			    	}
			    	
			    }					
		            
		    }
			
	        connection.write(batchPointsMeasurements);
	
	}
	
	/**
	 * Sets the part information for the DB-pointer
	 * 
	 * @param pointBuilder
	 * @param part Part as PPMP java binding object
	 */
	private void setPartInfoForPointer(Builder pointBuilder, Part part){
		
		assert !part.getpartID().isEmpty();
		
		pointBuilder.tag("partID",part.getpartID());
		
		if (part.getPartTypeId() != null){
			pointBuilder.tag("PartTypeID",part.getPartTypeId());
		}
		
		if (part.getPartTypeId() != null){
			pointBuilder.tag("PartType",part.getType().toString());
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
