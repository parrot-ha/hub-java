<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>Installed Automation Apps</v-card-title>
            <v-card-text>
              <router-link :to="{ name: 'InstalledAutomationAppAdd' }"
                >Add Automation App</router-link
              >
              <v-simple-table>
                <thead>
                  <tr>
                    <th scope="col" style="width:4%"></th>
                    <th scope="col" style="width:48%">Label</th>
                    <th scope="col" style="width:48%">Type</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="installedAutomationApp in installedAutomationApps"
                    :key="installedAutomationApp.id"
                  >
                    <td>
                      <router-link
                        :to="{
                          name: 'InstalledAutomationAppInfo',
                          params: { id: installedAutomationApp.id }
                        }"
                        ><v-icon>mdi-alert-circle-outline</v-icon></router-link
                      >
                    </td>
                    <td>
                      <router-link
                        :to="{
                          name: 'InstalledAutomationAppConfig',
                          params: { id: installedAutomationApp.id }
                        }"
                        >{{ installedAutomationApp.label }}</router-link
                      >
                    </td>
                    <td>{{ installedAutomationApp.type }}</td>
                  </tr>
                </tbody>
              </v-simple-table>
            </v-card-text>
            <v-card-actions> </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-layout>
  </v-container>
</template>
<script>
export default {
  name: 'InstalledAutomationAppList',
  data() {
    return {
      installedAutomationApps: []
    };
  },
  mounted: function() {
    fetch('/api/iaas?includeChildren=false')
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.installedAutomationApps = data;
        }
      });
  }
};
</script>
<style scoped></style>
