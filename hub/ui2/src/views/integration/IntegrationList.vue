<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col">
        <div class="card">
          <h5 class="card-title">Integrations</h5>
          <div class="card-text">
            <router-link :to="{ name: 'IntegrationAdd' }"
              >Add Integration</router-link
            >
            <v-simple-table>
              <thead>
                <tr>
                  <th scope="col" style="width: 48%">Name</th>
                  <th scope="col" style="width: 52%">Description</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="integration in integrations" :key="integration.id">
                  <td>
                    <router-link
                      :to="{
                        name: 'Integration',
                        params: { id: integration.id },
                      }"
                      >{{ integration.label }} ({{
                        integration.name
                      }})</router-link
                    >
                  </td>
                  <td>{{ integration.description }}</td>
                </tr>
              </tbody>
            </v-simple-table>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: "IntegrationList",
  data() {
    return {
      integrations: [],
    };
  },
  mounted: function () {
    fetch("/api/integrations")
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.integrations = data;
        }
      });
  },
};
</script>
<style scoped></style>
