CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

CREATE TABLE ppmp_messages (
	time        TIMESTAMP         NOT NULL,
	deviceId    TEXT              NOT NULL,
	code        TEXT              NOT NULL,
	severity    TEXT              NULL,
	title       TEXT              NULL,
	description TEXT              NULL,
	hint        TEXT              NULL,
	type        TEXT              NULL
);

SELECT create_hypertable('ppmp_messages', 'time');

CREATE TABLE ppmp_measurements (
	time             TIMESTAMP         NOT NULL,
	deviceid         TEXT              NOT NULL,
	measurementpoint TEXT              NOT NULL,
	value            DECIMAL           NOT NULL
);

SELECT create_hypertable('ppmp_measurements', 'time');

CREATE TABLE ppmp_processes (
	time             TIMESTAMP         NOT NULL,
	deviceid         TEXT              NOT NULL,
	programid        TEXT              NULL,
	payload          JSON              NOT NULL
);

SELECT create_hypertable('ppmp_processes', 'time');
