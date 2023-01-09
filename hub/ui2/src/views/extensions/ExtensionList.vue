<template>
  <div class="container-fluid">
    <h5>Extensions</h5>

    <ul class="nav nav-tabs">
      <li class="nav-item">
        <a
          :class="'nav-link' + (tab == 'INT' ? ' active' : '')"
          :aria-current="tab == 'INT'"
          href="#"
          @click="tab = 'INT'"
          >Installed</a
        >
      </li>
      <li class="nav-item">
        <a
          :class="'nav-link' + (tab == 'AVL' ? ' active' : '')"
          :aria-current="tab == 'AVL'"
          href="#"
          @click="tab = 'AVL'"
          >Available</a
        >
      </li>
      <li class="nav-item">
        <a
          :class="'nav-link' + (tab == 'LOC' ? ' active' : '')"
          :aria-current="tab == 'LOC'"
          href="#"
          @click="tab = 'LOC'"
          >Locations</a
        >
      </li>
    </ul>
    <br />
    <div class="tab-content">
      <div v-if="tab == 'INT'">
        <table class="table">
          <thead>
            <tr>
              <th scope="col" style="width: 15%">Actions</th>
              <th scope="col" style="width: 20%">Name</th>
              <th scope="col" style="width: 55%">Description</th>
              <th scope="col" style="width: 10%">Version</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="extension in installedExtensions" :key="extension.id">
              <td>
                <div>
                  <div class="btn-group">
                    <are-you-sure-dialog
                      button-class="btn-sm btn-outline-primary"
                      title="Delete Extension"
                      body="Are you sure you want to delete this extension?"
                      confirm-button="Delete"
                      @confirm-action="deleteExtension(extension.id)"
                      ><i class="bi bi-trash"></i
                    ></are-you-sure-dialog>
                    <button
                      v-if="extension.updateAvailable"
                      type="button"
                      class="btn btn-sm btn-outline-primary css-tooltip"
                      @click="updateExtension(extension.id)"
                      :data-tooltip="
                        'Update to version ' + extension.updateInfo?.version
                      "
                    >
                      <i class="bi bi-cloud-download"></i>
                    </button>
                  </div>
                </div>
              </td>
              <td>
                {{ extension.name }}
              </td>
              <td>{{ extension.description }}</td>
              <td>{{ extension.version }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else-if="tab == 'AVL'">
        <table class="table">
          <thead>
            <tr>
              <th scope="col" style="width: 5%">Actions</th>
              <th scope="col" style="width: 20%">Name</th>
              <th scope="col" style="width: 65%">Description</th>
              <th scope="col" stype="width: 10%">Version</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="extension in availableExtensions" :key="extension.id">
              <td>
                <button
                  type="button"
                  class="btn btn-sm btn-outline-primary"
                  @click="downloadExtension(extension.id)"
                >
                  <i class="bi bi-cloud-download"></i>
                </button>
              </td>
              <td>{{ extension.name }}</td>
              <td>{{ extension.description }}</td>
              <td>{{ extension.version }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else-if="tab == 'LOC'">
        <div class="row g-3">
          <div class="col-auto me-auto"></div>
          <div class="col-auto">
            <extension-location-edit
              @location-saved="locationsUpdated"
            ></extension-location-edit>
          </div>
        </div>
        <table class="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Type</th>
              <th>Location</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="location in locations" :key="location.id">
              <td>
                {{ location.name }}
              </td>
              <td>{{ location.type }}</td>
              <td>{{ location.location }}</td>
              <td>
                <div class="btn-group">
                  <are-you-sure-dialog
                    button-class="btn-sm btn-outline-primary"
                    title="Delete Extension Location"
                    body="Are you sure you want to delete this extension location?"
                    confirm-button="Delete"
                    @confirm-action="deleteExtensionLocation(location.id)"
                    ><i class="bi bi-trash"></i
                  ></are-you-sure-dialog>
                  <extension-location-edit
                    button-class="btn-sm btn-outline-primary"
                    :id="location.id"
                    :name="location.name"
                    :type="location.type"
                    :location="location.location"
                    @location-saved="locationsUpdated"
                    ><i class="bi bi-pencil"></i
                  ></extension-location-edit>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
<script>
import AreYouSureDialog from "@/components/common/AreYouSureDialog.vue";
import ExtensionLocationEdit from "@/components/extensions/ExtensionLocationEdit.vue";

function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

export default {
  name: "ExtensionList",
  data() {
    return {
      tab: "INT",
      extensions: [],
      locations: [],
    };
  },
  components: {
    AreYouSureDialog,
    ExtensionLocationEdit,
  },
  computed: {
    installedExtensions: function () {
      return this.extensions.filter(function (ext) {
        return ext.installed === true;
      });
    },
    availableExtensions: function () {
      return this.extensions.filter(function (ext) {
        return ext.installed !== true;
      });
    },
  },
  watch: {},

  methods: {
    loadExtensions: function (doRefresh) {
      fetch(`/api/extensions?refresh=${doRefresh}`)
        .then((response) => response.json())
        .then((data) => {
          if (typeof data !== "undefined" && data != null) {
            this.extensions = data;
          }
        });
    },

    loadExtensionLocations: function () {
      fetch("/api/extension_locations")
        .then((response) => response.json())
        .then((data) => {
          if (typeof data !== "undefined" && data != null) {
            this.locations = data;
          }
        });
    },

    updateExtension: function (extensionId) {
      var url = `/api/extensions/${extensionId}?action=update`;
      //TODO: handle response
      fetch(url, {
        method: "POST",
        body: null,
      })
        .then(handleErrors)
        .then(() => {
          this.loadExtensions(true);
        });
    },

    downloadExtension: function (extensionId) {
      var url = `/api/extensions/${extensionId}?action=download`;
      //TODO: handle response
      fetch(url, {
        method: "POST",
        body: null,
      })
        .then(handleErrors)
        .then(() => {
          this.loadExtensions(true);
        });
    },
    deleteExtension: function (extensionId) {
      var url = `/api/extensions/${extensionId}`;
      //TODO: handle response
      fetch(url, {
        method: "DELETE",
        body: null,
      })
        .then(handleErrors)
        .then(() => {
          this.loadExtensions(true);
        });
    },
    deleteExtensionLocation(locationId) {
      var url = `/api/extension_locations/${locationId}`;
      //TODO: handle response
      fetch(url, {
        method: "DELETE",
        body: null,
      })
        .then(handleErrors)
        .then(() => {
          this.loadExtensionLocations();
          this.loadExtensions(false);
        });
    },
    locationsUpdated() {
      this.$nextTick(() => {
        this.loadExtensionLocations();
        this.loadExtensions(false);
      });
    },
  },
  mounted: function () {
    this.loadExtensions(false);
    this.loadExtensionLocations();
  },
};
</script>
