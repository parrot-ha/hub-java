<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>Device Handler Code</v-card-title>
            <v-card-text>
              <router-link :to="{ name: 'DeviceHandlerCodeAdd' }"
                >Add Device Handler Code</router-link
              >
              <v-data-table
                :headers="headers"
                :items="deviceHandlerCodeList"
                sort-by="name"
                disable-pagination
                hide-default-footer
                class="elevation-1"
              >
                <template v-slot:item.name="{ item }">
                  <router-link
                    :to="{
                      name: 'DeviceHandlerCodeEdit',
                      params: { id: item.id }
                    }"
                    >{{ item.name }}</router-link
                  >
                </template>
              </v-data-table>
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
  name: 'DeviceHandlerCodeList',
  data() {
    return {
      deviceHandlerCodeList: [],
      headers: [
        { text: 'Name', value: 'name' },
        { text: 'Namespace', value: 'namespace' }
      ]
    };
  },
  mounted: function() {
    fetch('/api/device-handlers?filter=user')
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.deviceHandlerCodeList = data;
        }
      });
  }
};
</script>
<style scoped></style>
