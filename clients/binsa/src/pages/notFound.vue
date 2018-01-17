<template>
  <div class="notFound">
    <div class="fa fa-frown-o spin" aria-hidden="true"></div>
    <div>{{ $t('notFound', {path: $route.path} ) }}</div>
  </div>
</template>

<style lang="scss">
.notFound {
  text-align: center;
  div.fa {
    font-size: 14em;
    vertical-align: middle;
    animation: spin 1s infinite linear;
    animation-direction: alternate;
    animation-timing-function: ease-in-out;
  }
  div:not(.fa) {
    font-weight: bold;
    font-size: 2em;
  }
  @keyframes spin {
    0%   { transform: rotate(-30deg);}
    50%  {
      color: red;
      font-size: 15em;
    }
    100% {transform: rotate(30deg);}
  }
}
</style>
