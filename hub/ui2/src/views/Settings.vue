<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col">
        <div class="card">
          <h5 class="card-title"></h5>
          <div class="card-text">
            <router-link :to="{ name: 'LoggerConfig' }"
              >Logger Configuration</router-link
            >
          </div>
          <v-card-actions>
            <v-btn color="primary" @click="reloadAutomationApps">
              Reload Automation Apps
            </v-btn>
            <v-btn color="primary" @click="reloadDeviceHandlers">
              Reload Device Handlers
            </v-btn>
          </v-card-actions>
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
  name: "Settings",
  data() {
    return {};
  },
  methods: {
    reloadAutomationApps: function () {
      var body = { action: "reload" };
      fetch("/api/settings/automation-apps", {
        method: "POST",
        body: JSON.stringify(body),
      })
        .then(handleErrors)
        .then((response) => {
          return response.json();
        })
        .then((data) => {
          console.log("success");
        })
        .catch((error) => {
          console.log(error);
        });
    },
    reloadDeviceHandlers: function () {
      var body = { action: "reload" };
      fetch("/api/settings/device-handlers", {
        method: "POST",
        body: JSON.stringify(body),
      })
        .then(handleErrors)
        .then((response) => {
          return response.json();
        })
        .then((data) => {
          console.log("success");
        })
        .catch((error) => {
          console.log(error);
        });
    },
  },
};
</script>
<style scoped></style>
