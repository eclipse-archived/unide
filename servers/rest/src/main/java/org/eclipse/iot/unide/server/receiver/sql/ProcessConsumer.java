/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.eclipse.iot.unide.server.receiver.sql;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

import org.eclipse.iot.unide.ppmp.commons.Device;
import org.eclipse.iot.unide.ppmp.process.Process;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;
import org.eclipse.iot.unide.ppmp.process.Program;
import org.eclipse.iot.unide.server.receiver.util.PpmpHelper;

/**
 * Consumer class for Process
 */
class ProcessConsumer extends AbstractSqlConsumer<ProcessWrapper> {

	private static final String INSERT_PROCESS = "INSERT INTO ppmp_processes(time, deviceid, programid, payload) VALUES(?,?,?,?)";
	private static final String INSERT_PROCESS_PG = "INSERT INTO ppmp_processes(time, deviceid, programid, payload) VALUES(?,?,?,to_json(?::json))";

	/**
	 * @param executorService
	 *            - the connection
	 */
	ProcessConsumer(JdbcExecutorService executorService) {
		super(executorService);
	}

	@Override
	public void accept(ProcessWrapper data) {
		insertProcessData(data);
	}

	/**
	 * Inserts the process data
	 *
	 * @param processWrapper
	 *            The whole process message
	 */
	private void insertProcessData(ProcessWrapper processWrapper) {
		getExecutorService().execute(connection -> {
			Device device = processWrapper.getDevice();
			String programId = getProgramId(processWrapper.getProcess());
			long time = processWrapper.getProcess().getTimestamp().toInstant().toEpochMilli();

			String sql = INSERT_PROCESS;
			String dbType = getExecutorService().getDbType();
			if (dbType == "postgres") {
				sql = INSERT_PROCESS_PG;
			}
			PreparedStatement insertProcess = connection.prepareStatement(sql);
			insertProcess.setTimestamp(1, new Timestamp(time));
			insertProcess.setString(2, device.getDeviceID());
			insertProcess.setString(3, programId);
			insertProcess.setString(4, PpmpHelper.toJson(processWrapper));
			insertProcess.executeUpdate();
		});
	}

	private static String getProgramId(Process process) {
		if (process == null || process.getProgram() == null) {
			return null;
		}
		return process.getProgram().getId();
	}
}
