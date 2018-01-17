<template>
  <div class="ciHelp">
    <div v-for="c in colors" class="tag is-medium color" :class="['color-'+c]">{{ c }}</div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      colors: ['bosch-prussianblue', 'bosch-matisse', 'bosch-wildblueyonder', 'bosch-spindle', 'bosch-red', 'bosch-aluminium', 'bosch-darkgray', 'bosch-gainsboro', 'bosch-eggplant', 'bosch-windsor', 'bosch-cobalt', 'bosch-pacificblue', 'bosch-bondiblue', 'bosch-lima', 'bosch-watercourse', 'bosch-fiord', 'primary', 'success', 'info', 'warning', 'danger']
    };
  }
};
</script>

<style lang='scss'>
@import '~styles/variables.scss';

@each $name, $value in $bosch-colors {
  .tag:not(body).color-#{$name} {
    background-color: nth($value, 1);
    color: nth($value, 2);
  }
}

@each $name, $value in $colors {
  .tag:not(body).color-#{$name} {
    background-color: nth($value, 1);
    color: nth($value, 2);
  }
}

.ciHelp {
  .color {
    margin: 10px;
    padding: 10px;
  }
  .prussianblue {
    background-color: $bosch-prussianblue;
  }
}
</style>
