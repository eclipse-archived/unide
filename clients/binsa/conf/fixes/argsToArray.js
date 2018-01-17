// dropin es6 replacement for argsarray module

export default fn => function(...args) {
  return fn.call(this, args);
};
