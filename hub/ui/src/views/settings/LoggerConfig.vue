<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title></v-card-title>
            <v-card-text>
              <div>
                <v-data-table :headers="headers" :items="loggers">
                  <template v-slot:top>
                    <v-toolbar flat>
                      <v-toolbar-title>Logging Configuration</v-toolbar-title>
                      <v-divider class="mx-4" inset vertical></v-divider>
                      <v-spacer></v-spacer>
                      <v-dialog v-model="dialog" max-width="500px">
                        <template v-slot:activator="{ on, attrs }">
                          <v-btn
                            color="primary"
                            dark
                            class="mb-2"
                            v-bind="attrs"
                            v-on="on"
                          >
                            New Item
                          </v-btn>
                        </template>
                        <v-card>
                          <v-card-title>
                            <span class="headline">{{ formTitle }}</span>
                          </v-card-title>

                          <v-card-text>
                            <div>
                              <v-text-field
                                v-model="editedItem.name"
                                label="Logger Name"
                              ></v-text-field>
                            </div>
                            <div>
                              <v-select
                                v-model="editedItem.level"
                                :items="loggerLevels"
                                label="Level"
                              ></v-select>
                            </div>
                          </v-card-text>

                          <v-card-actions>
                            <v-spacer></v-spacer>
                            <v-btn
                              color="blue darken-1"
                              text
                              @click="closeNewDialog"
                            >
                              Cancel
                            </v-btn>
                            <v-btn
                              color="blue darken-1"
                              text
                              @click="saveNewLogger"
                            >
                              Save
                            </v-btn>
                          </v-card-actions>
                        </v-card>
                      </v-dialog>
                    </v-toolbar>
                  </template>
                  <template v-slot:item.level="props">
                    <v-edit-dialog
                      :return-value.sync="props.item.level"
                      large
                      persistent
                      @save="save(props.item)"
                      @cancel="cancel"
                    >
                      <div>{{ props.item.level }}</div>
                      <template v-slot:input>
                        <div class="mt-4 title">
                          Update Level
                        </div>
                        <v-select
                          v-model="props.item.level"
                          :items="loggerLevels"
                          label="Level"
                        ></v-select>
                      </template>
                    </v-edit-dialog>
                  </template>
                </v-data-table>
                <v-snackbar v-model="snack" :timeout="3000" :color="snackColor">
                  {{ snackText }}

                  <template v-slot:action="{ attrs }">
                    <v-btn v-bind="attrs" text @click="snack = false">
                      Close
                    </v-btn>
                  </template>
                </v-snackbar>
              </div>
            </v-card-text>
            <v-card-actions> </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-layout>
  </v-container>
</template>
<script>
function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

export default {
  name: 'LoggerConfig',
  data() {
    return {
      dialog: false,
      dialogDelete: false,
      snack: false,
      snackColor: '',
      snackText: '',
      editedIndex: -1,
      editedItem: {
        name: '',
        level: 'DEBUG'
      },
      defaultItem: {
        name: '',
        level: 'DEBUG'
      },
      headers: [
        {
          text: 'Name',
          align: 'start',
          sortable: false,
          value: 'name'
        },
        { text: 'Level', value: 'level' }
      ],
      loggerLevels: ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'],
      loggers: []
    };
  },
  methods: {
    save(item) {
      var editedIndex = this.loggers.indexOf(item);
      console.log(`saving ${JSON.stringify(this.loggers[editedIndex])}`);
      fetch('/api/settings/logging-config', {
        method: 'PUT',
        body: JSON.stringify(this.loggers[editedIndex])
      })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            //TODO: put message on ui
            this.snack = true;
            this.snackColor = 'success';
            this.snackText = 'Level saved';
          } else {
            this.snack = true;
            this.snackColor = 'error';
            this.snackText = 'Problem Saving Level';
            this.loadLoggingConfig();
          }
        });
    },
    cancel() {
      this.snack = true;
      this.snackColor = 'error';
      this.snackText = 'Canceled';
    },
    closeNewDialog() {
      this.dialog = false;
      this.$nextTick(() => {
        this.editedItem = Object.assign({}, this.defaultItem);
        this.editedIndex = -1;
      });
    },
    saveNewLogger() {
      fetch('/api/settings/logging-config', {
        method: 'POST',
        body: JSON.stringify(this.editedItem)
      })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            this.loggers.push(this.editedItem);
            this.closeNewDialog();
            //put message on ui
            this.snack = true;
            this.snackColor = 'success';
            this.snackText = 'Level Added';
          } else {
            this.closeNewDialog();
            this.snack = true;
            this.snackColor = 'error';
            this.snackText = 'Problem Adding Level';
            this.loadLoggingConfig();
          }
        });
    },
    loadLoggingConfig() {
      fetch('/api/settings/logging-config')
        .then(response => response.json())
        .then(data => {
          if (typeof data !== 'undefined' && data != null) {
            this.loggers = data.loggers;
          }
        });
    }
  },
  mounted: function() {
    this.loadLoggingConfig();
  }
};
</script>
<style scoped></style>
