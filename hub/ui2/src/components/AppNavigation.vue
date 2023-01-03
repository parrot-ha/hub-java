<template>
  <nav
    id="sidebarMenu"
    class="col-md-3 col-lg-2 d-md-block bg-light sidebar collapse"
  >
    <div class="position-sticky pt-3">
      <ul class="nav flex-column">
        <li class="nav-item" v-for="item in items" :key="item.title">
          <RouterLink :to="item.href" class="nav-link"
            ><span data-bs-target="#sidebarMenu" data-bs-toggle="collapse"
              ><i :class="item.icon"></i> {{ item.title }}</span
            ></RouterLink
          >
        </li>
      </ul>
    </div>
  </nav>
</template>

<script>
export default {
  name: "AppNavigation",
  data() {
    return {
      appTitle: "Parrot Hub",
      sidebarMenu: true,
      toggleMini: false,
      items: [
        { title: "Home", href: "/", icon: "bi bi-house" },
        {
          title: "Location",
          href: "/location",
          icon: "bi bi-geo-alt",
        },
        { title: "Hub", href: "/hub", icon: "bi bi-modem" },
        {
          title: "Devices",
          href: "/devices",
          icon: "bi bi-boxes",
        },
        { title: "AutomationApps", href: "/iaas", icon: "bi bi-gear" },
        {
          title: "Integrations",
          href: "/integrations",
          icon: "bi bi-diagram-3",
        },
        { title: "Settings", href: "/settings", icon: "bi bi-sliders" },
        {
          title: "AutomationApp Code",
          href: "/aa-code",
          icon: "bi bi-file-earmark-text",
        },
        {
          title: "Device Handler Code",
          href: "/dh-code",
          icon: "bi bi-file-earmark-text",
        },
        {
          title: "Extensions",
          href: "/extensions",
          icon: "bi bi-puzzle",
        },
      ],
    };
  },
};
</script>

<style scoped></style>
