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
      <ul>
        <li>
          <a>http://[host]:[port]/rest/v2/message?validate=true</a>
        </li>
        <li>
          <a>http://[host]:[port]/rest/v2/measurement?validate=true</a>
        </li>
        <li>
          <a>http://[host]:[port]/rest/v2/process?validate=true</a>
        </li>
        <li>
          <a>http://[host]:[port]/rest/basicauth/v2/message?validate=true</a>
        </li>
        <li>
          <a>http://[host]:[port]/rest/basicauth/v2/measurement?validate=true</a>
        </li>
        <li>
          <a>http://[host]:[port]/rest/basicauth/v2/process?validate=true</a>
        </li>
      </ul>
    </div>
  </div>
</template>
