<template>
  <b-modal :active="show" :canCancel="true" :onCancel="saveAnonymous" has-modal-card class="login">
    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">
          {{ $t('login.title') }}
        </p>
        <selectLang/>
      </header>
      <section class="modal-card-body">
        <div class="field">
          <label class="label">
            {{ $t('login.user.label') }}
          </label>
          <div class="control has-icons-right">
            <input type="text" v-model="user" :placeholder="$t('login.user.placeholder')" autocomplete="on" class="input" v-validate="'required'" data-vv-name="user" @focus="$event.target.select()" :disabled="remember">
            <span class="icon is-small is-right">
              <i class="fa fa-user"></i>
            </span>
          </div>
          <p v-show="errors.has('user')" class="help is-danger">{{ errors.first('user') }}</p>
        </div>
        <div class="field">
          <label class="label">
            {{ $t('login.password.label') }}
          </label>
          <div class="control has-icons-right">
            <input type="password" v-model="password" :placeholder="$t('login.password.placeholder')" autocomplete="on" class="input" v-validate="'required|min:5'" data-vv-name="password" @keydown.prevent.enter="saveCredentials" @focus="$event.target.select()" :disabled="remember">
            <span class="icon is-small is-right">
              <i class="fa fa-key"></i>
            </span>
          </div>
          <p v-show="errors.has('password')" class="help is-danger">{{ errors.first('password') }}</p>
        </div>
        <dataConcent/>
        <label tabindex="0" class="checkbox" @keydown.prevent.enter.space="remember = !remember">
          <input type="checkbox" v-model="remember" true-value="true">
          <span class="check"></span>
          <span class="control-label">
            {{ $t('login.remember') }}
          </span>
        </label>
      </section>
      <footer class="modal-card-foot">
        <button class="button is-primary" @click="saveCredentials" :disabled="errors.any() || remember">
          {{ $t('login.login') }}
        </button>
        <button class="button is-warning" @click="saveAnonymous">
          {{ $t('login.anonymous') }}
        </button>
      </footer>
    </div>
  </b-modal>
</template>

<script>
import selectLang from 'components/selectLang';
import dataConcent from 'components/dataConcent';

export default {
  props: {
    show: Boolean
  },
  data: () => ({
    user:     '',
    password: '',
    remember: false
  }),
  mounted() {
  },
  methods: {
    saveCredentials() {
      return this.$validator.validateAll().then(result => {
        if(result) {
          this.$emit('input', {
            user:     this.user,
            password: this.password,
            remember: this.remember
          });
        }
        return result;
      });
    },
    async saveAnonymous() {
      this.user = this.password = '';
      await this.$store.dispatch('configuration/loadConfigurations', {
        remember: this.remember
      });
      this.remember = false;
    }
  },
  components: {
    dataConcent, selectLang
  }
};
</script>
