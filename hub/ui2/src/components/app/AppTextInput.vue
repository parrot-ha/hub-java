<template>
  <v-text-field
    type="text"
    :label="body.title"
    :id="body.name"
    :name="body.name"
    :value="value"
    v-on:input="$emit('input', $event)"
  ></v-text-field>
</template>

<script>
export default {
  name: "AppTextInput",
  props: ["value", "body"],
  data() {
    return {};
  },
  methods: {},
};
</script>

<style scoped></style>
