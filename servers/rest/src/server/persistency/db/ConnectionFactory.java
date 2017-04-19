/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.persistency.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

/**
 * Manages connections to InfluxDB
 *
 */
public class ConnectionFactory {

	/**
	 * The logger.
	 */
	private static final Logger LOG = Logger.getLogger(ConnectionFactory.class);

	private static ConnectionFactory instance = new ConnectionFactory();
	private static InfluxDB connection;
	private Properties properties;
	private String dbNameMessages;
	private String dbNameMeasurements;

	private ConnectionFactory() {

	}

	/**
	 * Returns the instance of the ConnectionFactory
	 * 
	 * @return
	 */
	public static ConnectionFactory getInstance() {
		return instance;
	}

	/**
	 * Create the connection to a configured InfluxDB
	 * 
	 * @return InfluxDB connection
	 * @throws IOException
	 */
	private synchronized InfluxDB createConnection() throws IOException {

		if (connection == null) {

			if (properties == null) {
				getConfiguration();
			}

			String url = properties.getProperty("dbhost").concat(":")
					.concat(properties.getProperty("dbport"));
			String user = properties.getProperty("dbuser");
			String password = properties.getProperty("dbpassword");

			dbNameMessages = properties.getProperty("dbNameMessages");
			dbNameMeasurements = properties.getProperty("dbNameMeasurements");

			connection = InfluxDBFactory.connect(url, user, password);

			if (connection != null) {
				LOG.info("DB connected");
				connection.createDatabase(dbNameMeasurements);
				connection.createDatabase(dbNameMessages);
			}
		}

		return connection;
	}

	/**
	 * Creates a new DB-connection or returns the existing connection
	 * 
	 * @return InfluxDB connection
	 * @throws IOException
	 */
	public static InfluxDB getConnection() throws IOException {

		if (connection == null) {
			instance.createConnection();
		}

		return connection;
	}

	/**
	 * Gets the InfluxDB-Name for Messages
	 * 
	 * @return InfluxDB-Name for Messages
	 */
	public static String getDatabasenameMessages() {
		return ConnectionFactory.getInstance().dbNameMessages;
	}

	/**
	 * Gets the InfluxDB-Name for Measurements
	 * 
	 * @return InfluxDB-Name for Measurements
	 */
	public static String getDatabasenameMeasurements() {
		return ConnectionFactory.getInstance().dbNameMeasurements;
	}

	/**
	 * Reads the database configuration of the project
	 * 
	 * @throws IOException
	 */
	private void getConfiguration() throws IOException {

		properties = new Properties();
		InputStream input = null;

		try {

			input = getClass().getClassLoader().getResourceAsStream(
					"config.properties");

			properties.load(input);

			LOG.info(properties.getProperty("dbtype"));
			LOG.info(properties.getProperty("dbuser"));
			LOG.info(properties.getProperty("dbhost"));
			LOG.info(properties.getProperty("dbport"));
			LOG.info(properties.getProperty("dbpassword"));

		} catch (IOException ex) {
			LOG.debug("cannot load config!", ex );
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					LOG.debug("cannot close config stream!", e);
					throw e;
				}
			}
		}

	}
}