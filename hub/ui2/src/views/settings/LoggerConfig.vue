<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <div
              class="card-title d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3"
            >
              <h5>Loggers</h5>
              <div class="btn-toolbar mb-2 mb-md-0">
                <logger-edit-modal @loggerSaved="loadLoggingConfig"
                  >Add Logger</logger-edit-modal
                >
              </div>
            </div>
            <div class="card-text">
              <div>
                <table class="table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Level</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="logger in loggers" :key="logger.name">
                      <td>{{ logger.name }}</td>
                      <td>
                        <logger-edit-modal
                          @loggerSaved="loadLoggingConfig"
                          :logger-name="logger.name"
                          :logger-level="logger.level"
                          >{{ logger.level }}</logger-edit-modal
                        >
                      </td>
                    </tr>
                  </tbody>
                </table>

                <div
                  class="modal fade"
                  id="addModal"
                  tabindex="-1"
                  aria-labelledby="addModalLabel"
                  aria-hidden="true"
                >
                  <div class="modal-dialog">
                    <div class="modal-content">
                      <div class="modal-header">
                        <h1 class="modal-title fs-5" id="addModalLabel">
                          Add Logger
                        </h1>
                        <button
                          type="button"
                          class="btn-close"
                          data-bs-dismiss="modal"
                          aria-label="Close"
                        ></button>
                      </div>
                      <div class="modal-body">
                        Are you sure you want to delete this integration?
                      </div>
                      <div class="modal-footer">
                        <button
                          type="button"
                          class="btn btn-secondary"
                          data-bs-dismiss="modal"
                        >
                          Cancel
                        </button>
                        <button
                          type="button"
                          class="btn btn-danger"
                          @click="deleteIntegration"
                        >
                          Save
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import LoggerEditModal from "@/components/settings/LoggerEditModal.vue";

export default {
  name: "LoggerConfig",
  components: {
    LoggerEditModal,
  },
  data() {
    return {
      loggers: [],
    };
  },
  methods: {
    loadLoggingConfig() {
      fetch("/api/settings/logging-config")
        .then((response) => response.json())
        .then((data) => {
          if (typeof data !== "undefined" && data != null) {
            this.loggers = data.loggers;
          }
        });
    },
  },
  mounted: function () {
    this.loadLoggingConfig();
  },
};
</script>
<style scoped></style>
