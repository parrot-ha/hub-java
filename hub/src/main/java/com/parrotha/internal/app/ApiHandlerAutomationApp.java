/**
 * Copyright (c) 2021 by the respective copyright holders.
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
package com.parrotha.internal.app;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import groovy.lang.MissingMethodException;
import io.javalin.Javalin;
import org.apache.commons.lang3.StringUtils;
import com.parrotha.api.Response;
import com.parrotha.internal.BaseApiHandler;
import com.parrotha.internal.hub.ScheduleService;
import com.parrotha.internal.entity.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.javalin.core.security.SecurityUtil.roles;
import static com.parrotha.internal.ui.UIFramework.UIRole.ADMIN;
import static com.parrotha.internal.ui.UIFramework.UIRole.ANYONE;

public class ApiHandlerAutomationApp extends BaseApiHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiHandlerAutomationApp.class);

    AutomationAppService automationAppService;
    EntityService entityService;
    ScheduleService scheduleService;

    public ApiHandlerAutomationApp(AutomationAppService automationAppService, EntityService entityService,
                                   ScheduleService scheduleService) {
        this.automationAppService = automationAppService;
        this.entityService = entityService;
        this.scheduleService = scheduleService;
    }

    public void setupApi(Javalin app) {
        // local endpoint for web service apps
        app.get("/api/automationapps/installations/:id/*", ctx -> {
            String installedAutomationAppId = ctx.pathParam("id");
            String path = "/" + ctx.splat(0);

            Map<String, List<String>> queryParamMap = ctx.queryParamMap();
            Map<String, Object> params = new HashMap();
            for (String key : queryParamMap.keySet()) {
                List<String> queryParams = queryParamMap.get(key);
                if (queryParams.size() == 1) {
                    params.put(key, queryParams.get(0));
                } else {
                    params.put(key, queryParams);
                }
            }

            Response response = entityService
                    .processInstalledAutomationAppWebRequest(installedAutomationAppId, "GET", path, null, params, null);
            if (response != null) {
                ctx.status(response.getStatus());
                ctx.contentType(response.getContentType());
                ctx.result(response.getData());
            } else {
                buildStandardJsonResponse(ctx, true);
            }

        }, roles(ANYONE, ADMIN));

        app.get("/api/automation-apps", ctx -> {
            String filter = ctx.queryParam("filter");
            Collection<AutomationApp> aas;
            if (StringUtils.isNotBlank(filter) && filter.equals("user")) {
                aas = automationAppService.getUserAutomationApps();
            } else {
                aas = automationAppService.getAllAutomationApps(false);
            }

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(aas).toString());
        });

        app.get("/api/automation-apps/:id", ctx -> {
            String id = ctx.pathParam("id");
            AutomationApp automationApp = automationAppService.getAutomationAppById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("version", "1");
            response.put("name", automationApp.getName());
            response.put("namespace", automationApp.getNamespace());
            response.put("oAuthEnabled", automationApp.isOAuthEnabled());
            response.put("oAuthClientId", automationApp.getoAuthClientId());
            response.put("oAuthClientSecret", automationApp.getoAuthClientSecret());
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(response).toString());
        });

        app.put("/api/automation-apps/:id", ctx -> {
            String id = ctx.pathParam("id");

            boolean automationAppSaved = false;
            String body = ctx.body();

            if (StringUtils.isNotBlank(body)) {
                Object jsonBodyObj = new JsonSlurper().parseText(body);
                if (jsonBodyObj instanceof Map) {
                    Map<String, Object> automationAppMap = (Map<String, Object>) jsonBodyObj;
                    AutomationApp automationApp = automationAppService.getAutomationAppById(id);

                    if (automationApp != null) {
                        if (automationAppMap.containsKey("oAuthEnabled")) {
                            if (automationAppMap.get("oAuthEnabled") instanceof Boolean) {
                                if ((Boolean) automationAppMap.get("oAuthEnabled") && !automationApp.isOAuthEnabled()) {
                                    logger.warn("OAuth enabled! " + automationAppMap.get("oAuthEnabled"));
                                    automationApp.setoAuthClientId(UUID.randomUUID().toString());
                                    automationApp.setoAuthClientSecret(UUID.randomUUID().toString());
                                }
                            }
                        }
                        automationAppSaved = automationAppService.updateAutomationApp(automationApp);
                    }
                }
            }

            buildStandardJsonResponse(ctx, automationAppSaved);
        });

        app.get("/api/automation-apps/:id/source", ctx -> {
            String id = ctx.pathParam("id");
            String sourceCode = automationAppService.getAutomationAppSourceCode(id);
            Map<String, String> response = new HashMap<>();
            response.put("id", id);
            response.put("version", "1");
            response.put("status", "published");
            response.put("sourceCode", sourceCode);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(response).toString());
        });

        app.put("/api/automation-apps/:id/source", ctx -> {
            String id = ctx.pathParam("id");
            Map bodyMap = (Map) new JsonSlurper().parse(ctx.bodyAsInputStream());
            String sourceCode = (String) bodyMap.get("sourceCode");
            try {
                boolean response = entityService.updateAutomationAppSourceCode(id, sourceCode);
                buildStandardJsonResponse(ctx, response);
            } catch (RuntimeException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result(new JsonBuilder(response).toString());
            }
        });

        // create new automation app from source code
        app.post("/api/automation-apps/source", ctx -> {
            Map bodyMap = (Map) new JsonSlurper().parse(ctx.bodyAsInputStream());
            String sourceCode = (String) bodyMap.get("sourceCode");
            try {
                //save source code
                String aaId = automationAppService.addAutomationAppSourceCode(sourceCode);

                if (aaId != null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("aaId", aaId);
                    ctx.status(200);
                    ctx.contentType("application/json");
                    ctx.result(new JsonBuilder(response).toString());
                } else {
                    buildStandardJsonResponse(ctx, 200, false, "Unable to save Automation App");
                }
            } catch (RuntimeException e) {
                buildStandardJsonResponse(ctx, 200, false, e.getMessage());
            }
        });

        app.get("/api/iaas/:id", ctx -> {
            String id = ctx.pathParam("id");
            InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);
            Map<String, Object> model = new HashMap<>();
            model.put("id", installedAutomationApp.getId());
            model.put("label", installedAutomationApp.getLabel());
            AutomationApp automationApp = automationAppService
                    .getAutomationAppById(installedAutomationApp.getAutomationAppId());
            model.put("name", automationApp.getName());
            model.put("namespace", automationApp.getNamespace());
            model.put("automationAppId", automationApp.getName());
            model.put("settings", installedAutomationApp.getSettings());
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        app.put("/api/iaas/:id", ctx -> {
            String id = ctx.pathParam("id");

            boolean installedAutomationAppSaved = false;
            String body = ctx.body();

            if (StringUtils.isNotBlank(body)) {
                Object jsonBodyObj = new JsonSlurper().parseText(body);
                if (jsonBodyObj instanceof Map) {
                    Map<String, Object> installedAutomationAppMap = (Map<String, Object>) jsonBodyObj;
                    InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);

                    if (installedAutomationApp != null) {
                        if (installedAutomationAppMap.containsKey("label")) {
                            installedAutomationApp.setLabel((String) installedAutomationAppMap.get("label"));
                        }
                        installedAutomationAppSaved = automationAppService
                                .updateInstalledAutomationApp(installedAutomationApp);

                        //TODO: run updated method
                    }
                }
            }

            Map<String, Object> model = new HashMap<>();
            model.put("success", installedAutomationAppSaved);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        // remove an installed automation app
        app.delete("/api/iaas/:id", ctx -> {
            String id = ctx.pathParam("id");

            //run uninstalled method
            try {
                entityService.runInstalledAutomationAppMethod(id, "uninstalled");
            } catch (MissingMethodException mme) {
                // only throw missing method exception if its for a method other than uninstalled
                if (!mme.getMessage().contains(".uninstalled()")) {
                    throw mme;
                }
            }

            boolean iaaRemoved = automationAppService.removeInstalledAutomationApp(id);
            Map<String, Object> model = new HashMap<>();


            model.put("success", iaaRemoved);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        app.post("/api/iaas/:id/methods/:methodName", ctx -> {
            String id = ctx.pathParam("id");
            String methodName = ctx.pathParam("methodName");
            entityService.runInstalledAutomationAppMethod(id, methodName);
            ctx.status(200);
        });

        app.get("/api/iaas/:id/schedules", ctx -> {
            String id = ctx.pathParam("id");

            List<Map<String, String>> scheduleList = scheduleService.getSchedulesForInstalledAutomationApp(id);

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(scheduleList).toString());
        });

        app.post("/api/iaas", ctx -> {
            String body = ctx.body();
            Map bodyMap = (Map) (new JsonSlurper().parseText(body));

            String type = (String) bodyMap.get("type");

            Map<String, Object> automationAppModel = new HashMap<>();
            automationAppModel.put("message", "");
            String automationAppId = "";

            if("child".equals(type)) {
                String parentAppId = (String) bodyMap.get("id");
                String appName = (String) bodyMap.get("appName");
                String namespace = (String) bodyMap.get("namespace");

                automationAppId = automationAppService.addChildInstalledAutomationApp(parentAppId, appName, namespace);
            } else {
                String automationAppTypeId = (String) bodyMap.get("id");
                automationAppId = automationAppService.addInstalledAutomationApp(automationAppTypeId);
            }
            automationAppModel.put("id", automationAppId);

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(automationAppModel).toString());
        });

        app.get("/api/iaas/:id/child-apps", ctx -> {
            // get the installed child apps of an installed automation app
            String id = ctx.pathParam("id");
            String appName = ctx.queryParam("appName");
            String namespace = ctx.queryParam("namespace");
            List<InstalledAutomationApp> childApps = automationAppService.getChildInstalledAutomationApps(id, appName, namespace);
            List<Map> childAppListMap = childApps.stream().map(ca -> Map.of("id", ca.getId(), "displayName", ca.getDisplayName())).collect(Collectors.toList());
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(childAppListMap).toString());
        });

        app.get("/api/iaas/:id/cfg/page", ctx -> {
            // get first page of automation app configuration or if single page app, get that.
            String id = ctx.pathParam("id");
            Object pageInfo = entityService.getInstalledAutomationAppConfigurationPage(id, null);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(pageInfo).toString());
        });

        app.get("/api/iaas/:id/cfg/page/:pageName", ctx -> {
            // get named page of automation app configuration.
            String id = ctx.pathParam("id");
            String pageName = ctx.pathParam("pageName");
            Object pageInfo = entityService.getInstalledAutomationAppConfigurationPage(id, pageName);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(pageInfo).toString());
        });

        app.get("/api/iaas/:id/cfg/settings", ctx -> {
            String id = ctx.pathParam("id");
            InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);
            List<InstalledAutomationAppSetting> settings = installedAutomationApp.getSettings();
            Map<String, Map> settingsMap;
            if (settings != null) {
                settingsMap = settings.stream().
                        collect(Collectors.toMap(data -> data.getName(), data -> data.toMap(true)));
            } else {
                settingsMap = new HashMap<>();
            }
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(settingsMap).toString());
        });

        app.get("/api/iaas/:id/cfg/info", ctx -> {
            String id = ctx.pathParam("id");
            InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);
            Map<String, Object> installedAutomationAppInfo = new LinkedHashMap<>();
            installedAutomationAppInfo.put("label", installedAutomationApp.getLabel());

            //TODO: get mode info as well.
            installedAutomationAppInfo.put("modes", new ArrayList<String>());

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(installedAutomationAppInfo).toString());
        });

        app.get("/api/iaas", ctx -> {
            List<Map> iaaListData = new ArrayList<>();
            boolean includeChildren = "true".equals(ctx.queryParam("includeChildren"));

            Collection<InstalledAutomationApp> installedAutomationApps = automationAppService
                    .getAllInstalledAutomationApps();

            for (InstalledAutomationApp iaa : installedAutomationApps) {
                if(iaa.getParentInstalledAutomationAppId() == null || includeChildren) {
                    Map<String, String> iaaData = new HashMap<>();
                    iaaData.put("id", iaa.getId());
                    iaaData.put("parentAppId", iaa.getParentInstalledAutomationAppId());
                    iaaData.put("label", iaa.getLabel());
                    AutomationApp aaInfo = automationAppService.getAutomationAppById(iaa.getAutomationAppId());

                    iaaData.put("type", aaInfo.getName());
                    iaaListData.add(iaaData);
                }
            }

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(iaaListData).toString());
        });

        // we are updating iaa config and we are not done updating the cfg
        app.patch("/api/iaas/:id/cfg/settings", ctx -> {
            String id = ctx.pathParam("id");
            String body = ctx.body();
            updateIAASettings(id, body);
            buildStandardJsonResponse(ctx, true);
        });

        // we are done updating an iaa so run installed or updated depending
        app.post("/api/iaas/:id/cfg/settings", ctx -> {
            String id = ctx.pathParam("id");
            String body = ctx.body();
            updateIAASettings(id, body);

            try {
                entityService.updateOrInstallInstalledAutomationApp(id);
                buildStandardJsonResponse(ctx, true);
            } catch (Exception e) {
                buildStandardJsonResponse(ctx, false, e.getMessage());
            }
        });
    }

    private void updateIAASettings(String iaaId, String body) {
        if (StringUtils.isNotBlank(body)) {
            Object jsonBodyObj = new JsonSlurper().parseText(body);
            if (jsonBodyObj instanceof Map) {
                Map<String, Object> jsonBody = (Map) jsonBodyObj;
                automationAppService.updateInstalledAutomationAppSettings(iaaId, jsonBody);
            }
        }
    }
}
