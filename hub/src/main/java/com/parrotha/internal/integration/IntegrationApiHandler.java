/**
 * Copyright (c) 2021-2022 by the respective copyright holders.
 * All rights reserved.
 * <p>
 * This file is part of Parrot Home Automation Hub.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.parrotha.internal.integration;

import com.parrotha.integration.DeviceIntegration;
import com.parrotha.integration.extension.DeviceExcludeIntegrationExtension;
import com.parrotha.integration.extension.DeviceScanIntegrationExtension;
import com.parrotha.integration.extension.ItemListIntegrationExtension;
import com.parrotha.integration.extension.ResetIntegrationExtension;
import com.parrotha.internal.BaseApiHandler;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IntegrationApiHandler extends BaseApiHandler {
    IntegrationService integrationService;

    public IntegrationApiHandler(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    public void setupApi(Javalin app) {
        app.get("/api/integration_types", ctx -> {
            List<Map<String, String>> integrations = integrationService.getIntegrationTypes();

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(integrations).toString());
        });

        app.get("/api/integrations", ctx -> {
            String integrationType = ctx.queryParam("type");
            List<String> fields = ctx.queryParams("field");

            Collection<IntegrationConfiguration> integrations = integrationService.getIntegrations();

            if (fields != null && fields.size() > 0) {
                List<Map<String, Object>> integrationList = new ArrayList<>();
                for (IntegrationConfiguration integration : integrations) {
                    AbstractIntegration abstractIntegration = integrationService.getIntegrationById(integration.getId());
                    if (integrationType == null || ("DEVICE".equals(integrationType) && abstractIntegration instanceof DeviceIntegration)) {
                        Map<String, Object> integrationMap = new HashMap<>();
                        for (String field : fields) {
                            switch (field) {
                                case "id":
                                    integrationMap.put("id", integration.getId());
                                    break;
                                case "name":
                                    integrationMap.put("name", integration.getName());
                                    break;
                                case "label":
                                    integrationMap.put("label",
                                            integration.getLabel() != null ? integration.getLabel() : integration.getName());
                                    break;
                                case "tags":
                                    integrationMap.put("tags", ((DeviceIntegration) abstractIntegration).getTags());
                            }
                        }

                        integrationList.add(integrationMap);
                    }
                }
                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result(new JsonBuilder(integrationList).toString());
            } else {
                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result(new JsonBuilder(integrations).toString());
            }
        });

        app.post("/api/integrations", ctx -> {
            String body = ctx.body();
            Map bodyMap = (Map) (new JsonSlurper().parseText(body));
            String integrationTypeId = (String) bodyMap.get("id");

            Map<String, Object> integrationModel = new HashMap<>();
            integrationModel.put("message", "");
            String integrationId = "";

            ctx.status(200);
            try {
                integrationId = integrationService.createIntegration(integrationTypeId);
            } catch (Exception e) {
                //TODO: allow other error codes.
                ctx.status(500);
                //TODO: return a descriptive message
                integrationModel.put("message", "Error occurred: " + e.getMessage());
            }
            integrationModel.put("id", integrationId);

            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(integrationModel).toString());
        });

        app.get("/api/integrations/:id", ctx -> {
            Map<String, Object> integrationModel = new HashMap<>();
            String id = ctx.pathParam("id");

            AbstractIntegration abstractIntegration = integrationService.getIntegrationById(id);

            if (abstractIntegration != null) {
                integrationModel.put("name", abstractIntegration.getName());
                integrationModel.put("label", abstractIntegration.getLabel());

                integrationModel.put("information", abstractIntegration.getDisplayInformation());
//                integrationModel.put("settings", abstractIntegration.getSettings());

                Map<String, Map> features = new HashMap<>();
                if (abstractIntegration instanceof DeviceScanIntegrationExtension) {
                    features.put("deviceScan", null);
                }
                if (abstractIntegration instanceof DeviceExcludeIntegrationExtension) {
                    features.put("deviceExclude", null);
                }
                if (abstractIntegration instanceof ResetIntegrationExtension) {
                    Map<String, String> options = new HashMap<>();
                    options.put("resetWarning", ((ResetIntegrationExtension) abstractIntegration).getResetWarning());
                    features.put("reset", options);
                }
                if (abstractIntegration instanceof ItemListIntegrationExtension) {
                    features.put("itemList", null);
                }
                integrationModel.put("features", features);
            }
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(integrationModel).toString());
        });

        app.get("/api/integrations/:id/settings", ctx -> {
            String id = ctx.pathParam("id");
            Map<String, Map> settingsMap = new HashMap<>();
            AbstractIntegration abstractIntegration = integrationService.getIntegrationById(id);

            if (abstractIntegration != null) {
                List<IntegrationSetting> settings = abstractIntegration.getSettings();
                if (settings != null) {
                    settingsMap = settings.stream().
                            collect(Collectors.toMap(data -> data.getName(), data -> data.toMap(true)));
                }
            }
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(settingsMap).toString());
        });

        app.delete("/api/integrations/:id", ctx -> {
            String id = ctx.pathParam("id");

            boolean integrationRemoved = integrationService.removeIntegration(id);
            Map<String, Object> model = new HashMap<>();
            model.put("success", integrationRemoved);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        app.get("/api/integrations/:id/preferences-layout", ctx -> {
            String id = ctx.pathParam("id");
            AbstractIntegration abstractIntegration = integrationService.getIntegrationById(id);

            if (abstractIntegration != null) {
                Map preferencesLayout = abstractIntegration.getPreferencesLayout();

                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result(new JsonBuilder(preferencesLayout).toString());
            } else {
                ctx.status(404);
            }
        });

        app.get("/api/integrations/:id/page-layout", ctx -> {
            String id = ctx.pathParam("id");
            AbstractIntegration abstractIntegration = integrationService.getIntegrationById(id);

            if (abstractIntegration != null) {
                List pageLayout = abstractIntegration.getPageLayout();

                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result(new JsonBuilder(pageLayout).toString());
            } else {
                ctx.status(404);
            }
        });

        app.get("/api/integrations/:id/page-data", ctx -> {
            String id = ctx.pathParam("id");
            AbstractIntegration abstractIntegration = integrationService.getIntegrationById(id);

            if (abstractIntegration != null) {
                Map pageData = abstractIntegration.getPageData();

                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result(new JsonBuilder(pageData).toString());
            } else {
                ctx.status(404);
            }
        });

        app.post("/api/integrations/:id/features/:feature", ctx -> {
            String id = ctx.pathParam("id");
            String feature = ctx.pathParam("feature");
            String body = ctx.body();
            Map bodyMap = (Map) (new JsonSlurper().parseText(body));
            String action = (String) bodyMap.get("action");
            Map options = (Map) bodyMap.get("options");

            Map response = null;
            boolean success = false;
            if ("deviceScan".equals(feature)) {
                response = handleDeviceScanFeature(id, action, options);
                success = true;
            } else if ("deviceExclude".equals(feature)) {
                response = handleDeviceExcludeFeature(id, action, options);
                success = true;
            } else if ("reset".equals(feature)) {
                success = handleResetFeature(id, action, options);
            }

            ctx.status(200);
            ctx.contentType("application/json");
            Map<String, Object> model = new HashMap<>();
            model.put("success", success);
            if (response != null) {
                model.put("response", response);
            }
            ctx.result(new JsonBuilder(model).toString());
        });

        app.post("/api/integrations/:id/settings", ctx -> {
            String id = ctx.pathParam("id");
            String body = ctx.body();
            Map bodyMap = (Map) (new JsonSlurper().parseText(body));
            integrationService.updateIntegrationSettings(id, bodyMap);

            ctx.status(200);
            ctx.contentType("application/json");
            Map<String, Object> model = new HashMap<>();
            model.put("success", true);
            ctx.result(new JsonBuilder(model).toString());
        });
    }

    private Map handleDeviceScanFeature(String integrationId, String action, Map options) {
        AbstractIntegration abstractIntegration = integrationService.getIntegrationById(integrationId);
        if (abstractIntegration == null) {
            return null;
        }
        if (abstractIntegration instanceof DeviceScanIntegrationExtension) {
            if ("startScan".equals(action)) {
                ((DeviceScanIntegrationExtension) abstractIntegration).startScan(options);
            } else if ("stopScan".equals(action)) {
                ((DeviceScanIntegrationExtension) abstractIntegration).stopScan(options);
            } else if ("getScanStatus".equals(action)) {
                return ((DeviceScanIntegrationExtension) abstractIntegration).getScanStatus(options);
            }
        }
        return null;
    }

    private Map handleDeviceExcludeFeature(String integrationId, String action, Map options) {
        AbstractIntegration abstractIntegration = integrationService.getIntegrationById(integrationId);
        if (abstractIntegration == null) {
            return null;
        }
        if (abstractIntegration instanceof DeviceExcludeIntegrationExtension) {
            if ("startExclude".equals(action)) {
                ((DeviceExcludeIntegrationExtension) abstractIntegration).startExclude(options);
            } else if ("stopExclude".equals(action)) {
                ((DeviceExcludeIntegrationExtension) abstractIntegration).stopExclude(options);
            } else if ("getExcludeStatus".equals(action)) {
                return ((DeviceExcludeIntegrationExtension) abstractIntegration).getExcludeStatus(options);
            }
        }
        return null;
    }

    private boolean handleResetFeature(String integrationId, String action, Map options) {
        AbstractIntegration abstractIntegration = integrationService.getIntegrationById(integrationId);
        if (abstractIntegration == null) {
            return false;
        }
        if (abstractIntegration instanceof ResetIntegrationExtension) {
            if ("reset".equals(action)) {
                return ((ResetIntegrationExtension) abstractIntegration).reset(options);
            }
        }
        return false;
    }
}
