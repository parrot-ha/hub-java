<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col :cols="12">
          <h2>{{ integration.label }} ({{ integration.name }})</h2>

          <v-card>
            <v-card-title>Information</v-card-title>
            <v-card-text>
              <v-simple-table>
                <tbody>
                  <tr
                    v-for="(value, name, i) in integration.information"
                    :key="i"
                  >
                    <td>{{ name }}</td>
                    <td>{{ value }}</td>
                  </tr>
                </tbody>
              </v-simple-table>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col :cols="12">
          <v-card>
            <v-card-title>Settings</v-card-title>
            <v-card-text>
              <v-form>
                <v-text-field
                  label="Label"
                  v-model="integration.label"
                ></v-text-field>
                <v-divider></v-divider>
                <div v-for="(section, i) in preferences.sections" :key="i">
                  <div v-for="(body, j) in section.body" :key="j">
                    <div v-if="body.type === 'text'">
                      <v-text-field
                        :label="body.title"
                        v-model="settings[body.name].value"
                      ></v-text-field>
                    </div>
                    <div v-else-if="body.type === 'enum'">
                      <v-select
                        :items="body.options"
                        :label="body.title"
                        v-model="settings[body.name].value"
                      ></v-select>
                    </div>
                    <div v-else-if="body.type === 'bool'">
                      <v-switch
                        :label="body.title"
                        v-model="settings[body.name].value"
                      ></v-switch>
                    </div>
                  </div>
                </div>
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-btn color="primary" @click="saveIntegration">
                Save
              </v-btn>
              <v-spacer></v-spacer>
              <v-dialog
                v-model="integrationDeleteDialog"
                persistent
                max-width="290"
              >
                <template v-slot:activator="{ on, attrs }">
                  <v-btn color="error" v-bind="attrs" v-on="on">Delete</v-btn>
                </template>
                <v-card>
                  <v-card-title class="headline">
                    Are you sure?
                  </v-card-title>
                  <v-card-text
                    >Are you sure you want to delete this
                    integration?</v-card-text
                  >
                  <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn
                      color="primary"
                      text
                      @click="integrationDeleteDialog = false"
                    >
                      Cancel
                    </v-btn>
                    <v-btn color="error" text @click="deleteIntegration">
                      Delete
                    </v-btn>
                  </v-card-actions>
                </v-card>
              </v-dialog>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col v-for="(section, i) in pageLayout" :key="i" :cols="12">
          <v-card>
            <v-card-title>{{ section.title }} </v-card-title>
            <v-card-text>
              <div v-for="(bodyItem, j) in section.body" :key="j">
                <div v-if="bodyItem.type === 'table'">
                  <table>
                    <thead>
                      <tr v-for="column in bodyItem.columns" :key="column.data">
                        <th>{{ column.title }}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr
                        v-for="(dataRow, k) in pageData[bodyItem.data]"
                        :key="k"
                      >
                        <td
                          v-for="column in bodyItem.columns"
                          :key="column.data"
                        >
                          {{ dataRow[column.data] }}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div v-if="bodyItem.type === 'button'">
                  <v-btn
                    :color="bodyItem.color"
                    @click="handleButtonAction(bodyItem.action)"
                    :disabled="pageData[bodyItem.disabled]"
                  >
                    {{ bodyItem.title }}
                  </v-btn>
                </div>
              </div>
            </v-card-text>
            <v-card-actions> </v-card-actions>
          </v-card>
        </v-col>

        <v-col v-if="featureExists('deviceScan')" :cols="12">
          <v-card>
            <v-card-title
              >Device Scanning
              <v-progress-circular
                v-show="scanDevicesRunning"
                indeterminate
                color="primary"
              ></v-progress-circular>
            </v-card-title>
            <v-card-text>
              <div id="foundDeviceDiv">
                {{ foundDevices }}
              </div>
            </v-card-text>
            <v-card-actions>
              <v-btn
                :disabled="scanDevicesRunning"
                color="success"
                @click="startScan"
              >
                Start Scan
              </v-btn>
              <v-btn
                :disabled="!scanDevicesRunning"
                color="error"
                @click="stopScan"
                >Stop Scan</v-btn
              >
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col v-if="featureExists('deviceExclude')" :cols="12">
          <v-card>
            <v-card-title
              >Device Exclude
              <v-progress-circular
                v-show="excludeDevicesRunning"
                indeterminate
                color="primary"
              ></v-progress-circular
            ></v-card-title>
            <v-card-text>
              <div id="deviceExcludedDiv">
                {{ excludedDevices }}
              </div>
            </v-card-text>
            <v-card-actions>
              <v-btn
                :disabled="excludeDevicesRunning"
                color="success"
                @click="startExclude"
              >
                Start Exclude
              </v-btn>
              <v-btn
                :disabled="!excludeDevicesRunning"
                color="error"
                @click="stopExclude"
                >Stop Exclude</v-btn
              >
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col v-if="featureExists('reset')" :cols="12">
          <v-card>
            <v-card-title>Reset</v-card-title>
            <v-card-text>
              <h5 style="color:red;">
                {{ getFeatureOption('reset', 'resetWarning') }}
              </h5>
              <v-form>
                <v-text-field
                  label='Type "reset" and click Reset button to reset'
                  v-model="reset"
                ></v-text-field>
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-btn color="error" @click="resetIntegration">Reset</v-btn>
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
  name: 'Integration',
  data() {
    return {
      integrationId: '',
      integration: {
        settings: {}
      },
      settings: {},
      preferences: {},
      pageLayout: [],
      pageData: {},
      foundDevices: null,
      scanDevicesRunning: false,
      excludedDevices: null,
      excludeDevicesRunning: false,
      reset: '',
      integrationDeleteDialog: false
    };
  },
  methods: {
    handleButtonAction: function(action) {
      console.log('handleButtonAction ' + action);
    },
    deleteIntegration: function() {
      fetch(`/api/integrations/${this.integrationId}`, {
        method: 'DELETE'
      })
        .then(handleErrors)
        .then(response => {
          return response.json();
        })
        .then(data => {
          console.log('integration was deleted!');
          this.$router.push('/integrations');
        })
        .catch(error => {
          console.log(error);
        });
    },
    saveIntegration: function() {
      var updatedSettings = this.settings;
      updatedSettings.label = { value: this.integration.label };
      fetch(`/api/integrations/${this.integrationId}/settings`, {
        method: 'POST',
        body: JSON.stringify(updatedSettings)
      })
        .then(handleErrors)
        .then(response => {
          return response.json();
        })
        .then(data => {
          console.log('success');
        })
        .catch(error => {
          console.log(error);
        });
    },

    featureExists: function(value) {
      if (this.integration == null || typeof this.integration === 'undefined')
        return false;
      if (
        this.integration.features == null ||
        typeof this.integration.features === 'undefined'
      )
        return false;
      for (const property in this.integration.features) {
        if (property == value) return true;
      }
      return false;
    },
    getFeatureOption: function(featureName, optionName) {
      if (this.integration == null || typeof this.integration === 'undefined')
        return '';
      if (
        this.integration.features == null ||
        typeof this.integration.features === 'undefined'
      )
        return '';
      if (
        this.integration.features[featureName] == null ||
        typeof this.integration.features[featureName] === 'undefined'
      )
        return '';
      return this.integration.features[featureName][optionName];
    },
    checkStatus: function() {
      var vm = this;
      var body = { action: 'getScanStatus' };
      fetch(`/api/integrations/${vm.integrationId}/features/deviceScan`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
        .then(response => response.json())
        .then(data => {
          vm.scanDevicesRunning = data.response.running;
          vm.foundDevices = data.response.foundDevices;
          if (data.response.running) {
            setTimeout(
              function() {
                this.checkStatus();
              }.bind(this),
              2000
            );
          }
        });
    },
    startScan: function() {
      var vm = this;
      var body = { action: 'startScan' };
      fetch(`/api/integrations/${vm.integrationId}/features/deviceScan`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
        .then(response => response.json())
        .then(data => {
          vm.scanDevicesRunning = true;
          // check status
          setTimeout(
            function() {
              this.checkStatus();
            }.bind(this),
            2000
          );
        });
    },
    stopScan: function() {
      var vm = this;
      var body = { action: 'stopScan' };
      fetch(`/api/integrations/${vm.integrationId}/features/deviceScan`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
        .then(response => response.json())
        .then(data => {
          vm.checkStatus();
        });
    },
    checkExcludeStatus: function() {
      var vm = this;
      var body = { action: 'getExcludeStatus' };
      fetch(`/api/integrations/${vm.integrationId}/features/deviceExclude`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
        .then(response => response.json())
        .then(data => {
          vm.excludeDevicesRunning = data.response.running;
          vm.excludedDevices = data.response.excludedDevices;
          if (data.response.running) {
            setTimeout(
              function() {
                this.checkExcludeStatus();
              }.bind(this),
              2000
            );
          }
        });
    },
    startExclude: function() {
      var vm = this;
      var body = { action: 'startExclude' };
      fetch(`/api/integrations/${vm.integrationId}/features/deviceExclude`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
        .then(response => response.json())
        .then(data => {
          vm.excludeDevicesRunning = true;
          // check status
          setTimeout(
            function() {
              this.checkExcludeStatus();
            }.bind(this),
            2000
          );
        });
    },
    stopExclude: function() {
      var vm = this;
      var body = { action: 'stopExclude' };
      fetch(`/api/integrations/${vm.integrationId}/features/deviceExclude`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
        .then(response => response.json())
        .then(data => {
          vm.checkExcludeStatus();
        });
    },
    resetIntegration: function() {
      var vm = this;
      if (vm.reset === 'reset') {
        var body = { action: 'reset' };
        fetch(`/api/integrations/${vm.integrationId}/features/reset`, {
          method: 'POST',
          body: JSON.stringify(body)
        })
          .then(response => response.json())
          .then(data => {
            vm.checkStatus();
          });
      } else {
        //TODO: display this to the user instead of just in the console.
        console.log('Please type reset into text box');
      }
    },
    loadPreferencesLayout: function() {
      fetch(`/api/integrations/${this.integrationId}/preferences-layout`)
        .then(response => response.json())
        .then(data => {
          this.preferences = data;
          for (var section of data.sections) {
            for (var input of section.input) {
              if (typeof this.settings[input.name] === 'undefined') {
                //TODO: is empty string ok, or should it be null?
                this.settings[input.name] = {
                  name: input.name,
                  value: input.multiple ? [] : null,
                  type: input.type,
                  multiple: input.multiple
                };
              } else {
                // check if multiple changed
                if (this.settings[input.name].multiple != input.multiple) {
                  // update setting
                  this.settings[input.name].multiple = input.multiple;
                  // we are changing to true, check value
                  if (input.multiple) {
                    if (
                      this.settings[input.name].value === null ||
                      this.settings[input.name].value === ''
                    ) {
                      this.settings[input.name].value = [];
                    } else if (
                      !Array.isArray(this.settings[input.name].value)
                    ) {
                      this.settings[input.name].value = Array.from(
                        this.settings[input.name].value
                      );
                    }
                  }
                  //TODO: handle multiple going from true to false
                }
              }
            }
          }
        });
    },
    loadPageLayout: function() {
      fetch(`/api/integrations/${this.integrationId}/page-layout`)
        .then(response => response.json())
        .then(data => {
          this.pageLayout = data;
        });
    }
  },
  mounted: function() {
    this.integrationId = this.$route.params.id;

    fetch(`/api/integrations/${this.integrationId}`)
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.integration = data;
          if (this.settings == null) {
            this.settings = {};
          }
        }
      });

    fetch(`/api/integrations/${this.integrationId}/settings`)
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.settings = data;
        }
      })
      .then(this.loadPreferencesLayout());

    fetch(`/api/integrations/${this.integrationId}/page-data`)
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.pageData = data;
        }
      })
      .then(this.loadPageLayout());
  }
};
</script>
<style scoped></style>
