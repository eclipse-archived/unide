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

CREATE TABLE ppmp_measurements (
	time             TIMESTAMP         NOT NULL,
	deviceid         TEXT              NOT NULL,
	measurementpoint TEXT              NOT NULL,
	value            DECIMAL           NOT NULL
);

CREATE TABLE ppmp_processes (
	time             TIMESTAMP         NOT NULL,
	deviceid         TEXT              NOT NULL,
	programname      TEXT              NOT NULL,
	payload          CLOB              NOT NULL
);
