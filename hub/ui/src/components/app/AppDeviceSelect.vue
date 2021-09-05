<template>
  <div>
    <div v-if="body.multiple">
      <v-card @click="deviceSelectClick">
        <v-card-title>{{ body.title }}</v-card-title>
        <v-card-text>
          <div v-if="value">
            <div v-for="settingVal in value" :key="settingVal">
              {{ devices[settingVal.trim()] }}
            </div>
          </div>
          <div v-else>
            <div>{{ body.description }}</div>
          </div>
        </v-card-text>
      </v-card>
    </div>
    <div v-else>
      <v-card @click="deviceSelectClick">
        <v-card-title>{{ body.title }}</v-card-title>
        <v-card-text>
          <div v-if="value">
            {{ devices[value.trim()] }}
          </div>
          <div v-else>
            <div>{{ body.description }}</div>
          </div>
        </v-card-text>
      </v-card>
    </div>

    <v-row justify="center">
      <v-dialog
        v-model="dialog"
        fullscreen
        hide-overlay
        transition="dialog-bottom-transition"
      >
        <v-card>
          <v-toolbar dark color="primary">
            <v-btn icon dark @click="closeDialog()">
              <v-icon>mdi-close</v-icon>
            </v-btn>
            <v-toolbar-title>Device List</v-toolbar-title>
            <v-spacer></v-spacer>
            <v-toolbar-items>
              <v-btn dark text @click="closeDialog()">
                Save
              </v-btn>
            </v-toolbar-items>
          </v-toolbar>
          <div v-for="deviceItem in deviceList" :key="deviceItem.id">
            <v-checkbox
              v-model="value"
              :value="deviceItem.id"
              :label="deviceItem.displayName"
            ></v-checkbox>
          </div>
        </v-card>
      </v-dialog>
    </v-row>
    <br />
    <br />
  </div>
</template>

<script>
export default {
  name: 'AppDeviceSelect',
  props: ['value', 'body', 'devices'],
  data() {
    return {
      deviceList: {},
      //selectedDevices: [],
      dialog: false
    };
  },
  methods: {
    closeDialog: function() {
      this.dialog = false;
      this.$emit('input', this.value);
    },
    deviceSelectClick: function() {
      fetch(`/api/devices?filter=${this.body.type}`)
        .then(response => response.json())
        .then(data => {
          if (typeof data !== 'undefined' && data != null) {
            this.deviceList = data;
          }
        });

      this.dialog = true;
    }
  }
};
</script>

<style scoped></style>
