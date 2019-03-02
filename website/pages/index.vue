<template>
  <div class="home">
    <div>
      <h1 class="title is-3">
        Understand Industry devices
      </h1>
      <div class="card is-pulled-right">
        <div class="card-image">
          <figure class="image">
            <img src="images/languages.png" alt="Overview Languages" title="Overview Languages"/>
          </figure>
        </div>
        <div class="card-content">Languages of the world</div>
      </div>
      <p class="is-clearfix">
        Imagine the following: You go on vacation to a foreign country and want to get by. Usually you will neither have the time nor the resources to learn a new language for each trip. Luckily this is not necessary. A handful of simple phrases such as <i>hello</i>, <i>I am hungry</i>, etc. are already sufficient to cope with many situations. To have such phrases at hand you don’t need to be fluent in a language, a mini phrase book will already cover a good portion. Thus, to master common situations in different languages you will be well served by a collection of mini phrase books in different languages.
      </p>
      <div class="card is-pulled-left">
        <div class="card-image">
          <figure class="image">
            <img src="images/machines.jpg" alt="EDM machines" title="EDM machines">
          </figure>
        </div>
        <div class="card-content">Machines in production</div>
      </div>
      <p class="is-clearfix">
      A similar experience we face in Industry 4.0: Imagine you are a system integrator, you want to realize a monitoring application for plants of a manufacturer and, since the Eclipse iot stack realizes a good portion of functionality you need to implement, you want to build your application on top of it. It would really help you if you can communicate with the machines on a simple, commonly understood protocol that is easily processible by the existing stack. However, there are a lot of different machines out there, using a lot of different languages and accents. Older machines especially do not speak new protocols (e.g. OPC-UA). Yet, we need to understand how to monitor their condition or interpret specific measurements.<br>
      A simple approach to send and understand such industry-specific content implemented for multiple systems would ease the use of the existing Eclipse iot stack and thereby help a lot on the way to a connected industry.
      </p>
    </div>
    <div>
      <div class="card is-pulled-right">
        <div class="card-image">
          <figure class="image">
            <img src="images/analysis.jpg" alt="Cooperation" title="Cooperation">
          </figure>
        </div>
        <div class="card-content">Collaboration with partner</div>
      </div>
      <h2 class="title is-4">Production Performance Management Protocol (<i>PPMP</i>)</h2>
      <p class="is-clearfix">
      Through hands-on experience and collaboration with customers and partners we learned which information is key in understanding industry devices. For example, when sending measurements, the currently produced part or lot number together with machine identification are crucial to get any useful interpretation out of the information.<br>
      We started to capture this information in a simple payload specification and just like in the introducing language example where you can use phrases via phone, letter or in person, the payload is meant to be sent over different protocols (REST, MQTT, AMQP). This payload is the Production Performance Management Protocol (<i>PPMP</i>).
      </p>
      <div class="card is-pulled-left">
        <div class="card-image">
          <figure class="image">
            <img src="images/eclipse-426x100.png" alt="Eclipse logo" title="Eclipse logo">
          </figure>
        </div>
      </div>
      <p>
      In order to, first, leverage the Eclipse IoT stack in Industry 4.0 scenarios and, second, build a lively community around the specification, we consider the Eclipse iot working group as the premier place to host Unide.
      </p>
    </div>
  </div>
</template>

<style lang="scss">
.home {
  h2 {
    margin-top: 1rem;
  }
  .card {
    max-width: 33%;
    margin: 0 1rem 1rem 1rem;
    .card-content {
      padding: 0.5rem;
      font-size: 0.75rem;
    }
    &.is-pulled-left {
      margin-left: 0;
    }
    &.is-pulled-right {
      margin-right: 0;
      .card-content {
        text-align: right;
      }
    }
  }
}
</style>
