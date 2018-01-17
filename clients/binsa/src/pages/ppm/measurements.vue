<template>
  <div class="measurements">
    <div class="field is-horizontal">
      <div class="field-label is-normal">
        <label for="config" class="label">{{ $t('ppm.measurements.sensors') }}:</label>
      </div>
      <div class="field-body">
        <div class="field is-expanded">
          <div class="control">
            <multiselect
              v-model="selected"
              :options="measurementPoints"
              :hideSelected="true"
              :multiple="true"
              :closeOnSelect="false"
              :loading="loadingMeasurementPoints"
              :placeholder="$t('ppm.measurements.placeholder')"
              :selectLabel="$t('ppm.measurements.selectLabel')"
              :selectedLabel="$t('ppm.measurements.selectedLabel')"
              :deselectLabel="$t('ppm.measurements.deselectLabel')">
              <span slot="noResult">
                {{ $t('ppm.measurements.noResults') }}
              </span>
            </multiselect> 
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Multiselect  from 'vue-multiselect';
// import Multiselect  from 'vue-multiselect/src/Multiselect';
import axios        from 'axios';
import { mapState } from 'vuex';

export default {
  props: {
    configId: {
      type:     String,
      required: true
    },
    deviceId: {
      type:     String,
      required: true
    }
  },
  data: () => ({
    selected:                 null,
    measurementPoints:        [],
    loadingMeasurementPoints: false
  }),
  created() {
    this.updateMeasurementPoints();
  },
  computed: mapState('configuration', ['configuration']),
  watch:    {
    confidId() {
    },
    deviceId() {
      this.measurementPoints = [];
      this.selected = [];
      if(this.deviceId) {
        this.updateMeasurementPoints();
      }
    }
  },
  methods: {
    updateMeasurementPoints() {
      const cached = {
              configId: this.configId,
              deviceId: this.deviceId
            },
            config = this.configuration[this.configId];
      this.loadingMeasurementPoints = true;
      axios.get(`/INL_CY/rest/v1/devices/${this.deviceId}`, {
        method:  'get',
        baseURL: config.url,
        params:  {},
        timeout: 5000,
        auth:    {
          username: config.user,
          password: config.password
        }
      })
        .then(res =>
          axios.get(`/INL_CY/rest/v1/devicetypes/${res.data.result.deviceTypeId}`, {
            method:  'get',
            baseURL: config.url,
            params:  {},
            timeout: 5000,
            auth:    {
              username: config.user,
              password: config.password
            }
          })
        )
        .then(res => {
          if(cached.configId === this.configId && cached.deviceId === this.deviceId) {
            this.measurementPoints = res.data.result.deviceProperties.filter(prop => prop.devicePropertyType === 'measurement').map(prop => prop.name);
            this.loadingMeasurementPoints = false;
          }
          return res;
        })
        .catch(() => {
          if(cached.configId === this.configId && cached.deviceId === this.deviceId) {
            this.loadingMeasurementPoints = false;
          }
        });
    }
  },
  components: { Multiselect }
};

</script>

<style lang="scss">
@import "~styles/variables.scss";

.measurements {
  .multiselect__tag, .multiselect__option--highlight, .multiselect__option--highlight:after {
    background: $primary;
  }
  .multiselect__spinner:after {
    border-color: $primary transparent transparent;
  }
}
.b-tabs section.tab-content {
  overflow: visible;
}
</style>
