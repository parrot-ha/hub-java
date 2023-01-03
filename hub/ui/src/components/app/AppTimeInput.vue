<template>
  <v-text-field
    type="time"
    :label="body.title"
    :id="body.name"
    :name="body.name"
    :value="dateTimeToTime(value)"
    v-on:input="$emit('input', timeToDateTime($event))"
  ></v-text-field>
</template>

<script>
export default {
  name: 'AppTimeInput',
  props: ['value', 'body'],
  data() {
    return {};
  },
  methods: {
    dateTimeToTime: function(dateTime) {
      if (dateTime == null) {
        return null;
      }

      if (dateTime.length > 5) {
        return dateTime.substring(11, 16);
      } else {
        return dateTime;
      }
    },
    timeToDateTime: function(time) {
      if (time == null) {
        return time;
      } else if (time.length == 0) {
        return time;
      } else {
        return '1970-01-01T' + time + ':00.000-00:00';
      }
    }
  }
};
</script>

<style scoped></style>
