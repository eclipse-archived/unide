/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.eclipse.iot.unide.server.receiver.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;

/**
 * Service to execute JDBC statements. Exception handling and commits are done
 * by this service.
 *
 * Provides method for execution of flyway changelog.
 */
public class JdbcExecutorService {

	private static final String FLYWAY_BASE = "org/eclipse/iot/unide/server/receiver/sql/migrations";
	private static final String FLYWAY_TABLE = "SCHEMA_VERSION";

	private DataSource dataSource;

	public JdbcExecutorService(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * The given runner will be executed by this method by passing a valid jdbc
	 * connection. The caller does not have handle connection related actions.
	 * Commit, close and exception handling is done by this method.
	 *
	 * @param runner
	 *            executes the given runner
	 */
	public void execute(ExecutorCommand<Connection> runner) {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			runner.accept(connection);
			connection.commit();
		} catch (Exception ex) {
			throw new RuntimeException("Data access failed.", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				// ignore
			}
		}
	}

	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@FunctionalInterface
	public interface ExecutorCommand<T> {
		void accept(T t) throws Exception;
	}

	/**
	 * Executes the database changelog using flyway.
	 */
	void executeChangeLog() {
		Flyway flyway = new Flyway();
		flyway.setTable(FLYWAY_TABLE);
		flyway.setDataSource(dataSource);
		flyway.setLocations(FLYWAY_BASE + "/" + getDbType());
		flyway.setBaselineOnMigrate(true);
		flyway.migrate();
	}

	/**
	 * Returns the database type, if known.
	 *
	 * @return String which is database type
	 */
	public String getDbType() {
		String dbName = "unknown";
		String dbType;
		try {
			dbName = dataSource.getConnection().getMetaData().getDatabaseProductName();
		} catch (SQLException e) {
            throw new RuntimeException( "Failed to get connection", e );
		}
		switch (dbName) {
		case "PostgreSQL":
		case "MockDatabase":
			dbType = "postgres";
			break;
		case "H2":
			dbType = "h2";
			break;
		// case "Microsoft SQL Server":
		// case "Oracle":
		// case "MySQL":
		// case "HSQL Database Engine":
		default:
			throw new RuntimeException("Unknown database '" + dbName + "'");
		}
		return dbType;
	}
}
