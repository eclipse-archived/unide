<template>
  <div class="specification">
    <div class="content">
      <h1>
        <a id="PPMP-Specification" title="Production Performance Management Protocol Specification"></a>
        Production Performance Management Protocol Specification
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
      The specification is structured into three payload formats: Measurement payload, message payload and process payload. The Measurement payload contains measurements from machines such as the temperature of a machine at a specific point in time together with the currently produced part. The message payload contains arbitrary messages sent by a machine, e.g. alerts or the like. A process message consists of information about a discrete e.g. tightening or welding process with all their characterizing data, which are needed to describe and analyze it.
      </p>
      <p>
      The default way of transporting the json payload is via http to allow for an easy integration into various backend systems. Other transportation methods are possible and welcome.
      </p>
      <h2>Validation</h2>
      <p>
        Did you know that you can easily validate your Production Performance Management Protocol payload against the published online version of the Production Performance Management Protocol? For exapmle, <a href="https://www.jsonschemavalidator.net">jsonschemavalidator.net</a> provides an online validation frontend. Just use your payload as JSON input and the respective github link to the Production Performance Management Protocol specification as a <code>$ref</code> in the schema. For example:
        <prism language="json">{
  "$ref": "https://raw.githubusercontent.com/eclipse/unide/master/ppmp/ppmp-schema/src/main/resources/org/eclipse/iot/unide/ppmp/v3/process_schema.json"
}</prism>
      </p>
    </div>
    <div class="columns" id="messageDetail">
      <div class="column">
        <nuxt-link :to="'/specification' + (isStatic ? '#messageDetail' : '')" exact>
          Overview
        </nuxt-link>
      </div>
      <div class="column">
        <nuxt-link :to="'/specification/machine-message' + (isStatic ? '#messageDetail' : '')" :class="{ 'nuxt-link-active': $route.path.endsWith('machine-message')}">
          Machine&nbsp;Message
        </nuxt-link>
        (v
        <nuxt-link :to="'/specification/v2/machine-message' + (isStatic ? '#messageDetail' : '')">
          2
        </nuxt-link>
        /
        <nuxt-link :to="'/specification/v3/machine-message' + (isStatic ? '#messageDetail' : '')">
          3
         </nuxt-link>
        )
      </div>
      <div class="column">
        <nuxt-link :to="'/specification/measurement-message' + (isStatic ? '#messageDetail' : '')" :class="{ 'nuxt-link-active': $route.path.endsWith('measurement-message')}">
          Measurement&nbsp;Message
        </nuxt-link>
        (v
        <nuxt-link :to="'/specification/v2/measurement-message' + (isStatic ? '#messageDetail' : '')">
          2
        </nuxt-link>
        /
        <nuxt-link :to="'/specification/v3/measurement-message' + (isStatic ? '#messageDetail' : '')">
          3
        </nuxt-link>
        )
      </div>
      <div class="column">
        <nuxt-link :to="'/specification/process-message' + (isStatic ? '#messageDetail' : '')" :class="{ 'nuxt-link-active': $route.path.endsWith('process-message')}">
          Process&nbsp;Message
        </nuxt-link>
        (v
        <nuxt-link :to="'/specification/v2/process-message' + (isStatic ? '#messageDetail' : '')">
          2
        </nuxt-link>
        /
        <nuxt-link :to="'/specification/v3/process-message' + (isStatic ? '#messageDetail' : '')">
          3
        </nuxt-link>
        )
      </div>
    </div>

    <nuxt-child/>
  </div>
</template>

<script>
import prism from 'vue-prism-component';

export default {
  head() {
    return {
      title: 'Specification'
    };
  },
  asyncData({ isStatic }) {
    return { isStatic };
  },
  components: {
    prism
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
  #messageDetail.columns {
    margin-top: 3em;
    border-bottom: 2px solid $primary;
    margin-bottom: 2em;
    > .column {
      white-space: nowrap;
      display: block;
      font-size: 1.2rem;
      color: $bosch-darkgray;
      a {
        text-decoration: none;
        &:hover {
          color: $bosch-lima;
          font-weight: bold;
        }
        &.nuxt-link-active {
          color: $bosch-lima;
        }
      }
    }
  }
  .diagram {
      text-align: center;
  }
}
</style>
