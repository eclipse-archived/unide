<template>
  <div class="processes tile is-ancestor is-vertical">
    <div class="tile is-parent">
      <a class="button is-large is-fullwidth" :class="{ 'is-primary': !recording, 'is-danger': recording }" @click="recording ? stopRecording() : startRecording()" :disabled="!countTotalTranslations">
        <i class="fa" :class="recording ? 'fa-stop' : 'fa-play'"></i>
      </a>
    </div>
    <transition name="fade">
      <div class="tile is-parent" v-if="recording">
        <a class="button is-large is-fullwidth" @click="recording && phaseBreak()" :disabled="!recording">
          <i class="fa fa-step-forward"></i>
        </a>
      </div>
    </transition>
    <transition name="fade">
      <div class="tile" v-if="recording">
        <div class="tile is-parent">
          <div class="tile is-child box">
            <p class="title is-5">{{ $t('ppmp.processes.startTime') }}:</p>
            <p class="subtitle">{{ startTime | timeStamp($i18n.locale) }}</p>
          </div>
        </div>
        <div class="tile is-parent">
          <div class="tile is-child box">
            <p class="title is-5">{{ $t('ppmp.processes.duration') }}:</p>
            <p class="subtitle">{{ processDuration | duration }}</p>
          </div>
        </div>
      </div>
    </transition>
    <transition name="fade">
      <div class="tile" v-if="recording">
        <div class="tile is-parent">
          <div class="tile box is-child">
            <p class="title is-5">{{ $t('ppmp.processes.measurements') }}:</p>
            <p class="subtitle">{{ measurementCount }}</p>
          </div>
        </div>
        <div class="tile is-parent">
          <div class="tile box is-child">
            <p class="title is-5">{{ $t('ppmp.processes.phases') }}:</p>
            <p class="subtitle">{{ phases }}</p>
          </div>
        </div>
      </div>
    </transition>
    <card :collapsed="true">
      <template slot="header">
        {{ $t('ppmp.processes.details') }}
      </template>
      <div class="field is-horizontal">
        <div class="field-label is-normal">
          <label for="result" class="label">result:</label>
        </div>
        <div class="field-body">
          <div class="field is-expanded">
            <div class="field">
              <div class="select field">
                <select v-model="result">
                  <option></option>
                  <option>OK</option>
                  <option>NOK</option>
                </select>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="field is-horizontal">
        <div class="field-label is-normal">
          <label for="externalProcessId" class="label">externalProcessId:</label>
        </div>
        <div class="field-body">
          <div class="field is-expanded">
            <div class="field">
              <input class="input" v-model="externalProcessId" :placeholder="$t('ppmp.optional')"/>
            </div>
          </div>
        </div>
      </div>
      <div class="field is-horizontal">
        <div class="field-label is-normal">
          <label for="programId" class="label">program.id:</label>
        </div>
        <div class="field-body">
          <div class="field is-expanded">
            <div class="field">
              <input class="input" v-model="programId" :placeholder="$t('ppmp.optional')"/>
            </div>
          </div>
        </div>
      </div>
      <div class="field is-horizontal">
        <div class="field-label is-normal">
          <label for="programName" class="label">program.name:</label>
        </div>
        <div class="field-body">
          <div class="field is-expanded">
            <div class="field">
              <input class="input" v-model="programName" :placeholder="$t('ppmp.optional')"/>
            </div>
          </div>
        </div>
      </div>
    </card>
  </div>
</template>

<script>
import axios    from 'axios';
import {
  mapState
}               from 'vuex';
import card     from 'components/collapsibleCard';
import {
  PhaseJob
}               from './sendingDaemon';

const RECORDINGKEY = 'process';

class Trie {
  constructor(head = [], values = [], children = {}) {
    this.head     = head;
    this.values   = values;
    this.children = children; // are Tries, indexed with first node
  }

  flatten() {
    return Object.values(this.children).reduce((flat, child) =>
      flat.concat(child.flatten()),
      this.values.length ? [{
        keys:   [],
        values: this.values
      }] : [])
      .map(flat => ({
        keys:   this.head.concat(flat.keys),
        values: flat.values
      }));
  }

  add(sequence, value) {
    if(!sequence.length) {
      return;
    }
    if(!this.head) {
        this.head   = sequence;
        this.values = [value];
    }
    let cursor = 0;
    while(sequence.length &&
          this.head.length > cursor &&
          sequence[0] === this.head[cursor]) {
      sequence = sequence.slice(1);
      cursor++;
    }
    if(!sequence.length) {
      if(cursor < this.head.length) {
        // have to split current tail
        const tail = new Trie(this.head.slice(cursor), this.values, this.children);
        Object.assign(this, {
          head: this.head.slice(0, cursor),
          children: {
              [tail.head[0]]: tail 
          },
          values: [value]
        });
      } else {
        this.values.push(value);
      }
    } else {
      if(this.children[sequence[0]]) {
        this.children[sequence[0]].add(sequence, value);
      } else {
        this.children[sequence[0]] = new Trie(sequence, [value]);
        if(cursor < this.head.length) {
          // have to split current tail
          const tail              = this.head.slice(cursor);
          this.head               = this.head.slice(0, cursor);
          this.children[tail[0]]  = new Trie(tail, this.values);
          this.values             = [];
        }
      }
    }
  }
}

export default {
  props: {
    configId: {
      type:     String,
      required: true
    },
    deviceId: {
      type:     String,
      required: true
    },
    operationalStatus: {
      type:     String,
      default:  null
    }
  },

  data: () => ({
    recording:         false,
    processDuration:   0,
    measurementCount:  0,
    startTime:         null,
    timerId:           null,
    phases:            0,
    result:            '',
    externalProcessId: '',
    programId:         '',
    programName:       ''
  }),

  created() {
    this.$watch('recorder', (newVal, oldVal) => {
      if(oldVal) {
        oldVal.removeListener('runningChanged', this.onRecorderEvent);
      }
      if(newVal) {
        newVal.removeListener('runningChanged', this.onRecorderEvent);
        newVal.addListener('runningChanged', this.onRecorderEvent);
        this.onRecorderEvent();
      }
    }, {
      immediate: true
    });
  },

  filters: {
    duration(v) {
      let s  = Math.floor(v/1000),
          m  = Math.floor(s/60);
      const h  = Math.floor(m/60),
            ms = v % 1000;
      m %= 60;
      s %= 60;
      if(m) {
        if(s<10) {
          s = `0${s}`;
        }
        if(h) {
          if(m<10) {
            m = `0${m}`;
          }
          return `${h}:${m}:${s}.${ms}`;
        }
        return `${m}:${s}.${ms}`;
      }
      return `${s}.${ms}`;
    },
    timeStamp(v, locale) {
      return Intl.DateTimeFormat(locale, {
        year: "2-digit",
        month: "2-digit",
        day: "2-digit",
        hour:	"numeric",
        minute: "2-digit",
        second: "2-digit"
      }).format(v);
    }
  },

  methods: {
    onRecorderEvent() {
      this.recording = this.recorder && this.recorder.running;
      if(!this.recording && this.timerId) {
        clearInterval(this.timerId);
      } else if(this.recording && this.timerId === null) {
        this.timerId = setInterval(this.updateTiles, 50);
      }
    },

    updateTiles() {
      if(!this.recording) {
        throw new Error("update Tiles: not recording?!");
      }
      this.processDuration  = new Date() - this.recorder.startTime;
      this.measurementCount = this.recorder.count;
      this.phases           = this.recorder.phases.length + 1; // + the last one
      this.startTime        = this.recorder.startTime;
    },

    startRecording() {
      if(this.countTotalTranslations === 0 || this.recording) {
        return;
      } else if(!this.recorder) {
        this.$store.commit('configuration/ppmp/register', {
          key:      RECORDINGKEY,
          configId: this.configId,
          deviceId: this.deviceId,
          daemon:   new PhaseJob(this.configId, this.deviceId)
        });
      }
      try {
        this.recorder.start();
      } catch(err) {
        console.error(err);
        this.stopRecording();
      }
      this.timerId   = setInterval(this.updateTiles, 50);
    },

    phaseBreak() {
      if(this.recorder) {
        this.recorder.phaseBreak();
      }
    },

    async stopRecording() {
      this.processDuration = this.measurementCount = this.phases = 0;
      if(!this.recording) {
        return;
      }
      try {
        const phases    = this.recorder.flushCache(),
              startTime = this.recorder.startTime,
              device    = {
                deviceID: this.deviceId
              },
              process   = {
                ts:           startTime.toISOString(),
                shutoffPhase: (phases.length - 1).toString()
              };
        this.recorder.stop();
        this.$store.commit('configuration/ppmp/register', {
          key:      RECORDINGKEY,
          configId: this.configId,
          deviceId: this.deviceId,
          daemon:   null
        });
        if(this.operationalStatus) {
          device.operationalStatus = this.operationalStatus;
        }
        if(this.result) {
          process.result = this.result;
        }
        if(this.externalProcessId) {
          process.externalProcessId = this.externalProcessId;
        }
        if(this.programId) {
          process.program    = process.program || {};
          process.program.id = this.programId;
        }
        if(this.programName) {
          process.program      = process.program || {};
          process.program.name = this.programName;
        }
        let conf = {
          method: 'post',
          url: this.configuration[this.configId].url,
          data: {
            'content-spec': 'urn:spec://eclipse.org/unide/process-message#v2',
            device,
            process,
            measurements: phases.reduce((l, cache, phase) => {
              // split measurements of different phases into their own measurement block;
              // make sure to list series with same time array in one series/measurement object,
              // but split series with different time array into seperate series/measurement
              const t = Object.entries(cache).reduce((t, [name, series]) => {
                // split time apart as Trie key
                t.add(series.map(v => v[0]), {
                  name,
                  values: series.map(v => v[1])
                });
                return t;
              }, new Trie());
              return l.concat(t.flatten().map(({ keys, values }) => {
                const measurementStartTime = keys[0];
                return {
                  ts:     (new Date(measurementStartTime)).toISOString(),
                  phase:  phase.toString(),
                  series: values.reduce((serie, { name, values }) => {
                    serie[name] = values;
                    return serie;
                  }, {
                    $_time: keys.map(time => time - measurementStartTime)
                  })
                };
              }));
            }, [])
          }
        };
        if(this.configuration[this.configId].user && this.configuration[this.configId].password) {
          conf.auth = {
            username: this.configuration[this.configId].user,
            password: this.configuration[this.configId].password
          };
        }
        if(this.configuration[this.configId].appendType !== false) {
          conf.url += "/v2/process";
        }
        await axios.request(conf);
      } catch(err) {
        console.error(err);
        this.$emit('connectionError', err);
      }
      clearInterval(this.timerId);
      this.timerId = null;
    },

    isRecording() {
      return this.recorder && this.recorder.running;
    }
  },

  computed: Object.assign({
    recorder() {
      return this.$store.getters['configuration/ppmp/getRegistry'](RECORDINGKEY, this.configId, this.deviceId);
    },

    countTotalTranslations() {
      return this.$store.getters['configuration/ppmp/getTotalTranslationsFor'](this.configId, this.deviceId);
    }
  }, mapState('configuration', ['configuration'])),

  components: { card }
}
</script>

<style lang="scss">
@import "~styles/variables.scss";

.processes {
  a.button {
    transition: background-color 0.25s;
  }
  .select, select {
    width: 100%;
  }
}
</style>
