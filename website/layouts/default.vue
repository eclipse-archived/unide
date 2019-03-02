<template>
  <div>
    <navbar>
      <nuxt-link exact active-class="is-active" class="navbar-item" to="/">
        Home
      </nuxt-link>
      <nuxt-link active-class="is-active" class="navbar-item" to="/proposal">
        Proposal
      </nuxt-link>
      <nuxt-link active-class="is-active" class="navbar-item" to="/specification">
        Specification
      </nuxt-link>
      <nuxt-link active-class="is-active" class="navbar-item" to="/blog">
        Blog Archives
      </nuxt-link>
      <nuxt-link active-class="is-active" class="navbar-item" to="/faq">
        FAQ
      </nuxt-link>
    </navbar>
    <div class="container">
      <div class="header clearfix">
      </div>
      <nuxt/>
    </div>
    <eclipseFooter/>
  </div>
</template>

<script>
import eclipseFooter from '~/components/eclipseFooter.vue';
import navbar from '~/components/navbar.vue';

export default {
  components: {
    eclipseFooter, navbar
  }
};
</script>
