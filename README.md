![Unide logo](images/logo.png)

[Eclipse Unide](https://www.eclipse.org/unide) publishes the current version of Production Performance Management Protocol (PPMP) and develops simple server/client implementations. The implementations store the payloads in a database and displays them through a simple user interface. Everyone can then use these samples for their custom condition monitoring applications.

## Unide Server

The server helps to implement PPMP to your system by providing endpoints for validating and storing PPMP-messages to an influxDB. Using these stored data, you can visualize measurements and process messages of your system.

## Getting started

# Installation

Run `mvn clean install`

# Configuring

Open `src\main\application_conf.json` and set database- and http-settings

# Running

run `java -jar ppmp-server-0.0.1-fat.jar -conf application_conf.json`