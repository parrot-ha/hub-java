<template>
  <v-container fluid class="fill-height">
    <div class="row">
      <div class="col">
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
      </div>
    </div>
    <div class="row">
      <div class="col">
        <code-editor
          :source="deviceHandler.sourceCode"
          title="Edit"
          :savePending="savePending"
          :editorHeight="editorHeight"
          @saveCodeButtonClicked="saveCode"
        ></code-editor>
      </div>
    </div>
  </div>
</template>
<script>
function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

import CodeEditor from "@/components/common/CodeEditor";
import _debounce from "lodash/debounce";

export default {
  name: "DeviceHandlerCodeAdd",
  components: {
    CodeEditor,
  },
  data() {
    return {
      savePending: false,
      alert: false,
      alertMessage: "",
      deviceHandler: { sourceCode: "" },
      editorHeight: "500px",
    };
  },
  watch: {
    alert() {
      this.debouncedResizeEditor();
    },
  },
  methods: {
    saveCode(updatedCode) {
      this.savePending = true;
      this.alert = false;
      this.alertMessage = "";
      this.deviceHandler.sourceCode = updatedCode;

      fetch(`/api/device-handlers/source`, {
        method: "POST",
        body: JSON.stringify(this.deviceHandler),
      })
        .then(handleErrors)
        .then((response) => {
          return response.json();
        })
        .then((data) => {
          this.savePending = false;
          if (!data.success) {
            this.alertMessage = data.message;
            this.alert = true;
          } else {
            this.$router.push(`/dh-code/${data.id}/edit`);
          }
        })
        .catch((error) => {
          this.savePending = false;
          console.log(error);
        });
    },
    onResize() {
      this.debouncedResizeEditor();
    },
    resizeEditor() {
      this.editorHeight = `${
        window.innerHeight - (this.$refs.alertBox.clientHeight + 202)
      }px`;
    },
  },
  created() {
    window.addEventListener("resize", this.onResize);
    this.debouncedResizeEditor = _debounce(this.resizeEditor, 250);
  },
  unmounted() {
    window.removeEventListener("resize", this.onResize);
  },
  mounted: function () {
    this.$nextTick(() => {
      this.resizeEditor();
    });
  },
};
</script>
<style scoped></style>
