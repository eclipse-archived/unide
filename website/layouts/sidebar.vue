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
      <div class="columns">
        <nuxt class="column is-8"/>
        <div class="sidebar column is-3 is-offset-1">
          <recentPosts/>
          <blogArchives/>
        </div>
      </div>
    </div>
    <eclipseFooter/>
  </div>
</template>

<script>
import navbar from "~/components/navbar.vue";
import recentPosts from "~/components/recentPosts.vue";
import blogArchives from "~/components/blogArchives.vue";
import eclipseFooter from "~/components/eclipseFooter.vue";

export default {
  components: {
    eclipseFooter,
    blogArchives,
    navbar,
    recentPosts
  }
};
</script>

<style lang="scss">
body {
  .sidebar div {
    margin-bottom: 2em;
    font-size: 0.875em;
    .subtitle {
      margin-bottom: 0.5rem;
    }
  }
}
</style>

