<template>
  <header class="nav-header">
    <div class="container">
      <nav class="navbar">
        <div id="navMenu" class="navbar-menu" :class="{ 'is-active': isMenuActive }" @click="isMenuActive = false">
          <div class="navbar-start">
            <slot/>
          </div>
          <!--div style="overflow: visible" class="navbar-end">
            <nuxt-link active-class="is-active" class="navbar-item" to="/about">
              <i class="fa fa-info-circle">&nbsp;</i>
            </nuxt-link>
          </div-->
        </div>
        <div class="navbar-brand">
          <div class="navbar-burger burger" :class="{ 'is-active': isMenuActive }" @click="isMenuActive = !isMenuActive">
            <span></span>
            <span></span>
            <span></span>
          </div>
        
          <nuxt-link class="navbar-item" to="/">
            <img src="logo.svg" :alt="brand">
          </nuxt-link>
        </div>
      </nav>
    </div>
  </header>
</template>

<script>
export default {
  data: () => ({
    brand:        process.env.BRAND,
    isMenuActive: false
  }),
  components: {}
};
</script>

<style lang="scss">
  @import "~assets/variables.scss";

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
        align-self: stretch;
        .navbar-item img {
          max-height: none;
          width: 240px;
        }
      }
      .navbar-item, .navbar-link, .navbar-link a {
        color: $bosch-darkgray;
        &:hover, &.is-active {
          color: $bosch-lima;
        }
        &:hover {
          background-color: transparent;
          text-decoration: none;
          font-weight: bold;
        }
        .dropdown-trigger {
          display: flex;
        }
      }

      align-items: flex-end;
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

