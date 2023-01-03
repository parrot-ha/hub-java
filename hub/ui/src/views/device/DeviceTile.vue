<template>
  <v-container fluid>
    <v-layout>
      <v-row>
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
                  :items="deviceHandlers"
                  :item-text="item => item.name + ' (' + item.namespace + ')'"
                  item-value="id"
                  label="Type"
                  v-model="device.deviceHandlerId"
                ></v-select>
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-btn color="primary" @click="saveDevice">
                Save
              </v-btn>
              <v-btn color="error" @click="deleteDevice">Delete</v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
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
                    <td></td>
                  </tr>
                  <tr>
                    <td>Current States</td>
                    <td></td>
                  </tr>
                  <tr>
                    <td>Events</td>
                    <td>
                      <a :href="`/device/${device.id}/events`">List Events</a>
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
      </v-row>
    </v-layout>
  </v-container>
</template>
<script>
export default {
  name: 'Device Show',
  data() {
    return {
      device: {},
      deviceHandlers: {},
      preferences: {},
      settings: {},
      commands: []
    };
  },
  mounted: function() {
    var deviceId = this.$route.params.id;

    fetch(`/api/devices/${deviceId}`)
      .then(response => response.json())
      .then(data => {
        if (data != null) {
          this.device = data;
        }
      });

    fetch(`/api/devices/${deviceId}/commands`)
      .then(response => response.json())
      .then(data => {
        if (data != null) {
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

    fetch(`/api/device-handlers?field=id&field=name&field=namespace`)
      .then(response => response.json())
      .then(data => {
        if (data != null) {
          this.deviceHandlers = data;
        }
      });
  }
};
</script>
<style scoped></style>
