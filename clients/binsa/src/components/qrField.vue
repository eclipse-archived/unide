<template>
  <p class="qrField control has-icons-right">
    <input class="input" :value="value" @change="$emit('input', $event.target.value)" :disabled="disabled">
    <span class="qrcode icon is-small is-right" @click="!disabled && startQrScan()">
      <i class="fa fa-qrcode"></i>
    </span>
    <b-modal :active="loading || config !== null" has-modal-card :canCancel="true" :onCancel="stopQrScan">
      <div class="modal-card">
        <header class="modal-card-head">
          <p class="modal-card-title">
            {{ $t('ppmp.scanQrCode') }}
          </p>
          <button class="delete" @click="stopQrScan"></button>
        </header>
        <section class="modal-card-body" :class="{ loading: loading }">
          <video autoplay="autoplay" ref="preview"></video>
          <canvas ref="canvas" style="display:none;"></canvas>
        </section>
      </div>
    </b-modal>
  </p>
</template>

<script>
import Dialog from 'buefy/src/components/dialog';
import QrCode from 'qrcode-reader';

class NoCamsFoundError extends Error {};
class MediaDevicesNotSupportedError extends Error {};

export default {
  props: {
    value: {
      type:    String,
      default: ''
    },
    disabled: {
      type:    Boolean,
      default: false
    }
  },

  data() {
    return {
      config:  null,
      loading: false
    };
  },

  methods: {
    onScan(value) {
      this.$emit('input', value);
      this.stopQrScan();
    },
    scan() {
      if(!this.config) {
        return;
      }
      const config = this.config;
      config.canvasContext.drawImage(
        config.video,
        /* left */ 0,
        /* top */ 0,
        config.width,
        config.height
      );

      config.scanner.decode(config.canvasContext.getImageData(0, 0, config.width, config.height));
      requestAnimationFrame(() => this.scan());
    },
    async startQrScan(el) {
      this.loading = true;
      try {
        if(!navigator.mediaDevices) {
          throw new MediaDevicesNotSupportedError('navigator.mediaDevices not supported');
        }
        const devices = await navigator.mediaDevices.enumerateDevices(),
              cams    = devices.filter(d => d.kind === 'videoinput');
        if(!cams.length) {
          throw new NoCamsFoundError('no camera found!');
        }
        // eslint-disable-next-line one-var
        const stream = await navigator.mediaDevices.getUserMedia({
                audio: false,
                video: {
                  mandatory: {
                    sourceId:       cams[cams.length - 1].deviceId,
                    minWidth:       600,
                    maxWidth:       800,
                    minAspectRatio: 1.6
                  },
                  optional: []
                }
              }),
              config = {
                video:   this.$refs.preview,
                stream:  stream,
                scanner: new QrCode()
              };
        config.scanner.callback = (err, result) => {
          if(err) {
            console.error(err);
            // this.stopQrScan();
            return;
          }
          this.onScan(result.result);
        };
        this.$refs.preview.srcObject = config.stream;
        await new Promise((resolve, reject) => {
          const onLoadedMetaData = () => {
            this.$refs.preview.removeEventListener('loadedmetadata', onLoadedMetaData);
            this.config = config;
            /*
            try {
              const constraints = tracks[0].getConstraints();
              config.width  = constraints.width.max;
              config.height = constraints.aspectRatio.min * config.width;
              if((typeof config.height !== 'number') || (typeof config.width !== 'number')) {
                throw new Error("Can't find height/width of video track");
              }
            }
            */
            config.width  = this.$refs.preview.videoWidth;
            config.height = this.$refs.preview.videoHeight;
            this.$refs.canvas.width  = config.width;
            this.$refs.canvas.height = config.height;
            config.canvasContext = this.$refs.canvas.getContext('2d');
            this.$nextTick(this.scan);
            this.loading = false;
            resolve(stream);
          };
          this.$refs.preview.addEventListener('loadedmetadata', onLoadedMetaData);
        });
      } catch(err) {
        console.error(err);
        let msg = err.message || 'unknown error, see console';
        if(err instanceof NoCamsFoundError) {
          msg = this.$t('ppmp.noCameras');
        } else if(err instanceof MediaDevicesNotSupportedError) {
          msg = this.$t('notSupported');
        }
        this.stopQrScan();
        Dialog.alert({
          title:   this.$t('ppmp.connectionError'),
          message: msg,
          type:    'is-danger',
          hasIcon: true,
          icon:    'times-circle'
        });
      }
    },
    stopQrScan() {
      this.loading = false;
      if(this.config) {
        this.$refs.preview.src = '';
        let stream;
        for(stream of this.config.stream.getVideoTracks()) {
          stream.stop();
        }
        this.config = null;
        /*
        this.scanner.removeListener('scan', this.onScan);
        this.scanner.stop();
        this.scanner = null;
        */
      }
    }
  }
};
</script>

<style lang='scss'>
.qrField {
  video {
    width: 100%;
  }
}
</style>
