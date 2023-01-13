<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <div
              class="card-title d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3"
            >
              <h5>Installed Automation Apps</h5>
              <div class="btn-toolbar mb-2 mb-md-0">
                <router-link
                  class="btn btn-outline-secondary"
                  :to="{ name: 'InstalledAutomationAppAdd' }"
                  >Add Automation App</router-link
                >
              </div>
            </div>

            <div class="card-text">
              <table class="table">
                <thead>
                  <tr>
                    <th scope="col"></th>
                    <th scope="col">Label</th>
                    <th scope="col">Type</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="iaa in sortedInstalledAutomationApps"
                    :key="iaa.id"
                  >
                    <td>
                      <router-link
                        :to="{
                          name: 'InstalledAutomationAppInfo',
                          params: { id: iaa.id },
                        }"
                        ><i class="bi bi-exclamation-circle"></i
                      ></router-link>
                    </td>
                    <td>
                      <router-link
                        :to="{
                          name: 'InstalledAutomationAppConfig',
                          params: { id: iaa.id },
                        }"
                        >{{ iaa.label }}</router-link
                      >
                    </td>
                    <td>{{ iaa.type }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: "InstalledAutomationAppList",
  data() {
    return {
      installedAutomationApps: [],
    };
  },
  computed: {
    sortedInstalledAutomationApps() {
      let sortedInstalledAutomationApps = [
        ...this.installedAutomationApps,
      ].sort((a, b) => {
        const nameA = a.label.toUpperCase();
        const nameB = b.label.toUpperCase();
        if (nameA < nameB) {
          return -1;
        }
        if (nameA > nameB) {
          return 1;
        }

        return 0;
      });
      return sortedInstalledAutomationApps;
    },
  },

  mounted: function () {
    fetch("/api/iaas?includeChildren=false")
      .then((response) => response.json())
      .then((data) => {
        if (typeof data !== "undefined" && data != null) {
          this.installedAutomationApps = data;
        }
      });
  },
};
</script>
<style scoped></style>
