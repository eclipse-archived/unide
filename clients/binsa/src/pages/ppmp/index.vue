<template>
  <div class="box column is-half-desktop is-offset-one-quarter-desktop ppmp">
    <h1 class="title">{{ $t("ppmp.title") }}</h1>
    <card>
      <template slot="headerCollapsed">
        {{ $t('ppmp.target') }} ({{ (configId && filteredConfigurations[configId]) ? filteredConfigurations[configId].name : '?' }})
      </template>
      <template slot="header">
        {{ $t('ppmp.target') }}
      </template>
      <div class="field is-horizontal">
        <div class="field-label is-normal">
          <label for="configId" class="label">{{ $t('ppmp.config') }}<sup>*</sup>:</label>
        </div>
        <div class="field-body">
          <div class="field is-expanded">
            <div class="control">
              <span class="select is-fullwidth" v-if="Object.keys(filteredConfigurations).length > 0">
                <select id="configId" v-model="configId">
                  <option></option>
                  <option v-for="config in filteredConfigurations" :key="config._id" :value="config._id">{{ config.name }}</option>
                </select>
              </span>
              <a class="button is-fullwidth" v-else @click="initSample">{{ $t('ppmp.initSample') }}</a>
            </div>
            <p v-if="connectionError" class="help is-danger">{{ $t('ppmp.connectionError') }}</p>
          </div>
        </div>
      </div>
      <div class="field is-horizontal">
        <div class="field-label is-normal">
          <label for="deviceId" class="label">{{ $t('ppmp.device') }}<sup>*</sup>:</label>
        </div>
        <div class="field-body">
          <div class="field is-expanded">
            <qrField v-model.trim.lazy="deviceId" :disabled="!configId || connectionError"/>
          </div>
        </div>
      </div>
      <div class="field is-horizontal">
        <div class="field-label is-normal">
          <label for="deviceId" class="label">{{ $t('ppmp.operationalStatus') }}:</label>
        </div>
        <div class="field-body">
          <div class="field is-expanded">
            <input type="text" class="input" v-model.trim.lazy="operationalStatus" :disabled="!configId || !deviceId || connectionError"/>
          </div>
        </div>
      </div>
    </card>
    <transition name="fade">
      <div class="card" v-if="configId && filteredConfigurations[configId] && deviceId">
        <div class="card-content">
          <nav class="tabs">
            <ul>
              <router-link tag="li" active-class="is-active" :to="`${subPageBasePath}/measurements`">
                <a>
                  <span>{{ $t('ppmp.measurements.title') }}</span>
                </a>
              </router-link>
              <router-link tag="li" active-class="is-active" :to="`${subPageBasePath}/processes`">
                <a>
                  <span>{{ $t('ppmp.processes.title') }}</span>
                </a>
              </router-link>
              <router-link tag="li" active-class="is-active" :to="`${subPageBasePath}/messages`">
                <a>
                  <span>{{ $t('ppmp.messages.title') }}</span>
                </a>
              </router-link>
            </ul>
          </nav>
          <section class="tab-content">
            <router-view :operationalStatus="operationalStatus" @connectionError="connectionError = true"></router-view>
            <!--measurements :configId="configId" :deviceId="deviceId"/-->
          </section>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import card         from 'components/collapsibleCard';
import qrField      from 'components/qrField';

export default {
  data() {
    return {
      configId:           this.$route.params.configId || null,
      deviceId:           this.$route.params.deviceId || null,
      operationalStatus:  '',
      connectionError:    false
    };
  },

  created() {
    if(!this.configExists) {
      this.configId = null;
    }
  },

  computed: Object.assign({
    configExists() {
      return this.configuration && this.configuration.hasOwnProperty(this.configId);
    },
    filteredConfigurations() {
      return Object.values(this.configuration || {}).filter(v => v.type === 'ppmp' || v.type === 'ppm').reduce((l, v) => {
        l[v._id] = v;
        return l;
      }, {});
    },
    subPageBasePath() {
      return `/ppmp/${encodeURIComponent(this.configId)}/${encodeURIComponent(this.deviceId)}`;
    }
  }, mapState('configuration', ['configuration'])),

  methods: {
    async initSample() {
      const id     = (Math.max(0, Object.keys(this.configuration || {}).map(i => +i).filter(i => !isNaN(i))) + 1).toString(),
            config = {
              _id: id,
              type: "ppmp",
              name: "Eclipse Server",
              url: "https://unide.eclipse.org/rest",
              messages: [{
                code: "ok",
                title: "OK",
                description: "everything ok",
                hint: "no action necessary",
                type: "DEVICE",
                severity: "LOW",
                color: "#78BE20"
              },{
                code: "nok",
                title: "Not OK",
                description: "Something went wrong",
                hint: "take action immediately",
                type: "DEVICE",
                severity: "HIGH",
                color: "#E20015"
              },{
                code: "supempt",
                title: "Supermarket empty",
                description: "please refill raw material",
                hint: "inform material handler",
                type: "TECHNICAL_INFO",
                severity: "MEDIUM",
                color: "#A00266"
              }]
            };
      try {
        let remoteConf = await this.$store.dispatch('configuration/saveConfiguration', config);
        this.configId = remoteConf._id;
      } catch(e) {
        console.error(e);
      }
    },

    syncRoute() {
      // routing not in sync, data is master
      if(this.configId && this.deviceId) {
        if(this.$route.params.configId !== this.configId || this.$route.params.deviceId !== this.deviceId) {
          this.$router.push({
            path: `${this.subPageBasePath}/measurements`
          });
        }
      } else {
        if(this.$route.params.configId || this.$route.params.deviceId) {
          this.$router.push({
            path: `/ppmp`
          });
        }
      }
    }
  },

  watch: {
    configExists() {
      if(!this.configExists) {
        this.configId = null;
      }
    },
    configId() {
      this.connectionError = null;
      this.deviceId = null;
      this.device = [];
    },
    '$route'(to, from) {
      this.syncRoute();
    },
    deviceId() {
      this.syncRoute();
    }
  },

  components: {
    card, qrField
  }
};

</script>

<style lang="scss">
.ppmp {
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }
  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
  .control.has-icons-right .icon.qrcode {
    pointer-events: auto;
  }
}
@media screen and (min-width: 769px), print {
.ppmp {
  .field-label {
    flex-grow: 2;
  }
}
}
</style>
