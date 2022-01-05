<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>Device Events</v-card-title>
            <v-card-text>
              <br /><br />
              Events:<br />
              <v-data-table
                :headers="headers"
                :items="events"
                sort-by="date"
                sort-desc
                class="elevation-1"
              >
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
  name: 'DeviceEvents',
  data() {
    return {
      deviceId: '',
      events: [],
      headers: [
        { text: 'Date', value: 'date' },
        { text: 'Source', value: 'source' },
        { text: 'Type', value: 'type' },
        { text: 'Name', value: 'name' },
        { text: 'Value', value: 'value' },
        { text: 'User', value: 'user' },
        { text: 'Displayed Text', value: 'displayedText' },
        { text: 'Changed', value: 'stateChange' }
      ]
    };
  },
  mounted: function() {
    this.deviceId = this.$route.params.id;

    fetch(`/api/devices/${this.deviceId}/events`)
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.events = data;
        }
      });
  }
};
</script>
<style scoped></style>
