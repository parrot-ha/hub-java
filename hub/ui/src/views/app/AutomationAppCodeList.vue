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
              <v-data-table
                :headers="headers"
                :items="codeList"
                sort-by="name"
                disable-pagination
                hide-default-footer
                class="elevation-1"
              >
                <template v-slot:item.name="{ item }">
                  <router-link
                    :to="{
                      name: 'AutomationAppCodeEdit',
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
  name: 'AutomationAppCodeList',
  data() {
    return {
      codeList: [],
      headers: [
        { text: 'Name', value: 'name' },
        { text: 'Namespace', value: 'namespace' }
      ]
    };
  },
  mounted: function() {
    fetch('/api/automation-apps?filter=user')
      .then(response => response.json())
      .then(data => {
        if (data != null) {
          this.codeList = data;
        }
      });
  }
};
</script>
<style scoped></style>
