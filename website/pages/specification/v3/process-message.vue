<template>
  <div class="process-message content">
    <h1>
      <a id="Process-Payload" title="Process Message payload"></a>
      Process Payload
    </h1>
    <p>The process message is the format to exchange data out of discrete processes. It also allows to transport process information, part information and measurement data for each phase of the process.</p>
    <img src="images/specification/v3/processPayload.svg" alt="Class diagram of the Process message payload" title="Class diagram of the Process Message payload" class="is-center">

    <schemaDetail type="v3/process" :examples="$static.examples">
      <card :collapsed="true">
        <template slot="header">
          Minimal message example 
        </template>
        <prism language="json">{{ $static.message | stringify }}</prism>
      </card>

      <card :collapsed="true">
        <template slot="header">
          Process message example 
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
      title: "Specification for process messages"
    };
  },
  created() {
    const now = new Date(),
      deviceId = "a4927dad-58d4-4580-b460-79cefd56775b";
    this.$static = {
      message: {
        'content-spec': 'urn:spec://eclipse.org/unide/process-message#v2',
        device:         {
          deviceID: deviceId
        },
        process: {
          ts: now.toISOString()
        },
        measurements: [
          {
            ts:     (new Date(now.valueOf() + 100)).toISOString(),
            series: {
              force:    [26, 23, 24],
              pressure: [52.4, 46.32, 44.2432]
            }
          }
        ]
      },
      complexMessage: {
        'content-spec': 'urn:spec://eclipse.org/unide/process-message#v2',
        device:         {
          deviceID:          deviceId,
          operationalStatus: 'normal',
          metaData:          {
            swVersion: '2.0.3.13',
            swBuildId: '41535'
          }
        },
        part: {
          type:       'SINGLE',
          partTypeID: 'F00VH07328',
          partID:     '420003844',
          result:     'NOK',
          code:       'HUH289',
          metaData:   {
            toolId: '32324-432143'
          }
        },
        process: {
          externalProcessId: 'b4927dad-58d4-4580-b460-79cefd56775b',
          ts:                now.toISOString(),
          result:            'NOK',
          shutoffPhase:      'phase 2',
          program:           {
            id:              '1',
            name:            'Programm 1',
            lastChangedDate: '2002-05-30T09:30:10.123+02:00'
          },
          shutoffValues: {
            force: {
              ts:         (new Date(now.valueOf() + 10000)).toISOString(),
              value:      24,
              upperError: 26,
              lowerError: 22,
              upperWarn:  25,
              lowerWarn:  23,
              target:     24
            },
            pressure: {
              value:      50,
              upperError: 52,
              lowerError: 48
            }
          },
          metaData: {
            maxDuration: '30min',
            escalation:  'shift leader'
          }
        },
        measurements: [{
          ts:     (new Date(now.valueOf() + 100)).toISOString(),
          phase:  'phase 1',
          name:   'heating up',
          result: 'OK',
          code:   '0000 EE01',
          context: {
            limits: {
              pressure: {
                upperError: 4444,
                lowerError: 44,
                upperWarn:  2222,
                lowerWarn:  46,
                target:     35
              },
              force: {
                upperError: [27, 24, 25],
                lowerError: [25, 22, 23]
              }
            }
          },
          specialValues: [{
            // eslint-disable-next-line camelcase
            time: 12,
            name:   'turning point',
            value:  {
              pressure: 24,
              force:    50
            }
          }],
          series: {
            time:        [30, 36, 42],
            force:       [26, 23, 24],
            pressure:    [52.4, 46.32, 44.2432],
            temperature: [45.4243, 46.42342, 44.2432]
          }
        }, {
          ts:     (new Date(now.valueOf() + 430)).toISOString(),
          phase:  'phase 2',
          name:   'processing',
          result: 'OK',
          series: {
            // eslint-disable-next-line camelcase
            time:      [0, 23, 24],
            temperature: [49.2, 48.8, 50]
          }
        }]
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
        'measurements[0].name',
        'measurements[0].phase',
        'measurements[0].result',
        'measurements[0].series',
        'measurements[0].series.time',
        'measurements[0].specialValues',
        'measurements[0].specialValues[0].time',
        'measurements[0].specialValues[0].name',
        'measurements[0].specialValues[0].value',
        'measurements[0].ts',
        'part',
        'part.code',
        'part.metaData',
        'part.partID',
        'part.partTypeID',
        'part.result',
        'part.type',
        'process',
        'process.externalProcessId',
        'process.metaData',
        'process.program',
        'process.program.id',
        'process.program.lastChangedDate',
        'process.program.name',
        'process.result',
        'process.shutoffPhase',
        'process.shutoffValues',
        'process.ts']
      .reduce((l, v) => {
        l[v.replace(/(^|\.)/g, '$1properties.').replace(/\[[^]]*]/g, '.items')] = v;
        return l;
      }, {
        'properties.measurements.items.properties.limits.patternProperties["^[^$]+"].oneOf[0]': 'measurements[0].limits.pressure',
        'properties.measurements.items.properties.limits.patternProperties["^[^$]+"].oneOf[1]': 'measurements[0].limits.force',
        'properties.measurements.items.properties.series.patternProperties["^[^$]+"]':          'measurements[0].series.force',
        'properties.process.properties.shutoffValues.patternProperties["^[^$]+"]':              'process.shutoffValues.force'
      }),
      ...['lowerError', 'lowerWarn', 'target', 'upperError', 'upperWarn'].reduce((l, key) => {
        l[`properties.measurements.items.properties.limits.patternProperties["^[^$]+"].oneOf[0].properties.${key}`] = `measurements[0].limits.pressure.${key}`;
        l[`properties.measurements.items.properties.limits.patternProperties["^[^$]+"].oneOf[1].properties.${key}`] = `measurements[0].limits.force.${key}`;
        l[`properties.process.properties.shutoffValues.patternProperties["^[^$]+"].properties.${key}`] = `process.shutoffValues.force.${key}`
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
