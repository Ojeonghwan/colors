<template>
  <button class="customButton" v-on="$listeners" v-bind="$attrs">{{ btnText }}</button>
</template>

<script>
export default {
  props: {
    btnText: String,
  },
};
</script>

<style scoped>
button {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;

  margin: 0;
  padding: 0.5rem 1rem;

  /* font-family: "Pretendard"; */
  text-align: center;
  text-decoration: none;

  display: inline-block;
  width: auto;

  background-color: #ffffff;
  border: 2px solid #d0d1ff;
  border-radius: 10px;

  filter: drop-shadow(0px 4px 4px rgba(0, 0, 0, 0.25));

  cursor: pointer;

  transition: 0.2s;

  color: #6667ab;
}

button:hover {
  background-color: #d0d1ff;
}
</style>
