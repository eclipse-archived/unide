<template>
  <div class="box column is-half-desktop is-offset-one-quarter-desktop configuration">
    <header>
      <b-dropdown class="is-pulled-right" @input="addConfiguration">
        <button class="button is-success" slot="trigger">
          <i class="fa fa-plus">&nbsp;</i>
          <i class="fa fa-caret-down">&nbsp;</i>
        </button>
        <b-dropdown-item value="ppmp">PPMP Server</b-dropdown-item>
        <b-dropdown-item value="ppm">PPM Server</b-dropdown-item>
        <!--b-dropdown-item value="ppm">PPM Server</b-dropdown-item>
        <b-dropdown-item value="im3">IM3 Server</b-dropdown-item-->
      </b-dropdown>
      <h1 class="title">{{ $t("configuration.title") }}
        <template v-if="credentials && credentials.user">
          ({{ credentials.user }})
        </template>
        <template v-else>
          (<span class="anonymous">
            anonymous
              <a href="#" class="signOut" @click="signOut">
                <i class="fa fa-sign-in">&nbsp;</i>
              </a>
          </span>)
        </template>
    </h1>
    </header>
    <card v-for="(type, id) in mergedConfiguration" :key="id" @expanding="setExpandedCardId(id)" :collapsed="id !== expandedCardId" :class="{'has-danger': messages[id]}">
      <template slot="headerCollapsed">{{ $t(`configuration.sections.${type==='singleton'?id:type}`) }} (#{{ id+(localConf[id]?'*':'') }})</template>
      <template slot="header">{{ $t(`configuration.sections.${type==='singleton'?id:type}`) }}
        <span v-if="isNew(id)" class="tag is-success">new</span>
        <span v-if="hasChanged(id)" class="tag is-warning">changed</span>
        <span v-if="messages[id]" class="tag is-danger">error</span>
      </template>
      <component class="field" :is="type==='singleton'?id:type" :disabled="!localConf[id]" :value="localConf[id] || configuration[id]" @input="conf => { localConf[id] = conf }"></component>
      <footer>
        <button type="button" class="button is-danger" @click.prevent="deleteConfiguration(id)" v-if="localConf[id] || type !== 'singleton'"><i class="fa fa-trash"></i>&nbsp;{{ localConf[id]?$t("cancel"):$t("delete") }}</button>
        <button type="button" class="button is-primary" :disabled="!isEditable(id)" @click.prevent="isEditable(id) && saveConfiguration(id)" v-if="localConf[id]"><i class="fa fa-floppy-o"></i>&nbsp;{{ $t("save") }}</button>
        <button type="button" class="button is-success" @click.prevent="editConfiguration(id)" v-else><i class="fa fa-pencil"></i>&nbsp;{{ $t("edit") }}</button>
        <p class="help is-danger" v-if="messages[id]">{{ messages[id] }}</p>
      </footer>
    </card>
  </div>
</template>

<script>
import Vue         from 'vue';
import {
  mapState,
  mapMutations,
  mapActions
}                   from 'vuex';
import card         from 'components/collapsibleCard';
// import ppm          from './ppm';
import ppmp         from './ppmp';
// import im3          from './im3';
import preferences  from './preferences';
import cloneDeep    from 'lodash/cloneDeep';
import isEqual      from 'lodash/isEqual';
// import VeeValidate  from  'vee-validate';

export default {
  data() {
    return {
      showDropdown: false,
      localConf:    {},
      messages:     {},
      langs:        process.env.LANGS
    };
  },
  computed: Object.assign({}, {
    mergedConfiguration() {
      return Object.values(Object.assign({}, this.configuration, this.localConf)).reduce((l, v) => {
        l[v._id] = v.type;
        return l;
      }, {});
    }
  }, mapState('configuration', ['credentials', 'configuration', 'expandedCardId'])),
  created() {
    document.addEventListener('click', this.collapseDropdown, /* useCapture */ true);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.collapseDropdown);
  },
  methods: Object.assign({
    async addConfiguration(type) {
      const id = (Math.max(0, Array.prototype.concat(Object.keys(this.configuration || {}), Object.keys(this.localConf || {})).map(i => +i).filter(i => !isNaN(i))) + 1).toString();
      this.$set(this.localConf, id, { _id: id, type });
      await this.setExpandedCardId(id);
      return this.localConf[id];
    },
    collapseDropdown() {
      this.showDropdown = false;
    },
    async saveConfiguration(id) {
      let   remoteConf = null;
      const config     = this.localConf[id];
      this.$delete(this.messages, config._id);
      try {
        remoteConf = await this.$store.dispatch('configuration/saveConfiguration', config);
        if(remoteConf._id !== config._id) {
          console.error(`configuration item id ${config._id} has changed to ${remoteConf._id}`);
          if(this.localConf[remoteConf._id]) {
            throw new Error(`id conflict ${remoteConf._id}<->${config._id}`);
          }
          if(this.expandedCardId === config._id) {
            await this.setExpandedCardId(remoteConf._id);
          }
          this.$delete(this.localConf, config._id);
        }
        // this.$set(this.localConf, remoteConf._id, remoteConf);
        this.$delete(this.localConf, remoteConf._id);
      } catch(e) {
        console.error(e);
        this.$set(this.messages, config._id, `${e.message} (${e.status} - ${e.name})`);
      }
      return config;
    },
    editConfiguration(id) {
      this.$set(this.localConf, id, cloneDeep(this.configuration[id]));
    },
    async deleteConfiguration(id) {
      if(!this.localConf[id]) {
        if(this.configuration[id]) {
          await this.$store.dispatch('configuration/deleteConfiguration', id);
        }
      }
      if(this.messages[id]) {
        this.$delete(this.messages, id);
      }
      this.$delete(this.localConf, id);
    },
    isNew(id) {
      return this.localConf[id] && !this.configuration[id];
    },
    hasChanged(id) {
      return this.localConf[id] && this.configuration[id] && !isEqual(this.localConf[id], this.configuration[id]);
    },
    isEditable(id) {
      return !this.messages[id] && (this.isNew(id) || this.hasChanged(id));
    }
  }, mapMutations('configuration', ['setExpandedCardId']), mapActions('configuration', ['signOut'])),
  components: {
    card,
    ppmp: ppmp,
    ppm:  Vue.extend(ppmp).extend({
      props: {
        path: {
          type:     String,
          required: false,
          default:  '/ppm'
        }
      }
    }),
    /* ppm, im3,*/
    preferences
  }
};

</script>

<style lang="scss">
@import "~styles/variables.scss";

.configuration {
  .anonymous {
    color: $danger;
    .signOut {
      font-size: 0.5em;
      vertical-align: middle;
    }
  }
  & > header {
    margin-bottom: 1.5rem;
  }
  & > .card {
    .card-header-title .tag {
      margin-left: 1em;
    }
    &:not(:first-of-type) {
        border-top: 0;
        border-bottom-left-radius: $radius;
        border-bottom-right-radius: $radius;
    }
    &:not(:last-of-type) {
        border-top-left-radius: $radius;
        border-top-right-radius: $radius;
    }
    & > .card-content > footer {
      text-align: right;
    }
    & .content>.field:last-child {
      margin-top: 0.75rem;
    }
  }
}
</style>
