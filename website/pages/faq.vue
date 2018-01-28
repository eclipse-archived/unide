<template>
  <div class="faq">
    <h1 class="title is-3">
      Frequently Asked Questions
    </h1>
    <div>
      We assembled a few frequently asked questions about the Production Performance Protocol (PPMP) and the Unide project. You're welcome to raise your questions in the <a href="https://www.eclipse.org/forums/index.php/f/348/">Unide forum</a>.
    </div>
    <div class="accordion">
      <card v-for="(faq, idx) in faqs" :key="idx" :collapsed="true">
        <template slot="header">
          {{ faq.question }}
        </template>
        <div v-html="faq.answer"></div>
      </card>
    </div>
  </div>
</template>

<script>
import card from '~/components/collapsibleCard.vue';

export default {
  layout: 'sidebar',
  data:   () => ({
    faqs: [{
      question: 'What does "Unide" stand for?',
      answer:   "The projects name is derived from understand industry devices. Other ideas like connect industry machines turned out to have meanings in foreign languages or being trademarked already.<br/>Regarding the logo: Unide and PPMP's goal is to enable you to connect machines and receive their measurements / alerts. The logo represents that. It has you ('<i>U</i>') highlighted and connects to rings. As a side note, we found that the words almost look like <i>you-nice</i>."
    }, {
      question: "What's the difference between Unide and PPMP?",
      answer:   'The Production Performance Mangement Protocol (PPMP) is the name of the structure of the <a href="specification/">payload</a> whereas Unide is the project that aims to provide sample implementations and further development of this protocol in and with the Eclipse Open Source community. Nevertheless, PPMP is an open suggestion, and everyone is welcome to using it, even without contributing to the Unide project.'
    }, {
      question: 'Why introducing another industry protocol?',
      answer:   'There are already plenty of transport protocols (REST, AMQP, etc.) available. PPMP aims to provide the very industry specific semantic structure for their payload. So it aims to complete such IoT transport infrastructure and bridge the gap to the manufacturing domain. OPC-DA/UA on the other hand covers full service oriented architecture, security concerns and information model, which makes it more complex and costly to implement and operate. In some use cases, this is not necessary.'
    }, {
      question: 'Why have you choosen JSON as syntax?',
      answer:   "JSON is a good fit regarding understandability and size. Having a structured and comprehensible basis is the first step for acceptance in the industry. In the spirit of Donald E. Knuth (\"<a href='http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.103.6084'><i>premature optimization is the root of all evil</i></a>\"), further compacting is possible in a later stage."
    }, {
      question: 'Why are time and measurement points separated in multiple arrays?',
      answer:   "We have discussed multiple options for <a href='specification'>series</a>: having an array of<br/>tupels (<code>[[0, 23.34],[...],...]</code>),<br/>plain objects (<code>[{time: 0, temperature: 23.34}, {...}...]</code>),<br/>objects with time as key (<code>{ \"0\": [23.34,...],...}</code>) and other.<br/>When parsing, we had to recognize, that unsigned long int for time and floating point measurements are of different types. Also, all former variants are more verbose due to additional brackets, commas or quotation marks. We believe that the current version is a good compromise between readability and structure."
    }, {
      question: "I'm interested in Unide/PPMP. How can I contribute?",
      answer:   "The first goal of Unide is to provide sample client/server implementations of PPMP. Secondly, we're looking forward to improving PPMP in future versions together with the eclipse community.<br/>If you want to contribute with source code, use cases or implementing it in your devices, let us know and discuss in the <a href=\"https://github.com/eclipse/unide\">unide forum</a> or by forking / filing an issue on <a href=\"https://github.com/eclipse/unide\">github</a>."
    }, {
      question: 'Why have you included content-spec uri, if that can also be expressed in the a REST url already?',
      answer:   '<a href="specification">content-spec</a> is included in the payload, because PPMP does not rely on a specific transport protocol (like REST).'
    }]
  }),
  components: {
    card
  }
};
</script>

<style lang="scss">
@import "~assets/variables.scss";

.faq {}
</style>
