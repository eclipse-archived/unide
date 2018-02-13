# Unide and the Eclipse Production Performance Management Testbed


## Abstract

In this blog post we present a real world condition monitoring scenario used in
the [Eclipse Production Performance Management Testbed][1], where Unide and
PPMP plays a crucial role. Moreover, we also provide a code repository and
instructions so that you can recreate the scenario in your own computer and
learn about its components and the communication with each other.


## Introduction

In the context of the [Eclipse Production Performance Management Testbed][1], a
real world condition monitoring scenario has been recreated, in which a
grinding machine is being continuously monitored allowing to have real time
health checks and prevent unexpected failures. Further details 

This scenario consists of the following building blocks: device, gateway,
backend. The communication between them happens seamlessly thanks to the PPMP
protocol.

- The device, i.e. the grinding machine, has been retrofitted with an
  acceleration sensor attached to one of its critical components. The data
  provided by this sensor is routed to the gateway in the form of a [PPMP
  Measurement Message][2].

- The gateway receives the raw acceleration measurements, calculates some
  statistical characteristics and and applies some machine learning techniques
  to them in order to evaluate the condition of the grinding machine. Then both
  the statistical values and the condition are routed to the backend, again in
  the form of a PPMP Measurement Message.

- The backend, usually in the cloud, is any component that "speaks unide" and
  performs meaningful operations on the incoming messages. Most frequently it
  is responsible for storing the arriving data and making it available to apps
  that want to consume it. Common responsibilities of these apps are the
  representation or display of the data (e.g. dashboards), or workflows
  management (e.g. the grinding machine is in bad condition and an inspection
  must be carried out). 
  
  The backend can be swapped effortless with any entity adhering to PPMP standard.
  In the Eclipse PPM Testbed at least two different backends have been used: 
    - One directly based on [Unide][3]
    - One based on [CONTACT Elements for IoT][4].


## Hands-on demonstration

In order to make this scenario more hands-on and interesting, within the
[Production Performance Management Testbed repository][5] project in Github, we
have set up a [subproject for the grinding machine scenario][7]. There you can
find a thorough description and code to simulate the device and the gateway,
and route messages to a given backend. The only requirements are git and conda
(the easiest way to get conda is to [install miniconda][6]).

So, let's get started! First of all, open your terminal and clone the
repository:

```bash
$ git clone https://github.com/eclipselabs/eclipseiot-testbed-productionperformancemanagement eclipse-testbed-ppm
$ cd eclipse-testbed-ppm/grinding-machine
```

Then move to the grinding machine subproject directory:

```bash
$ cd eclipse-testbed-ppm/grinding-machine
```

In that folder you will see two subfolders. The first one
`unide-grinding-machine` contains a Python program that simulates grinding
machine and the second one `unide-grinding-machine-gateway` simulates the
gateway. 


### Grinding Machine simulator

Let's start with the first component. First `cd` to `unide-grinding-machine`
and then create the conda environment:

```bash
$ conda env create --prefix=env
```

And then in order to use this environment, we need to activate it.

```bash
$ source activate env    # or in windows: activate env 
```

Run `unide-grinding-machine -h` to get familiar with the CLI. With this
executable we can generate some machine grinding data, and (for the moment)
print messages to the console. To do so, run `unide-grinding-machine send
random` and you should get a large message on the console. This is the PPMP
message ought to be sent to the gateway.

We don't have a gateway so far, but don't worry, we will get to that in a
minute. Don't close this console!


### Gateway simulator

Open a new console, and similarly to the previous point, first `cd` to
`unide-grinding-machine-gateway`, create the environment (`conda env
create --prefix=env`) and activate it (`source activate env`).

The run the command `unide-grinding-machine-gateway -h` to test that everything
is in place. You should get the CLI documentation. 

Now, let's fire up the gateway by calling `unide-grinding-machine-gateway
start_server`, and a message like this should show up:

```
Running <unide_grinding_machine_gateway.server.App object at 0x.....>
Listening on http://127.0.0.1:5000
Press Ctrl-C to stop...
```

We are getting closer. Leave the gateway running.


### Communication Device-Gateway

Let's go back to the previous console where we had the `unide-grinding-machine`
environment and  call again the `unide-grinding-machine` program, but this time
passing the as an argument where the gateway is listening to messages.

```bash
$ unide-grinding-machine send random --endpoint=http://127.0.0.1:5000
```

If you take peek at the gateway output, you should see new information printed
out, which is a PPMP message containing the result of the classification
algorithms applied to the device data we just sent to it. 

We are getting somewhere! But still, it is not extremely useful to print some
characters into a console, is it?


### Communication Device-Gateway-Backend

There is only a small but relevant point missing: the routing from the gateway
to the backend. We need a backend and the Unide project provides a service that
can be used as a playground for these purposes. So let's restart the gateway,
this time passing the Unide endpoint:

```bash
$ unide-grinding-machine-gateway start_server --endpoint=https://unide.eclipse.org/rest/v2
```

Now we can use the `unide-grinding-machine` to send raw data to the gateway
which in turn sends its results to the backend.  In order to be able to
identify this data later, we are going to define a proper device ID (argument
`--device-id`), and also tell it to send data once each 10 seconds (argument
`--period`):

```bash
$ unide-grinding-machine send random --endpoint=http://127.0.0.1:5000 --device-id=IoT-000028--3 --period=10
```

Once started, a continuous flow of data is pumped through each component until
it reaches the backend, allowing us to use the applications that consume this
data. For instance, the basic backend provided by Unide offers a [Grafana][8] based
dashboard that offers live monitoring of the statistical values calculated in the 
gateway:

![Grafana based dashboard monitoring grinding machine][9]

If instead of that we use the the [CONTACT Elements for IoT][4] based backend,
we can see a different and richer dashboard: charts, 3D models, list of
maintenance events, device master data and the Activity Stream, a place where
different parties can exchange information related to the grinding machine:

![CONTACT Elements for IoT Dashboard][10]

[1]: https://iot.eclipse.org/testbeds/production-performance-management/#
[2]: https://www.eclipse.org/unide/specification/measurement-message#messageDetail
[3]: https://github.com/eclipse/unide
[4]: https://www.contact-software.com/en/products/elements-for-iot/
[5]: https://github.com/eclipselabs/eclipseiot-testbed-productionperformancemanagement
[6]: https://conda.io/miniconda.html
[7]: https://github.com/eclipselabs/eclipseiot-testbed-productionperformancemanagement/tree/master/grinding-machine
[8]: https://grafana.com/
[9]: https://raw.githubusercontent.com/bgusach/unide/master/website/blog/assets/grinding-machine-grafana-dashboard.png
[10]: https://raw.githubusercontent.com/bgusach/unide/master/website/blog/assets/grinding-machine-ce4iot-dashboard.png

