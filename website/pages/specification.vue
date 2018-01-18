<template>
  <div class="specification">
    <div class="content">
      <h1>
        <a id="PPMP-Specification" title="PPMP Specification"></a>
        PPMP Specification
      </h1>
      <h2>Rationale</h2>
      <p>
      Noticeably in recent years, there is a continuous increase in demand, price pressure and complexity in manufacturing. Things need to move faster, be more flexible, and fulfill higher quality standards forcing manufacturers to optimize their processes.
      </p>
      <p>
      Production facilities and their performance have a huge impact on the overall performance of manufacturing processes. However, to identify bottlenecks and possibilities for improvements detailed data from machines are required.
      </p>
      <p>
      Such data is surely provided by machines either via modern protocols such as OPC UA or via proprietary access methods but it is not provided in a common and easily accessible format, which can be used to do performance analysis and optimization. This renders holistic process improvement efforts infeasible for many, especially smaller organizations.
      </p>
      <p>
      The Production Performance Management Protocol (PPMP) specifies a format that allows to capture data that is required to do performance analysis of production facilities. It allows monitoring backends to collect and evaluate key metrics of machines in the context of a production process. It is doing that by allowing to relate the machine status with currently produced parts.
      </p>
      <p>
      The specification is structured into three payload formats: Measurement payload, message payload and process payload. The Measurement payload contains measurements from machines such as the temperature of a machine at a specific point in time together with the currently produced part. The message payload contains arbitrary messages sent by a machine, e.g. alerts or the like. A process message consists of information about a discrete e.g. tightening or welding processes with all their characterising data which are needed to describe and analyze it.
      </p>
      <p>
      The default way of transporting the json payload is via http to allow for an easy integration into various backend systems. Other transportation methods are possible and welcome.
      </p>
    </div>
    <div class="columns" id="messageDetail">
      <nuxt-link :to="'/specification' + (isStatic ? '#messageDetail' : '')" exact class="column" tag="div">
        <a>Overview</a>
      </nuxt-link>
      <nuxt-link :to="'/specification/machine-message' + (isStatic ? '#messageDetail' : '')" class="column" tag="div">
        <a>Machine&nbsp;Message</a>
      </nuxt-link>
      <nuxt-link :to="'/specification/measurement-message' + (isStatic ? '#messageDetail' : '')" class="column" tag="div">
        <a>Measurement&nbsp;Message</a>
      </nuxt-link>
      <nuxt-link :to="'/specification/process-message' + (isStatic ? '#messageDetail' : '')" class="column" tag="div">
        <a>Process&nbsp;Message</a>
      </nuxt-link>
    </div>

    <nuxt-child/>
  </div>
</template>

<script>

export default {
  asyncData({ isStatic }) {
    return { isStatic };
  }
};
</script>

<style lang="scss">
@import "~assets/variables.scss";

.specification {
  .card-content {
    padding: 0.5em;
  }
  img.is-centered {
    max-width: 100%;
    margin-left: auto;
    margin-right: auto;
    display: block;
  }
  .property.card {
    .card-content {
      font-size: 0.875em;
      pre[class*="language-"] {
        width: 100%;
      }
      .field-body {
        display: block;
        &>div:not(:last-child) {
          margin-bottom: 0.75rem;
        }
      }
    }
    margin-bottom: 2em;
    & > .card-header {
      p {
        margin-bottom: 0;
      }
      background-color: $light;
      .card-header-icon {
        text-decoration: none;
      }
    }
  }
  .property {
    ul {
      list-style-type: disc;
      padding-left: 1em;
      margin-left: 0;
      margin-top: 0;
    }
  }
  #messageDetail.columns {
    margin-top: 3em;
    border-bottom: 2px solid $primary;
    margin-bottom: 2em;
    > .column {
      a {
        font-size: 1.2rem;
        color: $bosch-darkgray;
        display: block;
        padding: 1rem 0.5rem;
        text-decoration: none;
        &:hover {
          color: $bosch-lima;
          font-weight: bold;
        }
      }
      &.nuxt-link-active a {
        color: $bosch-lima;
      }
    }
  }
}
</style>

