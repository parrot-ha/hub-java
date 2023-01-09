<template>
  <div class="form-check form-switch mb-3">
    <br />
    <input
      class="form-check-input"
      type="checkbox"
      role="switch"
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
    />
    <label class="form-check-label">{{ body.title }}</label>
  </div>
</template>

<script>
export default {
  name: "AppBoolInput",
  props: ["modelValue", "body"],
  data() {
    return {};
  },
  methods: {},
};
</script>

<style scoped></style>
