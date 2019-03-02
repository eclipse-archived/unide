<template>
  <div id="schema-detail" class="schemaDetail" :class="{'is-loading': loading}">
    <div class="accordion">
      <card :collapsed="true" v-if="masterSchema !== null">
        <template slot="header">
          Structure of the payload
        </template>
        <ul>
          <schemaToc v-for="(node, key) in masterSchema.properties" :key="key" :schema="node"></schemaToc>
        </ul>
      </card>

      <slot/>
    </div>

    <h1>
      <a :id="type + '-schema-detail'" :title="type + ' Fields definition'"></a>
      Fields definition
    </h1>

    <div class="card property" v-for="schema in schemas" :key="schema.$id">
      <header class="card-header">
        <a :id="'p-'+schema.$id"></a>
        <p class="card-header-title">
        <span v-html="schema.getPath()"/>
        </p>
        <nuxt-link to="#schema-detail" title="go up" class="card-header-icon">
          <span class="icon">
            <i class="fa fa-angle-up"></i>
          </span>
        </nuxt-link>
      </header>
      <div class="card-content">
        <div class="field is-horizontal" v-if="schema.description || schema.type">
          <div class="field-label">
            <label class="label">Description:</label>
          </div>
          <div class="field-body" v-if="schema.description">
            {{ schema.description }}
          </div>
          <div class="field-body" v-else-if="schema.type">
            A simple {{ schema.type | capitalize }}
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.$parent">
          <div class="field-label">
            <label class="label">Parent:</label>
          </div>
          <div class="field-body">
            <nuxt-link :to="'#p-'+schema.$parent.schema.$id">
              <span v-html="schema.$parent.schema.getPath()"/>
            </nuxt-link>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.type">
          <div class="field-label">
            <label class="label">Type:</label>
          </div>
          <div class="field-body">
            {{ schema.type | capitalize(schema) }}
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.format">
          <div class="field-label">
            <label class="label">Format:</label>
          </div>
          <div class="field-body">
            {{ schema.format }}
            <a v-if="$static.formats[schema.format]" :href="$static.formats[schema.format].link">&nbsp;({{ $static.formats[schema.format].name }})</a>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.enum && schema.enum.length">
          <div class="field-label">
            <label class="label">Possible values:</label>
          </div>
          <div class="field-body">
            <div>
              {{ schema.enum.join(', ') }}
            </div>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.hasOwnProperty('default')">
          <div class="field-label">
            <label class="label">Defaults to:</label>
          </div>
          <div class="field-body">
            <div>
              {{ schema.default }}
            </div>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.maxLength">
          <div class="field-label">
            <label class="label">Maximum length:</label>
          </div>
          <div class="field-body">
            <div>
              {{ schema.maxLength }}
            </div>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.properties || schema.patternProperties || schema.items">
          <div class="field-label">
            <label class="label">Restriction on subfields:</label>
          </div>
          <div class="field-body">
            <schemaLink :link="schema.properties" v-if="Object.keys(schema.properties || {}).length > 0"/>
            <schemaLink :link="schema.patternProperties" v-if="Object.keys(schema.patternProperties || {}).length > 0">
              <div>Matching regular expressions:</div>
            </schemaLink>
            <schemaLink :link="schema.items" v-if="schema.items && !(schema.items instanceof Array)"/>
            <schemaLink :link="schema.items" v-else-if="schema.items">
              <div>Ordered items of type:</div>
            </schemaLink>
            <template v-if="Object.keys(schema.properties || {}).length + Object.keys(schema.patternProperties || {}).length === 0 && !schema.items">none</template>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.minProperties">
          <div class="field-label">
            <label class="label">Minimum amount of subfields:</label>
          </div>
          <div class="field-body">
            <div>
              {{ schema.minProperties }}
            </div>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.minItems">
          <div class="field-label">
            <label class="label">Minimum amount of items:</label>
          </div>
          <div class="field-body">
            <div>
              {{ schema.minItems }}
            </div>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.hasOwnProperty('additionalProperties')">
          <div class="field-label">
            <label class="label">Allows additional fields:</label>
          </div>
          <schemaLink :link="schema.additionalProperties" v-if="typeof schema.additionalProperties === 'boolean'" class="field-body"/>
          <div class="field-body" v-else>
            if fulfilling <schemaLink :link="schema.additionalProperties" class="field-body"/>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.not || schema.oneOf || schema.allOf || schema.anyOf">
          <div class="field-label">
            <label class="label">Restriction with boolean logic:</label>
          </div>
          <div class="field-body">
            <schemaLink :link="schema.not" v-if="schema.not">
              Not fulfilling&nbsp;
            </schemaLink>
            <schemaLink :link="schema.oneOf" v-if="schema.oneOf">
              <div>Fulfilling exactly one of:</div>
            </schemaLink>
            <schemaLink :link="schema.allOf" v-if="schema.allOf">
              <div>Fulfilling all of:</div>
            </schemaLink>
            <schemaLink :link="schema.anyOf" v-if="schema.anyOf">
              <div>Fulfilling one of:</div>
            </schemaLink>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.required && schema.required.length">
          <div class="field-label">
            <label class="label">Required fields:</label>
          </div>
          <div class="field-body">
            {{ schema.required.join(', ') }}
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.examples">
          <div class="field-label">
            <label class="label">Example:</label>
          </div>
          <div class="field-body">
            <prism language="json" v-for="(example, idx) in schema.examples" :key="idx">{{ example | stringify }}</prism>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import card from '~/components/collapsibleCard.vue';
import get from 'lodash/get';
import merge from 'lodash/merge';
import prism from 'vue-prism-component';
import JSONSchema from '../assets/schema';
import schemaLink from '~/components/schemaLink.vue';
import schemaToc from '~/components/schemaToc.vue';
import set from 'lodash/set';

export default {
  props: {
    type: {
      type:     String,
      required: true
    },
    examples: {
      type:     Object,
      required: false,
      default:  () => ({})
    }
  },
  data() {
    return {
      masterSchema: null,
      loading:      true
    };
  },
  created() {
    const rfcs = {
      date: {
        link: 'https://tools.ietf.org/html/rfc3339#section-5.6',
        name: 'RFC3339, 5.6'
      },
      email: {
        link: 'https://tools.ietf.org/html/rfc5322#section-3.4.1',
        name: 'RFC5322, 3.4.1'
      }
    };
    import(`~/assets/schemas/${this.type}_schema.json`)
      .then((schema) => {
        this.masterSchema = new JSONSchema('$', null, schema.default);
        // inject examples
        Object.entries(this.examples).forEach(([path, example]) => {
          if(get(this.masterSchema, path)) {
            set(this.masterSchema, `${path}.examples`, example);
          } else {
            console.error(`schema path not found in ${this.type}: ${path}`);
          }
        });
        this.loading = false;
        return schema.default;
      })
      .catch(err => {
        throw err;
      });

    this.$static = {
      formats: {
        'date-time': rfcs.date,
        date:        rfcs.date,
        time:        rfcs.date,
        email:       rfcs.email
      }
    };
  },
  methods: {
    traverse(obj, fn) {
      if(obj instanceof JSONSchema) {
        fn(obj);
        Object.entries(obj)
          .filter(([key, value]) => key !== '$parent')
          .forEach(([key, value]) => this.traverse(value, fn));
      } else if(obj instanceof Array) {
        obj.forEach(v => this.traverse(v, fn));
      } else if(obj instanceof Object) {
        Object.values(obj).forEach(v => this.traverse(v, fn));
      }
    },
    simplifySchema(schema) {
      const removableWith = (subschema) => {
              if(subschema.anyOf && subschema.anyOf.indexOf(true) >= 0) {
                return [subschema, true];
              }
              if(subschema.allOf && subschema.allOf.indexOf(false) >= 0) {
                return [subschema, false];
              }
              if(subschema.allOf && subschema.allOf.length) {
                return [subschema, new JSONSchema(subschema.$step, subschema.$parent, merge({}, ...subschema.allOf.map(item => item.toJSON())))];
              }
              if(subschema.not === true) {
                return [subschema, false];
              }
              if(Object.keys(subschema).filter(key => key[0] !== '$').length === 0) {
                return [subschema, true];
              }
              return null;
            },
            replace = (replacement) => {
              if(!replacement) {
                return;
              }
              const [rschema, rpl] = replacement,
                    parentRef     = rschema.$parent;
              if(parentRef) { // is this replacement still valid?
                set(parentRef.schema, parentRef.path, rpl);
              }
              schema.$parent = null;
              replace(removableWith(parentRef.schema)); // repeat for parent
            };
      this.traverse(schema, obj => {
        replace(removableWith(obj));
      });
      return schema;
    }
  },
  computed: {
    schemas() {
      if(!this.masterSchema) {
        return null;
      }
      // flatten schema
      const schemas = {};
      this.traverse(this.simplifySchema(this.masterSchema), obj => {
        schemas[obj.$id] = obj;
      });
      return schemas;
    }
  },
  filters: {
    stringify(v) {
      return JSON.stringify(v, ' ', 2);
    },
    capitalize(v, schema) {
      if(!v) {
        return '';
      }
      if(!(v instanceof Array)) {
        v = [v];
      }
      return v.map(i => `${i[0].toUpperCase()}${i.slice(1)}`).join(', ');
    }
  },
  components: {
    card,
    prism,
    schemaToc,
    schemaLink
  }
};
</script>

<style lang="scss">
.schemaDetail {
  a {
    text-decoration: none;
  }
  .property {
    ul {
      list-style-type: disc;
      padding-left: 1em;
      margin-left: 0;
      margin-top: 0;
    }
  }
}
</style>
