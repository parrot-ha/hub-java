<template>
  <div>
    <div v-if="body.multiple">
      <div v-for="childApp in childApps" :key="childApp.id">
        <v-btn block @click="onChildNav(childApp.id)">{{
          childApp.displayName
        }}</v-btn>
      </div>
      <v-btn block @click="onAddChild(body.namespace, body.appName)">{{
        body.title
      }}</v-btn>
    </div>
    <div v-else>
      <v-btn block @click="onAddChild(body.namespace, body.appName)">{{
        body.title
      }}</v-btn>
    </div>
  </div>
</template>

<script>
function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

export default {
  name: "AppChildApp",
  props: ["body"],
  data() {
    return {
      iaaId: "",
      childApps: {},
    };
  },
  mounted: function () {
    this.iaaId = this.$route.params.id;

    // get child apps
    fetch(
      `/api/iaas/${this.iaaId}/child-apps?appName=${this.body.appName}&namespace=${this.body.namespace}`
    )
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.childApps = data;
        }
      });
  },
  methods: {
    onChildNav: function (childAppid) {
      this.$router.push({
        name: "InstalledAutomationAppConfig",
        params: { id: childAppid },
      });
    },
    onAddChild: function (namespace, appName) {
      var body = {
        id: this.iaaId,
        type: "child",
        appName: appName,
        namespace: namespace,
      };
      fetch("/api/iaas", { method: "POST", body: JSON.stringify(body) })
        .then(handleErrors)
        .then((response) => {
          return response.json();
        })
        .then((data) => {
          //this.$router.push(`/iaas/${data.id}/cfg`);
          this.$router.push({
            name: "InstalledAutomationAppConfig",
            params: { id: data.id },
          });
        })
        .catch((error) => {
          console.log(error);
        });
    },
  },
};
</script>

<style scoped></style>
