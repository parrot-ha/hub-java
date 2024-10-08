<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-auto me-auto">
        <h5>{{ page.title }}</h5>
      </div>
      <div class="col-auto">
        <are-you-sure-dialog
          title="Are you sure?"
          body="Are you sure you want to uninstall this Automation
                    App?"
          confirm-button="Uninstall"
          @confirm-action="uninstallClick"
          >Uninstall</are-you-sure-dialog
        >
      </div>
    </div>

    <div v-for="(section, i) in page.sections" :key="i">
      <br />
      <h3>{{ section.title }}</h3>
      <div v-for="(body, j) in section.body" :key="j">
        <div
          v-if="body.element === 'input' && body.type.startsWith('capability')"
        >
          <!-- display a device input -->
          <device-select
            v-if="settings[body.name]"
            v-model="settings[body.name].value"
            :body="body"
            :devices="devices"
          ></device-select>
        </div>
        <div v-if="body.type === 'app'">
          <child-app v-bind:body="body"></child-app>
        </div>
        <div v-if="body.type === 'enum'">
          <enum-input
            v-if="settings[body.name]"
            v-bind:options="body.options"
            v-bind:body="body"
            v-model="settings[body.name].value"
          ></enum-input>
        </div>
        <div v-if="body.type === 'bool'">
          <bool-input
            v-if="settings[body.name]"
            v-bind:body="body"
            v-model:checked="settings[body.name].value"
          ></bool-input>
        </div>
        <div v-if="body.type === 'email'">
          <email-input
            v-if="settings[body.name]"
            v-bind:body="body"
            v-model="settings[body.name].value"
          ></email-input>
        </div>
        <div v-if="body.type === 'number'">
          <number-input
            v-if="settings[body.name]"
            :body="body"
            v-model="settings[body.name].value"
          ></number-input>
        </div>
        <div v-if="body.type === 'text' && body.element === 'input'">
          <text-input
            v-if="settings[body.name]"
            v-bind:body="body"
            v-model="settings[body.name].value"
          ></text-input>
        </div>
        <div v-if="body.type === 'password'">
          <password-input
            v-if="settings[body.name]"
            v-bind:body="body"
            v-model="settings[body.name].value"
          ></password-input>
        </div>
        <div v-if="body.type === 'time'">
          <time-input
            v-if="settings[body.name]"
            v-bind:body="body"
            v-model="settings[body.name].value"
          ></time-input>
        </div>
        <div v-if="body.type === 'paragraph'">
          <paragraph-element v-bind:body="body"></paragraph-element>
        </div>
        <div v-if="body.element === 'href'">
          <href-element
            v-bind:body="body"
            v-on:hrefPage="hrefClick"
          ></href-element>
        </div>
        <div v-if="body.type === 'text' && body.element === 'label'">
          ADD LABEL INPUT
        </div>
      </div>
    </div>
    <br />
    <div v-if="page.defaults">DEFAULTS GO HERE (MODE AND NAME)</div>

    <div class="row g-3">
      <div class="col-auto me-auto">
        <button
          class="btn btn-primary"
          v-if="doneButtonVisible"
          @click="doneClick"
        >
          Done
        </button>
      </div>
      <div class="col-auto">
        <button
          v-if="page.nextPage && page.install == false"
          class="btn btn-primary"
          @click="nextClick"
        >
          Next
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import { pathToRegexp } from "path-to-regexp";

import BoolInput from "@/components/app/AppBoolInput.vue";
import ChildApp from "@/components/app/AppChildApp.vue";
import DeviceSelect from "@/components/app/AppDeviceSelect.vue";
import EmailInput from "@/components/app/AppEmailInput.vue";
import EnumInput from "@/components/app/AppEnumInput.vue";
import HrefElement from "@/components/app/AppHrefElement.vue";
import NumberInput from "@/components/app/AppNumberInput.vue";
import PasswordInput from "@/components/app/AppPasswordInput.vue";
import TimeInput from "@/components/app/AppTimeInput.vue";
import TextInput from "@/components/app/AppTextInput.vue";
import ParagraphElement from "@/components/app/AppParagraphElement.vue";
import AreYouSureDialog from "@/components/common/AreYouSureDialog.vue";

export default {
  name: "InstalledAutomationAppConfig",
  components: {
    BoolInput,
    ChildApp,
    DeviceSelect,
    EmailInput,
    EnumInput,
    HrefElement,
    NumberInput,
    ParagraphElement,
    PasswordInput,
    TextInput,
    TimeInput,
    AreYouSureDialog,
  },
  data() {
    return {
      loading: true,
      iaaId: "",
      page: {},
      settings: {},
      savedSettings: {},
      previousSettings: {},
      devices: {},
      breadcrumb: [],
      refreshFunction: null,
      watchers: false,
    };
  },
  computed: {
    doneButtonVisible: function () {
      return (
        this.page.defaults ||
        this.page.install == true ||
        (this.page.install == false && this.page.nextPage == null)
      );
    },
  },
  watch: {
    $route() {
      // react to route changes...
      this.processRoute();
    },
  },
  methods: {
    hrefClick: function (pageName) {
      this.nextPageNavigate(pageName);
    },
    nextPageNavigate: function (pageName) {
      fetch(`/api/iaas/${this.iaaId}/cfg/settings`, {
        method: "PATCH",
        body: JSON.stringify(this.settings),
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
            // get next page
            this.breadcrumb.push(pageName);

            var newPath = this.breadcrumb.join("/");
            this.$router.push(`/iaas/${this.iaaId}/cfg/${newPath}`);
            this.loadInformation();
          } else {
            console.log("problem saving automation app");
          }
        });
    },
    nextClick: function () {
      this.nextPageNavigate(this.page.nextPage);
    },
    doneClick: function () {
      if (this.page.install == false) {
        fetch(`/api/iaas/${this.iaaId}/cfg/settings`, {
          method: "PATCH",
          body: JSON.stringify(this.settings),
        })
          .then((response) => response.json())
          .then((data) => {
            if (data.success) {
              // need to go back a page.
              this.breadcrumb.pop();
              var newPath = this.breadcrumb.join("/");
              this.$router.push(`/iaas/${this.iaaId}/cfg/${newPath}`);
              this.loadInformation();
            } else {
              //TODO: popup for user
              console.log("problem saving automation app");
            }
          });
      } else {
        fetch(`/api/iaas/${this.iaaId}/cfg/settings`, {
          method: "POST",
          body: JSON.stringify(this.settings),
        })
          .then((response) => response.json())
          .then((data) => {
            if (data.success) {
              this.$router.push("/iaas");
            } else {
              console.log("problem saving automation app");
            }
          });
      }
    },
    uninstallClick: function () {
      fetch(`/api/iaas/${this.iaaId}`, {
        method: "DELETE",
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.success) {
            this.$router.push("/iaas");
          } else {
            console.log("problem deleting automation app");
          }
        });
    },
    populateSettingsFromInputs: function () {
      if (Object.keys(this.settings).length == 0) {
        this.settings = JSON.parse(JSON.stringify(this.savedSettings));
        this.previousSettings = JSON.parse(JSON.stringify(this.savedSettings));
      } else if (Object.keys(this.savedSettings).length > 0) {
        for (var ssKey of Object.keys(this.savedSettings)) {
          if (
            this.savedSettings[ssKey]?.value !=
              this.previousSettings[ssKey]?.value &&
            this.previousSettings[ssKey]?.value == this.settings[ssKey]?.value
          ) {
            // the value changed on the hub and not on the ui.
            this.settings[ssKey] = JSON.parse(
              JSON.stringify(this.savedSettings[ssKey])
            );
          }
        }
      }
      for (var section of this.page.sections) {
        for (var input of section.input) {
          if (typeof this.settings[input.name] === "undefined") {
            //TODO: is empty string ok, or should it be null?
            this.settings[input.name] = {
              name: input.name,
              value: input.defaultValue
                ? input.defaultValue
                : input.multiple
                ? []
                : null,
              type: input.type,
              multiple: input.multiple,
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
                  this.settings[input.name].value === ""
                ) {
                  this.settings[input.name].value = [];
                } else if (!Array.isArray(this.settings[input.name].value)) {
                  this.settings[input.name].value = Array.from(
                    this.settings[input.name].value
                  );
                }
              }
              //TODO: handle multiple going from true to false
            }
          }
          // watch any settings that have submit on change
          if (input.submitOnChange) {
            if (!this.watchers) {
              this.$watch(`settings.${input.name}.value`, () => {
                fetch(`/api/iaas/${this.iaaId}/cfg/settings`, {
                  method: "PATCH",
                  body: JSON.stringify(this.settings),
                })
                  .then((response) => response.json())
                  .then((data) => {
                    if (data.success) {
                      fetch(`/api/iaas/${this.iaaId}/cfg/settings`)
                        .then((response) => response.json())
                        .then((data) => {
                          if (typeof data !== "undefined" && data != null) {
                            this.savedSettings = data;
                            this.loadPage();
                          }
                        });
                    } else {
                      //TODO: popup for user
                      console.log("problem saving automation app");
                    }
                  });
              });
            }
          }
        }
      }
      this.watchers = true;
    },
    loadInformation: function () {
      fetch("/api/device-id-map")
        .then((response) => response.json())
        .then((data) => {
          if (typeof data !== "undefined" && data != null) {
            this.devices = data;
          }
        });

      fetch(`/api/iaas/${this.iaaId}/cfg/settings`)
        .then((response) => response.json())
        .then((data) => {
          if (typeof data !== "undefined" && data != null) {
            this.savedSettings = data;
            this.loadPage();
          }
        });
    },
    loadPage: function () {
      var vm = this;
      var path = `/api/iaas/${this.iaaId}/cfg/page`;
      if (this.breadcrumb.length > 0)
        path = path
          .concat("/")
          .concat(this.breadcrumb[this.breadcrumb.length - 1]);

      //if (page != null) path = path.concat('/').concat(page);
      fetch(path)
        .then((response) => response.json())
        .then((data) => {
          if (typeof data !== "undefined" && data != null) {
            if (typeof data.defaults === "undefined") {
              this.page = data;
              this.populateSettingsFromInputs();
            } else {
              this.page = data;
              this.populateSettingsFromInputs();
              // TODO: if defaults is set to true, then add mode and name to settings
            }
            if (
              typeof this.page.name !== "undefined" &&
              this.page.name != null &&
              this.page.name !== "" &&
              this.breadcrumb.length == 0
            ) {
              // we have a page, push to router and breadcrumb
              this.breadcrumb.push(this.page.name);
              var newPath = this.breadcrumb.join("/");
              this.$router.push(`/iaas/${this.iaaId}/cfg/${newPath}`);
            }
            if (
              this.refreshFunction == null &&
              typeof this.page.refreshInterval !== "undefined" &&
              this.page.refreshInterval != null &&
              this.page.refreshInterval != "" &&
              this.page.refreshInterval > 0
            ) {
              this.refreshFunction = setInterval(function () {
                this.refreshFunction = null;
                vm.loadInformation();
              }, vm.page.refreshInterval * 1000);
            }
            this.loading = false;
          }
        });
    },
    processRoute: function () {
      this.iaaId = this.$route.params.id;
      var keys = [];
      var re = pathToRegexp("/iaas/:id/cfg/(.*)", keys);
      var existingPath = re.exec(this.$route.path);
      if (existingPath != null) {
        this.breadcrumb = existingPath[existingPath.length - 1].split("/");
      }

      this.loadInformation();
    },
  },
  mounted: function () {
    this.processRoute();
  },
  beforeUnmount: function () {
    if (this.refreshFunction != null) {
      clearTimeout(this.refreshFunction);
    }
  },
};
</script>
<style scoped></style>
