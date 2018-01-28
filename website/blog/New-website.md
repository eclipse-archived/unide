---
title: Updated Website
date: 2018-01-26 00:00:00
tags: "media"
---
It turned out that even with multiple reviews, it is possible to have small mistakes in the PPMP documentation. In order to avoid such contraditions between diagrams, specification, json-schema and eventual implementation, we decided to rework the project structure and fixed [the JSON Schemas](https://github.com/eclipse/unide/issues/21). The corresponding uml diagrams are generated with [plantuml](http://plantuml.com/). For even better understandibility, the attributes are sorted lexicographically.

Although the website has the same look, it is completely reworked as a [single-page application](https://en.wikipedia.org/wiki/Single-page_application) to generate the specification directly from the json-schema. Instead of using [hexo](https://hexo.io/) it is now based on [nuxt.js](https://nuxtjs.org/). [Vue.js](https://vuejs.org/) as a basis for nuxt is also used in the [new PPMP client application called binsa](https://github.com/eclipse/unide/tree/master/clients/binsa).


