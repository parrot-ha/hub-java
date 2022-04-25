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
package com.parrotha.internal.extension;

import com.parrotha.internal.BaseApiHandler;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import io.javalin.Javalin;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionApiHandler extends BaseApiHandler {
    private ExtensionService extensionService;

    public ExtensionApiHandler(ExtensionService extensionService) {
        this.extensionService = extensionService;
    }

    @Override
    public void setupApi(Javalin app) {
        app.get("/api/extensions/clear", ctx -> {
            extensionService.clearExtensions();
        });

        app.get("/api/extensions", ctx -> {
            boolean refresh = ctx.queryParam("refresh", "false").equals("true");

            //TODO: return pointer to status of refresh?
            if (refresh) {
                extensionService.refreshExtensionList();
            }

            Collection<Map> extensions = extensionService.getExtensionList();

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(extensions).toString());
        });

        app.post("/api/extensions/:id", ctx -> {
            String id = ctx.pathParam("id");
            if ("download".equals(ctx.queryParam("action"))) {
                // download extension
                extensionService.downloadAndInstallExtension(id);
            } else if ("update".equals(ctx.queryParam("action"))) {
                // update extension
                extensionService.updateExtension(id);
            }

            ctx.status(202);
        });

        app.delete("/api/extensions/:id", ctx -> {
            String id = ctx.pathParam("id");
            boolean status = extensionService.deleteExtension(id);

            ctx.status(200);
        });

        app.get("/api/extensions/:id/status", ctx -> {

        });

        app.get("/api/extension_settings", ctx -> {
            List extensionLocations = extensionService.getExtensionSettings();

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(extensionLocations).toString());
        });

        app.post("/api/extension_settings", ctx -> {
            boolean settingAdded = false;
            String body = ctx.body();
            String settingId = null;

            if (StringUtils.isNotBlank(body)) {
                Object jsonBodyObj = new JsonSlurper().parseText(body);
                if (jsonBodyObj instanceof Map) {
                    Map<String, Object> jsonBodyMap = (Map<String, Object>) jsonBodyObj;
                    Map<String, Object> settingMap = (Map<String, Object>) jsonBodyMap.get("setting");

                    String settingName = null;
                    if (settingMap.containsKey("name")) {
                        settingName = (String) settingMap.get("name");
                    }
                    String settingType = null;
                    if (settingMap.containsKey("type")) {
                        settingType = (String) settingMap.get("type");
                    }
                    String settingLocation = null;
                    if (settingMap.containsKey("location")) {
                        settingLocation = (String) settingMap.get("location");
                    }

                    if (StringUtils.isNotBlank(settingName) && StringUtils.isNotBlank(settingType) && StringUtils.isNotBlank(settingLocation)) {
                        settingId = extensionService.addSetting(settingName, settingType, settingLocation);
                        settingAdded = settingId != null;
                    } else {
                        settingAdded = false;
                    }
                }
            }

            Map<String, Object> model = new HashMap<>();
            model.put("success", settingAdded);
            if (settingAdded) {
                model.put("settingId", settingId);
            }
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        app.patch("/api/extension_settings/:id", ctx -> {
            String id = ctx.pathParam("id");
            //TODO: pass rest of values
            extensionService.updateSetting(id, null, null, null);


        });
    }
}
