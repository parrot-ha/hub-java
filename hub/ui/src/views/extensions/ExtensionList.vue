<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title
              >Extensions
              <v-tooltip bottom
                ><template v-slot:activator="{ on }">
                  <v-btn
                    class="ma-2"
                    text
                    icon
                    color="blue lighten-2"
                    @click="loadExtensions(true)"
                    v-on="on"
                  >
                    <v-icon>mdi-refresh</v-icon>
                  </v-btn>
                </template>
                <span>Refresh Extension List</span>
              </v-tooltip>
            </v-card-title>
            <v-card-text>
              <v-tabs v-model="tab">
                <v-tab>Installed</v-tab>
                <v-tab>Available</v-tab>
                <v-tab>Settings</v-tab>
              </v-tabs>
              <v-tabs-items v-model="tab">
                <v-tab-item>
                  <v-card>
                    <v-card-text
                      >Installed Extensions
                      <v-simple-table>
                        <thead>
                          <tr>
                            <th scope="col" style="width:5%">Actions</th>
                            <th scope="col" style="width:20%">Name</th>
                            <th scope="col" style="width:75%">Description</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="extension in installedExtensions"
                            :key="extension.id"
                          >
                            <td>
                              <div>
                                <v-tooltip
                                  bottom
                                  v-if="extension.updateAvailable"
                                  ><template v-slot:activator="{ on }">
                                    <v-btn
                                      class="ma-2"
                                      text
                                      icon
                                      color="blue lighten-2"
                                      @click="updateExtension(extension.id)"
                                      v-on="on"
                                    >
                                      <v-icon
                                        >mdi-cloud-download-outline</v-icon
                                      >
                                    </v-btn>
                                  </template>
                                  <span>Update</span>
                                </v-tooltip>

                                <v-dialog v-model="deleteDialog" width="500">
                                  <template v-slot:activator="{ on, attrs }">
                                    <v-btn
                                      class="ma-2"
                                      text
                                      icon
                                      color="blue lighten-2"
                                      v-bind="attrs"
                                      v-on="on"
                                    >
                                      <v-icon>mdi-delete-outline</v-icon>
                                    </v-btn>
                                  </template>
                                  <v-card>
                                    <v-card-title
                                      class="text-h5 grey lighten-2"
                                    >
                                      Delete Extension
                                    </v-card-title>

                                    <v-card-text>
                                      Are you sure you want to delete this
                                      extension?
                                    </v-card-text>

                                    <v-divider></v-divider>

                                    <v-card-actions>
                                      <v-spacer></v-spacer>
                                      <v-btn
                                        color="primary"
                                        text
                                        @click="deleteDialog = false"
                                      >
                                        Delete
                                      </v-btn>
                                      <v-btn
                                        color="primary"
                                        text
                                        @click="deleteDialog = false"
                                      >
                                        Cancel
                                      </v-btn>
                                    </v-card-actions>
                                  </v-card>
                                </v-dialog>
                              </div>
                            </td>
                            <td>
                              <router-link
                                :to="{
                                  name: 'Extension',
                                  params: { id: extension.id }
                                }"
                                >{{ extension.name }}</router-link
                              >
                            </td>
                            <td>{{ extension.description }}</td>
                          </tr>
                        </tbody>
                      </v-simple-table>
                    </v-card-text>
                  </v-card>
                </v-tab-item>
                <v-tab-item>
                  <v-card>
                    <v-card-text>
                      <v-simple-table>
                        <thead>
                          <tr>
                            <th scope="col" style="width:5%">Actions</th>
                            <th scope="col" style="width:20%">Name</th>
                            <th scope="col" style="width:75%">Description</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="extension in availableExtensions"
                            :key="extension.id"
                          >
                            <td>
                              <v-btn
                                class="ma-2"
                                text
                                icon
                                color="blue lighten-2"
                                @click="downloadExtension(extension.id)"
                              >
                                <v-icon>mdi-cloud-download-outline</v-icon>
                              </v-btn>
                            </td>
                            <td>{{ extension.name }}</td>
                            <td>{{ extension.description }}</td>
                          </tr>
                        </tbody>
                      </v-simple-table>
                    </v-card-text>
                  </v-card>
                </v-tab-item>
                <v-tab-item>
                  <v-card>
                    <v-card-text>
                      <v-simple-table>
                        <thead>
                          <tr>
                            <th scope="col" style="width:5%">Actions</th>
                            <th scope="col" style="width:20%">Name</th>
                            <th scope="col" style="width:20%">Type</th>
                            <th scope="col" style="width:55%">Location</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="setting in settings" :key="setting.id">
                            <td><v-icon>mdi-pencil-outline</v-icon></td>
                            <td>{{ setting.name }}</td>
                            <td>{{ setting.type }}</td>
                            <td>{{ setting.location }}</td>
                          </tr>
                        </tbody>
                      </v-simple-table>
                    </v-card-text>
                  </v-card>
                </v-tab-item>
              </v-tabs-items>
            </v-card-text>
            <v-card-actions> </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-layout>
  </v-container>
</template>
<script>
function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

export default {
  name: 'ExtensionList',
  data() {
    return {
      tab: null,
      extensions: [],
      settings: [],
      deleteDialog: false
    };
  },
  computed: {
    installedExtensions: function() {
      return this.extensions.filter(function(ext) {
        console.log('ext id ' + ext.id);
        console.log('ext installed ' + ext.installed);
        return ext.installed === true;
      });
    },
    availableExtensions: function() {
      return this.extensions.filter(function(ext) {
        return ext.installed !== true;
      });
    }
  },
  methods: {
    loadExtensions: function(doRefresh) {
      fetch(`/api/extensions?refresh=${doRefresh}`)
        .then(response => response.json())
        .then(data => {
          if (typeof data !== 'undefined' && data != null) {
            this.extensions = data;
          }
        });
      fetch('/api/extension_settings')
        .then(response => response.json())
        .then(data => {
          if (typeof data !== 'undefined' && data != null) {
            this.settings = data;
          }
        });
    },
    updateExtension: function(extensionId) {
      var url = `/api/extensions/${extensionId}?action=update`;
      //TODO: handle response
      fetch(url, {
        method: 'POST',
        body: null
      })
        .then(handleErrors)
        .then(response => {
          this.loadExtensions(true);
        });
    },
    downloadExtension: function(extensionId) {
      var url = `/api/extensions/${extensionId}?action=download`;
      //TODO: handle response
      fetch(url, {
        method: 'POST',
        body: null
      })
        .then(handleErrors)
        .then(response => {
          this.loadExtensions(true);
        });
    },
    deleteExtension: function(extensionId) {
      var url = `/api/extensions/${extensionId}`;
      //TODO: handle response
      fetch(url, {
        method: 'DELETE',
        body: null
      })
        .then(handleErrors)
        .then(response => {
          this.loadExtensions(true);
        });
    }
  },
  mounted: function() {
    this.loadExtensions(false);
  }
};
</script>
<style scoped></style>
