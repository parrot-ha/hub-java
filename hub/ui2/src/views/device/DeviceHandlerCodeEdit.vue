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
          buttonName="Save"
          :savePending="savePending"
          :editorHeight="editorHeight"
          @saveCodeButtonClicked="saveCode"
        ></code-editor>
      </div>
    </div>
  </div>
</template>
<script>
import CodeEditor from "@/components/common/CodeEditor";
import _debounce from "lodash/debounce";

function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

export default {
  name: "DeviceHandlerCodeEdit",
  components: {
    CodeEditor,
  },
  data() {
    return {
      savePending: false,
      alert: false,
      alertMessage: "",
      dhId: "",
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

      fetch(`/api/device-handlers/${this.dhId}/source`, {
        method: "PUT",
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
        window.innerHeight -
        (this.editorHeightAdjustment = this.$refs.alertBox.clientHeight + 202)
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
    this.dhId = this.$route.params.id;

    fetch(`/api/device-handlers/${this.dhId}/source`)
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.deviceHandler = data;
        }
      });

    this.$nextTick(() => {
      this.debouncedResizeEditor();
    });
  },
};
</script>
<style scoped></style>
