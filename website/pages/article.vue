<template>
  <div class="article">
    <h1 v-if="post.title" class="title is-3">
      {{ post.title }}
    </h1>
    <h2 v-if="post.title && post.date" class="subtitle is-6">
      <i class="fa fa-calendar"></i> &nbsp;
      {{ post.date | timeStamp }}
    </h2>
    <div class="content">
      <div v-html="post.content"></div>
    </div>
    <div class="columns pageNav">
      <div class="column" v-if="post.prev">
        <nuxt-link :to="post.prev.url" class="button is-fullwidth">
          <span class="icon">
            <i class="fa fa-chevron-left"></i>
          </span>
          <span>{{ post.prev.title }}</span>
        </nuxt-link>
      </div>
      <div class="column" v-if="post.next">
        <nuxt-link :to="post.next.url" class="button is-fullwidth">
          <span>{{ post.next.title }}</span>
          <span class="icon">
            <i class="fa fa-chevron-right"></i>
          </span>
        </nuxt-link>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

const locale = process.browser ? ((navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage).toLowerCase().split(/[_-]+/)[0] : 'en';

export default {
  layout: 'sidebar',
  async asyncData({ params, payload, isServer}) {
    console.log("isServer: ",isServer);
    return {
      post: payload || (params.name && (await axios.get(`${isServer ? 'http://localhost:3000/' : '/'}unide/files/posts/${params.name}.json`)).data) || {}
    };
  },
  filters: {
    timeStamp(v) {
      v = (v instanceof Date) ? v : new Date(v);
      const printTime = (v.getHours() && v.getMinutes());
      return Intl.DateTimeFormat(locale, {
        hour12: false,
        year:   '2-digit',
        month:  '2-digit',
        day:    '2-digit',
        hour:   printTime ? 'numeric' : undefined,
        minute: printTime ? '2-digit' : undefined
      }).format(v);
    }
  }
};
</script>

<style lang="scss">
@import "~assets/variables.scss";

.article {
  h1 {
    font-weight: normal;
  }
  h2 {
    color: $bosch-darkgray;
  }
  .card {
    border-radius: $radius-large;
    .card-header {
      border-radius: $radius-large $radius-large 0 0;
      &.is-primary {
        background-color: $primary;
        .card-header-title {
          color: white;
        }
      }
    }
  }
  > .columns.pageNav {
    margin-top: 2em;
    .column a.button {
      font-size: 0.9em;
      padding: 1.3rem;
    }
  }
}
</style>

