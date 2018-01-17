<template>
  <div class="blogArchives">
    <h4 class="subtitle is-5">
      Blog Archives 
    </h4>
    <ul>
      <li v-for="post in posts" :key="post.url">
        <nuxt-link :to="post.url">
          {{ post | timeStamp }}
        </nuxt-link>
        ({{ post.count }})
      </li>
    </ul>
  </div>
</template>

<script>
import posts from 'posts/archives.json';

const locale = process.browser ? ((navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage).toLowerCase().split(/[_-]+/)[0] : 'en';

export default {
  data: () => ({
    posts: posts.slice().reverse()
  }),
  filters: {
    timeStamp({ year, month }) {
      const v = new Date(Date.UTC(year, month || 0, 1));
      return Intl.DateTimeFormat(locale, {
        month: 'long',
        year:  'numeric'
      }).format(v);
    }
  }
}
</script>

<style lang="scss">
.blogArchives {
  ul {
    list-style: none;
  }
}
</style>

