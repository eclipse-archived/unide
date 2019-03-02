<template>
  <component :is="tag">
    <nuxt-link :to="'#p-'+schema.$id">
      <span v-html="schema.$step"/>
    </nuxt-link>
    <ul v-if="schema.properties || schema.patternProperties || schema.items">
      <schemaToc v-for="(node, key) in schema.properties" :key="key" :schema="node"></schemaToc>
      <schemaToc v-for="(node, key) in schema.patternProperties" :key="key" :schema="node"></schemaToc>
      <template v-if="schema.items">
        <template v-if="schema.items instanceof Array">
          <schemaToc v-for="(node, idx) in schema.items" :key="idx" :schema="node"></schemaToc>
        </template>
        <schemaToc v-else :schema="schema.items"></schemaToc>
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
    schema: {
      type:     Object,
      required: true
    }
  }
};
</script>

<style lang="scss">
@import "~assets/variables.scss";

.schemaDocumenation {
}
</style>
