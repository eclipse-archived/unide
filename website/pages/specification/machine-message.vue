<template>
  <div class="machine-message content">
    <h1>
      <a id="Message-Payload" title="Machine Message Payload"></a>
      Machine Message Payload
    </h1>
    <p>The main purpose of the machine message format is to allow devices and integrators to send messages containing an interpretation of measurement data or status.</p>

    <img src="images/messagePayload.svg" alt="Class diagram of the message payload" title="Class diagram of the message payload" class="is-centered">

    <schemaDetail type="message" :examples="$static.examples">
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
import prism         from 'vue-prism-component';
import card          from '~/components/collapsibleCard.vue';
import schemaDetail  from '~/components/schemaDetail.vue';


export default {
  created() {
    const now      = new Date(),
          deviceId = "a4927dad-58d4-4580-b460-79cefd56775b";
    this.$static = {
      message: {
        "content-spec":"urn:spec://eclipse.org/unide/machine-message#v2",
        device: {
          deviceID: deviceId
        },
        messages: [{
          ts: now.toISOString(),
          code: "190ABT"
        }]
      },
      multipleMachineMessages: {
        "content-spec":"urn:spec://eclipse.org/unide/machine-message#v2",
        device: {
          deviceID: deviceId,
          operationalStatus: "normal",
          metaData:{
            swVersion: "2.0.3.13",
            swBuildID: "41535"
          }
        },
        messages: [{
          origin: "sensor-id-992.2393.22",
          ts: now.toISOString(),
          type: "DEVICE",
          severity: "HIGH",
          code: "190ABT",
          title: "control board damaged",
          description: "Electronic control board or its electrical connections are damaged", 
          hint: "Check the control board",
          metaData: {
            firmware: "20130304_22.020"
          }
        }, {
          ts: (new Date(now.valueOf() + 100)).toISOString(),
          type: "TECHNICAL_INFO",
          severity: "HIGH",
          code: "33-02",
          title: "Disk size limit reached",
          description: "Disk size has reached limit. Unable to write log files."
        }]
      }
    };
    this.$static.examples =  {
        "$/properties/content-spec": [this.$static.message, 'content-spec'],
        "$/properties/device": [this.$static.message, 'device'],
        "$/properties/device/properties/deviceID": [this.$static.message.device, 'deviceID'],
        "$/properties/device/properties/operationalStatus": [this.$static.multipleMachineMessages.device, 'operationalStatus'],
        "$/properties/device/properties/metaData": [this.$static.multipleMachineMessages.device, 'metaData'],
        "$/properties/messages": [this.$static.message, 'messages'],
        "$/properties/messages//properties/ts": [this.$static.message.messages[0], 'ts'],
        "$/properties/messages//properties/origin": [this.$static.multipleMachineMessages.messages[0], 'origin'],
        "$/properties/messages//properties/type": [this.$static.multipleMachineMessages.messages[0], 'type'],
        "$/properties/messages//properties/severity": [this.$static.multipleMachineMessages.messages[0], 'severity'],
        "$/properties/messages//properties/code": [this.$static.message.messages[0], 'code'],
        "$/properties/messages//properties/title": [this.$static.multipleMachineMessages.messages[0], 'title'],
        "$/properties/messages//properties/description": [this.$static.multipleMachineMessages.messages[0], 'description'],
        "$/properties/messages//properties/hint": [this.$static.multipleMachineMessages.messages[0], 'hint'],
        "$/properties/messages//properties/metaData": [this.$static.multipleMachineMessages.messages[0], 'metaData']
      }
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
