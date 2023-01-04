import { createRouter, createWebHistory } from "vue-router";
import Home from "../views/Home.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      component: Home,
    },
    {
      path: "/devices",
      name: "Devices",
      component: () => import("../views/device/DeviceList.vue"),
    },
    {
      path: "/device-add",
      name: "DeviceAdd",
      component: () => import("../views/device/DeviceAdd.vue"),
    },
    {
      path: "/devices/:id",
      name: "device",
      component: () => import("../views/device/Device.vue"),
    },
    {
      path: "/devices/:id/config",
      name: "DeviceConfig",
      component: () => import("../views/device/DeviceConfig.vue"),
    },
    {
      path: "/devices/:id/events",
      name: "DeviceEvents",
      component: () => import("../views/device/DeviceEvents.vue"),
    },
    {
      path: "/devicetiles/:id",
      name: "devicetile",
      component: () => import("../views/device/DeviceTile.vue"),
    },
    {
      path: "/iaas",
      name: "InstalledAutomationApps",
      component: () => import("../views/app/InstalledAutomationAppList.vue"),
    },
    {
      path: "/iaas/:id",
      name: "InstalledAutomationAppInfo",
      component: () => import("../views/app/InstalledAutomationAppInfo.vue"),
    },
    {
      path: "/iaas/:id/cfg",
      name: "InstalledAutomationAppConfig",
      component: () => import("../views/app/InstalledAutomationAppConfig.vue"),
    },
    {
      path: "/iaas/:id/cfg",
      name: "InstalledAutomationAppConfig",
      component: () => import("../views/app/InstalledAutomationAppConfig.vue"),
    },
    {
      path: "/iaas/:id/cfg/:approute(.*)",
      name: "InstalledAutomationAppConfigSubRoute",
      component: () => import("../views/app/InstalledAutomationAppConfig.vue"),
    },
    {
      path: "/iaa-add",
      name: "InstalledAutomationAppAdd",
      component: () => import("../views/app/InstalledAutomationAppAdd.vue"),
    },
    {
      path: "/integrations",
      name: "Integrations",
      component: () => import("../views/integration/IntegrationList.vue"),
    },
    {
      path: "/integrations/:id",
      name: "Integration",
      component: () => import("../views/integration/Integration.vue"),
    },
    {
      path: "/integration-add",
      name: "IntegrationAdd",
      component: () => import("../views/integration/IntegrationAdd.vue"),
    },
    {
      path: "/location",
      name: "Location",
      component: () => import("../views/hub/Location.vue"),
    },
    {
      path: "/hub",
      name: "Hub",
      component: () => import("../views/hub/Hub.vue"),
    },
    {
      path: "/settings",
      name: "Settings",
      component: () => import("../views/Settings.vue"),
    },
    {
      path: "/settings/logger-config",
      name: "LoggerConfig",
      component: () => import("../views/settings/LoggerConfig.vue"),
    },
    {
      path: "/aa-code",
      name: "AutomationAppCodeList",
      component: () => import("../views/app/AutomationAppCodeList.vue"),
    },
    {
      path: "/aa-code/add",
      name: "AutomationAppCodeAdd",
      component: () => import("../views/app/AutomationAppCodeAdd.vue"),
    },
    {
      path: "/aa-code/:id/edit",
      name: "AutomationAppCodeEdit",
      component: () => import("../views/app/AutomationAppCodeEdit.vue"),
    },
    {
      path: "/aa-code/:id/settings",
      name: "AutomationAppSettings",
      component: () => import("../views/app/AutomationAppSettings.vue"),
    },
    {
      path: "/dh-code",
      name: "DeviceHandlerCodeList",
      component: () => import("../views/device/DeviceHandlerCodeList.vue"),
    },
    {
      path: "/dh-code/add",
      name: "DeviceHandlerCodeAdd",
      component: () => import("../views/device/DeviceHandlerCodeAdd.vue"),
    },
    {
      path: "/dh-code/:id/edit",
      name: "DeviceHandlerCodeEdit",
      component: () => import("../views/device/DeviceHandlerCodeEdit.vue"),
    },
    {
      path: "/extensions",
      name: "Extensions",
      component: () => import("../views/extensions/ExtensionList.vue"),
    },
  ],
});

export default router;
