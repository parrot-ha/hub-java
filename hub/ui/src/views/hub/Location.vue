<template>
  <v-container fluid>
    <v-row>
      <v-col>
        <h1>Edit Location</h1>
        <hr />
        <v-form>
          <v-text-field label="Name" v-model="location.name"></v-text-field>

          <v-select
            :items="location.modes"
            item-text="name"
            item-value="id"
            label="Current Mode"
            v-model="location.currentMode.id"
          ></v-select>
          <v-select
            :items="['C', 'F']"
            label="Temperature Scale"
            v-model="location.temperatureScale"
          ></v-select>

          <v-text-field
            label="ZIP Code"
            v-model="location.zipCode"
          ></v-text-field>

          <v-text-field
            label="Latitude"
            v-model="location.latitude"
          ></v-text-field>
          <v-text-field
            label="Longitude"
            v-model="location.longitude"
          ></v-text-field>

          <v-btn color="primary" @click="saveLocation">
            Save
          </v-btn>

          <v-btn color="error" @click="loadLocationData">Cancel</v-btn>
        </v-form>
      </v-col>
      <v-col>
        <div id="map" style="width: 100%; height: 580px"></div>
      </v-col>
      >
    </v-row>
  </v-container>
</template>
<script>
import _debounce from 'lodash/debounce';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

L.Icon.Default.imagePath = '/img/leaflet/';

export default {
  name: 'Location',
  data() {
    return {
      location: {
        currentMode: {}
      },
      map: null,
      marker: null
    };
  },
  methods: {
    saveLocation: function() {
      fetch('/api/location', {
        method: 'PUT',
        body: JSON.stringify(this.location)
      })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            //TODO: put message on ui
            console.log('saved location');
          } else {
            console.log('problem saving location');
          }
        });
    },
    loadLocationData: function() {
      fetch('/api/location')
        .then(response => response.json())
        .then(data => {
          if (typeof data !== 'undefined' && data != null) {
            this.location = data;
          }
        });
    },
    setupLeafletMap: function() {
      var vm = this;
      var latlng = L.latLng(
        vm.safeLatLng(vm.location.latitude),
        vm.safeLatLng(vm.location.longitude)
      );
      vm.map = L.map('map').setView(latlng, 17);
      L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution:
          'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors'
      }).addTo(this.map);
      vm.marker = L.marker(latlng, { draggable: true }).addTo(vm.map);

      vm.marker.on('moveend', function(e) {
        vm.location.latitude = e.target.getLatLng().lat;
        vm.location.longitude = e.target.getLatLng().lng;
      });
    },
    safeLatLng: function(val) {
      if (typeof val === 'undefined' || val == null) return 0;
      return val;
    },
    updateMap: function() {
      var vm = this;
      if (vm.map == null) {
        vm.setupLeafletMap();
      } else {
        var latlng = vm.map.getCenter();
        var changed = false;
        if (latlng.lat != vm.location.latitude) {
          latlng.lat = vm.location.latitude;
          changed = true;
        }
        if (latlng.lng != vm.location.longitude) {
          latlng.lng = vm.location.longitude;
          changed = true;
        }
        if (changed) {
          vm.map.setView(latlng);
          vm.marker.setLatLng(latlng);
        }
      }
    }
  },
  created: function() {
    this.debouncedUpdateMap = _debounce(this.updateMap, 500);
  },
  watch: {
    'location.latitude': function(val) {
      this.debouncedUpdateMap();
    },
    'location.longitude': function(val) {
      this.debouncedUpdateMap();
    }
  },
  mounted: function() {
    this.loadLocationData();
    //fetch('/api/location')
    //  .then(response => response.json())
    //  .then(data => {
    //    if (typeof data !== 'undefined' && data != null) {
    //      this.location = data;
    //    }
    //  });
  }
};
</script>
<style scoped></style>
