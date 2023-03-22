<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col">
        <div
          ref="alertBox"
          class="alert alert-danger alert-dismissible"
          role="alert"
          v-if="alertMessage"
        >
          {{ alertMessage }}
          <button
            type="button"
            class="btn-close"
            aria-label="Close"
            @click="alertMessage = null"
          ></button>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <code-editor
          :source="deviceHandler.sourceCode"
          title="Edit"
          buttonName="Save"
          :savePending="updatePending"
          :editorHeight="editorHeight"
          @saveCodeButtonClicked="saveCode"
        >
          <are-you-sure-dialog
            title="Are you sure?"
            body="Are you sure you want to delete this device handler?"
            confirm-button="Delete"
            :buttonDisabled="updatePending"
            @confirm-action="deleteCode"
          ></are-you-sure-dialog>
        </code-editor>
      </div>
    </div>
  </div>
</template>
<script>
import CodeEditor from "@/components/common/CodeEditor.vue";
import _debounce from "lodash/debounce";
import AreYouSureDialog from "@/components/common/AreYouSureDialog.vue";

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
    AreYouSureDialog,
  },
  data() {
    return {
      updatePending: false,
      alertMessage: null,
      dhId: "",
      deviceHandler: { sourceCode: "" },
      editorHeight: "500px",
    };
  },
  watch: {
    alertMessage() {
      this.debouncedResizeEditor();
    },
  },
  methods: {
    saveCode(updatedCode) {
      this.updatePending = true;
      this.alertMessage = null;
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

      fetch(`/api/device-handlers/${this.dhId}`, {
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
            this.$router.push("/dh-code");
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
  created() {
    window.addEventListener("resize", this.onResize);
    this.debouncedResizeEditor = _debounce(this.resizeEditor, 250);
  },
  unmounted() {
    window.removeEventListener("resize", this.onResize);
  },
  mounted: function () {
    this.resizeEditor();
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
