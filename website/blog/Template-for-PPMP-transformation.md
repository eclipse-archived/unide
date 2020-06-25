---
title: "Use Cases: Template for transformation"
date: 2018-09-04 00:00:00
tags: "use cases"
---

# Quick start
In [a previous blog post](https://www.eclipse.org/unide/blog/2018/2/11/Transform-PPMP-with-camel/), I have already introduced Apache Camel as a tool to transform to the Production Performance Management Protocol. In this post, I want to dive a little deeper to get you started with your own transformation.
You can find corresponding source code with some transformation examples [in the repository of the Eclipse PPM Testbed](https://github.com/eclipselabs/eclipseiot-testbed-productionperformancemanagement/tree/master/camel-integrator). After download or checkout of the project, you can easily package everything necessary for an installation via [Apache Maven](https://maven.apache.org/) and java&#160;8+:
```bash
git clone https://github.com/eclipselabs/eclipseiot-testbed-productionperformancemanagement.git
cd eclipseiot-testbed-productionperformancemanagement
cd camel-integrator
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
After the first start, you will find an additional directory that contains the logs of every execution, as defined in the `conf/log4j.properties` file.

So this `camel-integrator-*-assembly.zip` is all you need, ready to being shipped and installed in your target system.

# How can I modify this?

In the `conf` folder of the output or `src/main/resources` src folder, you'll find multiple examples on how to use Apache Camel with Production Performance Management Protocol. Most of the examples are based on [spring xml](https://github.com/apache/camel/blob/master/components/camel-spring/src/main/docs/spring.adoc).
The most important terms to understand them are:

| xml tag | meaning |
|-|-|
| bean | additional functionality, coded as java class / function |
| camelContext | the main, camel specific configurations |
| route | describes the data flow as process |
| from / to | entry / exit points for the data flow. This is also, were external systems are accessed via [components](https://github.com/apache/camel/tree/camel-2.21.0/components) |
| onException | error handling for the data flow |
| pipeline, multicast, ... | routing of data via [Enterprise Integration pattern](http://camel.apache.org/enterprise-integration-patterns.html) |

Among others, you'll find:
<div class="card figure is-pulled-right">
	<div class="card-image">
		<figure class="image">
			<img alt="html log file" title="html log file" src="/unide/images/blog/Transform-PPMP-with-camel-2-logfile.png"></img>
		</figure>
	</div>
	<div class="card-content">
		html logfile before transformation to the Production Performance Management Protocol
	</div>
</div>

* `application-context.xml`
  Main entry file to start camel routes. It contains a general Production Performance Management Protocol validation REST endpoint that reuses the route from `ppmp-validate-context.xml`
* `mqtt-context.xml`
  A simple example of converting transport protocol (mqtt to REST), without looking at the payload
* `ppmp-splitAndRewrite-context.xml`
  Shows how to receive (Production Performance Management Protocol-) payload via different REST paths (/rest or /ppm), and each forwarding to two seperate endpoints (/rest and /ppm)
  * `ppmpRewrite.xml` and `ppmRewrite.xml`
    mapping examples to rewrite the urls
* `ppmp-validate-context.xml`
  A simple validation route for Production Performance Management Protocol messages that can be reused in many other routes.
* `psi6000-transform-context.xml`
  A more advanced example of converting a psi6000 JSON format, converting it to plain old java object (POJO), transforming that to Production Performance Management Protocol POJOs (using [unide.java](https://github.com/eclipse/unide.java)) and forwarding these to actual REST endpoints
* `kistler.xml`
   This example takes html files with a structure similar to the test file in [`src/test/data/00000855.html`](https://github.com/eclipselabs/eclipseiot-testbed-productionperformancemanagement/blob/master/camel-integrator/src/test/data/00000855.html), cleans the html structure, retrieves the relevant parts via [XPath](https://en.wikipedia.org/wiki/XPath) and creates a Production Performance Management Protocol process message out of that
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
* start a run directly (with bundling to a zip file) with:
  ```bash
	mvn exec:java
  ```
* and review the result. With the example above, just open the url http://localhost:9090 and see the `hello world!` in your browser

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

If you want to make use of any other of the (as of camel 2.21.0) [281+ components](https://github.com/apache/camel/tree/camel-2.21.0/components), you should add the respective maven dependency to the `pom.xml`. For example:
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

# Testing

Testing is an important part of the development cycle, especially for the core components that have to work reliably in a production environment.
The project includes example tests in the `src/test` folder:
* `java`
  contains the actual java unit tests that make use of [`CamelSpringTestSupport`](https://github.com/apache/camel/blob/61a58836da57bab38ce719cbd1effd36253687a4/docs/user-manual/en/spring-testing.adoc) to wire xml CamelContext configuration, test data and expected results together
* `resources`
  contains the spring xml configuration that is used by the java test classes and resamble the actual configurations from `src/main/resources`
* `data`
  contains testdata for the data flows and the transformation

Tests can easily be run from within an IDE (eclipse, visual code studio etc.) or via commandline
```bash
mvn test
```
You will see the output of the test runs. If it looks like this, it works as expected:
```bash
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.846 sec

Results :

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 32.267 s
[INFO] Finished at: 2018-04-09T15:25:25+02:00
[INFO] Final Memory: 25M/85M
[INFO] ------------------------------------------------------------------------
``` 

# Summary
The provided camel project template facilitates the creation of simple connectors. You can download it, modify or extend it, package the integrator to a zip file and deploy it on a target server. The included scripts help registering the integrator as an operating system service to run 24/7.