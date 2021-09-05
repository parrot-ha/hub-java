<template>
  <div class="d-flex flex-no-wrap my-3">
    <v-avatar v-if="body.image" size="75" tile>
      <v-img :src="body.image"></v-img>
    </v-avatar>
    <v-card class="app-paragraph-element" outlined>
      <v-card-title>{{ body.title }}</v-card-title>
      <v-card-text style="white-space: pre-wrap;">
        {{ body.description }}
      </v-card-text>
    </v-card>
  </div>
</template>

<script>
export default {
  name: 'AppParagraphElement',
  props: ['body'],
  data() {
    return {};
  },
  methods: {}
};
</script>

<style scoped>
.app-paragraph-element {
  border: 0px;
}
.v-card__text,
.v-card__title {
  padding: 0px;
}
</style>
