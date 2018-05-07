<template>
  <div class="home">
    <section class="hero is-primary is-bold">
      <div class="hero-body">
        <div class="container">
          <h1 class="title">
            {{ $t('home.title') }}
          </h1>
          <h2 class="subtitle">
            {{ $t('home.subtitle') }}
          </h2>
        </div>
      </div>
    </section>
    <div class="box">
      <ul class="menu-list">
        <li>
          <router-link to="/ppmp">
            Production Performance Management Protocol
          </router-link>
          <ul>
            <li><a>Measurements</a></li>
            <li><a>Messages</a></li>
            <li><a>Processes</a></li>
          </ul>
        </li>
        <li>
          <router-link to="/configuration">
            {{ $t('configuration.title') }}
          </router-link>
        </li>
        <li>
          <router-link to="/about">
            {{ $t('about.about') }} / {{ $t('about.licenses') }}
          </router-link>
        </li>
      </ul>
    </div>
  </div>
</template>

<style lang="scss">
.home {
  a:not([href]) {
    cursor: default;
  }
}
</style>