<template>
  <div class="dropdown" :class="{'is-active': isActive}" @click.stop="isActive = !isActive">
    <a role="button" class="dropdown-trigger navbar-link">
      <i class="fa fa-globe">&nbsp;</i>
    </a>
    <transition name="fade">
      <div v-show="isActive" class="background"></div>
    </transition>
    <transition name="fade">
      <div v-show="isActive" class="dropdown-menu">
        <div class="dropdown-content">
          <a class="dropdown-item" :class="{ 'is-selected': lang && option === lang }" @click="selectLang(option)" v-for="option in langs" :key="option">
            {{ option }}
          </a>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { mapState } from 'vuex';

export default {
  data: () => ({
    langs:    process.env.LANGS,
    isActive: false
  }),
  computed: mapState({
    lang: state => state.configuration.configuration ? state.configuration.configuration.preferences.lang : '...'
  }),
  methods: {
    selectLang(lang) {
      if(this.$store.state.configuration.configuration && this.$store.state.configuration.configuration.preferences.lang !== lang) {
        this.$store.dispatch('configuration/saveConfiguration', Object.assign({}, this.$store.state.configuration.configuration.preferences, {
          lang
        }));
      }
    },
    clickedOutside() {
      this.isActive = false;
    }
  },
  created() {
    if(typeof window !== 'undefined') {
      document.addEventListener('click', this.clickedOutside);
    }
  },
  beforeDestroy() {
    if(typeof window !== 'undefined') {
      document.removeEventListener('click', this.clickedOutside);
    }
  }
};
</script>

<style lang="scss">

</style>
