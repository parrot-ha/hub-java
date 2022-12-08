<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col :cols="12">
          <v-card>
            <v-card-title>Information</v-card-title>
            <v-card-text>
              <v-simple-table>
                <tbody>
                  <tr>
                    <td>Date Created</td>
                    <td></td>
                  </tr>
                  <tr>
                    <td>Last Updated</td>
                    <td></td>
                  </tr>
                  <tr>
                    <td>Data</td>
                    <td>
                      <ul>
                        <li
                          v-for="(value, name, i) in information.data"
                          :key="i"
                        >
                          {{ name }}:
                          <strong>{{ value }}</strong>
                        </li>
                      </ul>
                    </td>
                  </tr>
                  <tr>
                    <td>Current States</td>
                    <td>
                      <ul>
                        <li
                          v-for="currentState in currentStates"
                          :key="currentState.name"
                        >
                          {{ currentState.name }}:
                          <strong>{{ currentState.stringValue }}</strong>
                        </li>
                      </ul>
                    </td>
                  </tr>
                  <tr>
                    <td>Events</td>
                    <td>
                      <a :href="`/devices/${device.id}/events`">List Events</a>
                    </td>
                  </tr>
                  <tr>
                    <td>In Use By</td>
                    <td></td>
                  </tr>
                </tbody>
              </v-simple-table>
            </v-card-text>
            <v-card-actions> </v-card-actions>
          </v-card>
        </v-col>
        <v-col :cols="12">
          <v-card>
            <v-card-title>Commands</v-card-title>
            <v-card-text>
              <ul v-for="(command, i) in commands" :key="i">
                <device-command
                  :deviceId="device.id"
                  :command="command"
                ></device-command>
              </ul>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col :cols="12">
          <v-card>
            <v-card-title>Settings</v-card-title>
            <v-card-text>
              <v-form>
                <v-text-field label="Name" v-model="device.name"></v-text-field>
                <v-text-field
                  label="Label"
                  v-model="device.label"
                ></v-text-field>
                <v-text-field
                  label="Device Network ID"
                  v-model="device.deviceNetworkId"
                ></v-text-field>
                <v-select
                  :items="integrations"
                  :item-text="item => item.label"
                  item-value="id"
                  label="Integration"
                  v-model="device.integrationId"
                ></v-select>
                <v-select
                  :items="filteredDevices"
                  :item-text="item => item.name + ' (' + item.namespace + ')'"
                  item-value="id"
                  label="Type"
                  v-model="device.deviceHandlerId"
                ></v-select>
                <v-icon
                  :key="`icon-${dhFiltering}`"
                  @click="dhFiltering = !dhFiltering"
                  v-text="dhFiltering ? 'mdi-filter' : 'mdi-filter-off'"
                ></v-icon>
              </v-form>
            </v-card-text>

            <v-divider></v-divider>
            <v-card-title>Preferences</v-card-title>
            <v-card-text>
              <v-form>
                <div v-for="(section, i) in preferences.sections" :key="i">
                  <div v-for="(body, j) in section.body" :key="j">
                    <div v-if="body.type === 'bool'">
                      <v-switch
                        :id="body.name"
                        :name="body.name"
                        :label="body.title"
                        v-model="settings[body.name].valueAsType"
                      ></v-switch>
                    </div>
                    <div v-if="body.type === 'decimal'">
                      <v-text-field
                        type="number"
                        step="any"
                        :id="body.name"
                        :name="body.name"
                        :label="body.title"
                        v-model="settings[body.name].valueAsType"
                      ></v-text-field>
                    </div>
                    <div v-if="body.type === 'email'">
                      <v-text-field
                        type="email"
                        :id="body.name"
                        :name="body.name"
                        :label="body.title"
                        v-model="settings[body.name].valueAsType"
                      ></v-text-field>
                    </div>
                    <div v-if="body.type === 'enum'">
                      <enum-input
                        v-if="settings[body.name]"
                        v-bind:options="body.options"
                        v-bind:body="body"
                        v-model="settings[body.name].valueAsType"
                      ></enum-input>
                    </div>
                    <div v-if="body.type === 'number'">
                      <v-text-field
                        type="number"
                        step="1"
                        :id="body.name"
                        :name="body.name"
                        :label="body.title"
                        v-model="settings[body.name].valueAsType"
                      ></v-text-field>
                    </div>
                    <div v-if="body.type === 'password'">
                      <v-text-field
                        type="password"
                        :label="body.title"
                        :id="body.name"
                        :name="body.name"
                        v-model="settings[body.name].valueAsType"
                      ></v-text-field>
                    </div>
                    <div v-if="body.type === 'phone'">
                      <v-text-field
                        type="tel"
                        :id="body.name"
                        :name="body.name"
                        :label="body.title"
                        v-model="settings[body.name].valueAsType"
                      ></v-text-field>
                    </div>
                    <div v-if="body.type === 'time'">
                      <v-text-field
                        type="time"
                        :id="body.name"
                        :name="body.name"
                        :label="body.title"
                        v-model="settings[body.name].valueAsType"
                      ></v-text-field>
                    </div>
                    <div v-if="body.type === 'text' || body.type === 'string'">
                      <v-text-field
                        :id="body.name"
                        :name="body.name"
                        :label="body.title"
                        v-model="settings[body.name].valueAsType"
                      ></v-text-field>
                    </div>
                  </div>
                </div>
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-btn color="primary" @click="saveDevice">
                Save
              </v-btn>
              <v-spacer></v-spacer>
              <v-dialog v-model="deviceDeleteDialog" persistent max-width="290">
                <template v-slot:activator="{ on, attrs }">
                  <v-btn color="error" v-bind="attrs" v-on="on">Delete</v-btn>
                </template>
                <v-card>
                  <v-card-title class="headline">
                    Are you sure?
                  </v-card-title>
                  <v-card-text
                    >Are you sure you want to delete this device?</v-card-text
                  >
                  <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn
                      color="primary"
                      text
                      @click="deviceDeleteDialog = false"
                    >
                      Cancel
                    </v-btn>
                    <v-btn color="error" text @click="deleteDevice">
                      Delete
                    </v-btn>
                  </v-card-actions>
                </v-card>
              </v-dialog>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-layout>
  </v-container>
</template>
<script>
import DeviceCommand from '@/components/DeviceCommand';
import EnumInput from '@/components/device/DeviceEnumInput';

export default {
  name: 'Device Show',
  data() {
    return {
      deviceId: '',
      device: {},
      deviceHandlers: {},
      dhFiltering: false,
      integrations: {},
      preferences: {},
      settings: {},
      commands: [],
      currentStates: {},
      information: {},
      deviceDeleteDialog: false
    };
  },
  components: {
    DeviceCommand,
    EnumInput
  },
  computed: {
    filteredDevices: function() {
      if (this.dhFiltering && this.device.integrationId != null) {
        var integration = this.integrations.find(
          i => i.id == this.device.integrationId
        );
        if (integration.tags != null) {
          return this.deviceHandlers.filter(
            dh =>
              (dh.tags != null &&
                integration.tags.some(t => dh.tags.indexOf(t) >= 0)) ||
              this.device.deviceHandlerId == dh.id
          );
        } else {
          return this.deviceHandlers;
        }
      } else {
        return this.deviceHandlers;
      }
    }
  },
  methods: {
    updatePreferenceLayout: function() {
      fetch(`/api/devices/${this.deviceId}/preferences-layout`)
        .then(response => {
          if (response) {
            return response.json();
          } else {
            return {};
          }
        })
        .then(data => {
          this.preferences = data;
          if (data.sections) {
            for (var section of data.sections) {
              for (var input of section.input) {
                if (typeof this.settings[input.name] === 'undefined') {
                  this.settings[input.name] = {
                    name: input.name,
                    valueAsType:
                      input.defaultValue == null ||
                      typeof input.defaultValue === 'undefined'
                        ? null
                        : input.defaultValue,
                    type: input.type,
                    multiple: input.multiple ? true : false
                  };
                } else if (
                  this.settings[input.name].valueAsType == null &&
                  input.defaultValue != null &&
                  typeof input.defaultValue !== 'undefined'
                ) {
                  this.settings[input.name].valueAsType = input.defaultValue;
                }
              }
            }
          }
        });
    },
    saveDevice: function() {
      var body = { device: this.device, settings: this.settings };
      fetch(`/api/devices/${this.deviceId}`, {
        method: 'PUT',
        body: JSON.stringify(body)
      })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            console.log('success');
            this.updatePreferenceLayout();
          } else {
            console.log('problem saving device');
          }
        });
    },
    deleteDevice: function() {
      console.log('delete device!');
      fetch(`/api/devices/${this.deviceId}`, { method: 'DELETE' })
        .then(response => response.json())
        .then(data => {
          console.log(JSON.stringify(data));
          console.log(`data ${data}`);
          if (data.success) {
            console.log('device was deleted!');
            this.$router.push('/devices');
          } else {
            console.log('problem deleting device');
            //$('#deletModal').modal('hide');
          }
        });
    }
  },
  mounted: function() {
    this.deviceId = this.$route.params.id;

    fetch(
      '/api/integrations?type=DEVICE&field=id&field=name&field=label&field=tags'
    )
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.integrations = data;
          // add blank option
          this.integrations.push({ id: null, name: 'None', label: 'None' });
        }
      });

    fetch(`/api/devices/${this.deviceId}`)
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.device = data;
        }
      });

    fetch(`/api/devices/${this.deviceId}/commands`)
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          for (var cmd of data) {
            if (cmd.arguments) {
              cmd.values = [];
              for (var arg of cmd.arguments) {
                cmd.values.push({ name: arg, value: null });
              }
            }
          }
          this.commands = data;
        }
      });

    fetch(`/api/devices/${this.deviceId}/states`)
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.currentStates = data;
        }
      });

    fetch(`/api/devices/${this.deviceId}/information`)
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.information = data;
        }
      });

    fetch(`/api/device-handlers?field=id&field=name&field=namespace&field=tags`)
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.deviceHandlers = data;
        }
      });

    fetch(`/api/devices/${this.deviceId}/settings`)
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.settings = data;
        }
      })
      .then(this.updatePreferenceLayout());

    let connection = new WebSocket(
      `ws://${window.location.host}/api/devices/${this.deviceId}/events`
    );
    connection.onmessage = event => {
      var eventMap = JSON.parse(event.data);

      var matched = false;
      this.currentStates.forEach(function(item, index) {
        if (item.name == eventMap.name) {
          item.stringValue = eventMap.value;
          matched = true;
        }
      });
      if (!matched) {
        this.currentStates.push({
          name: eventMap.name,
          value: eventMap.value,
          stringValue: eventMap.value
        });
      }
    };
  }
};
</script>
<style scoped></style>
