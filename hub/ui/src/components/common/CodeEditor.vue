<template>
  <v-card height="100%" id="editorCard">
    <v-card-title>
      {{ title }}
      <v-spacer></v-spacer>
      <v-progress-circular
        v-show="savePending"
        indeterminate
        color="primary"
      ></v-progress-circular>
      <v-btn color="primary" :disabled="savePending" @click="saveCode">
        Save
      </v-btn>
      <slot></slot>
    </v-card-title>

    <v-card-text>
      <div class="editor-wrapper" :style="{ height: editorHeight }">
        <div class="ace-editor" ref="ace"></div>
      </div>
    </v-card-text>
  </v-card>
</template>
<script>
function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

import ace from 'ace-builds';
import 'ace-builds/webpack-resolver';
import 'ace-builds/src-noconflict/theme-monokai';
import 'ace-builds/src-noconflict/mode-groovy';

export default {
  name: 'CodeEditor',
  props: ['title', 'source', 'savePending', 'editorHeight'],
  emits: ['saveCodeButtonClicked'],
  data() {
    return {
      localSource: '',
      editor: null
    };
  },
  watch: {
    source(newSource) {
      this.localSource = newSource;
      this.editor.session.setValue(this.localSource);
      this.$nextTick(() => {
        this.editor.resize();
      });
    },
    editorHeight() {
      this.$nextTick(() => {
        this.editor.resize();
      });
    }
  },
  methods: {
    saveCode() {
      this.$emit('saveCodeButtonClicked', this.editor.getValue());
    }
  },
  mounted: function() {
    this.editor = ace.edit(this.$refs.ace, {
      minLines: 10,
      fontSize: 16,
      theme: this.themePath,
      mode: this.modePath,
      tabSize: 4,
      showGutter: true,
      showPrintMargin: false
    });
    this.editor.renderer.setScrollMargin(0, 5, 0, 5);
    this.editor.session.setMode('ace/mode/groovy');
    this.editor.setTheme('ace/theme/monokai');
  }
};
</script>
<style scoped>
.editor-wrapper {
  width: 100%;
  display: inline-block;
  position: relative;
}
.ace-editor {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
}
</style>
