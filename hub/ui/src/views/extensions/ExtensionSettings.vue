<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>Extension Location URLs</v-card-title>
            <v-card-text>
              <v-simple-table>
                <thead>
                  <tr>
                    <th scope="col" style="width:35%">Name</th>
                    <th scope="col" style="width:65%">URL</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="extensionLoc in extensionLocs"
                    :key="extensionLoc.id"
                  >
                    <td>
                      {{ extensionLoc.name }}
                    </td>
                    <td>{{ extensionLoc.url }}</td>
                  </tr>
                </tbody>
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
  name: 'ExtensionSettings',
  data() {
    return {
      extensionLocs: []
    };
  },
  mounted: function() {
    fetch('/api/extensions/locations')
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.extensionLocs = data;
        }
      });
  }
};
</script>
<style scoped></style>
