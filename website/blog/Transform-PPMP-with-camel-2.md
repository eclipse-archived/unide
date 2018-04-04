---
title: "PPMP Use Cases: Further Transformation with Apache Camel"
date: 2018-29-03 00:00:00
tags: "use cases"
---

# Quick start
In a previous blog post, I have already introduced Apache Camel as a tool to transform to PPMP. In this post, I want to dive a little deeper to get you started with your own transformation.
You can find corresponding sourcecode with some transformation examples [in the repository of the Eclipse PPM Testbed](https://github.com/eclipselabs/eclipseiot-testbed-productionperformancemanagement/tree/master/camel-integrator). After download or checkout of the project, you can easily package everything necessary for an installation via [Apache Maven](https://maven.apache.org/) and java&#160;8+:
```bash
mvn package
```
After successful build, you should get a zip file at `target/camel-integrator-*-assembly.zip` with the following content
* `bin`
The IntegratorStarter\* files for Windows and Linux can be used to start the integrator manually. WinServiceInstaller.bat und WinServiceUninstaller.bat can be executed with Admin permissions in Windows to register/unregister this java application as Windows Service called "Integrator Service".
* `conf`
This is where the main configuration file `application-context.xml`, further included spring xml configurations, as well as the configuration for logging `log4j.properties` and a key-value-file for quick configuration `application.properties` can be found. After modification of any of them, the integrator should be restarted.
* `lib`
Contains all java dependencies and their dependencies as individual jar files
* `log`
After the first start, you'll find an additional directory that contains the logs of every execution, as defined in the `conf/log4j.properties` file

# How can I modify this?

In the `conf` folder of the output or `src/main/resources` src folder, you'll find multiple examples on how to use Apache Camel with Production Performance Management Protocol. Most of the examples are based on [spring xml](https://github.com/apache/camel/blob/master/components/camel-spring/src/main/docs/spring.adoc). Among others, you'll find:
<div class="card figure is-pulled-right">
	<div class="card-image">
		<figure class="image">
			<img alt="html log file" title="html log file" src="/unide/images/blog/Transform-PPMP-with-camel-2-logfile.png"></img>
		</figure>
	</div>
	<div class="card-content">
		html logfile before transformation to PPMP
	</div>
</div>

* `application-context.xml`
  Main entry file to start camel routes. It contains a general PPMP validation REST endpoint that reuses the route from `ppmp-validate-context.xml`
* `mqtt-context.xml`
  A simple example of converting transport protocol (mqtt to REST), without looking at the payload
* `ppmp-splitAndRewrite-context.xml`
  Shows how to receive (PPMP-) payload via different REST paths (/rest or /ppm), and each forwarding to two seperate endpoints (/rest and /ppm)
  * `ppmpRewrite.xml` and `ppmRewrite.xml`
    mapping examples to rewrite the urls
* `ppmp-validate-context.xml`
  A simple validation route for PPMP messages that can be reused in many other routes.
* `psi6000-transform-context.xml`
  A more advanced example of converting a psi6000 JSON format, converting it to plain old java object (POJO), transforming that to PPMP POJOs (using [unide.java](https://github.com/eclipse/unide.java)) and forwarding these to actual REST endpoints
* `kistler.xml`
   This example takes html files with a structure similar to the test file in [`src/test/data/00000855.html`](https://github.com/eclipselabs/eclipseiot-testbed-productionperformancemanagement/blob/master/camel-integrator/src/test/data/00000855.html), cleans the html structure, retrieves the relevant parts via [XPath](https://en.wikipedia.org/wiki/XPath) and creates a PPMP process message out of that
* `application.properties`
  Contains key/value pairs that can be used in the camel context configurations.
* `log4j.properties`
  The configuration for logging. For testing purposes, the log4j.properties in /src/test/resources is used.

In order to get familiar with these transformations, I suggest you:
* open the `application-context.xml` in your favorite editor 
* remove the `<import .../>` statements
* add your own `<route>...</route>`, maybe as simple as
  ```xml
	<route>
		<from uri="jetty:http://0.0.0.0:9090/" />
		<setBody>
			<simple>hello world!</simple>
		</setBody>
	</route>
	```
* start a testrun with
  ```bash
  mvn exec:java
  ```
* and review the result. In the example above, just open your browser at http://localhost:9090

If you prefer a visual model of these camel xml definitions, you could also make use of [JBoss Fuse Tooling](https://tools.jboss.org/features/fusetools.html) which is available for [Eclipse Workbench 4.3+](https://projects.eclipse.org/releases/oxygen) through [the Marketplace](https://marketplace.eclipse.org/content/jboss-tools). Be aware that this might cause additional overhead.
<div class="card figure">
	<div class="card-image">
		<figure class="image">
			<img alt="JBoss Tools" title="JBoss Tools for Eclipse Workbench" src="/unide/images/blog/Transform-PPMP-with-camel-2-eclipse.png"></img>
		</figure>
	</div>
	<div class="card-content">
		JBoss Tools for Eclipse Workbench
	</div>
</div>

If you want to make use of any other of the (as of camel 2.21.0) [281+ components](https://github.com/apache/camel/tree/camel-2.21.0/components), your should add the respective maven dependency to the `pom.xml`. For example:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	...
	<dependencies>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-amqp</artifactId>
			<version>${camel.version}</version>
		</dependency>
	...
	</dependencies>
	...
</project>
```

# Summary
The provided camel project template facilitates the creation of simple connectors. You can download it, modify or extend it, package the integrator to a zip file and deploy it on a target server. The included scripts help registering the integrator as an operating system service to run 24/7.