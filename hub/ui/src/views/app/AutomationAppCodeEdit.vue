<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col">
        <div
          v-if="alertMessage"
          ref="alertBox"
          class="alert alert-danger alert-dismissible"
          role="alert"
        >
          {{ alertMessage }}
          <button
            type="button"
            class="btn-close"
            aria-label="Close"
            @click="alertMessage = null"
          />
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <code-editor
          :source="automationApp.sourceCode"
          title="Edit"
          :save-pending="updatePending"
          :editor-height="editorHeight"
          @save-code-button-clicked="saveCode"
        >
          <router-link
            class="btn btn-outline-primary"
            outlined
            :to="{
              name: 'AutomationAppSettings',
              params: { id: saId },
            }"
            mx-2
          >
            App Settings
          </router-link>
          <are-you-sure-dialog
            title="Are you sure?"
            body="Are you sure you want to delete this automation app code?"
            confirm-button="Delete"
            :button-disabled="updatePending"
            @confirm-action="deleteCode"
          />
        </code-editor>
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

import CodeEditor from "@/components/common/CodeEditor.vue";
import _debounce from "lodash/debounce";
import AreYouSureDialog from "@/components/common/AreYouSureDialog.vue";

export default {
  name: "AutomationAppCodeEdit",
  components: {
    CodeEditor,
    AreYouSureDialog,
  },
  data() {
    return {
      updatePending: false,
      alertMessage: null,
      saId: "0",
      automationApp: { sourceCode: "" },
      editorHeight: "500px",
    };
  },
  watch: {
    alertMessage() {
      this.debouncedResizeEditor();
    },
  },
  created() {
    window.addEventListener("resize", this.onResize);
    this.debouncedResizeEditor = _debounce(this.resizeEditor, 500);
  },
  unmounted() {
    window.removeEventListener("resize", this.onResize);
  },
  mounted: function () {
    this.resizeEditor();

    this.saId = this.$route.params.id;

    fetch(`/api/automation-apps/${this.saId}/source`)
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.automationApp = data;
        }
      });

    this.$nextTick(() => {
      this.debouncedResizeEditor();
    });
  },
  methods: {
    saveCode(updatedCode) {
      this.updatePending = true;
      this.alertMessage = null;
      this.automationApp.sourceCode = updatedCode;

      fetch(`/api/automation-apps/${this.saId}/source`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ sourceCode: updatedCode }),
      })
        .then(handleErrors)
        .then((response) => {
          return response.json();
        })
        .then((data) => {
          this.updatePending = false;
          if (!data.success) {
            this.alertMessage = data.message;
          }
        })
        .catch((error) => {
          this.updatePending = false;
          console.log(error);
        });
    },
    deleteCode() {
      this.updatePending = true;
      this.alertMessage = null;

      fetch(`/api/automation-apps/${this.saId}`, {
        method: "DELETE",
      })
        .then(handleErrors)
        .then((response) => {
          return response.json();
        })
        .then((data) => {
          this.updatePending = false;
          if (!data.success) {
            this.alertMessage = data.message;
          } else {
            this.$router.push("/aa-code");
          }
        })
        .catch((error) => {
          this.updatePending = false;
          console.log(error);
        });
    },
    onResize() {
      this.debouncedResizeEditor();
    },
    resizeEditor() {
      this.editorHeight = `${
        window.innerHeight -
        (this.editorHeightAdjustment =
          (this.$refs.alertBox?.clientHeight
            ? this.$refs.alertBox.clientHeight + 20
            : 0) + 153)
      }px`;
    },
  },
};
</script>
<style scoped></style>
