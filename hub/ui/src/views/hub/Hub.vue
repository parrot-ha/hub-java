<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>{{ hub.name }}</v-card-title>
            <v-card-text>
              <v-simple-table>
                <template v-slot:default>
                  <tbody>
                    <tr>
                      <td>Name</td>
                      <td>{{ hub.name }}</td>
                    </tr>
                    <tr>
                      <td>Hub ID</td>
                      <td>{{ hub.id }}</td>
                    </tr>
                    <tr>
                      <td>Version</td>
                      <td>{{ hub.version }}</td>
                    </tr>
                    <tr>
                      <td>Date Created</td>
                      <td></td>
                    </tr>
                    <tr>
                      <td>IP Address</td>
                      <td></td>
                    </tr>
                    <tr>
                      <td>MAC Address</td>
                      <td></td>
                    </tr>
                    <tr v-for="(integration, i) in integrations" :key="i">
                      <td>{{ integration.name }}</td>
                      <td>
                        <ul>
                          <li
                            v-for="(value, name) in integration.settings"
                            :key="name"
                          >
                            {{ name }}: <strong>{{ value }}</strong>
                          </li>
                        </ul>
                      </td>
                    </tr>
                    <tr>
                      <td>Events</td>
                      <td></td>
                    </tr>
                    <tr>
                      <td>Utilities</td>
                      <td></td>
                    </tr>
                  </tbody>
                </template>
              </v-simple-table>
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
  name: 'Hub',
  data() {
    return {
      hub: {},
      integrations: {}
    };
  },
  mounted: function() {
    fetch('/api/hub')
      .then(response => response.json())
      .then(data => {
        if (data != null) {
          this.hub = data;
        }
      });

    fetch('/api/integrations')
      .then(response => response.json())
      .then(data => {
        if (data != null) {
          this.integrations = data;
        }
      });
  }
};
</script>
<style scoped></style>
