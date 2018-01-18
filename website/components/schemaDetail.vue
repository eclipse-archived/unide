<template>
  <div id="schema-detail" class="schemaDetail" :class="{'is-loading': loading}">
    <div class="accordion">
      <card :collapsed="true" v-if="schemas !== null">
        <template slot="header">
          Structure of payload
        </template>
        <schemaToc :schemas="schemas" tag="div">
        </schemaToc>
      </card>

      <slot/>
    </div>

    <h1>
      <a :id="type + '-schema-detail'" :title="type + ' Fields definition'"></a>
      Fields definition
    </h1>

    <div class="card property" v-for="schema in schemas" :key="schema.$key">
      <header class="card-header">
        <a :id="'p-'+schema.$idx"></a>
        <p class="card-header-title">
        <span v-html="schema.$path"/>
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
            <nuxt-link :to="'#p-'+schema.$parent[0].$idx">
              <span v-html="schema.$parent[0].$path"/>
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
            <schemaLink :schemas="schemas" :link="schema.properties" v-if="Object.keys(schema.properties || {}).length > 0"/>
            <schemaLink :schemas="schemas" :link="schema.patternProperties" v-if="Object.keys(schema.patternProperties || {}).length > 0">
              <div>Matching regular expressions:</div>
            </schemaLink>
            <schemaLink :schemas="schemas" :link="schema.items" v-if="schema.items && !(schema.items instanceof Array)"/>
            <schemaLink :schemas="schemas" :link="schema.items" v-else-if="schema.items">
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
          <schemaLink :schemas="schemas" :link="schema.additionalProperties" v-if="typeof schema.additionalProperties === 'boolean'" class="field-body"/>
          <div class="field-body" v-else>
            if fulfilling <schemaLink :schemas="schemas" :link="schema.additionalProperties" class="field-body"/>
          </div>
        </div>
        <div class="field is-horizontal" v-if="schema.not || schema.oneOf || schema.allOf || schema.anyOf">
          <div class="field-label">
            <label class="label">Restriction with boolean logic:</label>
          </div>
          <div class="field-body">
            <schemaLink :schemas="schemas" :link="schema.not" v-if="schema.not">
              Not fulfilling&nbsp;
            </schemaLink>
            <schemaLink :schemas="schemas" :link="schema.oneOf" v-if="schema.oneOf">
              <div>Fulfilling exactly one of:</div>
            </schemaLink>
            <schemaLink :schemas="schemas" :link="schema.allOf" v-if="schema.allOf">
              <div>Fulfilling all of:</div>
            </schemaLink>
            <schemaLink :schemas="schemas" :link="schema.anyOf" v-if="schema.anyOf">
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
        <div class="field is-horizontal" v-if="examples.hasOwnProperty(schema.$key)">
          <div class="field-label">
            <label class="label">Example:</label>
          </div>
          <div class="field-body">
            <prism language="json">...
"{{ examples[schema.$key][1] }}": {{ examples[schema.$key][0][examples[schema.$key][1]] | stringify }}
...</prism>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import prism      from 'vue-prism-component';
import cloneDeep  from 'lodash.clonedeep';
import card       from '~/components/collapsibleCard.vue';
import schemaToc  from '~/components/schemaToc.vue';
import schemaLink from '~/components/schemaLink.vue';

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
      schema:  null,
      loading: true
    };
  },
  created() {
    const rfcs     = {
      date: {
        link: 'https://tools.ietf.org/html/rfc3339#section-5.6',
        name: 'RFC3339, 5.6'
      },
      email: {
        link: 'https://tools.ietf.org/html/rfc5322#section-3.4.1',
        name: 'RFC5322, 3.4.1'
      }
    };
    import(`~/assets/schemas/v2/${this.type}_schema.json`)
      .then(schema => {
        this.schema = schema;
        this.loading = false;
        this.idx = 0;
        return schema;
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
    flattenSchema(schema = {}, id = { $key: '$', $path: '$' }) {
      // http://json-schema.org/latest/json-schema-core.html#rfc.section.4.3.1
      if(schema === true) {
        schema = {};
      } else if(schema === false) {
        schema = {
          not: true
        };
      }
      const schemas = {
        [id.$key]: Object.assign(schema, id, {
          $idx: ++this.idx
        })
      };
      if(schema.items) {
        if(schema.items instanceof Array) {
          schema.items.forEach((item, idx) => {
            const childId = {
              $key:    `${id.$key}/${idx}`,
              $path:   `${id.$path}[${idx}]`,
              $parent: [schema, schema.items, idx]
            };
            Object.assign(schemas, this.flattenSchema(item, childId));
            schema.items[idx] = childId.$key;
          });
        } else {
          const childId = {
            $key:    `${id.$key}/`,
            $path:   `${id.$path}[*]`,
            $parent: [schema, schema, 'items']
          };
          Object.assign(schemas, this.flattenSchema(schema.items, childId));
          schema.items = childId.$key;
        }
      }
      // Object with subschemas
      // the excluded keywords are for validation but don't create new children
      ['properties', 'patternProperties'] /*, 'dependencies' */
        .filter(key => schema.hasOwnProperty(key))
        .forEach(key =>
          Object.entries(schema[key])
          // .filter(([subkey, dependency]) => !(dependency instanceof Array)) // just needed for dependencies
            .forEach(([subkey, subschema], idx) => {
              const childId = {
                $key:    `${id.$key}/${key}/${encodeURIComponent(subkey)}`,
                $path:   `${id.$path}&#8203;.`,
                $parent: [schema, schema[key], subkey]
              };
              childId.$path += key === 'patternProperties' ? `&lt;field&gt;` : subkey;
              Object.assign(schemas, this.flattenSchema(subschema, childId));
              schema[key][subkey] = childId.$key;
            })
        );
      // simple direct subschemas
      // the excluded keywords are for validation but don't create new children
      // Definitions are inline through webpack loader already
      // additional* is not used here and more quirky to implement
      ['additionalItems', 'additionalProperties', 'contains', 'propertyNames', 'not'] /* 'if', 'then', 'else', 'propertyNames', 'contains', 'definitions' */
        .filter(key => schema.hasOwnProperty(key))
        .forEach(key => {
          const childId = {
            $key:    `${id.$key}/${key}`,
            $path:   `${id.$path}+`,
            $parent: [schema, schema, key]
          };
          Object.assign(schemas, this.flattenSchema(schema[key], childId));
          schema[key] = childId.$key;
        });
      // simple array of subschemas
      ['allOf', 'anyOf', 'oneOf']
        .filter(key => schema.hasOwnProperty(key))
        .forEach(key =>
          schema[key].forEach((subschema, idx) => {
            const childId = {
              $key:    `${id.$key}/${key}/${idx}`,
              $path:   `${id.$path}(${idx})?`,
              $parent: [schema, schema[key], idx]
            };
            Object.assign(schemas, this.flattenSchema(subschema, childId));
            schema[key][idx] = childId.$key;
          })
        );
      return schemas;
    },
    shrinkSchemas(schemas) {
      const removableWith = schema => {
              if(schema.anyOf && schema.anyOf.indexOf(true) >= 0) {
                return [schema, true];
              }
              if(schema.allOf && schema.allOf.indexOf(false) >= 0) {
                return [schema, false];
              }
              if(schema.not === true) {
                return [schema, false];
              }
              if(Object.keys(schema).filter(key => (key[0] !== '$')).length === 0) {
                return [schema, true];
              };
              return null;
            },
            replace = replacement => {
              if(!replacement) {
                return;
              }
              const [schema, rpl] = replacement;
              schema.$parent[1][schema.$parent[2]] = rpl;
              delete schemas[schema.$key];
              replace(removableWith(schema.$parent[0]));
            };
      Object.values(schemas).map(removableWith).filter(s => s).forEach(replace);
      return schemas;
    }
  },
  computed: {
    schemas() {
      if(this.schema === null) {
        return null;
      }
      return this.shrinkSchemas(this.flattenSchema(cloneDeep(this.schema)));
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
    card, prism, schemaToc, schemaLink
  }
};
</script>
