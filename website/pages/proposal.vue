<template>
  <div class="proposal content">
    <h1>
    What is Unide?
    </h1>
    <p>Noticeably in the recent years, there’s a continuous increase in demand, price pressure and complexity in manufacturing. Things need to move faster, be more flexible, and fulfill higher quality standards. Often, optimization in this regard is referred to as Production Performance Management (PPM). The basis for this optimization is information from the machines and devices related to a product, such as:</p>
    <ul>
      <li>Torque and angle for a tightening process</li>
      <li>Influx / efflux of throttle plate test station</li>
      <li>Energy consumption of spindles in a cutting process</li>
    </ul>
    <p>Nowadays, brand new machines and software are often shipped with powerful communication capabilities. For older machines, smaller factories and SMEs (Small and Medium Enterprises), it can be a challenge to catch up with the complex IT standards that come along. In fact, sometimes the hardware itself imposes limits.</p>
    <p>Nevertheless, in a manufacturing environment, monitoring makes most sense if there are no blind spots. To achieve that, upgrading machines and software is one option, retrofitting another. For the latter, the hurdle should be as low as possible. Yet, there should be a few guidelines for a one-way exchange of the most necessary information (machine messages, measurements):</p>
    <p>The structure of the data shall be:</p>
    <ul>
      <li>flexible enough to be carried via multiple protocols,<br>in order to not reinvent the wheel and leverage on existing technology</li>
      <li>similar in each case,<br>to be able to use the same software to interpret the data</li>
      <li>easily understandable,<br>so that implementation in a custom environment won’t be a hurdle</li>
      <li>as simple as possible,<br>to not produce an opinionated framework</li>
      <li>and yet containing enough information for the use cases,<br>to fulfill the functional requirements</li>
    </ul>
    <p>We decided the least common denominator of necessary information are the mandatory fields, common relevant information to be optional, and the possibility for further extension points through metadata.</p>
    <h2>
      Industry benefits of Unide
    </h2>
    <p>This open source project aims to make PPM affordable for smaller organizations by reducing cost and complexity of data communication. Through sample implementations of a simple information structure (Production Performance Management Protocol - PPMP) tailored to manufacturing environments, we want to encourage the community to use, share, and evolve this structure together.</p>
    <p>PPMP has already been introduced as part of a PPM testbed at a medium-sized Bosch plant, working with many SMEs as partners and suppliers. Furthermore, this testbed aims to create an ecosystem which allows SMEs and larger companies to collaborate efficiently.</p>
    <p>The PPMP takes an important role by ensuring interoperability between different types of devices and software. It is designed to gather and aggregate machine and sensor data from different devices. It focuses on defining the JSON payload which can be transferred via a variety of transports (REST, AMQP, MQTT). The primary goals of PPMP are:</p>
    <ol>
      <li>Integrating devices (across domains; e.g. manufacturing or energy) for data aggregation and gathering purposes</li>
      <li>Providing a very lightweight and easy-to-use solution for small and medium-sized enterprises (SMEs)</li>
    </ol>
    <h1>
      Scope
    </h1>
    <p>The Eclipse Unide project publishes the current version of Production Performance Management Protocol (PPMP) and develops simple server/client implementations. The implementations store the payloads in a database and displays them through a simple user interface. Everyone can then use these samples for their custom condition monitoring applications.</p>
    <p>The project will contain:</p>
    <p><em>PPMP Specification:</em></p>
    <ul>
      <li>Machine message and measurement interfaces</li>
      <li>JSON based</li>
      <li>Versioned</li>
    </ul>
    <p><em>Client:</em></p>
    <ul>
      <li>Chrome / nw.js App</li>
      <li>For Industry Machines and Devices</li>
      <li>Node.js/Python client running on Raspberry Pi</li>
    </ul>
    <p><em>Server:</em></p>
    <ul>
      <li>(REST) Server including persistence and simple data visualization</li>
      <li>Multiple examples for different technologies</li>
    </ul>
    <h1>
      Description
    </h1>
    <p>Eclipse Unide provides a lightweight Production Performance Management Protocol (PPMP) server-client implementations (using JSON, REST and other). The core of the protocol are two interface definitions:</p>
    <ul>
      <li>PPMP Measurement Interface: This is a public REST API with the purpose of receiving time series and machine data.</li>
      <li>PPMP Messages Interface: This is a public REST API with the purpose of receiving machine messages.</li>
    </ul>
    <h1>
     Why Here?
    </h1>
    <p>The PPMP extends the existing Eclipse IoT world by providing an easy and lightweight format for gathering data from industry sensors and machines, focusing on normalizing time series and alert messages. It can be transported by protocols like REST, AMQP, MQTT. As PPMP, these technologies do also not require complex and expensive middleware nor do their systems cause additional license fees. Eclipse is an excellent platform for developing PPMP sample implementations and evolving the format hands-on with the community. In fact, it could even join forces with existing Eclipse IoT projects like <a href="https://projects.eclipse.org/projects/iot.hono" target="_blank" rel="external">Hono</a>, <a href="https://www.eclipse.org/paho/" target="_blank" rel="external">Paho</a> or <a href="https://www.eclipse.org/kura/" target="_blank" rel="external">Kura</a>.</p>
    <h1>Interested Parties</h1>
    <ul>
      <li><a href="https://www.contact-software.com" target="_blank" rel="external">CONTACT Software GmbH</a></li>
      <li><a href="http://www.balluff.com/" target="_blank" rel="external">Balluff GmbH</a></li>
      <li><a href="http://sap.com/" target="_blank" rel="external">SAP</a>, Industry Standards &amp; Open Source</li>
      <li><a href="http://www.tcs.com/" target="_blank" rel="external">Tata Consultancy Services</a>, Internet of Things Group</li>
      <li><a href="http://www.3ds.com/" target="_blank" rel="external">Dassault Systèmes</a></li>
      <li><a href="http://www.cadis-engineering.de/" target="_blank" rel="external">CADIS Service GmbH</a></li>
      <li><a href="http://www.egt.de/" target="_blank" rel="external">EGT AG</a></li>
      <li><a href="http://www.klw.com/" target="_blank" rel="external">KLW Karl Lutz GmbH</a></li>
      <li><a href="http://www.rampf-gruppe.de/" target="_blank" rel="external">RAMPF Machine Systems GmbH &amp; Co. KG</a></li>
      <li><a href="http://www.schmalz.com/" target="_blank" rel="external">J. Schmalz GmbH</a></li>
      <li><a href="https://www.bosch-si.com/" target="_blank" rel="external">Bosch Software Innovations GmbH</a></li>
    </ul>
  </div>
</template>

<script>
export default {
  layout: 'sidebar'
};
</script>
