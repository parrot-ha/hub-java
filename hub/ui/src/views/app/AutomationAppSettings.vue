<template>
  <div class="container-fluid">
    <h5>Definition</h5>

    <form>
      <div class="mb-3">
        <label class="form-label">Name</label>
        <input
          type="text"
          class="form-control"
          v-model="automationApp.name"
          disabled
        />
      </div>
      <div class="mb-3">
        <label class="form-label">Namespace</label>
        <input
          type="text"
          class="form-control"
          v-model="automationApp.namespace"
          disabled
        />
      </div>
    </form>

    <div class="col-12">
      <h5>Settings</h5>
      Add setting definitions in the source code and then set the values here
    </div>

    <br />
    <h5 class="card-title">OAuth</h5>

    <div v-if="!automationApp.oAuthEnabled">
      <br />
      <button
        class="btn btn-primary"
        outlined
        @click="automationApp.oAuthEnabled = true"
        mx-2
      >
        Enable OAuth in Automation App
      </button>
    </div>
    <div v-else>
      <div class="mb-3">
        <label class="form-label">Client ID</label>
        <input
          type="text"
          class="form-control"
          v-model="automationApp.oAuthClientId"
          placeholder="Public client ID for accessing this SmartApp via its REST API"
          readonly
        />
      </div>
      <div class="mb-3">
        <label class="form-label">Client Secret</label>
        <input
          type="text"
          class="form-control"
          v-model="automationApp.oAuthClientSecret"
          placeholder="Confidential secret key for accessing this SmartApp via its REST API"
          readonly
        />
      </div>
    </div>
    <br />
    <button class="btn btn-primary" @click="updateAutomationApp">Update</button>
  </div>
</template>
<script>
export default {
  name: "AutomationAppSettings",
  data() {
    return {
      aaId: "",
      automationApp: { oAuthEnabled: false },
    };
  },
  methods: {
    updateAutomationApp: function () {
      var body = this.automationApp;
      fetch(`/api/automation-apps/${this.aaId}`, {
        method: "PUT",
        body: JSON.stringify(body),
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
            console.log("success");
          } else {
            console.log("problem saving automation app");
          }
        });
    },
  },

  mounted: function () {
    this.aaId = this.$route.params.id;

    fetch(`/api/automation-apps/${this.aaId}`)
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.automationApp = data;
        }
      });
  },
};
</script>
<style scoped></style>
