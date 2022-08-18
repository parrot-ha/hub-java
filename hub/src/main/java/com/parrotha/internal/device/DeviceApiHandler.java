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
package com.parrotha.internal.device;

import com.parrotha.app.EventWrapper;
import com.parrotha.internal.BaseApiHandler;
import com.parrotha.internal.entity.EntityService;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import io.javalin.Javalin;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeviceApiHandler extends BaseApiHandler {
    DeviceService deviceService;
    EntityService entityService;

    public DeviceApiHandler(DeviceService deviceService, EntityService entityService) {
        this.deviceService = deviceService;
        this.entityService = entityService;
    }

    public void setupApi(Javalin app) {
        app.ws("/api/devices/:id/events", ws -> {
            final Map<String, DeviceSocketEventListener> deviceSocketEventListenerMap = new HashMap<>();
            ws.onConnect(ctx -> {
                String deviceId = ctx.pathParam("id");
                DeviceSocketEventListener deviceEventListener = new DeviceSocketEventListener();
                deviceEventListener.registerCtx(deviceId, ctx);
                deviceSocketEventListenerMap.put(ctx.getSessionId(), deviceEventListener);
                entityService.registerEventListener(deviceEventListener);
            });

            ws.onClose(ctx -> {
                DeviceSocketEventListener deviceEventListener = deviceSocketEventListenerMap.remove(ctx.getSessionId());
                entityService.unregisterEventListener(deviceEventListener);
                deviceEventListener.unregisterCtx();
            });
        });

        app.get("/api/devices/:id/settings", ctx -> {
            String id = ctx.pathParam("id");
            Device device = deviceService.getDeviceById(id);
            Map<String, DeviceSetting> settings = device.getNameToSettingMap();
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(settings).toString());
        });

        app.get("/api/devices/:id/preferences-layout", ctx -> {
            // get device handler preferences.
            String id = ctx.pathParam("id");
            Object pageInfo = entityService.getDevicePreferencesLayout(id);
            if (pageInfo == null) {
                pageInfo = new HashMap<>();
            }
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(pageInfo).toString());
        });

        app.get("/api/devices", ctx -> {
            String filter = ctx.queryParam("filter");
            Collection<Device> devices;

            if (StringUtils.isNotBlank(filter) && filter.startsWith("capability.")) {
                devices = deviceService.getDevicesByCapability(filter.substring("capability.".length()));
            } else {
                devices = deviceService.getAllDevices();
            }

            List<Map<String, String>> deviceListData = new ArrayList<>();
            for (Device device : devices) {
                Map<String, String> devData = new HashMap<>();
                devData.put("id", device.getId());
                devData.put("name", device.getName());
                devData.put("label", device.getLabel());
                devData.put("displayName", device.getDisplayName());
                DeviceHandler dh = deviceService.getDeviceHandler(device.getDeviceHandlerId());
                if (dh != null) {
                    devData.put("type", dh.getName());
                }
                devData.put("deviceNetworkId", device.getDeviceNetworkId());
                deviceListData.add(devData);
            }

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(deviceListData).toString());
        });

        // retrieve a device
        app.get("/api/devices/:id", ctx -> {
            String id = ctx.pathParam("id");
            Device device = deviceService.getDeviceById(id);

            Map<String, Object> model = new HashMap<>();
            model.put("id", device.getId());
            model.put("name", device.getName());
            model.put("label", device.getLabel());
            String integrationId = null;
            if (device.getIntegration() != null) {
                integrationId = device.getIntegration().getId();
            }
            model.put("integrationId", integrationId);
            DeviceHandler dh = deviceService.getDeviceHandler(device.getDeviceHandlerId());
            if (dh != null) {
                model.put("type", dh.getName());
            }
            model.put("deviceHandlerId", device.getDeviceHandlerId());
            model.put("deviceNetworkId", device.getDeviceNetworkId());
            //TODO: get rest of data (created, updated, data, current states, in use by)

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        // remove a device
        app.delete("/api/devices/:id", ctx -> {
            String id = ctx.pathParam("id");
            boolean deviceRemoved = deviceService.removeDevice(id);
            Map<String, Object> model = new HashMap<>();
            model.put("success", deviceRemoved);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        // Create a device
        app.post("/api/devices", ctx -> {
            boolean deviceAdded = false;
            String body = ctx.body();
            String deviceId = null;

            if (StringUtils.isNotBlank(body)) {
                Object jsonBodyObj = new JsonSlurper().parseText(body);
                if (jsonBodyObj instanceof Map) {
                    Map<String, Object> jsonBodyMap = (Map<String, Object>) jsonBodyObj;
                    Map<String, Object> deviceMap = (Map<String, Object>) jsonBodyMap.get("device");

                    String deviceName = null;
                    if (deviceMap.containsKey("name")) {
                        deviceName = (String) deviceMap.get("name");
                    }
                    String deviceHandlerId = null;
                    if (deviceMap.containsKey("deviceHandlerId")) {
                        deviceHandlerId = (String) deviceMap.get("deviceHandlerId");
                    }
                    String deviceNetworkId = null;
                    if (deviceMap.containsKey("deviceNetworkId")) {
                        deviceNetworkId = (String) deviceMap.get("deviceNetworkId");
                    }
                    String integrationId = null;
                    if (deviceMap.containsKey("integrationId")) {
                        integrationId = (String) deviceMap.get("integrationId");
                    }

                    if (StringUtils.isNotBlank(deviceHandlerId) && StringUtils.isNotBlank(deviceName) && StringUtils.isNotBlank(deviceNetworkId)) {
                        deviceId = deviceService.addDevice(integrationId, deviceHandlerId, deviceName, deviceNetworkId, null, null);
                        deviceAdded = deviceId != null;

                        Device device = deviceService.getDeviceById(deviceId);

                        //handle preferences
                        Map<String, Object> settingsMap = (Map<String, Object>) jsonBodyMap.get("settings");
                        for (String key : settingsMap.keySet()) {
                            // TODO: check for null values
                            device.addSetting(new DeviceSetting((Map) settingsMap.get(key)));
                        }

                        if (deviceMap.containsKey("label")) {
                            String label = (String) deviceMap.get("label");
                            if (StringUtils.isNotEmpty(label)) {
                                device.setLabel(label);
                            }
                        }
                        deviceService.saveDevice(device);

                        //TODO: run installed method
                        try {
                            entityService.runDeviceMethod(deviceId, "installed");
                        } catch (Exception e) {
                            //logger.warn("Exception running device method", e);
                        }
                    } else {
                        deviceAdded = false;
                    }
                }
            }

            Map<String, Object> model = new HashMap<>();
            model.put("success", deviceAdded);
            if (deviceAdded) {
                model.put("deviceId", deviceId);
            }
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        // Update a device
        app.put("/api/devices/:id", ctx -> {
            String id = ctx.pathParam("id");

            boolean deviceSaved = false;
            String body = ctx.body();

            if (StringUtils.isNotBlank(body)) {
                Object jsonBodyObj = new JsonSlurper().parseText(body);
                if (jsonBodyObj instanceof Map) {
                    Device device = deviceService.getDeviceById(id);
                    if (device != null) {
                        Map<String, Object> jsonBodyMap = (Map<String, Object>) jsonBodyObj;
                        Map<String, Object> deviceMap = (Map<String, Object>) jsonBodyMap.get("device");
                        //handle preferences
                        Map<String, Object> settingsMap = (Map<String, Object>) jsonBodyMap.get("settings");

                        deviceSaved = deviceService.updateDevice(id, deviceMap, settingsMap);

                        if (deviceSaved) {
                            //run updated method
                            try {
                                entityService.runDeviceMethod(id, "updated");
                                buildStandardJsonResponse(ctx, true);
                            } catch (Exception e) {
                                buildStandardJsonResponse(ctx, false, e.getMessage());
                            }
                        }
                    }
                }
            }

            Map<String, Object> model = new HashMap<>();
            model.put("success", deviceSaved);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        app.get("/api/device-id-map", ctx -> {
            Map<String, Object> model = new HashMap<>();
            Collection<Device> devices = deviceService.getAllDevices();
            for (Device device : devices) {
                model.put(device.getId(), device.getDisplayName());
            }
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        app.get("/api/devices/:id/states", ctx -> {
            String id = ctx.pathParam("id");
            Device device = deviceService.getDeviceById(id);

            Collection<State> currentStates;
            if (device.getCurrentStates() != null) {
                currentStates = device.getCurrentStates().values();
            } else {
                currentStates = new ArrayList<>();
            }

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(currentStates).toString());
        });

        app.get("/api/devices/:id/events", ctx -> {
            String id = ctx.pathParam("id");
            Date fromDate = Date.from(LocalDateTime.now().minusDays(7).atZone(ZoneId.systemDefault()).toInstant());
            List<EventWrapper> events = entityService.eventsSince("DEVICE", id, fromDate, -1);

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(events).toString());
        });

        app.get("/api/devices/:id/information", ctx -> {
            String id = ctx.pathParam("id");
            Device device = deviceService.getDeviceById(id);
            device.getData();
            Map<String, Object> information = new HashMap<>();
            information.put("data", device.getData());
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(information).toString());
        });

        app.get("/api/devices/:id/commands", ctx -> {
            String id = ctx.pathParam("id");
            Device device = deviceService.getDeviceById(id);

            Map<String, Object> model = new HashMap<>();
            model.put("deviceId", id);
            Set<Command> commands = new LinkedHashSet<>();

            String deviceHandlerId = device.getDeviceHandlerId();
            DeviceHandler deviceHandlerInfo = deviceService.getDeviceHandler(deviceHandlerId);
            if (deviceHandlerInfo != null) {
                List<String> capabilityList = deviceHandlerInfo.getCapabilityList();
                if (capabilityList != null) {
                    for (String capabilityName : capabilityList) {
                        capabilityName = StringUtils.deleteWhitespace(capabilityName);

                        Capability capability = Capabilities.getCapability(capabilityName);
                        if (capability != null) {
                            if (capability.getCommands() != null) {
                                for (Command command : capability.getCommands()) {
                                    // TODO: check for existing command and don't add duplicates
                                    commands.add(command);
                                }
                            }
                        }
                    }
                }
                List<Command> commandList = deviceHandlerInfo.getCommandList();
                if (commandList != null) {
                    for (Command command : deviceHandlerInfo.getCommandList()) {
                        commands.add(command);
                    }
                }
            }

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(commands).toString());
        });

        app.post("/api/devices/:id/commands/:command", ctx -> {
            String id = ctx.pathParam("id");
            String command = ctx.pathParam("command");
            String body = ctx.body();
            if (StringUtils.isNotBlank(body)) {
                Object jsonBodyObj = new JsonSlurper().parseText(body);
                if (jsonBodyObj instanceof List) {
                    entityService.runDeviceMethod(id, command, castArgs((List) jsonBodyObj));
                }
            } else {
                entityService.runDeviceMethod(id, command);
            }
            ctx.status(202);
        });

        app.get("/api/device-handlers", ctx -> {
            String filter = ctx.queryParam("filter");
            List<String> fields = ctx.queryParams("field");

            Collection<DeviceHandler> deviceHandlers;
            if (StringUtils.isNotBlank(filter) && filter.equals("user")) {
                deviceHandlers = deviceService.getUserDeviceHandlers();
            } else {
                deviceHandlers = deviceService.getAllDeviceHandlers();
            }

            if (fields != null && fields.size() > 0) {
                List<Map<String, Object>> dhList = new ArrayList<>();
                for (DeviceHandler dh : deviceHandlers) {
                    Map<String, Object> dhInfo = new HashMap<>();
                    for (String field : fields) {
                        switch (field) {
                            case "id":
                                dhInfo.put("id", dh.getId());
                                break;
                            case "name":
                                dhInfo.put("name", dh.getName());
                                break;
                            case "namespace":
                                dhInfo.put("namespace", dh.getNamespace());
                                break;
                            case "tags":
                                dhInfo.put("tags", dh.getTags());
                        }
                    }
                    dhList.add(dhInfo);
                }
                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result(new JsonBuilder(dhList).toString());
            } else {
                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result(new JsonBuilder(deviceHandlers).toString());
            }
        });

        app.get("/api/device-handlers/:id/source", ctx -> {
            String id = ctx.pathParam("id");
            String sourceCode = deviceService.getDeviceHandlerSourceCode(id);
            Map<String, String> response = new HashMap<>();
            response.put("id", id);
            response.put("version", "1");
            response.put("status", "published");
            response.put("sourceCode", sourceCode);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(response).toString());
        });

        app.put("/api/device-handlers/:id/source", ctx -> {
            String id = ctx.pathParam("id");
            Map bodyMap = (Map) new JsonSlurper().parse(ctx.bodyAsInputStream());
            String sourceCode = (String) bodyMap.get("sourceCode");
            try {
                boolean response = entityService.updateDeviceHandlerSourceCode(id, sourceCode);
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

        // create new device handler from source code
        app.post("/api/device-handlers/source", ctx -> {
            Map bodyMap = (Map) new JsonSlurper().parse(ctx.bodyAsInputStream());
            String sourceCode = (String) bodyMap.get("sourceCode");
            try {
                //save source code in new file
                String dhId = deviceService.addDeviceHandlerSourceCode(sourceCode);

                if (dhId != null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("id", dhId);
                    ctx.status(200);
                    ctx.contentType("application/json");
                    ctx.result(new JsonBuilder(response).toString());
                } else {
                    buildStandardJsonResponse(ctx, 200, false, "Unable to save Device Handler");
                }
            } catch (RuntimeException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result(new JsonBuilder(response).toString());
            }
        });

        app.get("/api/device-handlers/:id/preferences-layout", ctx -> {
            // get device handler preferences.
            String id = ctx.pathParam("id");
            Object pageInfo = entityService.getDeviceHandlerPreferencesLayout(id);
            if (pageInfo == null) {
                pageInfo = new HashMap<>();
            }
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(pageInfo).toString());

        });
    }

    private Object castArgs(List args) {
        if (args == null || args.size() == 0) {
            return null;
        }

        Object[] retArgs = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            Map arg = (Map) args.get(i);
            String argType = ((String) arg.get("type")).toUpperCase();
            if ("NUMBER".equals(argType)) {
                retArgs[i] = NumberUtils.createNumber(arg.get("value").toString());
            } else {
                retArgs[i] = arg.get("value").toString();
            }
        }

        if (retArgs.length == 1) {
            return retArgs[0];
        } else {
            return retArgs;
        }
    }
}
