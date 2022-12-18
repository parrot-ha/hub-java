<template>
  <v-container fluid class="fill-height">
    <v-row>
      <v-col>
        <div ref="alertBox">
          <v-alert
            v-model="alert"
            close-text="Close Alert"
            outlined
            type="error"
            dismissible
            @input="debouncedResizeEditor"
          >
            {{ alertMessage }}
          </v-alert>
        </div>
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <code-editor
          :source="automationApp.sourceCode"
          title="Add"
          :savePending="savePending"
          :editorHeight="editorHeight"
          @saveCodeButtonClicked="saveCode"
        ></code-editor>
      </v-col>
    </v-row>
  </v-container>
</template>
<script>
function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

import CodeEditor from '@/components/common/CodeEditor';
import _debounce from 'lodash/debounce';

export default {
  name: 'AutomationAppCodeAdd',
  components: {
    CodeEditor
  },
  data() {
    return {
      savePending: false,
      alert: false,
      alertMessage: '',
      automationApp: { sourceCode: '' },
      editorHeight: '500px'
    };
  },
  watch: {
    alert() {
      this.debouncedResizeEditor();
    }
  },
  methods: {
    saveCode(updatedCode) {
      this.savePending = true;
      this.alert = false;
      this.alertMessage = '';
      this.automationApp.sourceCode = updatedCode;

      fetch(`/api/automation-apps/source`, {
        method: 'POST',
        body: JSON.stringify(this.automationApp)
      })
        .then(handleErrors)
        .then(response => {
          return response.json();
        })
        .then(data => {
          this.savePending = false;
          if (!data.success) {
            this.alertMessage = data.message;
            this.alert = true;
          } else {
            this.$router.push(`/aa-code/${data.aaId}/edit`);
          }
        })
        .catch(error => {
          this.savePending = false;
          console.log(error);
        });
    },
    onResize() {
      this.debouncedResizeEditor();
    },
    resizeEditor() {
      this.editorHeight = `${window.innerHeight -
        (this.$refs.alertBox.clientHeight + 202)}px`;
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
      this.debouncedResizeEditor();
    });
  }
};
</script>
<style scoped></style>
