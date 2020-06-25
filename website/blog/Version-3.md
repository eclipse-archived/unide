---
title: Protocol Version 3 release candidate
date: 2019-03-01 00:00:00
tags: "v3"
---

After a long discussion phase, the Production Performance Management Protocol version 3 is finally linked on the specification page. Special thanks to [bgusach](https://github.com/bgusach), [bf-bryants](https://github.com/bf-bryants), [muelsen](https://github.com/muelsen) and [alaendle](https://github.com/alaendle) for their contributions [via github issues](https://github.com/eclipse/unide/issues)!
# The most important changes
* `context` section in `measurements`\
  The optional context section ccontains information on how to interpret the measurements. This includes
  * the field '`type`' (Number, String or Boolean value) indicates which format the `<values>` of a `series` have.
  * The `unit` key can be used to describe the unit of measurement. There are different understandings and standarizations for units ('C' stands for Coulomb, not Celsius), like [International System of Units](https://en.wikipedia.org/wiki/International_System_of_Units), [SenML](https://tools.ietf.org/html/draft-ietf-core-senml-14#section-12.1), [ISO 80000-1:2009](https://www.iso.org/obp/ui/#iso:std:iso:80000:-1:ed-1:v1:en), etc.. The `unit` key here is a string without further restriction. If that is needed, it can be specified via an URI in `namespace`.
  * `namespace` may contain an URI to identify further detail on the values in this measurement section. This can be a description of units, lengths or naming conventions for measurement points, but also a reference to a semantic model. 
* `series` can contain other than numeric measurements\
  if `context.type` contains any of 'BASE64', 'BOOLEAN', 'NUMBER', 'STRING', 'REF' or 'OTHER', the corresponding measurement in `series` are represented in this type ('REF', 'OTHER' and 'BASE64' are Strings). This way for example, even small base64 encoded binary data, as send from iolink devices, can be included. 'REF' could be used in multipart messages to reference to other sections or even external content.
* `mode` and `state` are introduced in `device` and replaces v2's `operationalMode`, which was found to not clearly diffentiate the functional mode and technical state.
* A common `id` was used and replaces different spellings in `deviceID`, `partID`, `program.id`.
* `additionalData` in the various sections is used for any kind of not-specified data, that should be included in the telegram. Where v2's `metaData` had to be key/String pairs, `additionalData` can also be complex JSON objects.
* `time` field replaces the v2 `$_time` field, which is difficult to represent in some programming languages. Note that `time` is not an Integer anymore but a JSON number, to allow sub-millisecond values.
* no more `shutoffValues` and `shutoffPhase`\
  `shutoffValues` can be expressed as specialValues with a meaningful `name`, `shutoffPhase` is the measurement with the latest timestamp `ts`.
* a common definition.json schema is used for sections that are used in multiple telegrams. With the help of JSON schema 'anyOf', sections inherit and can extend from these base definitions. 

# Next steps
The most recent updates and this post should finalize the v3 schema. Vetos with suggestions that find a consensus fairly soon after posting are stil accepted [via github issues](https://github.com/eclipse/unide/issues). Major changes and suggestions should rather be addressed to a v4.

The bindings and server should be updated to accept and validate v3 as well. After updating further dependencies, creating eclipse CQs and undergoing the release process, everything should be wrapped up in the unide release 0.3.0.