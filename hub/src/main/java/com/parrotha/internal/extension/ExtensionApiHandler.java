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
import io.javalin.Javalin;

import java.util.List;
import java.util.Map;

public class ExtensionApiHandler extends BaseApiHandler {
    private ExtensionService extensionService;

    public ExtensionApiHandler(ExtensionService extensionService) {
        this.extensionService = extensionService;
    }

    @Override
    public void setupApi(Javalin app) {
        app.get("/api/extensions", ctx -> {
            boolean refresh = ctx.queryParam("refresh", "false").equals("true");

            //TODO: return pointer to status of refresh?
            if (refresh) {
                extensionService.refreshExtensionList();
            }

            List extensionList = extensionService.getInstalledExtensions();
            List availableExtensions = extensionService.getAvailableExtensions();

            Map extensions = Map.of("installed", extensionList, "available", availableExtensions);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(extensions).toString());
        });

        app.get("/api/extensions/locations", ctx -> {
            List extensionLocations = extensionService.getExtensionLocations();

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(extensionLocations).toString());
        });

    }
}
