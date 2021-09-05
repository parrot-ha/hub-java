<template>
  <v-container fluid class="fill-height">
    <v-layout>
      <v-row>
        <v-col :cols="12">
          <v-alert
            v-model="alert"
            close-text="Close Alert"
            outlined
            type="error"
            dismissible
          >
            {{ alertMessage }}
          </v-alert>
          <v-card height="100%" id="editorCard">
            <v-card-title>
              Add
              <v-spacer></v-spacer>
              <v-btn color="primary" @click="saveCode">
                Save
              </v-btn>
            </v-card-title>
            <v-card-text
              ><AceEditor
                v-model="deviceHandler.sourceCode"
                @init="editorInit"
                lang="groovy"
                theme="monokai"
                width="100%"
                :height="editorHeight"
                :options="{
                  enableBasicAutocompletion: true,
                  enableLiveAutocompletion: true,
                  fontSize: 14,
                  highlightActiveLine: true,
                  enableSnippets: true,
                  showLineNumbers: true,
                  tabSize: 2,
                  showPrintMargin: false,
                  showGutter: true
                }"
                :commands="[
                  {
                    name: 'save',
                    bindKey: { win: 'Ctrl-s', mac: 'Command-s' },
                    exec: saveCode,
                    readOnly: true
                  }
                ]"
              />
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-layout>
  </v-container>
</template>
<script>
function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

import AceEditor from 'vuejs-ace-editor';
import _debounce from 'lodash/debounce';

export default {
  name: 'DeviceHandlerCodeAdd',
  components: {
    AceEditor
  },
  data() {
    return {
      editor: null,
      editorHeight: '500px',
      alert: false,
      alertMessage: '',
      version: '',
      deviceHandler: { sourceCode: '' }
    };
  },
  methods: {
    saveCode() {
      this.alert = false;
      this.alertMessage = '';

      fetch(`/api/device-handlers/source`, {
        method: 'POST',
        body: JSON.stringify(this.deviceHandler)
      })
        .then(handleErrors)
        .then(response => {
          return response.json();
        })
        .then(data => {
          if (!data.success) {
            this.alertMessage = data.message;
            this.alert = true;
          } else {
            this.$router.push(`/dh-code/${data.dhId}/edit`);
          }
        })
        .catch(error => {
          console.log(error);
        });
    },
    editorInit: function(editor) {
      this.editor = editor;
      require('brace/ext/language_tools'); //language extension prerequsite...
      require('brace/mode/groovy'); //language
      require('brace/theme/monokai');
      require('brace/theme/textmate');
      require('brace/snippets/groovy'); //snippet
      require('brace/ext/searchbox'); // search box
    },
    onResize(e) {
      this.debouncedResizeEditor();
    },
    resizeEditor() {
      this.editorHeight = `${window.innerHeight - 180}px`;
      this.editor.resize();
    }
  },
  created() {
    window.addEventListener('resize', this.onResize);
    this.debouncedResizeEditor = _debounce(this.resizeEditor, 500);
  },
  destroyed() {
    window.removeEventListener('resize', this.onResize);
  },
  mounted: function() {
    this.$nextTick(() => {
      this.resizeEditor();
    });
  }
};
</script>
<style scoped></style>
