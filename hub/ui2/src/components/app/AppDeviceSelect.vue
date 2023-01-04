<template>
  <div>
    <div v-if="body.multiple">
      <div class="card">
        <div class="card-body">
          <h5 class="card-title">
            <a href="#" class="stretched-link" @click="deviceSelectClick"></a>
            {{ body.title }}
          </h5>
          <div class="card-text">
            <div v-if="value">
              <div v-for="settingVal in value" :key="settingVal">
                {{ devices[settingVal.trim()] }}
              </div>
            </div>
            <div v-else>
              <div>{{ body.description }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else>
      <div class="card">
        <div class="card-body">
          <h5 class="card-title">
            <a href="#" class="stretched-link" @click="deviceSelectClick"></a
            >{{ body.title }}
          </h5>
          <div class="card-text">
            <div v-if="value">
              {{ devices[value.trim()] }}
            </div>
            <div v-else>
              <div>{{ body.description }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row" justify="center">
      <div class="modal" tabindex="-1" :visible="dialog">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Device List</h5>
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>
            <div class="modal-body">
              <div v-for="deviceItem in deviceList" :key="deviceItem.id">
                <div class="form-check">
                  <input
                    v-bind="value"
                    class="form-check-input"
                    type="checkbox"
                    :value="deviceItem.id"
                  />
                  <label class="form-check-label">
                    {{ deviceItem.displayName }}
                  </label>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button
                type="button"
                class="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                Close
              </button>
              <button type="button" class="btn btn-primary">
                Save changes
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "AppDeviceSelect",
  props: ["value", "body", "devices"],
  data() {
    return {
      deviceList: {},
      //selectedDevices: [],
      dialog: false,
      modalVisible: false,
    };
  },
  methods: {
    closeDialog: function () {
      this.dialog = false;
      this.$emit("input", this.value);
    },
    deviceSelectClick: function () {
      console.log("device select")
      fetch(`/api/devices?filter=${this.body.type}`)
        .then((response) => response.json())
        .then((data) => {
          if (typeof data !== "undefined" && data != null) {
            this.deviceList = data;
          }
        });

      this.dialog = true;
    },
  },
};
</script>

<style scoped></style>
