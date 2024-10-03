<template>
  <div class="container-fluid">
    <div class="row">
      <div
        v-for="automationApp in automationApps"
        :key="automationApp.id"
        class="col-12"
      >
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">
              {{ automationApp.name }} ({{ automationApp.namespace }})
            </h5>
            <div class="card-text">
              {{ automationApp.description }}
            </div>

            <button
              class="btn btn-primary"
              @click="addAutomationApp(automationApp.id)"
            >
              Add
            </button>
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
  name: "InstalledAppAdd",
  data() {
    return {
      automationApps: [],
    };
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
  methods: {
    addAutomationApp: function (id) {
      var body = { id: id };
      fetch("/api/iaas", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      })
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
};
</script>
<style scoped></style>
