<template>
  <div class="container-fluid">
    <div class="row gy-3">
      <div class="col-12">
        <div
          ref="alertBox"
          class="alert alert-danger alert-dismissible"
          role="alert"
          v-if="alertMessage"
        >
          {{ alertMessage }}
          <button
            type="button"
            class="btn-close"
            aria-label="Close"
            @click="alertMessage = null"
          ></button>
        </div>
      </div>

      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Information</h5>
            <div class="card-text">
              <table class="table">
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
              </table>
            </div>
          </div>
        </div>
      </div>
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Commands</h5>
            <div class="card-text">
              <ul v-for="(command, i) in commands" :key="i">
                <device-command
                  :deviceId="device.id"
                  :command="command"
                ></device-command>
              </ul>
            </div>
          </div>
        </div>
      </div>
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Settings</h5>
            <div class="card-text">
              <form>
                <div class="mb-3">
                  <label for="nameInput" class="form-label">Name</label>
                  <input
                    type="text"
                    id="nameInput"
                    class="form-control"
                    v-model="device.name"
                  />
                </div>
                <div class="mb-3">
                  <label for="labelInput" class="form-label">Label</label>
                  <input
                    type="text"
                    id="labelInput"
                    class="form-control"
                    v-model="device.label"
                  />
                </div>
                <div class="mb-3">
                  <label for="deviceNetworkIdInput" class="form-label"
                    >Device Network ID</label
                  >
                  <input
                    type="text"
                    id="deviceNetworkIdInput"
                    class="form-control"
                    v-model="device.deviceNetworkId"
                  />
                </div>

                <div class="mb-3">
                  <label class="form-label">Integration</label>
                  <select
                    class="form-select"
                    aria-label="Integration"
                    v-model="device.integrationId"
                  >
                    <option
                      v-for="integration in integrations"
                      :key="integration.id"
                      :value="integration.id"
                    >
                      {{ integration.name }}
                    </option>
                  </select>
                </div>

                <div class="mb-3">
                  <label class="form-label">Type</label>

                  <div class="input-group mb-3">
                    <button
                      class="btn btn-outline-secondary css-tooltip"
                      type="button"
                      data-tooltip="Filter type based on selected integration"
                      @click="dhFiltering = !dhFiltering"
                    >
                      <span
                        :class="dhFiltering ? 'bi-funnel-fill' : 'bi-funnel'"
                      ></span>
                    </button>
                    <select
                      class="form-select"
                      aria-label="Type"
                      v-model="device.deviceHandlerId"
                    >
                      <option
                        v-for="dth in filteredDevices"
                        :key="dth.id"
                        :value="dth.id"
                      >
                        {{ dth.name }} ({{ dth.namespace }})
                      </option>
                    </select>
                  </div>
                </div>
              </form>

              <hr />

              <h5 class="card-title">Preferences</h5>
              <form>
                <div v-for="(section, i) in preferences.sections" :key="i">
                  <div v-for="(body, j) in section.body" :key="j">
                    <div
                      v-if="body.type === 'bool'"
                      class="form-check form-switch mb-3"
                    >
                      <input
                        class="form-check-input"
                        type="checkbox"
                        role="switch"
                        v-model="settings[body.name].value"
                      />
                      <label class="form-check-label">{{ body.title }}</label>
                    </div>
                    <div v-if="body.type === 'decimal'" class="mb-3">
                      <label class="form-label">{{ body.title }}</label>
                      <input
                        type="number"
                        step="any"
                        class="form-control"
                        v-model="settings[body.name].value"
                      />
                    </div>

                    <div v-if="body.type === 'email'">
                      <label class="form-label">{{ body.title }}</label>
                      <input
                        type="email"
                        class="form-control"
                        v-model="settings[body.name].value"
                      />
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
                      <label class="form-label">{{ body.title }}</label>
                      <input
                        type="number"
                        step="1"
                        class="form-control"
                        v-model="settings[body.name].value"
                      />
                    </div>
                    <div v-if="body.type === 'password'">
                      <label class="form-label">{{ body.title }}</label>
                      <input
                        type="password"
                        class="form-control"
                        v-model="settings[body.name].value"
                      />
                    </div>
                    <div v-if="body.type === 'phone'">
                      <label class="form-label">{{ body.title }}</label>
                      <input
                        type="tel"
                        class="form-control"
                        v-model="settings[body.name].value"
                      />
                    </div>
                    <div v-if="body.type === 'time'">
                      <label class="form-label">{{ body.title }}</label>
                      <input
                        type="time"
                        class="form-control"
                        v-model="settings[body.name].value"
                      />
                    </div>
                    <div v-if="body.type === 'text' || body.type === 'string'">
                      <div class="mb-3">
                        <label class="form-label">{{ body.title }}</label>
                        <input
                          type="text"
                          class="form-control"
                          v-model="settings[body.name].value"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </form>
            </div>

            <div class="row g-3">
              <div class="col-auto me-auto">
                <button
                  type="button"
                  class="btn btn-primary"
                  @click="saveDevice"
                >
                  Save
                </button>
              </div>
              <div class="col-auto">
                <are-you-sure-dialog
                  title="Are you sure?"
                  body="Are you sure you want to delete this device?"
                  confirm-button="Delete"
                  @confirm-action="deleteDevice"
                ></are-you-sure-dialog>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <generic-modal
      ref="deleteDeviceModal"
      title="Delete Device"
      :displayModal="deleteDeviceDisplayModal"
      ><template #content
        ><div v-if="displayForceDeleteDevice">
          The device does not appear to be responding
        </div>
        <div v-else>Deleting device, please wait...</div></template
      >
      <template #buttonSlot
        ><button
          type="button"
          class="btn btn-danger"
          @click="forceDeleteDevice()"
          :disabled="!displayForceDeleteDevice"
        >
          Force Delete
        </button></template
      >
    </generic-modal>
  </div>
</template>
<script>
import DeviceCommand from "@/components/device/DeviceCommand.vue";
import EnumInput from "@/components/device/DeviceEnumInput.vue";
import AreYouSureDialog from "@/components/common/AreYouSureDialog.vue";
import GenericModal from "@/components/common/GenericModal.vue";

export default {
  name: "DeviceView",
  data() {
    return {
      deviceId: "",
      device: {},
      deviceHandlers: {},
      dhFiltering: false,
      integrations: {},
      preferences: {},
      settings: {},
      commands: [],
      currentStates: {},
      information: {},
      deleteModal: null,
      deleteAreYouSure: true,
      deleteDeviceDisplayModal: false,
      displayForceDeleteDevice: false,
      alertMessage: null,
    };
  },
  components: {
    DeviceCommand,
    EnumInput,
    AreYouSureDialog,
    GenericModal,
  },
  computed: {
    filteredDevices: function () {
      if (this.dhFiltering && this.device.integrationId != null) {
        var integration = this.integrations.find(
          (i) => i.id == this.device.integrationId
        );
        if (integration.tags != null) {
          return this.deviceHandlers.filter(
            (dh) =>
              (dh.tags != null &&
                integration.tags.some((t) => dh.tags.indexOf(t) >= 0)) ||
              this.device.deviceHandlerId == dh.id
          );
        } else {
          return this.deviceHandlers;
        }
      } else {
        return this.deviceHandlers;
      }
    },
  },
  methods: {
    updatePreferenceLayout: function () {
      fetch(`/api/devices/${this.deviceId}/preferences-layout`)
        .then((response) => {
          if (response) {
            return response.json();
          } else {
            return {};
          }
        })
        .then((data) => {
          this.preferences = data;
          if (data.sections) {
            for (var section of data.sections) {
              if (section.input != null) {
                for (var input of section.input) {
                  if (typeof this.settings[input.name] === "undefined") {
                    this.settings[input.name] = {
                      name: input.name,
                      valueAsType:
                        input.defaultValue == null ||
                        typeof input.defaultValue === "undefined"
                          ? null
                          : input.defaultValue,
                      type: input.type,
                      multiple: input.multiple ? true : false,
                    };
                  } else if (
                    this.settings[input.name].valueAsType == null &&
                    input.defaultValue != null
                  ) {
                    this.settings[input.name].valueAsType = input.defaultValue;
                  }
                }
              }
            }
          }
        });
    },
    saveDevice: function () {
      var body = { device: this.device, settings: this.settings };
      fetch(`/api/devices/${this.deviceId}`, {
        method: "PUT",
        body: JSON.stringify(body),
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
            this.updatePreferenceLayout();
          } else {
            console.log("problem saving device");
          }
        });
    },
    deleteDevice: function () {
      this.$refs.deleteDeviceModal.displayModal();
      this.displayForceDeleteDevice = false;
      fetch(`/api/devices/${this.deviceId}`, { method: "DELETE" })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
            this.$refs.deleteDeviceModal.hideModal();
            this.$router.push("/devices");
          } else {
            this.displayForceDeleteDevice = true;
          }
        });
    },
    forceDeleteDevice: function () {
      this.displayForceDeleteDevice = false;
      fetch(`/api/devices/${this.deviceId}?force=true`, { method: "DELETE" })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
            this.$refs.deleteDeviceModal.hideModal();
            this.$router.push("/devices");
          } else {
            this.$refs.deleteDeviceModal.hideModal();
            this.alertMessage = "Unable to delete device";
            window.scrollTo(0, 0);
          }
        });
    },
  },
  mounted: function () {
    //this.deleteModal = new Modal(this.$refs.deviceDeleteModal);
    this.deviceId = this.$route.params.id;

    fetch(
      "/api/integrations?type=DEVICE&field=id&field=name&field=label&field=tags"
    )
      .then((response) => response.json())
      .then((data) => {
        if (data != null) {
          this.integrations = data;
          // add blank option
          this.integrations.push({ id: null, name: "None", label: "None" });
        }
      });

    fetch(`/api/devices/${this.deviceId}`)
      .then((response) => response.json())
      .then((data) => {
        if (data != null) {
          this.device = data;
        }
      });

    fetch(`/api/devices/${this.deviceId}/commands`)
      .then((response) => response.json())
      .then((data) => {
        if (data != null) {
          for (var cmd of data) {
            if (cmd.arguments) {
              for (var arg of cmd.arguments) {
                arg.value = null;
              }
            }
          }
          this.commands = data;
        }
      });

    fetch(`/api/devices/${this.deviceId}/states`)
      .then((response) => response.json())
      .then((data) => {
        if (data != null) {
          this.currentStates = data;
        }
      });

    fetch(`/api/devices/${this.deviceId}/information`)
      .then((response) => response.json())
      .then((data) => {
        if (data != null) {
          this.information = data;
        }
      });

    fetch(`/api/device-handlers?field=id&field=name&field=namespace&field=tags`)
      .then((response) => response.json())
      .then((data) => {
        if (data != null) {
          this.deviceHandlers = data;
        }
      });

    fetch(`/api/devices/${this.deviceId}/settings`)
      .then((response) => response.json())
      .then((data) => {
        if (data != null) {
          this.settings = data;
        }
      })
      .then(this.updatePreferenceLayout());

    let connection = new WebSocket(
      `ws://${window.location.host}/api/devices/${this.deviceId}/events`
    );
    connection.onmessage = (event) => {
      var eventMap = JSON.parse(event.data);

      var matched = false;
      this.currentStates.forEach(function (item) {
        if (item.name == eventMap.name) {
          item.stringValue = eventMap.value;
          matched = true;
        }
      });
      if (!matched) {
        this.currentStates.push({
          name: eventMap.name,
          value: eventMap.value,
          stringValue: eventMap.value,
        });
      }
    };
  },
};
</script>
<style scoped></style>
