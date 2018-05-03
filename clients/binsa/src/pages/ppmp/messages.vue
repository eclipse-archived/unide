<template>
  <div class="messages">
    <div class="box">
      <button
        v-for="(message, idx) in configuration[this.configId].messages"
        :key="idx"
        :style="{ 'background-color': message.color || 'white', color: message.color ? getFontColor(message.color) : 'black'}"
        class="button is-large"
        :class="{ 'is-loading': sendingPreconfigured === idx}"
        :disabled="sendingPreconfigured !== null && sendingPreconfigured !== idx"
        @click.prevent="sendingPreconfigured === null && sendPreconfigured(idx)"
        >
        {{ message.code }}
      </button>
    </div>
    <card :collapsed="true">
      <template slot="header">
        {{ $t('ppmp.messages.manual') }}
      </template>
      <messageForm v-model="message" class="field"/>
      <div class="field is-horizontal">
        <div class="field-label">
        </div>
        <div class="field-body">
          <div class="field is-grouped">
            <div class="control">
              <button type="button" class="button is-success" :class="{ 'is-loading': sendingManual }" :disabled="!message.code || errors.any()" @click.prevent="sendManual()">
                <i class="fa fa-floppy-o"></i>&nbsp;{{ $t("send") }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </card>
  </div>
</template>

<script>
import axios        from 'axios';
import {
  mapState
}                   from 'vuex';
import Toast        from 'buefy/src/components/toast';
import card         from 'components/collapsibleCard';
import messageForm  from 'components/messageForm';

export default {
  props: {
    configId: {
      type:     String,
      required: true
    },
    deviceId: {
      type:     String,
      required: true
    },
    operationalStatus: {
      type:     String,
      default:  null
    }
  },
  data: () => ({
    message:              {},
    sendingManual:        false,
    sendingPreconfigured: null
  }),

  methods: {
    getFontColor(hex) {
      let h = hex.substring(1);
      if(h.length === 3) {
        h = h.split('').map(c => c + c).join('');
      }
      const rgb = parseInt(h, 16),
            r   = (rgb >> 16) & 255,
            g   = (rgb >> 8) & 255,
            b   = rgb & 255;
      // https://www.w3.org/WAI/ER/WD-AERT/#q100
      if((r * 0.299 + g * 0.587 + b * 0.114) / 255 > 0.5) {
        return 'black';
      }
      return 'white';
    },
    async sendPreconfigured(idx) {
      if(this.sendingPreconfigured !== null) {
        return;
      }
      this.sendingPreconfigured = idx;
      const device = {
        deviceID: this.deviceId
      };
      if(this.operationalStatus) {
        device.operationalStatus = this.operationalStatus;
      }
      try {
        let conf = {
          method: 'post',
          url: this.configuration[this.configId].url,
          data: {
            'content-spec': 'urn:spec://eclipse.org/unide/machine-message#v2',
            device,
            messages: [Object.assign({
              ts: (new Date()).toISOString()
            },
              // eslint-disable-next-line indent
              messageForm.FIELDS.reduce((m, f) => {
                m[f] = this.configuration[this.configId].messages[idx][f];
                return m;
              }, {})
            )]
          }
        };
        if(this.configuration[this.configId].user && this.configuration[this.configId].password) {
          conf.auth = {
            username: this.configuration[this.configId].user,
            password: this.configuration[this.configId].password
          };
        }
        if(this.configuration[this.configId].appendType !== false) {
          conf.url += "/v2/message";
        }
        await axios.request(conf);
      } catch(err) {
        this.$emit('connectionError', err);
      }
      this.sendingPreconfigured = null;
    },
    async sendManual() {
      if(!this.message.code || this.errors.any() || this.sendingManual) {
        return;
      }
      this.sendingManual = true;
      const device = {
        deviceID: this.deviceId
      };
      if(this.operationalStatus) {
        device.operationalStatus = this.operationalStatus;
      }
      try {
        let conf = {
          method: 'post',
          url: this.configuration[this.configId].url,
          data: {
            'content-spec': 'urn:spec://eclipse.org/unide/machine-message#v2',
            device,
            messages: [Object.assign({
              ts: (new Date()).toISOString()
            },
              // eslint-disable-next-line indent
              messageForm.FIELDS.reduce((m, f) => {
                m[f] = this.message[f];
                return m;
              }, {})
            )]
          }
        };
        if(this.configuration[this.configId].user && this.configuration[this.configId].password) {
          conf.auth = {
            username: this.configuration[this.configId].user,
            password: this.configuration[this.configId].password
          };
        }
        if(this.configuration[this.configId].appendType !== false) {
          conf.url += "/v2/message";
        }
        await axios.request(conf);
      } catch(err) {
      }
      this.sendingManual = false;
    }
  },

  computed: Object.assign({
  }, mapState('configuration', ['configuration'])),

  components: { card, messageForm }
};
</script>

<style lang="scss">
@import "~styles/variables.scss";

.b-tabs section.tab-content {
  overflow: visible;
}

.messages {
  &>.box {
    display: flex;
    flex-wrap: wrap;
    .button {
      flex-grow: 1;
    }
  }
}
</style>
