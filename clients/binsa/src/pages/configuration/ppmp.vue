<template>
  <div class="ppmpConfiguration">
    <div class="field is-horizontal">
      <div class="field-label is-normal">
        <label :for="`${localValue._id}_name`" class="label">{{ $t('configuration.name.title') }}:</label>
      </div>
      <div class="field-body">
        <div class="field is-expanded">
          <div class="field">
            <input v-validate="'required'" data-vv-name="name" class="input" :id="`${localValue._id}_name`" :placeholder="$t('configuration.name.placeholder')" :disabled="disabled" v-model.trim="localValue.name">
            <p v-show="errors.has('name')" class="help is-danger">{{ errors.first('name') }}</p>
          </div>
        </div>
      </div>
    </div>
    <div class="field is-horizontal">
      <div class="field-label is-normal">
        <label :for="`${localValue._id}_url`" class="label">{{ $t('configuration.url.title') }}:</label>
      </div>
      <div class="field-body">
        <div class="field is-expanded">
          <input :id="`${localValue._id}_url`" :disabled="disabled" v-model="localValue.url" data-vv-name="url" v-validate="'url|required'" class="input" :placeholder="`https://unide.eclipse.org${path}`">
          <p v-show="errors.has('url')" class="help is-danger">{{ errors.first('url') }}</p>
        </div>
      </div>
    </div>
    <div class="field is-horizontal">
      <div class="field-label">
      </div>
      <div class="field-body">
        <div class="field is-expanded">
          <div class="field">
            <toggle :id="`${localValue._id}_appendType`" :disabled="disabled" v-model="localValue.appendType">{{ $t('configuration.appendType') }}</toggle>
          </div>
        </div>
      </div>
    </div>
    <card collapsed>
      <template slot="header">
        {{ $t('configuration.messages') }}
      </template>
      <header class="is-clearfix">
        <div class="field is-horizontal">
          <div class="field-label is-normal">
            <label for="code" class="label">{{ $t('configuration.filter') }}:</label>
          </div>
          <div class="field-body field has-addons">
            <div class="field control select is-narrow">
              <select class="input" v-model="msgFilterKey">
                <option v-for="key in MESSAGEFIELDS" :key="key">{{ key }}</option>
              </select>
            </div>
            <div class="field control has-icons-right">
              <input class="input" v-model="msgFilterStr">
              <span class="icon is-small is-right">
                <i class="fa fa-search"></i>
              </span>
            </div>
            <div class="control">
              <button type="submit" class="button" :class="{'is-success': !disabled}" @click.prevent="!disabled && addMessage()" :disabled="disabled">
                <i class="fa fa-plus"></i>
              </button>
            </div>
          </div>
        </div>
      </header>
      <div v-for="key in Object.keys(filteredMessages).reverse()" :key="key" class="box">
        <messageForm :disabled="disabled" v-model="localValue.messages[key]">
          <div class="field is-horizontal">
            <div class="field-label is-normal">
              <label for="color" class="label">color:</label>
            </div>
            <div class="field-body">
              <div class="field is-expanded">
                <div class="field">
                  <button :id="`${localValue._id}_color`" class="button color" @click.prevent="showColorDialogFor = disabled ? null : key" :style="{ 'background-color': localValue.messages[key].color }" :disabled="disabled"></button>
                </div>
              </div>
            </div>
          </div>
        </messageForm>
        <footer v-if="!disabled" class="is-clearfix">
          <button type="button" class="button is-danger is-pulled-right" @click.prevent="deleteMessage(key)">
            <i class="fa fa-trash"></i>&nbsp;{{ $t('delete') }}
          </button>
        </footer>
      </div>
    </card>

    <b-modal :active="showColorDialogFor !== null" :canCancel="true" :onCancel="() => showColorDialogFor = null" has-modal-card class="dialog">
      <div class="modal-card">
        <section class="modal-card-body is-titleless">
          <colorPicker v-model="color"/>
        </section>
        <footer class="modal-card-foot">
          <button type="button" class="button is-warning" @click.prevent="showColorDialogFor = null">
            {{ $t('cancel') }}
          </button>
          <button type="button" class="button is-success" @click.prevent="setColor(showColorDialogFor, color)">
            {{ $t('save') }}
          </button>
        </footer>
      </div>
    </b-modal>
  </div>
</template>

<script>
import cloneDeep    from 'lodash/cloneDeep';
import isEqual      from 'lodash/isEqual';
import defaults     from 'lodash/defaults';
import { Sketch }   from 'vue-color';
import card         from 'components/collapsibleCard';
import messageForm  from 'components/messageForm';
import Switch       from 'buefy/src/components/switch';

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
    },
    path: {
      type:     String,
      required: false,
      default:  '/rest'
    }
  },
  data() {
    return {
      localValue:         this.cloneValue(this.value),
      msgFilterKey:       '',
      msgFilterStr:       '',
      color:              {},
      showColorDialogFor: null
    };
  },
  methods: {
    setColor(id, { hex }) {
      this.$set(this.localValue.messages[id], 'color', hex);
      this.showColorDialogFor = null;
    },
    addMessage() {
      const ms = this.localValue.messages,
            id = Math.max(0, ...Object.keys(ms)) + 1;
      this.$set(ms, id, {});
      this.msgFilterStr = '';
    },
    deleteMessage(id) {
      const ms = this.localValue.messages;
      this.$delete(ms, id);
    },
    cloneValue(value) {
      const clone = defaults(cloneDeep(value), {
        name:       '',
        url:        '',
        appendType: true,
        messages: []
      });
      clone.messages = clone.messages.reduce((l, message, idx) => {
        l[idx] = message;
        return l;
      }, {});
      return clone;
    },
    cleanValue(value) {
      return Object.assign({}, value, {
        messages: Object.values(value.messages)
      });
    }
  },
  watch: {
    value: {
      handler() {
        if(!isEqual(this.value, this.cleanValue(this.localValue))) {
          this.$set(this, "localValue", this.cloneValue(this.value));
        }
      },
      deep: true
    },
    localValue: {
      handler() {
        this.$validator.validateAll({
          name: this.localValue.name,
          url:  this.localValue.url
        })
          .catch(() => {});
        const myValue = this.cleanValue(this.localValue);
        if(!isEqual(this.value, myValue) && !this.disabled) {
          this.$emit('input', myValue);
        }
      },
      deep: true
    },
    disabled() {
      if(!this.disabled) {
        this.$options.watch.localValue.handler.call(this);
      }
    }
  },
  computed: {
    MESSAGEFIELDS:    () => messageForm.FIELDS,
    filteredMessages: function() {
      if(this.msgFilterKey && this.msgFilterStr) {
        const filter = new RegExp(this.msgFilterStr);
        return Object.entries(this.localValue.messages).reduce((l, [key, message]) => {
          if(message[this.msgFilterKey] && message[this.msgFilterKey].match(filter) !== null) {
            l[key] = message;
          }
          return l;
        }, {});
      }
      return this.localValue.messages;
    }
  },
  components: {
    card,
    messageForm,
    colorPicker: Sketch,
    toggle: Switch
  }
};
</script>

<style lang="scss">
@import "~styles/variables.scss";

.ppmpConfiguration {
  p.control {
    margin-bottom: 0px;
  }
  .field-body .field .field {
    margin-right: 0px;
  }
  .card {
    &>header {
      background-color: $light;
      border-bottom: 1px solid rgba($black, 0.1);
      .card-header-title {
        font-weight: normal;
      }
      height: 2.25em;
    }
    .card-content {
      header {
        margin-bottom: 1em;
      }
      .messageForm {
        button.color {
          width: 100%;
        }
      }
      footer {
        margin-top: 1em;
      }
    }
    width: 100%;
  }
  .modal-card {
    min-width: 0px;
    .modal-card-body {
      .vc-sketch {
        box-shadow: none;
        padding: 0px;
      }
    }
  }
}
</style>
