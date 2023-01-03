<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col">
        <div class="card">
          <v-card-title>Automation App Code</v-card-title>
          <div class="card-text">
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
                    params: { id: item.id },
                  }"
                  >{{ item.name }}</router-link
                >
              </template>
            </v-data-table>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: "AutomationAppCodeList",
  data() {
    return {
      codeList: [],
      headers: [
        { text: "Name", value: "name" },
        { text: "Namespace", value: "namespace" },
      ],
    };
  },
  mounted: function () {
    fetch("/api/automation-apps?filter=user")
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.codeList = data;
        }
      });
  },
};
</script>
<style scoped></style>
