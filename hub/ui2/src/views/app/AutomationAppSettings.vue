<template>
  <div class="container-fluid">

      <div class="row">
        <div class="col" cols="12
          <div class="card">
            <v-card-title>Definition</v-card-title>
            <div class="card-text">
              <v-form>
                <v-text-field
                  label="Name"
                  v-model="automationApp.name"
                  disabled
                ></v-text-field>
                <v-text-field
                  label="Namespace"
                  v-model="automationApp.namespace"
                  disabled
                ></v-text-field>
              </v-form>
            </div>
          </div>
        </div>
        <div class="col-12">
          <div class="card">
            <v-card-title>Settings</v-card-title>
            <div class="card-text">
              Add setting definitions in the source code and then set the values
              here
            </div>
            <v-card-actions></v-card-actions>
          </div>
        </div>
        <div class="col-12">
          <div class="card">
            <v-card-title>OAuth</v-card-title>
            <div class="card-text">
              <div v-if="!automationApp.oAuthEnabled">
                <v-btn
                  color="primary"
                  outlined
                  @click="automationApp.oAuthEnabled = true"
                  mx-2
                >
                  Enable OAuth in Automation App
                </v-btn>
              </div>
              <div v-else>
                <v-text-field
                  label="Client ID"
                  v-model="automationApp.oAuthClientId"
                  hint="Public client ID for accessing this SmartApp via its REST API"
                  readonly
                ></v-text-field>

                <v-text-field
                  label="Client Secret"
                  v-model="automationApp.oAuthClientSecret"
                  hint="Confidential secret key for accessing this SmartApp via its REST API"
                  readonly
                ></v-text-field>
              </div>
            </div>
            <v-card-actions></v-card-actions>
          </div>
        </div>
        <div class="col-12">
          <v-btn color="primary" @click="updateAutomationApp"> Update </v-btn>
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
