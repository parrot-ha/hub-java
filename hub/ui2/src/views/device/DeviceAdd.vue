<template>
  <div class="container-fluid">
    <div class="row gy-3">
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
                  @click="addDevice"
                >
                  Add
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import EnumInput from "@/components/device/DeviceEnumInput.vue";

export default {
  name: "DeviceAdd",
  data() {
    return {
      device: { name: "", label: "", deviceNetworkId: "", deviceHandlerId: "" },
      dhFiltering: false,
      deviceHandlers: {},
      integrations: {},
      preferences: {},
      settings: {},
    };
  },
  components: {
    EnumInput,
  },
  watch: {
    "device.deviceHandlerId": function () {
      this.updatePreferenceLayout();
    },
  },
  computed: {
    filteredDevices: function () {
      if (this.dhFiltering && this.device.integrationId != null) {
        console.log("filtering");
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
      // TODO: validate the deviceHandlerId value is valid before calling
      fetch(
        `/api/device-handlers/${this.device.deviceHandlerId}/preferences-layout`
      )
        .then((response) => {
          if (response) {
            return response.json();
          } else {
            return {};
          }
        })
        .then((data) => {
          this.preferences = data;
          this.settings = {};
          if (data.sections) {
            for (var section of data.sections) {
              for (var input of section.input) {
                if (typeof this.settings[input.name] === "undefined") {
                  //TODO: is empty string ok, or should it be null?
                  this.settings[input.name] = {
                    name: input.name,
                    value: null,
                    type: input.type,
                    multiple: input.multiple ? true : false,
                  };
                }
              }
            }
          }
        });
    },
    addDevice: function () {
      var body = { device: this.device, settings: this.settings };
      console.log(JSON.stringify(body));

      fetch(`/api/devices`, {
        method: "POST",
        body: JSON.stringify(body),
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
            console.log("success " + data.deviceId);
            this.$router.push(`/devices/${data.deviceId}`);
          } else {
            console.log("problem adding device");
          }
        });
    },
  },
  mounted: function () {
    fetch(
      "/api/integrations?type=DEVICE&field=id&field=name&field=label&field=tags"
    )
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.integrations = data;
          // add blank option
          this.integrations.push({ id: null, name: "None", label: "None" });
        }
      });

    fetch(`/api/device-handlers?field=id&field=name&field=namespace&field=tags`)
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.deviceHandlers = data;
        }
      });
  },
};
</script>
<style scoped></style>
