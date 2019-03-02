<template>
  <div class="machine-message content">
    <h1>
      <a id="Message-Payload" title="Machine Message Payload"></a>
      Machine Message Payload
    </h1>
    <p>The main purpose of the machine message format is to allow devices and integrators to send messages containing an interpretation of measurement data or status.</p>

    <div class="diagram">
      <img src="images/specification/v3/messagePayload.svg" alt="Class diagram of the message payload" title="Class diagram of the message payload">
    </div>
    <schemaDetail type="v3/message" :examples="$static.examples">
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
        <prism language="json">{{ $static.multipleMachineMessages | stringify }}</prism>
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
      title: 'Specification for machine messages'
    };
  },
  created() {
    const now = new Date(),
          deviceId = 'a4927dad-58d4-4580-b460-79cefd56775b';
    this.$static = {
      message: {
        'content-spec': 'urn:spec://eclipse.org/unide/machine-message#v3',
        device:         {
          id: deviceId
        },
        messages: [{
          ts:   now.toISOString(),
          code: '190ABT'
        }]
      },
      multipleMachineMessages: {
        'content-spec': 'urn:spec://eclipse.org/unide/machine-message#v3',
        device:         {
          id:             deviceId,
          mode:           'auto',
          state:          'OK',
          additionalData: {
            swVersion: '2.0.3.13',
            swBuildID: '41535'
          }
        },
        messages: [{
          code:           '190ABT',
          description:    'Electronic control board or its electrical connections are damaged',
          hint:           'Check the control board',
          origin:         'sensor-id-992.2393.22',
          severity:       'HIGH',
          title:          'control board damaged',
          ts:             now.toISOString(),
          type:           'DEVICE',
          additionalData: {
            firmware: '20130304_22.020'
          }
        }, {
          code:        '33-02',
          description: 'Disk size has reached limit. Unable to write log files.',
          severity:    'HIGH',
          title:       'Disk size limit reached',
          ts:          new Date(now.valueOf() + 100).toISOString(),
          type:        'TECHNICAL_INFO'
        }]
      }
    };
    this.$static.examples = Object.entries({
      ...[
        'content-spec',
        'device',
        'device.id',
        'device.mode',
        'device.additionalData',
        'messages',
        'messages[0].code',
        'messages[0].description',
        'messages[0].hint',
        'messages[0].origin',
        'messages[0].severity',
        'messages[0].title',
        'messages[0].ts',
        'messages[0].type',
        'messages[0].additionalData'
      ].reduce((l, v) => {
        l[v.replace(/(^|\.)/g, '$1properties.').replace(/\[[^]]*]/g, '.items')] = v;
        return l;
      }, {})
    }).reduce((l, [key, path]) => {
      const example =
        get(this.$static.message, path) ||
        get(this.$static.multipleMachineMessages, path);
      if(example) {
        l[key] = [example];
      } else {
        console.error(`no example provided in machine-message for: "${key}": "${path}"`);
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
