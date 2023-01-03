<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col :cols="12">
          <v-card>
            <v-card-title>Settings</v-card-title>
            <v-card-text>
              <v-form>
                <v-text-field
                  label="Name"
                  v-model="device.name"
                  required
                ></v-text-field>
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
                  :items="deviceHandlers"
                  :item-text="item => item.name + ' (' + item.namespace + ')'"
                  item-value="id"
                  label="Type"
                  v-model="device.deviceHandlerId"
                ></v-select>
              </v-form>
            </v-card-text>

            <v-divider></v-divider>
            <v-card-title>Preferences</v-card-title>
            <v-card-text>
              <v-form>
                <div v-for="(section, i) in preferences.sections" :key="i">
                  <div v-for="(body, j) in section.body" :key="j">
                    <div v-if="body.displayDuringSetup === true">
                      <div v-if="body.type === 'bool'">
                        <v-switch
                          :id="body.name"
                          :name="body.name"
                          :label="body.title"
                          v-model="settings[body.name].value"
                        ></v-switch>
                      </div>
                      <div v-if="body.type === 'decimal'">
                        <v-text-field
                          type="number"
                          step="any"
                          :id="body.name"
                          :name="body.name"
                          :label="body.title"
                          v-model="settings[body.name].value"
                        ></v-text-field>
                      </div>
                      <div v-if="body.type === 'email'">
                        <v-text-field
                          type="email"
                          :id="body.name"
                          :name="body.name"
                          :label="body.title"
                          v-model="settings[body.name].value"
                        ></v-text-field>
                      </div>
                      <div v-if="body.type === 'enum'">
                        <enum-input
                          v-if="settings[body.name]"
                          v-bind:options="body.options"
                          v-bind:body="body"
                          v-model="settings[body.name].value"
                        ></enum-input>
                      </div>
                      <div v-if="body.type === 'number'">
                        <v-text-field
                          type="number"
                          step="1"
                          :id="body.name"
                          :name="body.name"
                          :label="body.title"
                          v-model="settings[body.name].value"
                        ></v-text-field>
                      </div>
                      <div v-if="body.type === 'password'">
                        <v-text-field
                          type="password"
                          :label="body.title"
                          :id="body.name"
                          :name="body.name"
                          v-model="settings[body.name].value"
                        ></v-text-field>
                      </div>
                      <div v-if="body.type === 'phone'">
                        <v-text-field
                          type="tel"
                          :id="body.name"
                          :name="body.name"
                          :label="body.title"
                          v-model="settings[body.name].value"
                        ></v-text-field>
                      </div>
                      <div v-if="body.type === 'time'">
                        <v-text-field
                          type="time"
                          :id="body.name"
                          :name="body.name"
                          :label="body.title"
                          v-model="settings[body.name].value"
                        ></v-text-field>
                      </div>
                      <div
                        v-if="body.type === 'text' || body.type === 'string'"
                      >
                        <v-text-field
                          :id="body.name"
                          :name="body.name"
                          :label="body.title"
                          v-model="settings[body.name].value"
                        ></v-text-field>
                      </div>
                    </div>
                  </div>
                </div>
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-btn color="primary" @click="addDevice">
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
import EnumInput from '@/components/device/DeviceEnumInput';

export default {
  name: 'Device Add',
  data() {
    return {
      device: { name: '', label: '', deviceNetworkId: '', deviceHandlerId: '' },
      deviceHandlers: {},
      integrations: {},
      preferences: {},
      settings: {}
    };
  },
  components: {
    EnumInput
  },
  watch: {
    'device.deviceHandlerId': function(val) {
      this.updatePreferenceLayout();
    }
  },
  methods: {
    updatePreferenceLayout: function() {
      // TODO: validate the deviceHandlerId value is valid before calling
      fetch(
        `/api/device-handlers/${this.device.deviceHandlerId}/preferences-layout`
      )
        .then(response => {
          if (response) {
            return response.json();
          } else {
            return {};
          }
        })
        .then(data => {
          this.preferences = data;
          this.settings = {};
          if (data.sections) {
            for (var section of data.sections) {
              for (var input of section.input) {
                if (typeof this.settings[input.name] === 'undefined') {
                  //TODO: is empty string ok, or should it be null?
                  this.settings[input.name] = {
                    name: input.name,
                    value: null,
                    type: input.type,
                    multiple: input.multiple ? true : false
                  };
                }
              }
            }
          }
        });
    },
    addDevice: function() {
      var body = { device: this.device, settings: this.settings };
      console.log(JSON.stringify(body));

      fetch(`/api/devices`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            console.log('success ' + data.deviceId);
            this.$router.push(`/devices/${data.deviceId}`);
          } else {
            console.log('problem adding device');
          }
        });
    }
  },
  mounted: function() {
    fetch('/api/integrations?field=id&field=name&field=label')
      .then(response => response.json())
      .then(data => {
        if (data != null) {
          this.integrations = data;
          // add blank option
          this.integrations.push({ id: null, name: 'None', label: 'None' });
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
