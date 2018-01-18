<template>
  <div>
    <slot/>
    <template v-if="link === true">
      yes 
    </template>
    <template v-else-if="link === false">
      no 
    </template>
    <template v-else-if="typeof link === 'string'">
      <nuxt-link :to="'#p-'+schemas[link].$idx">
        <span v-html="schemas[link].$path"></span>
      </nuxt-link>
    </template>
    <ul v-else-if="link instanceof Array">
      <li v-for="sublink in link">
        <nuxt-link :to="'#p-'+schemas[sublink].$idx">
          <span v-html="schemas[sublink].$path"></span>
        </nuxt-link>
      </li>
    </ul>
    <ul v-else-if="link instanceof Object">
      <li v-for="(subschema, key) in link">
        <nuxt-link :to="'#p-'+schemas[subschema].$idx">
          {{ key }}
        </nuxt-link>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  props: {
    schemas: {
      type:     Object,
      required: true
    },
    link: {
      required: true
    }
  }
};
</script>
