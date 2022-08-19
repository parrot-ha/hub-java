<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>Devices</v-card-title>
            <v-card-text>
              <router-link :to="{ name: 'DeviceAdd' }">Add Device</router-link>
              <br /><br />
              Devices:<br />
              <v-data-table
                :headers="headers"
                :items="devices"
                sort-by="displayName"
                disable-pagination
                hide-default-footer
                class="elevation-1"
              >
                <template v-slot:item.displayName="{ item }">
                  <router-link
                    :to="{ name: 'device', params: { id: item.id } }"
                    >{{ item.displayName }}</router-link
                  >
                </template>
                <template v-slot:item.id="{ item }">
                  <router-link :to="{ name: 'DeviceConfig', params: { id: item.id } }"
                    ><v-icon>mdi-alert-circle-outline</v-icon></router-link
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
  name: 'Devices',
  data() {
    return {
      devices: [],
      headers: [
        {
          text: '',
          align: 'start',
          sortable: false,
          value: 'id'
        },
        { text: 'Display Name', value: 'displayName' },
        { text: 'Type', value: 'type' },
        { text: 'Device Network Id', value: 'deviceNetworkId' },
        { text: '', value: '' }
      ]
    };
  },
  mounted: function() {
    fetch('/api/devices')
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.devices = data;
        }
      });
  }
};
</script>
<style scoped></style>
