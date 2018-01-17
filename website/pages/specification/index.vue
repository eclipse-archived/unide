<template>
  <div class="content">
    <h1>
      <a href="#Communicating-Parties" title="Communicating Parties"></a>
      Communicating Parties
    </h1>
    <p>
    Two parties are involved in a PPMP message exchange: the sender and the receiver. The sender pushes a message to the receiver. Usually the sender is a machine or a sensor of a machine.
    </p>
    <p>
    The receiver waits for messages. It offers a API that allows for sending either measurement payloads or message payloads.
    </p>
    <p>
    The communication is unidirectional. Only the sender can contact the receiver and send messages. No feedback from receiver to sender is provided.
    </p>
    <img class="is-centered" src="images/collaborators_v2.svg" alt="Overview PPMP" title="Overview PPMP" />
  </div>
</template>
