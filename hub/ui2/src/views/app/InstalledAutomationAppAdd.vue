<template>
  <div class="container-fluid">
    <div class="row">
      <div
        class="col-12"
        v-for="automationApp in automationApps"
        :key="automationApp.id"
      >
        <div class="card">
          <div class="card-body">
            <v-card-title
              >{{ automationApp.name }} ({{
                automationApp.namespace
              }})</v-card-title
            >
            <div class="card-text">
              {{ automationApp.description }}
            </div>
            <v-card-actions>
              <v-btn
                color="primary"
                @click="addAutomationApp(automationApp.id)"
              >
                Add
              </v-btn>
            </v-card-actions>
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
  name: "InstalledAutomationAppAdd",
  data() {
    return {
      automationApps: [],
    };
  },
  methods: {
    addAutomationApp: function (id) {
      var body = { id: id };
      fetch("/api/iaas", { method: "POST", body: JSON.stringify(body) })
        .then(handleErrors)
        .then((response) => {
          return response.json();
        })
        .then((data) => {
          console.log(JSON.stringify(data));
          this.$router.push(`/iaas/${data.id}/cfg`);
        })
        .catch((error) => {
          console.log(error);
        });
    },
  },
  mounted: function () {
    fetch("/api/automation-apps")
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.automationApps = data;
        }
      });
  },
};
</script>
<style scoped></style>
