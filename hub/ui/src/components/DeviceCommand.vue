<template>
  <div>
    <div v-if="command.arguments">
      <div class="input-group mb-3">
        <div class="input-group-prepend">
          <v-btn color="primary" @click="commandClick(deviceId, command.name)">
            {{ command.name }}
          </v-btn>
        </div>
        <input
          v-for="(arg, i) in command.values"
          type="text"
          class="form-control"
          v-model="arg.value"
          :placeholder="arg.name"
          :key="i"
        />
      </div>
    </div>
    <div v-else>
      <v-btn color="primary" @click="commandClick(deviceId, command.name)">
        {{ command.name }}
      </v-btn>
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
  name: 'DeviceCommand',
  props: ['deviceId', 'command'],
  data() {
    return {};
  },
  methods: {
    commandClick: function(deviceId, command) {
      var url = `/api/devices/${deviceId}/commands/${command}`;
      var setArgs = [];
      if (typeof this.command.values !== 'undefined') {
        console.log(this.command.values);
        for (var arg of this.command.values) {
          if (arg.value) {
            setArgs.push(arg.value);
          } else {
            // break out after first null
            break;
          }
        }
      }
      //TODO: handle response
      if (setArgs.length > 0) {
        fetch(url, {
          method: 'POST',
          body: JSON.stringify(setArgs)
        })
          .then(handleErrors)
          .then(response => {});
      } else {
        fetch(url, {
          method: 'POST',
          body: null
        })
          .then(handleErrors)
          .then(response => {});
      }
    }
  }
};
</script>

<style scoped></style>
