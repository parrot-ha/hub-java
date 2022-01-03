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
package com.parrotha.internal.hub;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import io.javalin.Javalin;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import com.parrotha.internal.BaseApiHandler;
import com.parrotha.internal.entity.EntityService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsApiHandler extends BaseApiHandler {
    EntityService entityService;

    public SettingsApiHandler(EntityService entityService) {
        this.entityService = entityService;
    }

    @Override
    public void setupApi(Javalin app) {
        app.get("/api/settings/logging-config", ctx -> {
            LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
            Configuration config = loggerContext.getConfiguration();
            List<Map<String, String>> loggerList = new ArrayList<>();
            for (LoggerConfig loggerConfig : config.getLoggers().values()) {
                Map<String, String> loggerInfo = new HashMap<>();
                if ("".equals(loggerConfig.getName())) {
                    loggerInfo.put("name", "ROOT");
                } else {
                    loggerInfo.put("name", loggerConfig.getName());
                }
                loggerInfo.put("level", loggerConfig.getLevel().name());
                loggerList.add(loggerInfo);
            }
            Map<String, Object> model = new HashMap<>();
            model.put("loggers", loggerList);

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        app.put("/api/settings/logging-config", ctx -> {
            try {
                String body = ctx.body();
                Map bodyMap = (Map) (new JsonSlurper().parseText(body));
                String name = (String) bodyMap.get("name");
                String level = (String) bodyMap.get("level");

                //https://stackoverflow.com/questions/23434252/programmatically-change-log-level-in-log4j2
                //http://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/config/LoggerConfig.html
                LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
                Configuration config = loggerContext.getConfiguration();
                LoggerConfig loggerConfig;
                if ("ROOT".equals(name)) {
                    loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
                } else {
                    loggerConfig = config.getLoggerConfig(name);
                }
                if (loggerConfig != null) {
                    loggerConfig.setLevel(Level.toLevel(level));
                    loggerContext.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.
                    buildStandardJsonResponse(ctx, true);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            buildStandardJsonResponse(ctx, false);
        });

        app.post("/api/settings/logging-config", ctx -> {
            String body = ctx.body();
            Map bodyMap = (Map) (new JsonSlurper().parseText(body));
            String name = (String) bodyMap.get("name");
            String level = (String) bodyMap.get("level");

            LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
            Configuration config = loggerContext.getConfiguration();
            config.addLogger(name, new LoggerConfig(name, Level.toLevel(level), false));
            loggerContext.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.
            buildStandardJsonResponse(ctx, true);
        });

        app.post("/api/settings/automation-apps", ctx -> {
            String body = ctx.body();
            Map bodyMap = (Map) (new JsonSlurper().parseText(body));
            String action = (String) bodyMap.get("action");
            if ("reload".equals(action)) {
                entityService.clearAutomationAppScripts();
                entityService.reprocessAutomationApps();
            }

            buildStandardJsonResponse(ctx, true);
        });

        app.post("/api/settings/device-handlers", ctx -> {
            String body = ctx.body();
            Map bodyMap = (Map) (new JsonSlurper().parseText(body));
            String action = (String) bodyMap.get("action");
            if ("reload".equals(action)) {
                entityService.clearDeviceHandlerScripts();
                entityService.reprocessDeviceHandlers();
            }

            buildStandardJsonResponse(ctx, true);
        });
    }
}
