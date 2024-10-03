<template>
  <div class="container-fluid">
    <h5>Definition</h5>

    <form>
      <div class="mb-3">
        <label class="form-label">Name</label>
        <input
          v-model="automationApp.name"
          type="text"
          class="form-control"
          disabled
        >
      </div>
      <div class="mb-3">
        <label class="form-label">Namespace</label>
        <input
          v-model="automationApp.namespace"
          type="text"
          class="form-control"
          disabled
        >
      </div>
    </form>

    <div class="col-12">
      <h5>Settings</h5>
      Add setting definitions in the source code and then set the values here
    </div>

    <br>
    <h5 class="card-title">
      OAuth
    </h5>

    <div v-if="!automationApp.oAuthEnabled">
      <br>
      <button
        class="btn btn-primary"
        outlined
        mx-2
        @click="automationApp.oAuthEnabled = true"
      >
        Enable OAuth in Automation App
      </button>
    </div>
    <div v-else>
      <div class="mb-3">
        <label class="form-label">Client ID</label>
        <input
          v-model="automationApp.oAuthClientId"
          type="text"
          class="form-control"
          placeholder="Public client ID for accessing this AutomationApp via its REST API"
          readonly
        >
      </div>
      <div class="mb-3">
        <label class="form-label">Client Secret</label>
        <input
          v-model="automationApp.oAuthClientSecret"
          type="text"
          class="form-control"
          placeholder="Confidential secret key for accessing this AutomationApp via its REST API"
          readonly
        >
      </div>
    </div>
    <br>
    <button
      class="btn btn-primary"
      @click="updateAutomationApp"
    >
      Update
    </button>
  </div>
</template>
<script>
export default {
  name: "AutomationAppSettings",
  data() {
    return {
      saId: "",
      automationApp: { oAuthEnabled: false },
    };
  },

  mounted: function () {
    this.saId = this.$route.params.id;

    fetch(`/api/automation-apps/${this.saId}`)
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.automationApp = data;
        }
      });
  },
  methods: {
    updateAutomationApp: function () {
      var body = this.automationApp;
      fetch(`/api/automation-apps/${this.saId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
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
};
</script>
<style scoped></style>
