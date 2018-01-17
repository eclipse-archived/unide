<template>
  <header class="nav-header">
    <div class="container">
      <nav class="navbar">
        <div id="navMenu" class="navbar-menu" :class="{ 'is-active': isMenuActive }" @click="isMenuActive = false">
          <div class="navbar-start">
            <slot/>
          </div>
          <div style="overflow: visible" class="navbar-end">
            <SelectLang/>
            <router-link active-class="is-active" class="navbar-item" to="/configuration">
              <i class="fa fa-cog">&nbsp;</i>
            </router-link>
            <router-link active-class="is-active" class="navbar-item" to="/about">
              <i class="fa fa-info-circle">&nbsp;</i>
            </router-link>
            <div class="navbar-item signOut" @click="signOut">
              <i class="fa" :class="{'fa-sign-out': authenticated, 'fa-sign-in': !authenticated}">&nbsp;</i>
            </div>
          </div>
        </div>
        <div class="navbar-brand">
          <div class="navbar-burger burger" :class="{ 'is-active': isMenuActive }" @click="isMenuActive = !isMenuActive">
            <span></span>
            <span></span>
            <span></span>
          </div>
        
          <router-link v-if="brand" class="navbar-item" to="/">
            <img src="../img/logo.svg" :alt="brand">
          </router-link>
        </div>
      </nav>
    </div>
  </header>
</template>
<script>
import SelectLang               from 'components/selectLang';
import { mapState, mapActions } from 'vuex';

export default {
  data: () => ({
    brand:        process.env.BRAND,
    isMenuActive: false
  }),
  computed: mapState({
    authenticated: state => {
      let c = state.configuration;
      if(!c || !c.credentials) {
        return false;
      }
      c = c.credentials;
      return (c.user && c.password);
    }
  }),
  methods: mapActions('configuration', ['signOut']),
  components: {
    SelectLang
  }
};
</script>

<style lang="scss">
  @import "~styles/variables.scss";
  header.nav-header {
    nav.navbar {
      display: flex;
      .navbar-burger {
        margin-left: 0px;
        height: auto;
        & span {
          height: 2px;
          width: 25px;
          &:nth-child(1) {
            margin-top: -9px;
          }
          &:nth-child(3) {
            margin-top: 7px;
          }
        }
      }
      .navbar-brand {
        height: 100%;
        .navbar-item img {
          max-height: none;
          width: 240px;
        }
      }
      .navbar-item, .navbar-link, .navbar-link a {
        color: $bosch-gainsboro;
        &:hover, &.is-active {
          color: $bosch-lima;
        }
        &:hover {
          background-color: transparent;
          font-weight: bold;
        }
        .dropdown-trigger {
          display: flex;
        }
      }

      align-items: flex-end;
      height: 100%;
      border-bottom: 5px solid $bosch-windsor;
    }
    box-shadow: 0px 3px 5px 0px $bosch-gainsboro;
    margin-bottom: 2em;
    padding-bottom: 0px;
  }
@media screen and (min-width: 1024px) {
  .nav-header .container > .navbar .navbar-menu {
    margin-right: 1rem;
  }
}
@media screen and (max-width: 1007px) {
  header.nav-header nav.navbar {
    flex-direction: column;
    align-items: stretch;
    .navbar-menu {
      order: 1;
      box-shadow: none;
    }
  }
}
@media screen and (min-width: 1008px) {
  nav.navbar .container .navbar-menu {
    height: 40px;
  }
}
</style>
