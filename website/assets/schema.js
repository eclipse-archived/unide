import cloneDeep from 'lodash/clonedeep';
import cloneDeepWith from 'lodash/clonedeepWith';
import omit from 'lodash/omit';

/*
schemas are enriched with:
  $step: a step of a (pseudo) json path,
  $parent: an object for backreference, consisting of
    0: actual parent object
    1: schema hiearchy parent
    2: key to the hierarchy parent
  $id: running counter for unique reference

*/

class Schema {
  constructor(step, parent, original) {
    this.$id = Schema.idx++;
    this.$step = step;
    this.$parent = parent;
    if(original) {
      this.parseFrom(original);
    }
  }
  toJSON(obj = this) {
    return cloneDeepWith(obj, (value, key, original, stack) => {
      if(value instanceof Schema) {
        return Object.entries(value).filter(([k, v]) => ['$id', '$step', '$parent'].indexOf(k) < 0).reduce((l, [k, v]) => {
          l[k] = this.toJSON(v);
          return l;
        }, {});
      }
    });
  }
  getPath() {
    let path = '';
    if(this.$parent) {
      path += this.$parent.schema.getPath();
      if(this.$parent.schema.type === 'object') {
        path += '.';
      }
    }
    path += this.$step;
    this.getPath = () => path;
    return path;
  }
  parseFrom(schema) {
    // http://json-schema.org/latest/json-schema-core.html#rfc.section.4.3.1
    if(schema === true) {
      schema = {};
    } else if(schema === false) {
      schema = {
        not: true
      };
    }
    Object.assign(this, omit(cloneDeep(schema), [
      '$step', '$id', '$parent',
      'items', 'properties', 'patternProperties', /* "dependencies", */
      'additionalItems', 'additionalProperties',
      'contains', 'propertyNames', 'not',
      /* "if", "then", "else", "propertyNames", "contains", "definitions", */
      'allOf', 'anyOf', 'oneOf']));

    if(schema.items) {
      if(schema.items instanceof Array) {
        this.items = schema.items.map((item, idx) =>
          (new Schema(
            `[${idx}]`,
            { schema: this, path: `items[${idx}]` }))
            .parseFrom(schema.items[idx])
        );
      } else {
        this.items = (new Schema(
          '[*]',
          { schema: this, path: 'items' }))
          .parseFrom(schema.items);
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
            this[key] = this[key] || {};
            this[key][subkey] = (new Schema(
              `${key === 'patternProperties' ? '&lt;field&gt;' : subkey}`,
              { schema: this, path: `${key}["${subkey}"]` }))
              .parseFrom(subschema);
          })
      );
    // simple direct subschemas
    // the excluded keywords are for validation but don't create new children
    // Definitions are inline through webpack loader already
    // additional* is not used here and more quirky to implement
    [
      'additionalItems',
      'additionalProperties',
      'contains',
      'propertyNames',
      'not'
    ] /* 'if', 'then', 'else', 'propertyNames', 'contains', 'definitions' */
      .filter(key => schema.hasOwnProperty(key))
      .forEach(path => {
        this[path] = (new Schema(
          '+',
          { schema: this, path }))
          .parseFrom(schema[path]);
      });
    ['allOf', 'anyOf', 'oneOf']
      .filter(key => schema.hasOwnProperty(key))
      .forEach(key =>
        schema[key].forEach((subschema, idx) => {
          this[key] = this[key] || [];
          this[key].push((new Schema(
            `(${idx})${(key === 'anyOf') ? '?' : '+'}`,
            { schema: this, path: `${key}[${idx}]` }))
            .parseFrom(subschema));
        })
      );
    return this;
  }
}
Schema.idx = 0;

export default Schema;
