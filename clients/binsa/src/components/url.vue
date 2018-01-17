<template>
  <div class="field url has-addons">
    <p class="control">
      <span class="select">
        <select :id="`${id}_protocol`" v-model="protocol" :disabled="disabled">
          <option>http</option>
          <option>https</option>
        </select>
      </span>
    </p>
    <p class="control">
      <a class="button is-static">
        ://
      </a>
    </p>
    <p class="control is-expanded">
      <input :id="`${id}_hostname`" class="input" type="text" placeholder="localhost" v-model.trim.lazy="hostname" :disabled="disabled"/>
    </p>
    <p class="control">
      <a class="button is-static">
        :
      </a>
    </p>
    <p class="control">
      <input :id="`${id}_port`" class="input" type="number" :placeholder="(protocol==='https')?443:80" v-model.number="port" :disabled="disabled"/>
    </p>
  </div>
</template>

<script>
// for best url matching pattern see https://gist.github.com/dperini/729294

export default {
  props: {
    value: {
      type:    String,
      default: ''
    },
    disabled: {
      type:     Boolean,
      required: false,
      default:  true
    },
    id: {
      required: false
    }
  },
  data() {
    return this.parseUrlString(this.value);
  },
  created() {
    const me = this;
    me.$watch('hostname', () => {
      if(~me.hostname.search(/\/$/)) {
        me.hostname = me.hostname.substr(0, me.hostname.length - 1);
      }
    });
    ['protocol', 'hostname'].forEach(a => {
      me.$watch(a, () => {
        // checks & cleaning
        if(~me[a].search(/[:\/@]/)) {
          me[a] = '';
        }
        let url = `${me.protocol || ''}://${me.hostname || ''}`;
        if(me.port) {
          url += `:${me.port}`;
        }
        me.$emit('input', url);
      });
    });
    me.$watch('port', () => {
      let url = `${me.protocol || ''}://${me.hostname || ''}`;
      if(me.port && me.port.toString().search(/^[0-9]+$/)) {
        me.port = '';
      }
      if(me.port) {
        url += `:${me.port}`;
      }
      me.$emit('input', url);
    });
  },
  methods: {
    parseUrlString(str) {
      // eslint-disable-next-line no-unused-vars
      const [url, protocol, hostname, port] = (str || '').match(/^(.*):\/\/([^:]*)(?::([0-9]*))?$/i) || ['', this.protocol, this.hostname, this.port];
      return {
        protocol: protocol || '',
        hostname: hostname || '',
        port:     port || ''
      };
    }
  },
  watch: {
    value() {
      Object.assign(this, this.parseUrlString(this.value));
    }
  }
};

</script>

<style lang="scss">
.url {
  .control {
    &:nth-child(1) {
      width: 5.5em;
    }
    &:nth-child(5) {
      min-width: 5em;
      max-width: 6em;
    }
  }
}
</style>
