---
title: Protocol v3 final
date: 2019-11-26 00:00:00
tags: "v3"
---

Version 3 of the Production Performance Management Protocol (Production Performance Management Protocol) has been finalized!
Thanks to CONTACT Software, Balluff GmbH, Bosch Plant Blaichach and Bosch Connected Industry for their proposals, ideas and patience.\
The major improvements in Version 3 include consistent structure and naming, context for measurements and better extensibility.

# Easy validation
Did you know that you can easily validate your payload against the published online version of Production Performance Management Protocol? For example, [jsonschemavalidator.net](https://www.jsonschemavalidator.net/) provides an online validation frontend. Just use your payload as JSON input and the respective github link to the Production Performance Management Protocol specification as a `$ref` in the schema. For example:
```json
{
  "$ref": "https://raw.githubusercontent.com/eclipse/unide/master/ppmp/ppmp-schema/src/main/resources/org/eclipse/iot/unide/ppmp/v3/process_schema.json"
}
```

# Changes
## General
* `content-spec` is now "urn:spec://eclipse.org/unide/< type >-message#**v3**"
* `mode` and `state` are introduced in `device` and replace v2's `operationalMode`, which was found to not clearly differentiate the functional mode and technical state.
* A common `id` is used and replaces different spellings in `deviceID`, `partID`, `program.id`.
* `additionalData` in the various sections is used for any kind of not-specified data, that should be honored by the receiving party as context information. Where v2's `metaData` had to be key/String pairs, `additionalData` can also be complex JSON objects.
* In addition to `additionalData`, it is possible to include completely undefined keys in most places, in order to introduce custom or future keys. With it, it's e.g. possible to define and test currently meaningless 'window' key as an extension to limits.
* A common definition.json schema is used for sections that are used in multiple telegrams. With the help of JSON schema 'anyOf', sections inherit and can extend from these base definitions.

## `messages`
* Messages can have a `state` to indicate if this alert newly appeared (`NEW`) or disappeared (`ENDED`). Together with a common `code`, it is now possible e.g. to correlate events like "*pressure too high*" and "*pressure not too high anymore*".
* What was before `type` has now been renamed to `source` to indicate if the message comes from the `DEVICE` itself or from the general infrastructure `TECHNICAL_INFO`.
* The `type` in v3 indicates the debug level of the message (`INFO`, `WARNING`, `ERROR`, `UNKNOWN`) and complements `severity` for describing urgency and relevance of a message.

## `measurements`
* The optional `context` section contains information on how to interpret the measurements. This includes
  * The field `type` (Number, String or Boolean value) indicates which format the <values> of a series have.
  * The `unit` key can be used to describe the unit of measurement. There are different understandings and standardizations for units ('C' stands for Coulomb, not Celsius), like [International System of Units](https://en.wikipedia.org/wiki/International_System_of_Units), [SenML](https://tools.ietf.org/html/draft-ietf-core-senml-14#section-12.1), [ISO 80000-1:2009](https://www.iso.org/obp/ui/#iso:std:iso:80000:-1:ed-1:v1:en), etc.. The `unit` key here is a string without further restriction. If that is needed, it can be specified via an URI in `namespace`.
  * `namespace` may contain an URI to identify further detail on the values in this measurement section. This can be a description of units, lengths or naming conventions for measurement points, but also a reference to a semantic model.
  * Each entry in `limits` can be constant or an array of values. If constant, it applies to all corresponding measurements in `series`. If an array of the same length than the corresponding `series` is provided, each entry limits the measurement at the respective position. 'null' can be used to skip a limit for an individual measurement.
* `series` can contain other than numeric measurements if `context.type` contains any of 'BASE64', 'BOOLEAN', 'NUMBER', 'STRING', 'REF' or 'OTHER', the corresponding measurement in series are represented in this type ('REF', 'OTHER' and 'BASE64' are Strings). This way for example, even small base64 encoded binary data, as send from iolink devices, can be included. 'REF' could be used in multipart messages to reference to other sections or even external content.
* `time` field replaces the v2 `$_time` field, which is difficult to represent in some programming languages. Note that time is not an Integer anymore but a JSON number, to allow sub-millisecond values.

## `processes`
* No more `shutoffValues` and `shutoffPhase`\
`shutoffValues` can be expressed as `specialValues` with a meaningful name, `shutoffPhase` is the measurement with the latest timestamp `ts`.
* Same improvements in measurements
