<template>
  <div class="mb-3">
    <label class="form-label">{{ body.title }}</label>
    <input
      class="form-control"
      type="time"
      :value="dateTimeToTime(modelValue)"
      @input="$emit('update:modelValue', timeToDateTime($event.target.value))"
    />
  </div>
</template>

<script>
export default {
  name: "AppTimeInput",
  props: ["modelValue", "body"],
  data() {
    return {};
  },
  methods: {
    dateTimeToTime: function (dateTime) {
      if (typeof dateTime === "undefined" || dateTime == null) {
        return null;
      }

      if (dateTime.length > 5) {
        return dateTime.substring(11, 16);
      } else {
        return dateTime;
      }
    },
    timeToDateTime: function (time) {
      if (typeof time === "undefined" || time == null) {
        return time;
      } else if (time.length == 0) {
        return time;
      } else {
        return "1970-01-01T" + time + ":00.000-00:00";
      }
    },
  },
};
</script>
