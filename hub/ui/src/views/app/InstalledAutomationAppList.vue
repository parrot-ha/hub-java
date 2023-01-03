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
              <v-data-table
                :headers="headers"
                :items="installedAutomationApps"
                sort-by="label"
                disable-pagination
                hide-default-footer
                class="elevation-1"
              >
                <template v-slot:item.label="{ item }">
                  <router-link
                    :to="{
                      name: 'InstalledAutomationAppConfig',
                      params: { id: item.id }
                    }"
                    >{{ item.label }}</router-link
                  >
                </template>
                <template v-slot:item.id="{ item }">
                  <router-link
                    :to="{
                      name: 'InstalledAutomationAppInfo',
                      params: { id: item.id }
                    }"
                    ><v-icon>mdi-alert-circle-outline</v-icon></router-link
                  >
                </template>
              </v-data-table>
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
      installedAutomationApps: [],
      headers: [
        {
          text: '',
          align: 'start',
          sortable: false,
          value: 'id',
          width: '5%'
        },
        { text: 'Label', value: 'label' },
        { text: 'Type', value: 'type' }
      ]
    };
  },
  mounted: function() {
    fetch('/api/iaas?includeChildren=false')
      .then(response => response.json())
      .then(data => {
        if (data != null) {
          this.installedAutomationApps = data;
        }
      });
  }
};
</script>
<style scoped></style>
