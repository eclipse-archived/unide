<template>
  <div class="measurements">
    <nav class="card">
      <header class="card-header">
        <p class="card-header-title">
          {{ $t('ppmp.measurements.sensors') }}
          <span v-if="countTotalTranslations" class="tag is-primary">{{ $t('ppmp.measurements.selected', [countTotalTranslations]) }}</span>
        </p>
        <span class="card-header-icon" @click="sending ? stopSending() : prepareSending()" :class="{ disabled: !countTotalTranslations }">
          <span class="icon" :class="{ 'has-text-danger': sending, 'has-text-primary': countTotalTranslations && !sending }">
            <i class="fa" :class="sending?'fa-stop-circle-o':'fa-play-circle-o'"></i>
          </span>
        </span>
      </header>
      <card class="card-content" :collapsed="true" v-for="type in Object.keys(measurementPoints).filter(k => measurementPoints[k])" :key="type">
        <template slot="headerCollapsed">
          <span class="panel-icon">
            <i class="fa fa-arrows-alt"></i>
          </span>
          {{ $t(`ppmp.measurements.sensorNames.${type}`) }}
          <span v-if="countTranslations[type]" class="tag is-primary">{{ $t('ppmp.measurements.selected', [countTranslations[type]]) }}</span>
        </template>
        <template slot="header">
          <span class="panel-icon">
            <i class="fa fa-arrows-alt"></i>
          </span>
          {{ $t(`ppmp.measurements.sensorNames.${type}`) }}
        </template>
        <table class="table is-striped is-fullwidth">
          <thead>
            <tr>
              <th>{{ $t('ppmp.measurements.sensor') }}</th>
              <th>{{ $t('ppmp.measurements.value') }}</th>
              <th>{{ $t('ppmp.measurements.alias') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="k in Object.keys(measurementPoints[type])" :key="k">
              <td>{{ k }}</td>
              <td>{{ (measurementPoints[type][k] || 0).toFixed(2) }}</td>
              <td>
                <span v-if="getTranslation(type, k)">
                  {{ getTranslation(type, k) }}
                  <a class="delete is-small" @click="removeTranslation(type, k)" :title="$t('delete')"></a>
                </span>
                <a v-else class="button is-primary is-small" @click="setTranslation(type, k)">
                  {{ $t('ppmp.measurements.assign') }}
                </a>
              </td>
            </tr>
          </tbody>
        </table>
      </card>
    </nav>
    <b-modal :active="showPlayDialog" :canCancel="true" :onCancel="() => showPlayDialog = false" has-modal-card class="dialog">
      <div class="modal-card">
        <section class="modal-card-body is-titleless">
          <div class="field">
            <label class="label">
              {{ $t('ppmp.measurements.sendingFrequency.title') }}
            </label>
            <div class="control">
              <input class="input" ref="sendingFrequency" required value="1000" type="number" :placeholder="$t('ppmp.measurements.sendingFrequency.placeholder')" v-validate="'required|numeric|min_value:100'" daba-vv-name="sendingFrequency">
            </div>
            <p v-show="errors.has('sendingFrequency')" class="help is-danger">{{ errors.first('sendingFrequency') }}</p>
          </div>
          <div class="field">
            <label class="label">
              {{ $t('ppmp.measurements.samplingFrequency.title') }}
            </label>
            <div class="control">
              <input class="input" ref="samplingFrequency" required value="100" type="number" :placeholder="$t('ppmp.measurements.samplingFrequency.placeholder')" v-validate="'required|numeric|min_value:10'" daba-vv-name="samplingFrequency" @input="assertSamplingFrequency">
            </div>
            <p v-show="errors.has('samplingFrequency')" class="help is-danger">{{ errors.first('samplingFrequency') }}</p>
          </div>
        </section>
        <footer class="modal-card-foot">
          <button class="button is-warning" @click="showPlayDialog = false">
            {{ $t('cancel') }}
          </button>
          <button class="button is-success" @click="startSending">
            {{ $t('send') }}
          </button>
        </footer>
      </div>
    </b-modal>
  </div>
</template>

<script>
import {
  mapActions,
  mapState
}                    from 'vuex';
import axios         from 'axios';
import Toast         from 'buefy/src/components/toast';
import Dialog        from 'buefy/src/components/dialog';
import card          from 'components/collapsibleCard';
import sensorBus     from '../../sensorBus';
import {
  AggregationJob
}                    from './sendingDaemon';
import cloneDeep     from 'lodash/cloneDeep';

const RECORDINGKEY = 'measurement';

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
    sending:           false,
    measurementPoints: cloneDeep(sensorBus.measurementPoints),
    showPlayDialog:    false
  }),

  created() {
    this.pollingInterval = setInterval(() => {
      this.$set(this, 'measurementPoints', sensorBus.measurementPoints);
    }, 100);

    this.$watch('sender', (newVal, oldVal) => {
      if(oldVal) {
        oldVal.removeListener('runningChanged', this.onSenderEvent);
      }
      if(newVal) {
        newVal.removeListener('runningChanged', this.onSenderEvent);
        newVal.addListener('runningChanged', this.onSenderEvent);
        this.onSenderEvent();
      }
    }, {
      immediate: true
    });
  },

  destroyed() {
    clearInterval(this.pollingInterval);
  },

  methods: Object.assign({
    onSenderEvent() {
      this.sending = this.sender && this.sender.running;
    },

    getTranslation(type, key) {
      let t = this.configuration[this.configId].translate;
      t = t ? t[this.deviceId] : t;
      t = t ? t[type] : t;
      t = t ? t[key] : t;
      return t;
    },

    removeTranslation(type, key) {
      if(!this.getTranslation(type, key)) {
        // key doesn't even exist
        throw new Error("given key doesn't exist");
      }

      // clone to not manipulate vuex store
      const translate = cloneDeep(this.configuration[this.configId].translate);

      // be nice, clean up
      delete translate[this.deviceId][type][key];
      if(!Object.keys(translate[this.deviceId][type]).length) {
        delete translate[this.deviceId][type];
      }
      if(!Object.keys(translate[this.deviceId]).length) {
        delete translate[this.deviceId];
      }
      return this.saveConfiguration(Object.assign({}, this.configuration[this.configId], {
        translate
      }))
        .then(() =>
          Toast.open(this.$t('ppmp.measurements.deleted'))
        )
        .catch(() => Toast.open(this.$t('ppmp.measurements.notDeleted')));
    },

    setTranslation(type, key) {
      const me = this;

      Dialog.prompt({
        message:    this.$t('ppmp.measurements.translate', { name: `${type} - ${key}` }),
        inputAttrs: {
          value: `${type}-${key}`
        },
        confirmText: this.$t('save'),
        cancelText:  this.$t('cancel'),
        onConfirm:   (value) => {
          if(!value) {
            return this.removeTranslation(type, key);
          }
          // clone to not manipulate vuex store
          const translate                   = cloneDeep(me.configuration[me.configId].translate) || {};
          translate[me.deviceId]            = translate[me.deviceId] || {};
          translate[me.deviceId][type]      = translate[me.deviceId][type] || {};
          translate[me.deviceId][type][key] = value;

          return me.saveConfiguration(Object.assign({}, me.configuration[me.configId], {
            translate
          }))
            .then(() =>
              Toast.open(me.$t('ppmp.measurements.saved'))
            )
            .catch(() => me.$t('ppmp.measurements.notSaved'));
        }
      });
    },

    async startSending() {
      const result = await this.$validator.validateAll();
      if(!result) {
        return result;
      }
      if(this.sending) {
        throw new Error('startSending: already sending');
      } else if(!this.sender) {
        this.$store.commit('configuration/ppmp/register', {
          key:      RECORDINGKEY,
          configId: this.configId,
          deviceId: this.deviceId,
          daemon:   new AggregationJob(this.configId, this.deviceId)
        });
      }
      this.showPlayDialog = false;
      this.sender.start(
        parseInt(this.$refs.samplingFrequency.value),
        parseInt(this.$refs.sendingFrequency.value),
        cache => {
            const device = {
              deviceID: this.deviceId
            };
            if(this.operationalStatus) {
              device.operationalStatus = this.operationalStatus;
            }
            axios.post(`${this.configuration[this.configId].url}/v2/measurement`, {
              'content-spec': 'urn:spec://eclipse.org/unide/measurement-message#v2',
              device,
              measurements:   Object.entries(cache).map(([name, series]) => {
                const startTime = series[0][0];
                return {
                  ts:     (new Date(startTime)).toISOString(),
                  series: {
                    // eslint-disable-next-line camelcase
                    $_time: series.map(v => v[0] - startTime),
                    [name]: series.map(v => v[1])
                  }
                };
              })
            })
              .catch(err => {
                console.error(err);
                this.stopSending();
                this.$emit('connectionError', err);
              });
        }
      );
      return true;
    },

    stopSending() {
      if(this.sending) {
        this.sender.stop();
        this.$store.commit('configuration/ppmp/register', {
          key:      RECORDINGKEY,
          configId: this.configId,
          deviceId: this.deviceId,
          daemon:   null
        });
      }
    },

    prepareSending() {
      if(this.countTotalTranslations === 0 || this.sending) {
        return;
      }
      this.showPlayDialog = true;
    },

    assertSamplingFrequency(ev) {
      const samplingFrequency = parseInt(ev.target.value);
      if(samplingFrequency > parseInt(this.$refs.sendingFrequency.value)) {
        ev.target.value = this.$refs.sendingFrequency.value;
      }
    }
  },

    // eslint-disable-next-line indent
  mapActions('configuration', ['saveConfiguration'])),

  computed: Object.assign({
    sender() {
      return this.$store.getters['configuration/ppmp/getRegistry'](RECORDINGKEY, this.configId, this.deviceId);
    },

    countTranslations() {
      return this.$store.getters['configuration/ppmp/getTranslationsFor'](this.configId, this.deviceId);
    },

    countTotalTranslations() {
      return this.$store.getters['configuration/ppmp/getTotalTranslationsFor'](this.configId, this.deviceId);
    }
  }, mapState('configuration', ['configuration'])),
  components: { card }
};

</script>

<style lang="scss">
@import "~styles/variables.scss";

.b-tabs section.tab-content {
  overflow: visible;
}

.measurements .card {
  &>header {
    background-color: $light;
    border-bottom: 1px solid rgba($black, 0.1);
    .card-header-title {
      font-weight: normal;
      span.tag {
        margin-left: 1em;
      }
    }
    .card-header-icon.disabled {
      color: $grey-lighter;
      cursor: auto;
    }
  }
  &.collapsed header {
    border-bottom: none;
  }
  .card-content.collapsing {
    position: relative;
    height: 0;
    overflow: hidden;
    transition-property: height, visibility;
    transition-duration: 0.35s;
    transition-timing-function: ease;
  }

  .field-body ul .field {
    padding-top: 0.375em;
  }
  .card-content {
    padding: 0;
  }
  .table {
    margin-bottom: 0;
  }
  .delete {
    background-color: $danger;
  }
}
</style>

