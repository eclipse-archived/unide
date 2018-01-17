<template>
  <div class="preferences">
    <div class="field is-horizontal">
      <div class="field-label is-normal">
        <label class="label">{{ $t('configuration.language') }}:</label>
      </div>
      <div class="field-body">
        <div class="field is-narrow">
          <b-select :disabled="disabled" v-model="localValue.lang">
            <option
              v-for="option in langs"
              :value="option"
              :key="option">
              {{ option }}
            </option>
          </b-select>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import cloneDeep from 'lodash/cloneDeep';
import isEqual   from 'lodash/isEqual';

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
      langs:      process.env.LANGS,
      localValue: cloneDeep(this.value)
    };
  },
  watch: {
    value: {
      handler() {
        if(!isEqual(this.value, this.localValue)) {
          this.localValue = cloneDeep(this.value);
        }
      },
      deep: true
    },
    localValue: {
      handler() {
        if(!isEqual(this.value, this.localValue)) {
          this.$emit('input', this.localValue);
        }
      },
      deep: true
    }
  }
};
</script>

<style lang="scss">
.preferences {
  > .field.is-horizontal > .field-body > .field {
    &, .select, select {
      width: 100%;
    }
  }
}
</style>
