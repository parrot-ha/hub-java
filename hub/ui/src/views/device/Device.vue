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
                  <td>Name</td>
                  <td>{{ device.name }}</td>
                </tr>
                <tr>
                  <td>Label</td>
                  <td>{{ device.label }}</td>
                </tr>
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
      </v-row>
    </v-layout>
  </v-container>
</template>
<script>
import DeviceCommand from '@/components/DeviceCommand';

export default {
  name: 'Device Show',
  data() {
    return {
      deviceId: '',
      device: {},
      commands: [],
      currentStates: {},
      information: {},
    };
  },
  components: {
    DeviceCommand
  },
  computed: {
  },
  methods: {
  },
  mounted: function() {
    this.deviceId = this.$route.params.id;

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
