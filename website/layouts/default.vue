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
    <footer class="footer">
      <div class="container">
        <div class="content has-text-centered">
          <ul>
            <span>
              <a href="http://www.eclipse.org/legal/privacy.php">Privacy Policy</a>
            </span>
            <span>
              <a href="http://www.eclipse.org/legal/termsofuse.php">Terms of Use</a>
            </span>
            <span>
              <a href="http://www.eclipse.org/legal/copyright.php">Copyright Agent</a>
            </span>
            <span>
              <a href="https://eclipse.org/security/ ">Security reports</a>
            </span>
            <span>
              <a href="http://www.eclipse.org/legal/">Legal</a>
            </span>
          </ul>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import navbar from '~/components/navbar.vue';

export default {
  components: {
    navbar
  }
};
</script>
