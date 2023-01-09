<template>
  <div class="container-fluid">
    <div class="row g-3">
      <div class="col-auto me-auto">
        <h5>Automation App Code</h5>
      </div>
      <div class="col-auto">
        <router-link
          class="btn btn-outline-secondary"
          :to="{ name: 'AutomationAppCodeAdd' }"
          >Add New</router-link
        >
      </div>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Namespace</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="aa in sortedCodeList" :key="aa.id">
          <td>
            <router-link
              :to="{
                name: 'AutomationAppCodeEdit',
                params: { id: aa.id },
              }"
              >{{ aa.name }}</router-link
            >
          </td>
          <td>{{ aa.namespace }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script>
export default {
  name: "AutomationAppCodeList",
  data() {
    return {
      codeList: [],
    };
  },
  computed: {
    sortedCodeList() {
      let sortedCodeList = [...this.codeList].sort((a, b) => {
        const nameA = a.name.toUpperCase();
        const nameB = b.name.toUpperCase();
        if (nameA < nameB) {
          return -1;
        }
        if (nameA > nameB) {
          return 1;
        }

        return 0;
      });
      return sortedCodeList;
    },
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
