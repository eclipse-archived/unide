<template>
  <div class="recentPosts">
    <h4 class="subtitle is-5">
      Recent Posts
    </h4>
    <ul>
      <li v-for="post in posts" :key="post.url">
        <nuxt-link :to="post.url">
          {{ post.title }}
        </nuxt-link>
      </li>
    </ul>
  </div>
</template>

<script>
import posts from 'posts/recent.json';

export default {
  data: () => ({
    posts: posts.slice().reverse()
  }),
  components: {
  }
}
</script>

<style lang="scss">
.recentPosts {
  ul {
    list-style: none;
  }
}
</style>
