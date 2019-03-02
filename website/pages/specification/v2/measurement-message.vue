<template>
  <div class="measurement-message content">
    <h1>
      <a id="Measurement-Payload" title="Measurement Payload"></a>
      Measurement Payload
    </h1>
    <p>The measurement message is the format to exchange simple (non-structured, non-complex ) measurement data. It also allows to transport multiple measurement data (eg. values over time), called 'series'.</p>
    <img src="images/specification/v2/measurementPayload.svg" alt="Class diagram of the measurement payload" title="Class diagram of the measurement payload" class="is-centered">

    <schemaDetail type="v2/measurement" :examples="$static.examples">
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
import prism from 'vue-prism-component';
import card from '~/components/collapsibleCard.vue';
import get from 'lodash/get';
import schemaDetail from '~/components/schemaDetail.vue';

export default {
  head() {
    return {
      title: 'Specification for measurement messages'
    };
  },
  created() {
    const now = new Date(),
          deviceId = 'a4927dad-58d4-4580-b460-79cefd56775b';
    this.$static = {
      message: {
        'content-spec': 'urn:spec://eclipse.org/unide/measurement-message#v2',
        device:         {
          deviceID: deviceId
        },

        measurements: [
          {
            ts:     now.toISOString(),
            series: {
              // eslint-disable-next-line camelcase
              $_time:      [0, 23, 24],
              temperature: [45.4231, 46.4222, 44.2432]
            }
          }
        ]
      },
      complexMessage: {
        'content-spec': 'urn:spec://eclipse.org/unide/measurement-message#v2',
        device:         {
          deviceID:          deviceId,
          operationalStatus: 'MM',
          metaData:          {
            swVersion: '2.0.3.13',
            swBuildID: '41535'
          }
        },
        part: {
          partTypeID: 'F00VH07328',
          partID:     '420003844',
          result:     'OK',
          code:       'HUH289',
          metaData:   {
            lotID:  '845849',
            toolID: '32324-432143'
          }
        },
        measurements: [
          {
            ts:     now.toISOString(),
            code:   '190ABT',
            result: 'OK',
            series: {
              // eslint-disable-next-line camelcase
              $_time:      [0, 23, 24],
              temperature: [45.4231, 46.4222, 44.2432]
            },
            limits: {
              temperature: {
                lowerError: 40,
                lowerWarn:  45,
                upperError: 50,
                upperWarn:  47.5
              }
            }
          },
          {
            ts:     new Date(now.valueOf() - 5000).toISOString(),
            series: {
              // eslint-disable-next-line camelcase
              $_time:   [0, 130, 2633],
              pressure: [52.4, 46.32, 44.2432]
            }
          }
        ]
      }
    };
    this.$static.examples = Object.entries({
      ...[
        'content-spec',
        'device',
        'device.deviceID',
        'device.metaData',
        'device.operationalStatus',
        'measurements',
        'measurements[0].code',
        'measurements[0].limits',
        'measurements[0].result',
        'measurements[0].series',
        'measurements[0].ts',
        'part',
        'part.code',
        'part.metaData',
        'part.partID',
        'part.partTypeID',
        'part.result'
      ].reduce(
        (l, v) => {
          l[v.replace(/(^|\.)/g, '$1properties.').replace(/\[[^]]*]/g, '.items')] = v;
          return l;
        },
        {
          'properties.measurements.items.properties.limits.patternProperties["^[^$]+"]':
            'measurements[0].limits.temperature',
          'properties.measurements.items.properties.series.patternProperties["^[^$]+"]':
            'measurements[0].series.temperature'
        }
      ),
      ...[
        'lowerError',
        'lowerWarn',
        'target',
        'upperError',
        'upperWarn'
      ].reduce((l, key) => {
        l[`properties.measurements.items.properties.limits.patternProperties["^[^$]+"].properties.${key}`] = `measurements[0].limits.temperature.${key}`;
        return l;
      }, {})
    }).reduce((l, [key, path]) => {
      const example = get(this.$static.message, path) || get(this.$static.complexMessage, path);
      if(example) {
        l[key] = [example];
      }
      return l;
    }, {});
  },
  filters: {
    stringify(v) {
      return JSON.stringify(v, ' ', 2);
    }
  },
  components: {
    card,
    prism,
    schemaDetail
  }
};
</script>
