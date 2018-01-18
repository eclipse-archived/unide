<template>
  <component :is="tag">
    <nuxt-link :to="'#p-'+schema.$idx">
      <span v-html="schema.$path"/>
    </nuxt-link>
    <ul v-if="schema.properties || schema.patternProperties || schema.items">
      <schemaToc v-for="(node, key) in schema.properties" :key="key" :schemas="schemas" :entryNode="node"></schemaToc>
      <schemaToc v-for="(node, key) in schema.patternProperties" :key="key" :schemas="schemas" :entryNode="node"></schemaToc>
      <template v-if="schema.items">
        <template v-if="schema.items instanceof Array">
          <schemaToc v-for="(node, idx) in schema.items" :key="idx" :schemas="schemas" :entryNode="node"></schemaToc>
        </template>
        <schemaToc v-else :schemas="schemas" :entryNode="schema.items"></schemaToc>
      </template>
    </ul>
  </component>
</template>

<script>

export default {
  name:  'schemaToc',
  props: {
    tag: {
      type:    String,
      default: 'li'
    },
    schemas: {
      type: Object
    },
    entryNode: {
      type:    String,
      default: '$'
    }
  },
  computed: {
    schema() {
      return this.schemas[this.entryNode];
    }
  }
};
</script>

<style lang="scss">
@import "~assets/variables.scss";

.schemaDocumenation {
}
</style>

