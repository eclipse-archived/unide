<template>
  <div class="ppm">
    <div class="field is-horizontal">
      <div class="field-label is-normal">
        <label :for="`${value._id}_name`" class="label">{{ $t('configuration.name.title') }}:</label>
      </div>
      <div class="field-body">
        <div class="field is-expanded">
          <div class="field">
            <input v-validate="'required'" data-vv-name="name" class="input" :id="`${value._id}_name`" :placeholder="$t('configuration.name.placeholder')" :disabled="disabled" v-model.trim="localValue.name">
            <p v-show="errors.has('name')" class="help is-danger">{{ errors.first('name') }}</p>
          </div>
        </div>
      </div>
    </div>
    <div class="field is-horizontal">
      <div class="field-label is-normal">
        <label :for="`${value._id}_login`" class="label">{{ $t('configuration.login.title') }}:</label>
      </div>
      <div class="field-body">
        <div class="field">
          <p class="control is-expanded has-icons-right">
            <input v-validate="'required'" data-vv-name="user" class="input" type="text" :placeholder="$t('configuration.user.placeholder')" :disabled="disabled" v-model.trim="localValue.user">
            <span class="icon is-small is-right">
              <i class="fa fa-user"></i>
            </span>
          </p>
        </div>
        <div class="field">
          <div class="control has-icons-right is-clearfix">
            <input v-validate="'required'" data-vv-name="password" class="input" type="password" :placeholder="$t('configuration.password.placeholder')" autocomplete="on" :disabled="disabled" v-model="localValue.password">
            <span class="icon is-small is-right">
              <i class="fa fa-eye-slash"></i>
            </span>
          </div>
        </div>
      </div>
    </div>
    <div class="field is-horizontal">
      <div class="field-label is-normal">
        <label :for="`${value._id}_name`" class="label">{{ $t('configuration.url.title') }}:</label>
      </div>
      <div class="field-body">
        <div class="field is-expanded">
          <Url :id="localValue._id" :disabled="disabled" v-model="localValue.url" data-vv-name="url" v-validate="'url|required'"></Url>
          <p v-show="errors.has('url')" class="help is-danger">{{ errors.first('url') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import cloneDeep from 'lodash/cloneDeep';
import isEqual   from 'lodash/isEqual';
import defaults  from 'lodash/defaults';
import Url       from 'components/url';

export default {
  props: {
    value: {
      type:     Object,
      required: true
    },
    disabled: {
      type:     Boolean,
      required: false,
      default:  true
    }
  },
  data() {
    return {
      localValue: this.cloneValue(this.value)
    };
  },
  methods: {
    cloneValue(value) {
      return defaults(cloneDeep(this.value), {
        name:     '',
        user:     '',
        password: '',
        url:      ''
      });
    }
  },
  watch: {
    value: {
      handler() {
        if(!isEqual(this.value, this.localValue)) {
          this.localValue = this.cloneValue(this.value);
        }
      },
      deep: true
    },
    localValue: {
      handler() {
        const me = this;
        this.$validator.validateAll({
          name:     me.localValue.name,
          user:     me.localValue.user,
          password: me.localValue.password,
          url:      me.localValue.url
        })
          .catch(() => {});
        if(!isEqual(this.value, this.localValue)) {
          this.$emit('input', this.localValue);
        }
      },
      deep: true
    }
  },
  components: {
    Url
  }
};
</script>

<style lang="scss">
.ppm {
  p.control {
    margin-bottom: 0px;
  }
  .field-body .field .field {
    margin-right: 0px;
  }
}
</style>
