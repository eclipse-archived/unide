/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.endpoints.receivers;

import java.util.HashSet;
import java.util.Set;

import server.persistency.dao.MachineMessageDAO;
import server.persistency.dao.MeasurementDAO;
import server.persistency.dao.ProcessDAO;

/**
 * Provides HTTP-endpoints for activate or deactivate storaging incoming data in a database
 */
public class ReceiverController {
		
	private static Set<IMachineMessageReceiver> machineMessageReceivers = new HashSet<IMachineMessageReceiver>();
	private static Set<IMeasurementMessageReceiver> measurementMessageReceivers = new HashSet<IMeasurementMessageReceiver>();
	private static Set<IProcessMessageReceiver> processMessageReceivers = new HashSet<IProcessMessageReceiver>();

	/**
	 * Adds influxDB-receivers for all message types
	 */
	public static void addAllReceivers()
	{
		addMachineMessageReceiver(new MachineMessageDAO());
		addMeasurementMessageReceiver(new MeasurementDAO());
		addProcessMessageReceiver(new ProcessDAO());
	}	
	
	/**
	 * Adds influxDB-receivers for machine message
	 */
	private static void addMachineMessageReceiver(IMachineMessageReceiver receiver) {
		if (!machineMessageReceivers.contains(receiver)){
			machineMessageReceivers.add(receiver);
		}		
	}
	
	/**
	 * Adds influxDB-receivers for measurement message
	 */
	private static void addMeasurementMessageReceiver(IMeasurementMessageReceiver receiver) {
		if (!measurementMessageReceivers.contains(receiver)){
			measurementMessageReceivers.add(receiver);
		}
	}
	
	/**
	 * Adds influxDB-receivers for process message
	 */
	private static void addProcessMessageReceiver(IProcessMessageReceiver receiver) {
		if (!processMessageReceivers.contains(receiver)){
			processMessageReceivers.add(receiver);
		}
	}
		
	/**
	 * Removes all influxDB-receivers
	 */
	public static void removeAllReceivers(){
		measurementMessageReceivers.clear();
		processMessageReceivers.clear();
		machineMessageReceivers.clear();
	}
	
	/**
	 * Gets influxDB-receivers for machine message
	 */
	public static Set<IMachineMessageReceiver> getMachineMessageReceivers(){
		return machineMessageReceivers;
	}
	
	/**
	 * Gets influxDB-receivers for measurement message
	 */
	public static Set<IMeasurementMessageReceiver> getMeasurementMessageReceivers(){
		return measurementMessageReceivers;
	}
	
	/**
	 * Gets influxDB-receivers for process message
	 */
	public static Set<IProcessMessageReceiver> getProcessMessageReceivers(){
		return processMessageReceivers;
	}
}