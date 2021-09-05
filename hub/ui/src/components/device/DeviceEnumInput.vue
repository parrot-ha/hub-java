<template>
  <div>
    <v-select
      :label="body.title"
      :id="body.name"
      :name="body.name"
      :hint="body.description"
      :items="itemList"
      :value="value"
      item-value="key"
      item-text="text"
      v-on:change="$emit('input', $event)"
      :multiple="body.multiple"
      persistent-hint
    ></v-select>
  </div>
</template>

<script>
export default {
  name: 'DeviceEnumInput',
  props: ['value', 'options', 'body'],
  data() {
    return {};
  },
  computed: {
    itemList: function() {
      var tempItemList = [];
      if (Array.isArray(this.options)) {
        this.options.forEach(element =>
          tempItemList.push({ key: element, text: element })
        );
      } else {
        for (const key in this.options) {
          tempItemList.push({ key: key, text: this.options[key] });
        }
      }
      return tempItemList;
    }
  },
  methods: {}
};
</script>

<style scoped></style>
