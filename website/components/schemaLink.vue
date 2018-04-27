<template>
  <div>
    <slot/>
    <template v-if="link === true">
      yes 
    </template>
    <template v-else-if="link === false">
      no 
    </template>
    <template v-else-if="isSchema(link)">
      <nuxt-link :to="'#p-'+link.$id">
        <span v-html="link.getPath()"></span>
      </nuxt-link>
    </template>
    <ul v-else-if="link instanceof Array">
      <li v-for="sublink in link" :key="sublink.$id">
        <nuxt-link :to="'#p-'+sublink.$id">
          <span v-html="sublink.getPath()"></span>
        </nuxt-link>
      </li>
    </ul>
    <ul v-else-if="link instanceof Object">
      <li v-for="(subschema, key) in link">
        <nuxt-link :to="'#p-'+subschema.$id">
          {{ key }}
        </nuxt-link>
      </li>
    </ul>
  </div>
</template>

<script>
import Schema from "../assets/schema.js";

export default {
  props: {
    link: {
      required: true
    }
  },
  methods: {
    isSchema: obj => obj instanceof Schema
  }
};
</script>
