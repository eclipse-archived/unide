<template>
  <div class="measurement-message content">
    <h1>
      <a id="Measurement-Payload" title="Measurement Payload"></a>
      Measurement Payload
    </h1>
    <p>The measurement message is the format to exchange simple (non-structured, non-complex ) measurement data. It also allows to transport multiple measurement data (eg. values over time), called 'series'.</p>
    <img src="images/measurementPayload.svg" alt="Class diagram of the measurement payload" title="Class diagram of the measurement payload" class="is-centered">

    <schemaDetail type="measurement" :examples="$static.examples">
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
import prism         from 'vue-prism-component';
import card          from '~/components/collapsibleCard.vue';
import schemaDetail  from '~/components/schemaDetail.vue';

export default {
  created() {
    const now      = new Date(),
          deviceId = 'a4927dad-58d4-4580-b460-79cefd56775b';
    this.$static = {
      message: {
        'content-spec': 'urn:spec://eclipse.org/unide/measurement-message#v2',
        device:         {
          deviceID: deviceId
        },

        measurements: [{
          ts:     now.toISOString(),
          series: {
            // eslint-disable-next-line camelcase
            $_time:      [0, 23, 24],
            temperature: [45.4231, 46.4222, 44.2432]
          }
        }]
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
        measurements: [{
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
              lowerWork:  45,
              upperError: 50,
              upperWarn:  47.5
            }
          }
        }, {
          ts:     (new Date(now.valueOf() - 5000)).toISOString(),
          series: {
            // eslint-disable-next-line camelcase
            $_time:   [0, 130, 2633],
            pressure: [52.4, 46.32, 44.2432]
          }
        }]
      }
    };
    this.$static.examples = {
      '$/properties/content-spec':                                                                                [this.$static.message, 'content-spec'],
      '$/properties/device':                                                                                      [this.$static.complexMessage, 'device'],
      '$/properties/device/properties/deviceID':                                                                  [this.$static.message.device, 'deviceID'],
      '$/properties/device/properties/metaData':                                                                  [this.$static.complexMessage.device, 'metaData'],
      '$/properties/device/properties/operationalStatus':                                                         [this.$static.complexMessage.device, 'operationalStatus'],
      '$/properties/measurements':                                                                                [this.$static.message, 'measurements'],
      '$/properties/measurements//properties/code':                                                               [this.$static.complexMessage.measurements[0], 'code'],
      '$/properties/measurements//properties/limits':                                                             [this.$static.complexMessage.measurements[0], 'limits'],
      '$/properties/measurements//properties/limits/patternProperties/%5E%5B%5E%24%5D.%2B':                       [this.$static.complexMessage.measurements[0].limits, 'temperature'],
      '$/properties/measurements//properties/limits/patternProperties/%5E%5B%5E%24%5D.%2B/properties/lowerError': [this.$static.complexMessage.measurements[0].limits.temperature, 'lowerError'],
      '$/properties/measurements//properties/limits/patternProperties/%5E%5B%5E%24%5D.%2B/properties/lowerWarn':  [this.$static.complexMessage.measurements[0].limits.temperature, 'lowerWarn'],
      '$/properties/measurements//properties/limits/patternProperties/%5E%5B%5E%24%5D.%2B/properties/upperError': [this.$static.complexMessage.measurements[0].limits.temperature, 'upperError'],
      '$/properties/measurements//properties/limits/patternProperties/%5E%5B%5E%24%5D.%2B/properties/upperWarn':  [this.$static.complexMessage.measurements[0].limits.temperature, 'upperWarn'],
      '$/properties/measurements//properties/result':                                                             [this.$static.complexMessage.measurements[0], 'result'],
      '$/properties/measurements//properties/series':                                                             [this.$static.message.measurements[0], 'series'],
      '$/properties/measurements//properties/series/patternProperties/%5E%5B%5E%24%5D.%2B':                       [this.$static.message.measurements[0].series, 'temperature'],
      '$/properties/measurements//properties/series/properties/%24_time':                                         [this.$static.message.measurements[0].series, '$_time'],
      '$/properties/measurements//properties/ts':                                                                 [this.$static.message.measurements[0], 'ts'],
      '$/properties/part':                                                                                        [this.$static.complexMessage, 'part'],
      '$/properties/part/properties/code':                                                                        [this.$static.complexMessage.part, 'code'],
      '$/properties/part/properties/metaData':                                                                    [this.$static.complexMessage.part, 'metaData'],
      '$/properties/part/properties/partID':                                                                      [this.$static.complexMessage.part, 'partID'],
      '$/properties/part/properties/partTypeID':                                                                  [this.$static.complexMessage.part, 'partTypeID'],
      '$/properties/part/properties/result':                                                                      [this.$static.complexMessage.part, 'result']
    };
  },
  filters: {
    stringify(v) {
      return JSON.stringify(v, ' ', 2);
    }
  },
  components: {
    card, prism, schemaDetail
  }
};
</script>
