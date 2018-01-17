<template>
  <div class="messageForm">
    <div class="field is-horizontal">
      <div class="field-label is-normal">
        <label for="code" class="label">code:</label>
      </div>
      <div class="field-body">
        <div class="field is-expanded">
          <div class="field">
            <qrField v-validate="'required'" data-vv-name="code" v-model="localValue.code" :disabled="disabled"/>
            <p v-show="errors.has('code')" class="help is-danger">{{ errors.first('code') }}</p>
          </div>
        </div>
      </div>
    </div>
    <div v-for="type in ['title', 'description', 'origin', 'hint']" :key="type" class="field is-horizontal">
      <div class="field-label is-normal">
        <label :for="type" class="label">{{ type }}:</label>
      </div>
      <div class="field-body">
        <div class="field is-expanded">
          <div class="field">
            <input class="input" v-model="localValue[type]" :placeholder="$t('ppmp.optional')" :disabled="disabled"/>
          </div>
        </div>
      </div>
    </div>
    <div class="field is-horizontal">
      <div class="field-label is-normal">
        <label for="type" class="label">type:</label>
      </div>
      <div class="field-body">
        <div class="field is-expanded">
          <div class="select field">
            <select v-model="localValue.type" :disabled="disabled">
              <option></option>
              <option>DEVICE</option>
              <option>TECHNICAL_INFO</option>
            </select>
          </div>
        </div>
      </div>
    </div>
    <div class="field is-horizontal">
      <div class="field-label is-normal">
        <label for="severity" class="label">severity:</label>
      </div>
      <div class="field-body">
        <div class="field is-expanded">
          <div class="select field">
            <select v-model="localValue.severity" :disabled="disabled">
              <option></option>
              <option>HIGH</option>
              <option>MEDIUM</option>
              <option>LOW</option>
              <option>UNKNOWN</option>
            </select>
          </div>
        </div>
      </div>
    </div>
    <slot/>
  </div>
</template>

<script>
import cloneDeep  from 'lodash/cloneDeep';
import isEqual    from 'lodash/isEqual';
import qrField    from 'components/qrField';

const FIELDS = ['code', 'title', 'description', 'origin', 'hint', 'type', 'severity'];

export default {
  props: {
    value: {
      type:     Object,
      required: true
    },
    disabled: {
      type:    Boolean,
      default: false
    }
  },
  FIELDS,
  data: function() {
    return {
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
  },
  components: { qrField }
};
</script>

<style lang="scss">
.messageForm {
  .select, select {
    width: 100%;
  }
}
@media screen and (min-width: 769px), print {
  .messageForm {
    .field-label {
      flex-grow: 2;
    }
  }
}
</style>
