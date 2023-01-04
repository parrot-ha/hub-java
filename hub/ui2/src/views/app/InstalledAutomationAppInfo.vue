<template>
  <div class="container-fluid">
    <div class="row gy-3">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Information</h5>
            <div class="card-text">
              <form>
                <div class="mb-3">
                  <label for="idInput" class="form-label">ID</label>
                  <input
                    type="text"
                    id="idInput"
                    class="form-control"
                    v-model="installedAutomationApp.id"
                    readonly
                  />
                </div>
                <div class="mb-3">
                  <label for="labelInput" class="form-label">Label</label>
                  <input
                    type="text"
                    id="labelInput"
                    class="form-control"
                    v-model="installedAutomationApp.label"
                  />
                </div>
              </form>
            </div>

            <button
              type="button"
              class="btn btn-primary"
              @click="saveInstalledAutomationApp"
            >
              Save
            </button>
          </div>
        </div>
      </div>
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Settings</h5>
            <div class="card-text">
              <table class="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Value</th>
                    <th>Type</th>
                    <th>Multiple</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="setting in installedAutomationApp.settings"
                    :key="setting.id"
                  >
                    <td>{{ setting.name }}</td>
                    <td>{{ setting.value }}</td>
                    <td>{{ setting.type }}</td>
                    <td>{{ setting.multiple }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <v-card-actions></v-card-actions>
          </div>
        </div>
      </div>
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Scheduled Jobs</h5>
            <div class="card-text">
              <table class="table">
                <thead>
                  <tr>
                    <th>Handler Method</th>
                    <th>Schedule</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(schedule, i) in schedules" :key="i">
                    <td>{{ schedule.handlerMethod }}</td>
                    <td>{{ schedule.schedule }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <v-card-actions></v-card-actions>
          </div>
        </div>
      </div>
      <div class="col-12">
        <div class="row g-3">
          <div class="col-auto me-auto">
            <button
              type="button"
              class="btn btn-primary"
              @click="runUpdatedMethod"
            >
              Run Updated Method
            </button>
          </div>
          <div class="col-auto">
            <button
              type="button"
              class="btn btn-danger"
              data-bs-toggle="modal"
              data-bs-target="#uninstallModal"
            >
              Uninstall
            </button>
          </div>
        </div>
        <div
          class="modal fade"
          id="uninstallModal"
          tabindex="-1"
          aria-labelledby="uninstallModalLabel"
          aria-hidden="true"
        >
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h1 class="modal-title fs-5" id="uninstallModalLabel">
                  Are you sure?
                </h1>
                <button
                  type="button"
                  class="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="Close"
                ></button>
              </div>
              <div class="modal-body">
                Are you sure you want to uninstall this Automation App?
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
                  @click="iaaUninstallDialog"
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        </div>
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

export default {
  name: "InstalledAutomationAppInfo",
  data() {
    return {
      iaaId: "",
      installedAutomationApp: {},
      iaaUninstallDialog: false,
      schedules: {},
    };
  },
  methods: {
    saveInstalledAutomationApp: function () {
      var body = this.installedAutomationApp;
      fetch(`/api/iaas/${this.iaaId}`, {
        method: "PUT",
        body: JSON.stringify(body),
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
            console.log("success");
          } else {
            console.log("problem saving installed automation app");
          }
        });
    },
    runUpdatedMethod: function () {
      var url = `/api/iaas/${this.iaaId}/methods/updated`;
      fetch(url, {
        method: "POST",
        body: null,
      })
        .then(handleErrors)
        .then((response) => {});
    },
    uninstallClick: function () {
      fetch(`/api/iaas/${this.iaaId}`, {
        method: "DELETE",
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
            this.$router.push("/iaas");
          } else {
            console.log("problem deleting automation app");
          }
        });
    },
  },
  mounted: function () {
    this.iaaId = this.$route.params.id;

    fetch(`/api/iaas/${this.iaaId}`)
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.installedAutomationApp = data;
        }
      });

    fetch(`/api/iaas/${this.iaaId}/schedules`)
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.schedules = data;
        }
      });
  },
};
</script>
<style scoped></style>
