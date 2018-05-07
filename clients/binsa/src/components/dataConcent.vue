<template>
  <card class="dataConcent" :collapsed="accepted" @collapsing="accepting">
    <template slot="header">{{ $t(`dataConcent.title`) }}</template>
    <template slot="headerCollapsed">{{ $t(`dataConcent.titleAccepted`) }}</template>
    <div>
      <p>{{ $t(`dataConcent.information`) }}</p>
      <p>{{ $t(`dataConcent.requirement`) }}</p>
    </div>
  </card>
</template>

<script>
import card from 'components/collapsibleCard';
import { mapState } from 'vuex';

export default {
  computed: mapState({
    accepted: state => state.configuration.configuration ? state.configuration.configuration.preferences.dataConcent: false
  }),
  methods: {
    accepting() {
      this.$store.dispatch('configuration/saveConfiguration', Object.assign({}, this.$store.state.configuration.configuration.preferences, {
        dataConcent: true
      }));
    }
  },
  components: { card }
}

</script>

<style lang="scss">
@import "~styles/variables.scss";

.dataConcent {
  .card-header * {
    padding: 0.25rem 0.75rem;
  }
  margin-bottom: 1rem;
}

</style>