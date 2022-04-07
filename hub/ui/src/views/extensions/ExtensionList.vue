<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>Extensions</v-card-title>
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
                            <th scope="col" style="width:20%">Name</th>
                            <th scope="col" style="width:40%">Description</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="extension in extensions.installed"
                            :key="extension.id"
                          >
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
                    <v-card-text
                      >Available Extensions
                      <v-simple-table>
                        <thead>
                          <tr>
                            <th scope="col" style="width:20%">Name</th>
                            <th scope="col" style="width:40%">Description</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="extension in extensions.available"
                            :key="extension.id"
                          >
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
                            <th scope="col" style="width:20%">Name</th>
                            <th scope="col" style="width:20%">Type</th>
                            <th scope="col" style="width:60%">Location</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="setting in settings" :key="setting.id">
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
export default {
  name: 'ExtensionList',
  data() {
    return {
      tab: null,
      extensions: {
        installed: [],
        available: []
      },
      settings: []
    };
  },
  mounted: function() {
    fetch('/api/extensions')
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.extensions = data;
        }
      });
    fetch('/api/extensions/settings')
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.settings = data;
        }
      });
  }
};
</script>
<style scoped></style>
