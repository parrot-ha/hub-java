<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col
          v-for="automationApp in automationApps"
          :key="automationApp.id"
          :cols="12"
        >
          <v-card>
            <v-card-title
              >{{ automationApp.name }} ({{
                automationApp.namespace
              }})</v-card-title
            >
            <v-card-text>
              {{ automationApp.description }}
            </v-card-text>
            <v-card-actions>
              <v-btn
                color="primary"
                @click="addAutomationApp(automationApp.id)"
              >
                Add
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-layout>
  </v-container>
</template>
<script>
function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

export default {
  name: 'InstalledAutomationAppAdd',
  data() {
    return {
      automationApps: []
    };
  },
  methods: {
    addAutomationApp: function(id) {
      var body = { id: id };
      fetch('/api/iaas', { method: 'POST', body: JSON.stringify(body) })
        .then(handleErrors)
        .then(response => {
          return response.json();
        })
        .then(data => {
          console.log(JSON.stringify(data));
          this.$router.push(`/iaas/${data.id}/cfg`);
        })
        .catch(error => {
          console.log(error);
        });
    }
  },
  mounted: function() {
    fetch('/api/automation-apps')
      .then(response => response.json())
      .then(data => {
        if (data != null) {
          this.automationApps = data;
        }
      });
  }
};
</script>
<style scoped></style>
