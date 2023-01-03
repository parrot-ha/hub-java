<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col :cols="12">
          <v-card>
            <v-card-title>Information</v-card-title>
            <v-card-text>
              <v-form>
                <v-text-field
                  label="ID"
                  v-model="installedAutomationApp.id"
                  readonly
                ></v-text-field>
                <v-text-field
                  label="Label"
                  v-model="installedAutomationApp.label"
                ></v-text-field>
              </v-form>
            </v-card-text>
            <v-card-actions
              ><v-btn color="primary" @click="saveInstalledAutomationApp">
                Save
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
        <v-col :cols="12">
          <v-card>
            <v-card-title>Settings</v-card-title>
            <v-card-text>
              <v-simple-table>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Value</th>
                    <th>Type</th>
                    <th>Multiple</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="setting in installedAutomationApp.settings"
                    :key="setting.id"
                  >
                    <td>{{ setting.name }}</td>
                    <td>{{ setting.value }}</td>
                    <td>{{ setting.type }}</td>
                    <td>{{ setting.multiple }}</td>
                  </tr>
                </tbody>
              </v-simple-table>
            </v-card-text>
            <v-card-actions></v-card-actions>
          </v-card>
        </v-col>
        <v-col :cols="12">
          <v-card>
            <v-card-title>Scheduled Jobs</v-card-title>
            <v-card-text>
              <v-simple-table>
                <thead>
                  <tr>
                    <th>Handler Method</th>
                    <th>Schedule</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(schedule, i) in schedules" :key="i">
                    <td>{{ schedule.handlerMethod }}</td>
                    <td>{{ schedule.schedule }}</td>
                  </tr>
                </tbody>
              </v-simple-table>
            </v-card-text>
            <v-card-actions></v-card-actions>
          </v-card>
        </v-col>
        <v-col :cols="12">
          <v-btn color="primary" @click="runUpdatedMethod">
            Run Updated Method
          </v-btn>
          <v-dialog v-model="iaaUninstallDialog" persistent max-width="290">
            <template v-slot:activator="{ on, attrs }">
              <v-btn color="error" v-bind="attrs" v-on="on">Uninstall</v-btn>
            </template>
            <v-card>
              <v-card-title class="headline">
                Are you sure?
              </v-card-title>
              <v-card-text
                >Are you sure you want to uninstall this Automation
                App?</v-card-text
              >
              <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn color="primary" text @click="iaaUninstallDialog = false">
                  Cancel
                </v-btn>
                <v-btn color="error" text @click="uninstallClick">
                  Delete
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>
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
  name: 'InstalledAutomationAppInfo',
  data() {
    return {
      iaaId: '',
      installedAutomationApp: {},
      iaaUninstallDialog: false,
      schedules: {}
    };
  },
  methods: {
    saveInstalledAutomationApp: function() {
      var body = this.installedAutomationApp;
      fetch(`/api/iaas/${this.iaaId}`, {
        method: 'PUT',
        body: JSON.stringify(body)
      })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            console.log('success');
          } else {
            console.log('problem saving installed automation app');
          }
        });
    },
    runUpdatedMethod: function() {
      var url = `/api/iaas/${this.iaaId}/methods/updated`;
      var setArgs = [];
      fetch(url, {
        method: 'POST',
        body: null
      })
        .then(handleErrors)
        .then(response => {});
    },
    uninstallClick: function(event) {
      fetch(`/api/iaas/${this.iaaId}`, {
        method: 'DELETE'
      })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            this.$router.push('/iaas');
          } else {
            console.log('problem deleting automation app');
          }
        });
    }
  },
  mounted: function() {
    this.iaaId = this.$route.params.id;

    fetch(`/api/iaas/${this.iaaId}`)
      .then(response => response.json())
      .then(data => {
        if (data != null) {
          this.installedAutomationApp = data;
        }
      });

    fetch(`/api/iaas/${this.iaaId}/schedules`)
      .then(response => response.json())
      .then(data => {
        if (data != null) {
          this.schedules = data;
        }
      });
  }
};
</script>
<style scoped></style>
