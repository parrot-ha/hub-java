<template>
  <v-switch
    :label="body.title"
    :id="body.name"
    :name="body.name"
    :input-value="value"
    v-on:change="$emit('input', $event)"
  ></v-switch>
</template>

<script>
export default {
  name: "AppBoolInput",
  props: ["value", "body"],
  data() {
    return {};
  },
  methods: {},
};
</script>

<style scoped></style>
