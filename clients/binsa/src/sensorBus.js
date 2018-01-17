import EventEmitter from 'events';

class SensorBus extends EventEmitter {
  constructor() {
    super();

    const eventTypes = ['motion', 'orientation', 'position', 'battery'],
          fns        = [],
          eventRegex = /^on[A-Z]/;

    // make sure that all event handlers are running in this context
    let proto = this;
    while(proto) {
      fns.push(...Object.getOwnPropertyNames(proto));
      proto = Object.getPrototypeOf(proto);
    }
    fns
      .filter(fn => (this[fn] instanceof Function) && (fn.match(eventRegex)))
      .forEach(fn => {
        this[fn] = this[fn].bind(this);
      });

    if(window.nwDispatcher || (window.chrome && window.chrome.system)) {
      this.lastCpu = null;
      this.pollingInterval = setInterval(this.onPollingTrigger, 1000);
      this.onPollingTrigger();
    }

    this.measurementPoints = {};

    eventTypes.forEach(ev => {
      this.measurementPoints[ev] = null;
      this.on(ev, v => {
        this.measurementPoints[ev] = v;
      });
    });

    if(window.DeviceMotionEvent) {
      window.addEventListener('devicemotion', this.onDeviceMotionEvent);
    }

    if(window.DeviceOrientationEvent) {
      window.addEventListener('deviceorientation', this.onDeviceOrientationEvent);
    }

    // eslint-disable-next-line one-var
    const me             = this,
          getBatteryInfo = function(b) {
            me.emit('battery', SensorBus.fixBattery(b));
            b.addEventListener('levelchange',     me.onBatteryLevelChangeEvent);
            b.addEventListener('chargingchange',  me.onBatteryChargingChangeEvent);
            b.addEventListener('chargingtime',    me.onBatteryChargingTimeChangeEvent);
            b.addEventListener('dischargingtime', me.onBatteryDischargingTimeChangeEvent);
          },
          bat                = navigator.battery || navigator.webkitBattery || navigator.mozBattery;

    if(navigator.getBattery) {
      navigator.getBattery()
        .then(getBatteryInfo)
        .catch(err => {
          console.error(err);
        });
    } else if(bat) {
      getBatteryInfo(bat);
    }

    // eslint-disable-next-line one-var
    /*
    const a = document.createElement('a');
    a.onclick = this.activateGeolocation;
    a.click();
    */
    this.activateGeolocation();
  }

  async onPollingTrigger() {
    if(window.nwDispatcher || (window.chrome && window.chrome.system)) {
      const [cpu, mem] = await Promise.all([
        this.getCpu(),
        this.getMem()
      ]);
      Object.assign(this.measurementPoints, { cpu, mem });
    } else {
      throw new Error('Cannot poll on the current platform');
    }
  }

  /*
   * singleton will not really ever be destroyed
   */
  destroyed() {
    if(window.DeviceMotionEvent) {
      window.removeEventListener('devicemotion', this.onDeviceMotionEvent);
    }

    if(window.DeviceOrientationEvent) {
      window.removeEventListener('deviceorientation', this.onDeviceOrientationEvent);
    }

    const me                = this,
          removeBatteryInfo = function(b) {
            b.removeEventListener('levelchange',     me.onBatteryLevelChangeEvent);
            b.removeEventListener('chargingchange',  me.onBatteryChargingChangeEvent);
            b.removeEventListener('chargingtime',    me.onBatteryChargingTimeChangeEvent);
            b.removeEventListener('dischargingtime', me.onBatteryDischargingTimeChangeEvent);
          },
          bat                   = navigator.battery || navigator.webkitBattery || navigator.mozBattery;

    if(navigator.getBattery) {
      navigator.getBattery()
        .then(removeBatteryInfo)
        .catch(err => {
          console.error(err);
        });
    } else if(bat) {
      removeBatteryInfo(bat);
    }

    if(navigator.geolocation) {
      navigator.geolocation.clearWatch(this.geolocationChangeId);
    }

    if(this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
  }

  async getCpu() {
    let p,
        keys;
    if(window.nwDispatcher) {
      const os = require('os');
      keys  = ['idle', 'user', 'total', 'irq', 'nice', 'sys'];
      p = os.cpus().map(cpu => cpu.times);
    } else if(window.chrome && window.chrome.system) {
      keys  = ['idle', 'user', 'total', 'kernel'];
      const cpuInfo = await (new Promise(resolve => chrome.system.cpu.getInfo(resolve)));
      p = cpuInfo.processors.map(cpu => cpu.usage);
    } else {
      throw new Error('getCpu(): unknown platform');
    }
    const current = p.reduce((l, v, idx, a) => {
      if(!v.total) { // for all the cpus that have times
        v.total = Object.values(v).reduce((s, i) => s + i, 0);
      }
      keys.forEach(k => {
        l[k] = (l[k] || 0) + (v[k] || 0) / a.length; // average over all cpus
      });
      return l;
    });
    if(this.lastCpu && current.total !== this.lastCpu.total) {
      current.percent = 100 - 100 * (current.idle - this.lastCpu.idle) / (current.total - this.lastCpu.total);
    } else {
      // can't determin percentage
      current.percent = null;
    }
    this.lastCpu = current;
    return current;
  }

  async getMem() {
    if(window.nwDispatcher) {
      const os = require('os');
      return os.totalmem() - os.freemem();
    } else if(window.chrome && window.chrome.system) {
      const info = await (new Promise(resolve => chrome.system.memory.getInfo(resolve)));
      info.percent = 100 * info.capacity / info.availableCapacity;
      return info;
      // return info.capacity - info.availableCapacity;
    }
    throw new Error('getMem(): unknown platform');
  }

  activateGeolocation() {
    if(navigator.geolocation) {
      this.geolocationChangeId = navigator.geolocation.watchPosition(this.onGeolocationChangeEvent, err => {
        console.error(err);
        navigator.geolocation.clearWatch(this.geolocationChangeId);
      }, {
        timeout:            5000,
        enableHighAccuracy: true,
        maximumAge:         Infinity
      });
    }
  }

  onDeviceMotionEvent(event) {
    this.emit('motion', {
      ax: event.acceleration.x,
      ay: event.acceleration.y,
      az: event.acceleration.z,

      agx: event.accelerationIncludingGravity.x,
      agy: event.accelerationIncludingGravity.y,
      agz: event.accelerationIncludingGravity.z,

      ra: event.rotationRate.alpha,
      rb: event.rotationRate.beta,
      rg: event.rotationRate.gamma
    });
  }

  onDeviceOrientationEvent(event) {
    // left/right
    if(event.gamma === null && event.beta === null && event.alpha === null) {
      return;
    }
    this.emit('orientation', {
      lr:  event.gamma,
      fb:  event.beta,
      dir: event.alpha
    });
  }

  onBatteryLevelChangeEvent({ target }) {
    this.emit('battery', SensorBus.fixBattery(target));
  }

  onBatteryChargingChangeEvent({ target }) {
    this.emit('battery', SensorBus.fixBattery(target));
  }

  onBatteryChargingTimeChangeEvent({ target }) {
    this.emit('battery', SensorBus.fixBattery(target));
  }

  onBatteryDischargingTimeChangeEvent({ target }) {
    this.emit('battery', SensorBus.fixBattery(target));
  }

  onGeolocationChangeEvent({ coords }) {
    const pos = this.measurementPoints.position;
    if(pos && !Object.keys(pos).find(k => pos[k] !== coords[k])) {
      return;
    }
    this.emit('position', {
      latitude:         coords.latitude,
      longitude:        coords.longitude,
      altitude:         coords.altitude,
      accuracy:         coords.accuracy,
      altitudeAccuracy: coords.altitudeAccuracy,
      heading:          coords.heading,
      speed:            coords.speed
    });
  }

  static fixBattery(b) {
    return {
      level:           b.level,
      charging:        b.charging ? 1 : 0,
      chargingTime:    b.chargingTime,
      dischargingTime: b.dischargingTime
    };
  }
}

export default new SensorBus();
