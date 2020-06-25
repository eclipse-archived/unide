---
title: "Unide 0.2.0 released"
date: 2018-03-26 10:39:00
tags: "releases"
---
Finally, the second version (0.2.0) of the Eclipse unide project is released! The [REST Server on the Testserver](http://unide.eclipse.org) has been updated as well.

# Quick start
## Binsa chrome app
Download the binsa chrome app from [download page](http://download.eclipse.org/unide/binsa.crx). Open your chrome browser at [chrome://extensions](chrome://extensions) and drag the binsa.crx file onto the page. You should be able to see your new chrome app in [chrome://apps](chrome://apps)

## REST Server
Download the REST server file from [repo.eclipse.org](https://repo.eclipse.org/content/repositories/unide-releases/org/eclipse/iot/unide/ppmp/ppmp-server/0.2.0/ppmp-server-0.2.0.jar), create a simple configuration file (e.g. application_conf.json):
```json
{
  "http.port": 8090,
  "persistence.enable": false
}

```
and start the server
```bash
java -jar ppmp-server-0.2.0.jar -conf application_conf.json
```
Point your browser to http://localhost:8090 and see the servers documentation running on your local server! At this point, Production Performance Management Protocol validation (via HTTP POST to `/rest/v2/validate`) is enabled, but no persistence.

If you want to send data to the server, a quick way is via [Postman App](https://www.getpostman.com/), which is available as native app or [Chrome app](https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop?hl=en). Once installed and started, you can easily use the "Import > Import From Link" feature to get the prepared [unide.postman_collection.json](https://raw.githubusercontent.com/eclipse/unide/f4d349bc4a0096d3f99e14cf5f8b9f53a5145fdd/servers/rest/assets/postman/unide.postman_collection.json) and the corresponding [unide.postman_environment](https://raw.githubusercontent.com/eclipse/unide/f4d349bc4a0096d3f99e14cf5f8b9f53a5145fdd/servers/rest/assets/postman/unide.postman_environment.json). You should now have Machine/Measurement/Process messages and requests prepared in the Collections tab. In order to send, you could extend the URL with "/validate" and hit "Send" button. If you get the response
> Ppmp Message of type 'xy' is valid

the unide REST server works as expected!

# Persistence
## Influx DB
[Download](https://portal.influxdata.com/downloads) the InfluxDB Time-Series Data Storage and unzip or install it on your system. By default, it opens an http port at 8086. In order to make use of it, the configuration file should be modified, e.g. to:
```json
{
  "http.port": 8090,
  "persistence.enable": true,
  "persistence.system": "influxDb",
  "influxDb.url": "http://localhost:8086",
  "influxDb.user": "root",
  "influxDb.password": "root"
}
```
After starting the database and the unide REST server, it should be possible to send POST requests to `http://localhost:8090/rest/v2`. This path receives all message types. To query the database after, use:
[http://localhost:8086/query?pretty=true&u=root&p=root&db=Measurements&q=SELECT * FROM "ppmp_measurements"](http://localhost:8086/query?pretty=true&u=root&p=root&db=Measurements&q=SELECT%20*%20FROM%20%22ppmp_measurements%22) or the other databases that are documented on the [running server instance](http://localhost:8090/#influxdb_schema_design), respectively.

## SQL (timescale, h2)
Download the appropriate jdbc driver, e.g. [h2-*.jar](http://repo1.maven.org/maven2/com/h2database/h2/1.4.197/h2-1.4.197.jar), modify the configuration file, e.g. for h2:
```json
{
  "http.port": 8090,
  "persistence.enable": true,
  "persistence.system": "sql",
  "sqlDb.driver": "org.h2.Driver",
  "sqlDb.url": "jdbc:h2:./test",
  "sqlDb.user": "sa",
  "sqlDb.password": ""
}
```
Start the Production Performance Management Protocol Server with the jdbc dependency on the classpath, e.g.:
```bash
java -cp "h2-1.4.197.jar;ppmp-server-0.2.0.jar" io.vertx.core.Launcher org.eclipse.iot.unide.server.MainVerticle -conf application.conf
```
Now, all the data send to the REST server is persisted in the file `test.mv.db`.
For concurrent db actions (reading while writing), use the h2 tcp server instead. h2 is not recommended for production use!

# Background

## Download

Available artifacts for download:
* [Production Performance Management Protocol Schema v2](https://repo.eclipse.org/content/repositories/unide-releases/org/eclipse/iot/unide/ppmp/ppmp-schema/2.0.0/)
* [Production Performance Management Protocol java Binding](https://repo.eclipse.org/content/repositories/unide-releases/org/eclipse/iot/unide/ppmp/ppmp-java-binding/0.2.0/)
* [Production Performance Management Protocol Python Binding](https://pypi.python.org/packages/d5/af/c90911d1da56a1ed0238338ba01068d43b8cd0137e9ca52b0f52a279f73e/unide_python-0.2.0-py2.py3-none-any.whl#md5=ca67bcd536b3a4015f72f5fa53d36543) 
* [Production Performance Management Protocol Server](https://repo.eclipse.org/content/repositories/unide-releases/org/eclipse/iot/unide/ppmp/ppmp-server/0.2.0/)
* [binsa client chrome app](http://download.eclipse.org/unide/binsa.crx)

Source code:
* [unide](https://github.com/eclipse/unide/releases/tag/v0.2.0)
* [java binding](https://github.com/eclipse/unide.java/releases/tag/v0.2.0)
* [python binding](https://github.com/eclipse/unide.python/releases/tag/v0.2.0)


## Release notes
### REST Server improvements (refactoring & persistence)
* Added persistence to timeseries/postgres db
* Outfactored schema files
* Adjusted maven file
* Added unit tests
* Added documentation & java docs
* Adjusted error handling
* Removed unused code
* Added postman collections
* Added grafana configuration file

### New HTML5 simulation client
* Progressive Webapp runs as website (see https://unide.eclipse.org/binsa), browser app, native client (via [nw.js](https://nwjs.io/)) or app (via [Apache cordova](https://cordova.apache.org/))

### Production Performance Management Protocol JSON Schema files
* Introduced separate bundle for schemas
* general fixes
  * make schema draft-06 compliant
  * added 'id' and 'descriptions' where applicable
  * added 'type' and 'default' for enums
  * added 'required' fields where necessary
  * made 'additionalProperties': true explicit for 'metaData'
  * sorted the properties for improved readability
* message_schema fixes
  * added minItems: 1 to messages
* measurement_schema fixes
  * completed 'limits' specification
  * added 'patternProperties' and minProperties detail to 'series'
  * added minItems: 1 to measurements
* process_schema fixes
  * made 'id' and 'name' of a 'program' mandatory
  * fixed 'shutoffValues', having properties not items
  * fixed 'limits', having properties not items and number values rather than object values as limits
  * fixed 'specialValues' to be an array of specialValue items with 'name' for indicating the nature, rather than one single object with its keys indicating the nature. Offering optional '$_time' tag to be consistent with measurements.
  * completed 'series' definition

### Website
* Autogenerating Production Performance Management Protocol specification from JSON schemas to avoid discrepancies
* Generating UML schemas with plantuml
* Using similar HTML5 stack as simulation client for SPA website

### Other
* java and new python bindings in separate github repositories
