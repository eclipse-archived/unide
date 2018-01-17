import EventEmitter from 'events';
import store        from '../../store';
import sensorBus    from '../../sensorBus';
import isEqual      from 'lodash/isEqual';
import cloneDeep    from 'lodash/cloneDeep';

const sensorsNeedPolling = {
  cpu: () => sensorBus.getCpu(),
  mem: () => sensorBus.getMem()
};

// jobs Object and methods are outsourced from vuex store because this is too volatile
export class RecordingJob extends EventEmitter {
  constructor(configId, deviceId) {
    super();
    if(configId === undefined || deviceId === undefined) {
      throw new Error('RecordingJob: configId & deviceId are mandatory!');
    }
    this.stop();
    Object.assign(this, {
      configId: configId,
      deviceId: deviceId,
      running:  false
    });
  }

  flushCache() {
    const returnValue = this.cache;
    if(this.cache) {
      this.cache = {};
    }
    return returnValue;
  }

  createSubscriptions(samplingFrequency) {
    if(this.subscriptions) {
      this.subscriptions.forEach(([name, subscription]) => {
        if(typeof subscription === 'function') {
          sensorBus.removeListener(name, subscription);
        } else if(typeof subscription === 'number') {
          clearInterval(subscription);
        }
      });
    }
    const translation = (store.state.configuration.configuration[this.configId].translate || {})[this.deviceId] || {};
    this.subscriptions = Object.entries(translation).map(([sensor, measurementPoints]) => {
      const subscription = value => {
        if(!this.running) {
          return;
        }
        const ts = Date.now();
        Object.entries(measurementPoints).forEach(([measurementPoint, alias]) => {
          if(!value[measurementPoint] && value[measurementPoint] !== 0) {
            return;
          }
          this.cache[alias] = this.cache[alias] || [];
          this.cache[alias].push([ts, value[measurementPoint]]);
          this.count++;
        });
      };
      if(sensorsNeedPolling[sensor]) {
        // eslint-disable-next-line space-before-function-paren
        return [sensor, setInterval(async () =>
          subscription(await sensorsNeedPolling[sensor]()),
                                    samplingFrequency)];
      }
      sensorBus.on(sensor, subscription);
      return [sensor, subscription];
    });
    return this.subscriptions;
  }

  start(samplingFrequency = 100) {
    if(this.running) {
      return this;
    }
    if(this.startTime) {
      // resume only
      this.running = true;
      return this;
    }
    this.createSubscriptions(samplingFrequency);
    Object.assign(this, {
      running:   true,
      startTime: new Date(),
      cache:     {},
      unwatch:   store.watch((state, getters) =>
        cloneDeep((store.state.configuration.configuration[this.configId].translate || {})[this.deviceId] || {}),
        /* eslint-disable indent */
        (newTranslation, oldTranslation) => {
          if(store.getters['configuration/ppmp/getTotalTranslationsFor'](this.configId, this.deviceId) === 0) {
            this.stop();
            return;
          }
          if(!isEqual(oldTranslation, newTranslation)) {
            this.createSubscriptions(samplingFrequency);
          }
        }, {
          deep: true
        }
        /* eslint-enable indent */
      )
    });
    this.emit('runningChanged', this.running);
    return this;
  }

  pause() {
    if(!this.running) {
      return this;
    }
    this.running = false;
    this.emit('runningChanged', this.running);
    return this;
  }

  stop() {
    this.running = false;
    if(this.subscriptions) {
      this.subscriptions.forEach(([name, subscription]) => {
        if(typeof subscription === 'function') {
          sensorBus.removeListener(name, subscription);
        } else if(typeof subscription === 'number') {
          clearInterval(subscription);
        }
      });
    }
    if(this.unwatch) {
      this.unwatch();
    }
    Object.assign(this, {
      cache:         null,
      count:         0,
      startTime:     null,
      subscriptions: null,
      unwatch:       null
    });
    this.emit('runningChanged', this.running);
    return this;
  }
}

export class AggregationJob extends RecordingJob {
  constructor(...args) {
    super(...args);
    this.aggregatedCache = null;
  }

  aggregate() {
    Object.entries(this.flushCache()).forEach(([name, series]) => {
      if(!series.length) {
        return;
      }
      this.aggregatedCache[name] = this.aggregatedCache[name] || [];
      this.aggregatedCache[name].push([(series[0][0] + series[series.length - 1][0]) / 2, series.reduce((l, [t, v]) => l + v, 0) / series.length]);
    });
  }

  start(samplingFrequency, sendingFrequency, callback) {
    if(!sendingFrequency || !samplingFrequency) {
      throw new Error('AggregationJob needs sending interval and samplingFrequency to start');
    }
    this.aggregatedCache = {};
    // aggregate flushCache return value to aggregatedCache
    if(!this.aggregationInterval) {
      this.aggregationInterval = setInterval(() => this.aggregate(), samplingFrequency);
    }
    if(!this.sendingInterval) {
      this.sendingInterval = setInterval(() => {
        if(Object.keys(this.aggregatedCache).length && this.running) {
          if(callback && callback instanceof Function) {
            callback(this.aggregatedCache);
          }
          this.aggregatedCache = {};
        };
      }, sendingFrequency);
    }
    return super.start(samplingFrequency);
  }

  stop(...args) {
    clearInterval(this.aggregationInterval);
    clearInterval(this.sendingInterval);
    this.aggregatedCache = null;
    return super.stop(...args);
  }
}

export class PhaseJob extends RecordingJob {
  constructor(...args) {
    super(...args);
    this.phases = null;
  }

  flushCache() {
    const returnValue = this.phases;
    if(this.phases && this.cache) {
      this.phases.push(super.flushCache());
      this.phases = [];
    }
    return returnValue;
  }

  phaseBreak() {
    if(this.phases) {
      this.phases.push(super.flushCache());
    }
    return this.phases;
  }

  start(...args) {
    this.phases = [];
    return super.start(...args);
  }

  stop(...args) {
    this.phases = null;
    return super.stop(...args);
  }
}
