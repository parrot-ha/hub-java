<template>
  <div class="container-fluid">

      <div class="row">
        <div class="col-12"
          v-for="integrationType in integrationTypes"
          :key="integrationType.id"
        >
          <div class="card">
            <v-card-title>{{ integrationType.name }}</v-card-title>
            <div class="card-text">
              {{ integrationType.description }}
            </div>
            <v-card-actions>
              <v-btn
                color="primary"
                @click="addIntegration(integrationType.id)"
              >
                Add
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
  name: "Integration Add",
  data() {
    return {
      integrationTypes: [],
    };
  },
  methods: {
    addIntegration: function (integrationTypeId) {
      var body = { id: integrationTypeId };
      fetch("/api/integrations", { method: "POST", body: JSON.stringify(body) })
        .then(handleErrors)
        .then((response) => {
          return response.json();
        })
        .then((data) => {
          console.log(JSON.stringify(data));
          this.$router.push(`/integrations/${data.id}`);
        })
        .catch((error) => {
          console.log(error);
        });
    },
  },
  mounted: function () {
    fetch("/api/integration_types")
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.integrationTypes = data;
        }
      });
  },
};
</script>
<style scoped></style>
