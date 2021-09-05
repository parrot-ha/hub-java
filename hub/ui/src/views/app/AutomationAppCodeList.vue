<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>Automation App Code</v-card-title>
            <v-card-text>
              <router-link :to="{ name: 'AutomationAppCodeAdd' }"
                >Add Automation App Code</router-link
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
                    v-for="automationAppCode in automationAppCodeList"
                    :key="automationAppCode.id"
                  >
                    <td></td>
                    <td>
                      <router-link
                        :to="{
                          name: 'AutomationAppCodeEdit',
                          params: { id: automationAppCode.id }
                        }"
                        >{{ automationAppCode.name }}</router-link
                      >
                    </td>
                    <td>{{ automationAppCode.namespace }}</td>
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
  name: 'AutomationAppCodeList',
  data() {
    return {
      automationAppCodeList: []
    };
  },
  mounted: function() {
    fetch('/api/automation-apps?filter=user')
      .then(response => response.json())
      .then(data => {
        if (typeof data !== 'undefined' && data != null) {
          this.automationAppCodeList = data;
        }
      });
  }
};
</script>
<style scoped></style>
