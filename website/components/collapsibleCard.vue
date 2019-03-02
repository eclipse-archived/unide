<template>
  <div class="card collapsibleCard" :class="{collapsed: !expanded}" >
    <header v-if="$slots.header" class="card-header" @click.prevent="toggle()">
      <p class="card-header-title">
        <slot v-if="!$slots.headerCollapsed || expanded" name="header"/>
        <slot v-else name="headerCollapsed"/>
      </p>
      <a class="card-header-icon">
        <span class="icon">
          <i class="fa" :class="{'fa-angle-down': expanded, 'fa-angle-up': !expanded}"></i>
        </span>
      </a>
    </header>
    <transition name="rollup"
                v-on:before-leave="beforeleave"
                v-on:leave="leave"
                v-on:after-leave="afterleave"
                leave-active-class="collapsing"

                v-on:before-enter="beforeenter"
                v-on:enter="enter"
                v-on:after-enter="afterenter"
                enter-active-class="collapsing">
    <div v-show="expanded">
      <div class="card-content">
        <slot/>
      </div>
    </div>
    </transition>
  </div>
</template>

<script>
// import { mapState, mapActions } from 'vuex';

export default {
  props: {
    collapsed: {
      type:    Boolean,
      default: false
    }
  },
  data: function() {
    return {
      expanded:      !this.collapsed,
      transitioning: false
    };
  },
  watch: {
    collapsed: function(c) {
      this.expanded = !c;
    }
  },
  methods: {
    // expanding
    beforeenter(el) {
      this.transitioning = true;
      el.style.height = 0;
    },
    enter(el) {
      el.style.height = el.scrollHeight + 'px';
    },
    afterenter(el) {
      el.style.height = '';
      this.transitioning = false;
    },
    // collapsing
    beforeleave(el) {
      this.transitioning = true;
      el.style.height = el.offsetHeight + 'px';
      el.offsetHeight;
    },
    leave(el) {
      el.style.height = 0;
      el.offsetHeight;
    },
    afterleave(el) {
      this.transitioning = false;
    },
    toggle() {
      if(this.transitioning) {
        return;
      }
      // this.transitioning = true;
      this.expanded = !this.expanded;
      this.$emit(this.expanded ? 'expanding' : 'collapsing', this.id);
    }
  }
};
</script>

<style lang="scss">
@import "~assets/variables.scss";

.card.collapsibleCard {
  &>header {
    .card-header-icon {
      text-decoration: none;
    }
    cursor: pointer;
    background-color: $light;
    border-bottom: 1px solid rgba($black, 0.1);
    .card-header-title {
      font-weight: normal;
    }
    p {
      margin-bottom: 0;
    }
  }
  &.collapsed header {
    border-bottom: none;
  }
  & > div.collapsing {
    position: relative;
    height: 0;
    overflow: hidden;
    display: block;
    transition-property: height;
    transition-duration: .35s;
    transition-timing-function: ease;
  }
}
</style>
