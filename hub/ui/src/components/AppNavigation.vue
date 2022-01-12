<template>
  <div>
    <v-app-bar app color="primary" dark elevation="0">
      <v-app-bar-nav-icon
        @click.stop="sidebarMenu = !sidebarMenu"
      ></v-app-bar-nav-icon>
      <v-spacer></v-spacer>
      <v-btn @click="toggleTheme" color="primary" class="mr-2">{{
        buttonText
      }}</v-btn>
      <v-icon>mdi-account</v-icon>
    </v-app-bar>
    <v-navigation-drawer
      v-model="sidebarMenu"
      app
      floating
      :permanent="sidebarMenu"
      :mini-variant.sync="mini"
    >
      <v-list dense color="primary" dark>
        <v-list-item>
          <v-list-item-action>
            <v-icon @click.stop="sidebarMenu = !sidebarMenu"
              >mdi-chevron-left</v-icon
            >
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>
              <h3 class="font-weight-thin">Parrot Hub</h3>
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
      <v-list-item class="px-2" @click="toggleMini = !toggleMini">
        <v-list-item-avatar>
          <v-icon>mdi-account-outline</v-icon>
        </v-list-item-avatar>
        <v-list-item-content class="text-truncate">
          User
        </v-list-item-content>
        <v-btn icon small>
          <v-icon>mdi-chevron-left</v-icon>
        </v-btn>
      </v-list-item>
      <v-divider></v-divider>
      <v-list>
        <v-list-item
          v-for="item in items"
          :key="item.title"
          link
          :to="item.href"
        >
          <v-list-item-icon>
            <v-icon color="primary">{{ item.icon }}</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title class="primary--text">{{
              item.title
            }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>
  </div>
</template>

<script>
export default {
  name: 'AppNavigation',
  data() {
    return {
      appTitle: 'Parrot Hub',
      sidebarMenu: true,
      toggleMini: false,
      items: [
        { title: 'Home', href: '/', icon: 'mdi-home-outline' },
        {
          title: 'Location',
          href: '/location',
          icon: 'mdi-map-clock-outline'
        },
        { title: 'Hub', href: '/hub', icon: 'mdi-desktop-tower' },
        {
          title: 'Devices',
          href: '/devices',
          icon: 'mdi-devices'
        },
        { title: 'AutomationApps', href: '/iaas', icon: 'mdi-cogs' },
        {
          title: 'Integrations',
          href: '/integrations',
          icon: 'mdi-lan-connect'
        },
        { title: 'Settings', href: '/settings', icon: 'mdi-tune' },
        {
          title: 'AutomationApp Code',
          href: '/aa-code',
          icon: 'mdi-file-document-edit-outline'
        },
        {
          title: 'Device Handler Code',
          href: '/dh-code',
          icon: 'mdi-file-document-edit-outline'
        },
        {
          title: 'Extensions',
          href: '/extensions',
          icon: 'mdi-puzzle-outline'
        }
      ]
    };
  },
  computed: {
    mini() {
      return this.$vuetify.breakpoint.smAndDown || this.toggleMini;
    },
    buttonText() {
      return !this.$vuetify.theme.dark ? 'Go Dark' : 'Go Light';
    }
  },
  methods: {
    toggleTheme() {
      this.$vuetify.theme.dark = !this.$vuetify.theme.dark;
    }
  }
};
</script>

<style scoped></style>
