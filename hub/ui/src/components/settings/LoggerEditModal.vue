<template>
  <div>
    <button
      type="button"
      class="btn btn-outline-secondary"
      @click="modal.show()"
    >
      <slot></slot>
    </button>
    <div
      class="modal fade"
      ref="loggerAddModal"
      tabindex="-1"
      aria-labelledby="loggerAddModalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h1 class="modal-title fs-5">
              {{ isEdit ? "Edit" : "Add" }}Logger
            </h1>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"
            ></button>
          </div>
          <div class="modal-body">
            <form>
              <div>
                <label class="form-label">Logger Name</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="localLoggerName"
                  :readonly="isEdit"
                />
              </div>
              <div>
                <label class="form-label">Logger Level</label>
                <select
                  class="form-select"
                  aria-label="Logger Level"
                  v-model="localLoggerLevel"
                >
                  <option v-for="ll in loggerLevels" :key="ll" :value="ll">
                    {{ ll }}
                  </option>
                </select>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              data-bs-dismiss="modal"
            >
              Cancel
            </button>
            <button type="button" class="btn btn-primary" @click="saveLogger">
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { Modal } from "bootstrap";

export default {
  name: "LoggerEditModal",
  props: ["loggerName", "loggerLevel"],
  emits: ["loggerAdded"],
  data() {
    return {
      modal: null,
      localLoggerName: null,
      localLoggerLevel: null,
      loggerLevels: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"],
      isEdit: false,
    };
  },
  methods: {
    saveLogger: function () {
      fetch("/api/settings/logging-config", {
        method: this.isEdit ? "PUT" : "POST",
        body: JSON.stringify({
          name: this.localLoggerName,
          level: this.localLoggerLevel,
        }),
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
            console.log("success");
            this.$emit("loggerSaved");
          }
          this.modal.hide();
        });
    },
  },
  mounted: function () {
    this.localLoggerName = null;
    this.localLoggerLevel = null;
    this.isEdit = false;
    if (this.loggerName != null && this.loggerLevel != null) {
      this.isEdit = true;
      this.localLoggerName = this.loggerName;
      this.localLoggerLevel = this.loggerLevel;
    }
    this.modal = new Modal(this.$refs.loggerAddModal);
  },
};
</script>
