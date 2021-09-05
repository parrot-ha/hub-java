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
              <v-simple-table>
                <thead>
                  <tr>
                    <th scope="col" style="width:4%"></th>
                    <th scope="col" style="width:48%">Name</th>
                    <th scope="col" style="width:48%">Namespace</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="deviceHandlerCode in deviceHandlerCodeList"
                    :key="deviceHandlerCode.id"
                  >
                    <td></td>
                    <td>
                      <router-link
                        :to="{
                          name: 'DeviceHandlerCodeEdit',
                          params: { id: deviceHandlerCode.id }
                        }"
                        >{{ deviceHandlerCode.name }}</router-link
                      >
                    </td>
                    <td>{{ deviceHandlerCode.namespace }}</td>
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
  name: 'DeviceHandlerCodeList',
  data() {
    return {
      deviceHandlerCodeList: []
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
