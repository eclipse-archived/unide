<template>
  <div class="box column is-half-desktop is-offset-one-quarter-desktop ppm">
    <h1 class="title">{{ $t("ppm.title") }}</h1>
    <card>
      <template slot="headerCollapsed">
        {{ $t('ppm.target') }} ({{ configId ? filteredConfigurations[configId].name : '?' }} / {{ deviceId? devices[deviceId].name : '?' }})
      </template>
      <template slot="header">
        {{ $t('ppm.target') }}
      </template>
      <div class="field is-horizontal">
        <div class="field-label is-normal">
          <label for="config" class="label">{{ $t('ppm.config') }}:</label>
        </div>
        <div class="field-body">
          <div class="field is-expanded">
            <div class="control">
              <span class="select">
                <select v-model="configId">
                  <option></option>
                  <option v-for="config in filteredConfigurations" :key="config._id" :value="config._id">{{ config.name }}</option>
                </select>
              </span>
            </div>
            <p v-if="connectionError" class="help is-danger">{{ $t('ppm.connectionError') }}</p>
          </div>
        </div>
      </div>
      <div class="field is-horizontal">
        <div class="field-label is-normal">
          <label for="config" class="label">{{ $t('ppm.device') }}:</label>
        </div>
        <div class="field-body">
          <div class="field is-expanded">
            <div class="control">
              <span class="select" :class="{'is-loading': loadingDevices}">
                <select v-model="deviceId" :disabled="!configId || connectionError">
                  <option v-for="device in devices" :key="device.deviceId" :value="device.deviceId" :title="device.description">{{ device.name }}</option>
                </select>
              </span>
            </div>
          </div>
        </div>
      </div>
    </card>
    <transition name="fade">
      <card v-if="configId && deviceId">
        <b-tabs :animated="false">
          <b-tab-item :label="$t('ppm.measurements.title')">
            <measurements :configId="configId" :deviceId="deviceId"/>
          </b-tab-item>
          <b-tab-item :label="$t('ppm.processes')">
            Processes
          </b-tab-item>
          <b-tab-item :label="$t('ppm.messages')">
            Messages
          </b-tab-item>
          <b-tab-item :label="$t('ppm.admin')">
            Admin
          </b-tab-item>
        </b-tabs>
      </card>
    </transition>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import card         from 'components/collapsible-card';
import measurements from './measurements';
import axios        from 'axios';

if(window.DeviceMotionEvent) {
  window.addEventListener('devicemotion', function(event) {
    /* eslint no-unused-vars: "off" */
    const ax  = event.acceleration.x,
          ay  = event.acceleration.y,
          az  = event.acceleration.z,

          agx = event.accelerationIncludingGravity.x,
          agy = event.accelerationIncludingGravity.y,
          agz = event.accelerationIncludingGravity.z,

          ra  = event.rotationRate.alpha,
          rb  = event.rotationRate.beta,
          rg  = event.rotationRate.gamma;
  });
}
if(window.DeviceOrientationEvent) {
  window.addEventListener('deviceorientation', function(event) {
    /* eslint no-unused-vars: "off" */
    // left/right
    const lr  = event.gamma,
          // front/back
          fb  = event.beta,
          // direction
          dir = event.alpha;
  });
}

const bat = navigator.battery || navigator.webkitBattery || navigator.mozBattery,
      getBatteryInfo  = function(battery) {
        /* eslint no-unused-vars: "off" */
        const bl  = battery.level,
              bc  = battery.charging,
              bct = battery.chargingTime,
              bdt = battery.dischargingTime;
        // charchingchange, chargingtimechange, discharchingtimechange, levelchange
        battery.addEventListener('chargingchange', function() {
        }, false);
      };
if(navigator.getBattery) {
  navigator.getBattery()
    .then(getBatteryInfo)
    .catch(err => {
      console.error(err);
    });
} else if(bat) {
  getBatteryInfo(bat);
}

export default {
  data: () => ({
    configId:        null,
    deviceId:        null,
    connectionError: false,
    loadingDevices:  false,
    devices:         []
  }),

  computed: Object.assign({}, {
    filteredConfigurations() {
      return Object.values(this.configuration || {}).filter(v => v.type === 'ppm').reduce((l, v) => {
        l[v._id] = v;
        return l;
      }, {});
    }
  }, mapState('configuration', ['configuration'])),

  watch: {
    configuration() {
      this.configId = null;
    },
    configId() {
      this.connectionError = null;
      this.deviceId = null;
      this.device = [];
      if(this.configId) {
        this.updateDevices();
      }
    }
  },

  methods: {
    updateDevices() {
      const cached    = {
              configId: this.configId
            },
            config    = this.configuration[this.configId];
      this.loadingDevices = true;
      return axios.get('/INL_CY/rest/v1/devices', {
        method:  'get',
        baseURL: config.url,
        params:  {},
        timeout: 5000,
        auth:    {
          username: config.user,
          password: config.password
        }
      })
        .then(res => {
        // just act, if the response corresponds to the actual setting still
          if(cached.configId === this.configId) {
            this.devices = res.data.result.sort((d1, d2) => (d1.name > d2.name) ? 1 : (d1.name < d2.name ? -1 : 0)).reduce((l, device) => {
              l[device.deviceId] = device;
              return l;
            }, {});
            this.loadingDevices = false;
          }
          return res;
        })
        .catch(err => {
        // just act, if the response corresponds to the actual setting still
          if(cached.configId === this.configId) {
            this.connectionError = err;
            this.loadingDevices = false;
          }
          throw err;
        });
    }
  },

  components: {
    card,
    measurements
  }
};

</script>

<style lang="scss">
.ppm {
  .field .control select {
    width: 10em;
  }
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }
  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
}
@media screen and (min-width: 769px), print {
.ppm {
  .field-label {
    flex-grow: 2;
  }
}
}
</style>
