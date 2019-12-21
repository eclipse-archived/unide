![Unide logo](website/static/logo.svg)

[Eclipse Unide](https://www.eclipse.org/unide) publishes the current version of Production Performance Management Protocol (PPMP) and develops simple server/client implementations. The implementations store the payloads in a database and displays them through a simple user interface. Everyone can then use these samples for their custom condition monitoring applications.

# Getting started

For installing the Unide server and binding implementations use Maven.

## Installation

Install the `ppmp-java-binding` dependency

    git clone https://github.com/eclipse/unide.java.git
    cd unide.java
    mvn clean install

Then install this repo

    git clone https://github.com/eclipse/unide.git
    cd unide
    mvn clean install

On success

    ...
    [INFO] Reactor Summary:
    [INFO]
    [INFO] ppmp-schema 3.0.0-SNAPSHOT ......................... SUCCESS [  1.058 s]
    [INFO] Unide 0.3.0-SNAPSHOT ............................... SUCCESS [  0.003 s]
    [INFO] unide-servers ...................................... SUCCESS [  0.003 s]
    [INFO] unide-server 0.3.0-SNAPSHOT ........................ SUCCESS [ 28.981 s]
    ...

Build the jars

    mvn package -Dmaven.test.skip=true

Create a test configuration `application_conf.json` for the REST server

    {
      "http.port": 8090,
      "persistence.enable": false
    }

Start the REST server

    java -jar ./servers/rest/target/ppmp-server-0.3.0-SNAPSHOT.jar -conf application_conf.json

Visit http://localhost:8090/


## References

- https://www.eclipse.org/unide/blog/2018/3/26/Release-0.2.0/
