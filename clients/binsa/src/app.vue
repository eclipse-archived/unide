<template>
  <div>
    <navbar>
      <router-link exact active-class="is-active" class="navbar-item" to="/">
        Home
      </router-link>
      <router-link active-class="is-active" class="navbar-item" to="/ppmp">
        PPMP
      </router-link>
      <router-link v-if="development" active-class="is-active" class="navbar-item" to="/bar">
        bar
      </router-link>
      <router-link v-if="development" active-class="is-active" class="navbar-item" to="/ciHelp">
        CI helper
      </router-link>
    </navbar>
    <div class="container">
      <div class="header clearfix">
      </div>
      <div v-if="loading" class="spinner">
        <span class="fa fa-spinner fa-pulse fa-3x"></span>
      </div>
      <Login v-else-if="showLogIn" :show="showLogIn" @input="saveCredentials"/>
      <router-view v-else></router-view>
    </div>
  </div>
</template>

<script>
import Vue            from 'vue';
import {
  sync
}                       from 'vuex-router-sync';
import router           from './router';
import store            from './store';
import { mapState }     from 'vuex';
import VeeValidate      from  'vee-validate';
import Dialog           from 'buefy/src/components/dialog';
import Toast            from 'buefy/src/components/toast';
import { Modal }        from 'buefy/src/components/modal';
import {
  Dropdown, DropdownItem
}                       from 'buefy/src/components/dropdown';
import {
  Tabs, TabItem
}                       from 'buefy/src/components/tabs';
import Select           from 'buefy/src/components/select';
import Icon             from 'buefy/src/components/icon';

import i18n             from './i18n';
import navbar           from 'components/navbar';
import Login            from 'components/login';

// import Toast          from 'buefy/src/components/toast'
// import Dialog         from 'buefy/src/components/dialog'

Vue.config.debug        = process.env.NODE_ENV === 'development';
Vue.config.devtools     = process.env.NODE_ENV === 'development';
Vue.config.errorHandler = function(err) {
  console.error('ERROR: ', err);
};

Vue.use(VeeValidate);
/* Vue.use(Buefy, {
  defaultIconPack: 'fa'
}); */
Vue.component(Modal.name, Modal);
Vue.component(Dropdown.name, Dropdown); // configuration/index
Vue.component(DropdownItem.name, DropdownItem);
Vue.component(Select.name, Select);
Vue.component(Tabs.name, Tabs);
Vue.component(TabItem.name, TabItem);
Icon.data = () => ({
  newPack: 'fa'
});

sync(store, router);

export default {
  router,
  store,
  i18n,
  computed: Object.assign({
    development: () => process.env.NODE_ENV === 'development'
  }, mapState({
    loading(state) {
      return !(this.$i18n.messages[this.$i18n.locale] && state.configuration.configuration);
    },
    showLogIn: state => !state.configuration.credentials
  })),
  methods: {
    saveCredentials({ user, password, remember }) {
      const tryToSave = force =>
        this.$store.dispatch('configuration/loadConfigurations', {
          user,
          password,
          remember,
          force
        })
          .catch(() => {
            return new Promise((resolve, reject) => {
              Dialog.confirm({
                title:       this.$t('login.confirmation.title'),
                message:     this.$t('login.confirmation.message', { name: user }),
                confirmText: this.$t('login.confirmation.ok'),
                cancelText:  this.$t('cancel'),
                type:        'is-danger',
                hasIcon:     true,
                onConfirm:   () => {
                  tryToSave(true)
                    .then(resolve)
                    .catch(reject);
                },
                onCancel: reject
              });
            });
          });
      tryToSave()
        .then(() =>
          Toast.open(this.$t('login.confirmation.confirmed', { name: user }))
        )
        .catch(err => {
          console.error('Login failed', err);
        // toast here?
        });
    }
  },
  components: {
    navbar, Login
  }
};
</script>

<style lang="scss">
.router-link-active {
  font-weight: bold;
}
.spinner {
  margin: 0 auto;
  max-width: 120px;
  text-align: center;
}
$table-row-active: green;
$table-row-active-background: green;

@import "~styles/variables.scss";
@import "buefy/src/scss/buefy-build.scss";
// @import "bulma/bulma";
@import "~font-awesome/scss/font-awesome.scss";

body {
  overflow: visible;
}

@keyframes spinAround {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(359deg);
  }
}
.loading {
  position: relative;
  * {
      pointer-events: none;
      opacity: 0.5;
  }
  &:after {
      animation: spinAround 500ms infinite linear;
      border: 2px solid $bosch-cobalt;
      border-radius: 290486px;
      border-right-color: transparent;
      border-top-color: transparent;
      content: "";
      display: block;
      height: 1em;
      position: relative;
      width: 1em;
      position: absolute;
      top: 4em;
      left: calc(50% - 2.5em);
      width: 5em;
      height: 5em;
      border-width: 0.25em;
  }
}

.tag {
  @each $name, $pair in $colors {
    $color: nth($pair, 1);
    $color-invert: nth($pair, 2);
    &.is-#{$name} {
      background-color: $color;
      color: $color-invert;
    }
  }
}

.login {
  .check {
    float: left;
  }
}
</style>
