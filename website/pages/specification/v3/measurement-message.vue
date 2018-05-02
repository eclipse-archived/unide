<template>
  <div class="measurement-message content">
    <h1>
      <a id="Measurement-Payload" title="Measurement Message payload"></a>
      Measurement Payload
    </h1>
    <p>The measurement message is the format to exchange simple (non-structured, non-complex ) measurement data. It also allows to transport multiple measurement data (eg. values over time), called 'series'.</p>
    <img src="images/specification/v3/measurementPayload.svg" alt="Class diagram of the Measurement Message payload" title="Class diagram of the Measurement Message payload" class="is-centered">

    <schemaDetail type="v3/measurement" :examples="$static.examples">
      <card :collapsed="true">
        <template slot="header">
          Minimal message example
        </template>
        <prism language="json">{{ $static.message | stringify }}</prism>
      </card>
      <card :collapsed="true">
        <template slot="header">
          Multiple message example
        </template>
        <prism language="json">{{ $static.complexMessage | stringify }}</prism>
      </card>
    </schemaDetail>
  </div>
</template>

<script>
import prism from "vue-prism-component";
import card from "~/components/collapsibleCard.vue";
import get from "lodash/get";
import schemaDetail from "~/components/schemaDetail.vue";

export default {
  head() {
    return {
      title: "Specification for measurement messages"
    };
  },
  created() {
    const now = new Date(),
      deviceId = "a4927dad-58d4-4580-b460-79cefd56775b";
    this.$static = {
      message: {
        "content-spec": "urn:spec://eclipse.org/unide/measurement-message#v3",
        device: {
          id: deviceId
        },

        measurements: [
          {
            ts: now.toISOString(),
            series: {
              // eslint-disable-next-line camelcase
              time: [0, 23, 24],
              temperature: [45.4231, 46.4222, 44.2432]
            }
          }
        ]
      },
      complexMessage: {
        "content-spec": "urn:spec://eclipse.org/unide/measurement-message#v3",
        device: {
          id: deviceId,
          mode: "maintenance",
          state: "WARN",
          swVersion: "2.0.3.13",
          swBuildID: "41535"
        },
        part: {
          code: "HUH289",
          id: "420003844",
          type: "BATCH",
          typeId: "F00VH07328",
          result: "OK",
          lotID: "845849",
          toolID: "32324-432143"
        },
        measurements: [
          {
            code: "190ABT",
            context: {
              temperature: {
                accuracy: [0.112, 0.115, 0.129],
                limits: {
                  lowerError: -2,
                  lowerWarn: -1,
                  target: 1.21,
                  upperError: 5,
                  upperWarn: 1.5
                },
                offset: 37,
                unit: "Cel"
              }
            },
            result: "OK",
            series: {
              // eslint-disable-next-line camelcase
              time: [0, 23, 24],
              temperature: [0.4231, 2.4222, 4.2432]
            },
            ts: now.toISOString()
          },
          {
            context: {
              scanData: {
                type: "BASE64"
              }
            },
            series: {
              // eslint-disable-next-line camelcase
              time: [0, 130, 2633],
              pressure: [52.4, 46.32, 44.2432],
              scanData: ["Zm9vCg==", "YmFyCg==", "Y2hlZXNlCg=="]
            },
            ts: new Date(now.valueOf() - 5000).toISOString()
          }
        ]
      }
    };
    this.$static.examples = Object.entries({
      ...[
        "content-spec",
        "device",
        "device.id",
        "device.mode",
        "device.state",
        "part",
        "part.code",
        "part.id",
        "part.result",
        "part.type",
        "part.typeId",
        "measurements",
        "measurements[0].code",
        "measurements[0].context",
        "measurements[0].result",
        "measurements[0].series",
        "measurements[0].ts",
        "part",
        "part.code",
        "part.id",
        "part.typeId",
        "part.result"
      ].reduce(
        (l, v) => {
          l[
            v.replace(/(^|\.)/g, "$1properties.").replace(/\[[^]]*]/g, ".items")
          ] = v;
          return l;
        },
        {
          'properties.measurements.items.properties.context.patternProperties["^[^$]+"]':
            "measurements[0].context.temperature",
          'properties.measurements.items.properties.series.patternProperties["^[^$]+"]':
            "measurements[0].series.temperature"
        }
      ),
      ...["accuracy", "limits", "offset", "unit"].reduce((l, key) => {
        l[
          `properties.measurements.items.properties.context.patternProperties["^[^$]+"].properties.${key}`
        ] = `measurements[0].context.temperature.${key}`;
        return l;
      }, {}),
      ...[
        "lowerError",
        "lowerWarn",
        "target",
        "upperError",
        "upperWarn"
      ].reduce((l, key) => {
        l[
          `properties.measurements.items.properties.context.patternProperties["^[^$]+"].properties.limits.oneOf[0].properties.${key}`
        ] = `measurements[0].context.temperature.limits.${key}`;
        return l;
      }, {})
    }).reduce((l, [key, path]) => {
      const example =
        get(this.$static.message, path) ||
        get(this.$static.complexMessage, path);
      if (example) {
        l[key] = [example];
      } else {
        console.error(`no example provided in measurement-message for:
"${key}": "${path}"`);
      }
      return l;
    }, {});
  },
  filters: {
    stringify(v) {
      return JSON.stringify(v, " ", 2);
    }
  },
  components: {
    card,
    prism,
    schemaDetail
  }
};
</script>
