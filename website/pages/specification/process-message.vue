<template>
  <div class="process-message content">
    <h1>
      <a id="Process-Payload" title="Process Payload"></a>
      Process Payload
    </h1>
    <p>The process message is the format to exchange data out of discrete processes. It also allows to transport process information, part information and measurement data for each phase of the process.</p>
    <img src="images/processPayload.svg" alt="Class diagram of the process data payload" title="Class diagram of the process data payload" class="is-center">

    <schemaDetail type="process" :examples="$static.examples">
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
import prism         from 'vue-prism-component';
import card          from '~/components/collapsibleCard.vue';
import schemaDetail  from '~/components/schemaDetail.vue';

export default {
  created() {
    const now      = new Date(),
          deviceId = "a4927dad-58d4-4580-b460-79cefd56775b";
    this.$static = {
      message: {
        "content-spec": "urn:spec://eclipse.org/unide/process-message#v2",
        device: {
          deviceID: deviceId
        },
        process: {
          ts: now.toISOString()
        },
        measurements: [
          {
            ts: (new Date(now.valueOf() + 100)).toISOString(),
            series: {
              force: [26, 23, 24],
              pressure: [52.4, 46.32, 44.2432]		
            }
          }
        ]
      },
      complexMessage: {
        "content-spec": "urn:spec://eclipse.org/unide/process-message#v2",
        device: {
          deviceID: deviceId,
          operationalStatus: "normal",
          metaData: {
            swVersion: "2.0.3.13",
            swBuildId: "41535" 
          }
        },
        part: {
          type: "SINGLE",
          partTypeID: "F00VH07328", 
          partID: "420003844",
          result: "NOK",
          code: "HUH289",
          metaData: {
            toolId: "32324-432143"     
          }
        },
        process: { 
          externalProcessId: "b4927dad-58d4-4580-b460-79cefd56775b",
          ts: now.toISOString(),
          result: "NOK",
          shutoffPhase: "phase 2",
          program: {
            id: "1",
            name: "Programm 1",
            lastChangedDate: "2002-05-30T09:30:10.123+02:00"
          },
          shutoffValues: {
            force: {
              ts: (new Date(now.valueOf() + 10000)).toISOString(),
              value: 24,
              upperError: 26,
              lowerError: 22,
              upperWarn: 25,
              lowerWarn: 23,
              target: 24
            },
            pressure: {               
              value: 50,
              upperError: 52,
              lowerError: 48
            }
          },
          metaData: {
            maxDuration: "30min",
            escalation: "shift leader"
          }
        },
        measurements: [{
          ts: (new Date(now.valueOf() + 100)).toISOString(),
          phase: "phase 1",
          name: "heating up",
          result: "OK",   
          code: "0000 EE01",
          limits: {
            pressure: {
              upperError: 4444,
              lowerError: 44,
              upperWarn: 2222,
              lowerWarn: 46,
              target: 35
            },
            force: {
              upperError: [27, 24, 25],
              lowerError: [25, 22, 23]                               
            }
          },
          specialValues: [{
            "$_time": 12,
            name: "turning point",
            value: {
              pressure: 24,
              force: 50
            }
          }],
          series: {
            time: [30, 36, 42],
            force: [26, 23, 24],
            pressure: [52.4, 46.32, 44.2432],
            temperature: [45.4243, 46.42342, 44.2432]           
          }
        }, {
          ts: (new Date(now.valueOf() + 430)).toISOString(),
          phase: "phase 2",
          name: "processing",
          result: "OK",
          series: {
            "$_time": [ 0, 23, 24 ],
            temperature: [ 49.2, 48.8, 50 ]
          }
        }]
      }
    };
    this.$static.examples = {
      "$/properties/content-spec": [this.$static.message, 'content-spec'],
      "$/properties/device": [this.$static.complexMessage, 'device'],
      "$/properties/device/properties/deviceID": [this.$static.message.device, 'deviceID'],
      "$/properties/device/properties/metaData": [this.$static.complexMessage.device, 'metaData'],
      "$/properties/device/properties/operationalStatus": [this.$static.complexMessage.device, 'operationalStatus'],
      "$/properties/measurements": [this.$static.message, 'measurements'],
      "$/properties/measurements//properties/code": [this.$static.complexMessage.measurements[0], 'code'],
      "$/properties/measurements//properties/limits": [this.$static.complexMessage.measurements[0], 'limits'],
      "$/properties/measurements//properties/limits/patternProperties/%5E%5B%5E%24%5D.%2B": [this.$static.complexMessage.measurements[0].limits, 'pressure'],
      ...['lowerError', 'lowerWarn', 'target', 'upperError', 'upperWarn'].reduce((l, key) => {
        l[`$/properties/measurements//properties/limits/patternProperties/%5E%5B%5E%24%5D.%2B/properties/${key}`] = [this.$static.complexMessage.measurements[0].limits.pressure, key];
        return l;
      }, {}),
      "$/properties/measurements//properties/name": [this.$static.complexMessage.measurements[0], 'name'],
      "$/properties/measurements//properties/phase": [this.$static.complexMessage.measurements[0], 'phase'],
      "$/properties/measurements//properties/result": [this.$static.complexMessage.measurements[0], 'result'],
      "$/properties/measurements//properties/series": [this.$static.message.measurements[0], 'series'],
      "$/properties/measurements//properties/series/patternProperties/%5E%5B%5E%24%5D.%2B": [this.$static.complexMessage.measurements[0].series, 'force'],
      "$/properties/measurements//properties/series/properties/%24_time": [this.$static.complexMessage.measurements[1].series, '$_time'],
      "$/properties/measurements//properties/specialValues": [this.$static.complexMessage.measurements[0], 'specialValues'],
      "$/properties/measurements//properties/specialValues//properties/%24_time": [this.$static.complexMessage.measurements[0].specialValues[0], '$_time'],
      "$/properties/measurements//properties/specialValues//properties/name": [this.$static.complexMessage.measurements[0].specialValues[0], 'name'],
      "$/properties/measurements//properties/specialValues//properties/value": [this.$static.complexMessage.measurements[0].specialValues[0], 'value'],
      "$/properties/measurements//properties/specialValues//properties/value/patternProperties/%5E%5B%5E%24%5D.%2B": [this.$static.complexMessage.measurements[0].specialValues[0].value, 'pressure'],
      "$/properties/measurements//properties/ts": [this.$static.complexMessage.measurements[0], 'ts'],
      "$/properties/part": [this.$static.complexMessage, 'part'],
      "$/properties/part/properties/code": [this.$static.complexMessage.part, 'code'],
      "$/properties/part/properties/metaData": [this.$static.complexMessage.part, 'metaData'],
      "$/properties/part/properties/partID": [this.$static.complexMessage.part, 'partId'],
      "$/properties/part/properties/partTypeID": [this.$static.complexMessage.part, 'partTypeID'],
      "$/properties/part/properties/result": [this.$static.complexMessage.part, 'result'],
      "$/properties/part/properties/type": [this.$static.complexMessage.part, 'type'],
      "$/properties/process": [this.$static.complexMessage, 'process'],
      "$/properties/process/properties/externalProcessId": [this.$static.complexMessage.process, 'externalProcessId'],
      "$/properties/process/properties/metaData": [this.$static.complexMessage.process, 'metaData'],
      "$/properties/process/properties/program": [this.$static.complexMessage.process, 'program'],
      "$/properties/process/properties/program/properties/id": [this.$static.complexMessage.process.program, 'id'],
      "$/properties/process/properties/program/properties/lastChangedDate": [this.$static.complexMessage.process.program, 'lastChangedDate'],
      "$/properties/process/properties/program/properties/name": [this.$static.complexMessage.process.program, 'name'],
      "$/properties/process/properties/result": [this.$static.complexMessage.process, 'result'],
      "$/properties/process/properties/shutoffPhase": [this.$static.complexMessage.process, 'shutoffPhase'],
      "$/properties/process/properties/shutoffValues": [this.$static.complexMessage.process, 'shutoffValues'],
      "$/properties/process/properties/shutoffValues/patternProperties/%5E%5B%5E%24%5D.%2B": [this.$static.complexMessage.process.shutoffValues, 'force'],
      ...['lowerError', 'lowerWarn', 'target', 'upperError', 'upperWarn', 'value', 'ts'].reduce((l, key) => {
        l[`$/properties/process/properties/shutoffValues/patternProperties/%5E%5B%5E%24%5D.%2B/properties/${key}`] = [this.$static.complexMessage.process.shutoffValues.force, key];
        return l;
      }, {}),
      "$/properties/process/properties/ts": [this.$static.complexMessage.process, 'ts']
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
}
</script>
