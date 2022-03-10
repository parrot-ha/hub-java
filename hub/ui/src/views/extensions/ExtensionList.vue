<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>Extensions</v-card-title>
            <v-card-text>
              <router-link :to="{ name: 'ExtensionAdd' }"
                >Add Extension</router-link
              ><br />
              <router-link :to="{ name: 'ExtensionSettings' }"
                >Extension Settings</router-link
              >
              <v-simple-table>
                <thead>
                  <tr>
                    <th scope="col" style="width:20%">Name</th>
                    <th scope="col" style="width:40%">Description</th>
                    <th scope="col" style="width:40%">Location</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="extension in extensions" :key="extension.id">
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
                    <td>{{ extension.location }}</td>
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
  name: 'ExtensionList',
  data() {
    return {
      extensions: []
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
  }
};
</script>
<style scoped></style>
