<template>
  <div class="blog">
    <h1 class="title is-3">
      {{ dateHeader }}
    </h1>
    <ul>
      <li v-for="item in list" v-if="item.title">
        <h2 class="title is-4">
          <nuxt-link :to="item.url">
            {{ item.title }}
          </nuxt-link>
        </h2>
        <h3 v-if="item.date" class="subtitle is-6">
          <nuxt-link :to="item.url">
            <i class="fa fa-calendar"></i> &nbsp;
            {{ item.date | timeStamp($route.params) }}
          </nuxt-link>
        </h3>
      </li>
    </ul>
  </div>
</template>

<script>
import axios from 'axios';

const locale = process.browser ? ((navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage).toLowerCase().split(/[_-]+/)[0] : 'en';

export default {
  layout: 'sidebar',
  async asyncData({ params = {}, query, payload }) {
    let path = '',
        list = null;
    if(params.year) {
      path += `${params.year}`;
      if(params.month) {
        path += `/${params.month}`;
        if(params.day) {
          path += `/${params.day}`;
        }
      }
    }
    if(path.length) {
      list = payload || (await axios.get(`/unide/files/posts/${path}.json`)).data;
    } else {
      list = await import('posts/recent.json');
    }
    return {
      list: list.slice().reverse()
    };
  },
  computed: {
    dateHeader() {
      const { year, month, day } = this.$route.params,
            v = new Date(Date.UTC(year || 2016, month || 0, day || 1));
      if(!year) {
        return 'Recent Posts';
      }
      return Intl.DateTimeFormat(locale, {
        month: month ? 'long' : undefined,
        year:  year ? 'numeric' : undefined,
        day:   day ? '2-digit' : undefined
      }).format(v);
    }
  },
  filters: {
    timeStamp(v, { year, month, day }) {
      v = (v instanceof Date) ? v : new Date(v);
      const printTime = (v.getHours() && v.getMinutes());
      return Intl.DateTimeFormat(locale, {
        hour12: false,
        month:  month ? undefined : 'long',
        year:   year ? undefined : 'numeric',
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

.blog {
  h1 {
    border-bottom: 1px solid gray;
    padding-bottom: 0.3em;
  }
  h3 a {
    color: $bosch-darkgray;
  }
  li {
    margin-bottom: 2em;
  }
}
</style>


