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
package com.parrotha.internal.entity;

import org.apache.groovy.util.Maps;
import com.parrotha.api.Response;
import com.parrotha.exception.NotFoundException;
import com.parrotha.internal.app.InstalledAutomationApp;
import com.parrotha.internal.app.InstalledAutomationAppSetting;
import com.parrotha.internal.hub.Location;
import com.parrotha.internal.hub.LocationService;
import com.parrotha.internal.system.OAuthToken;
import com.parrotha.service.CloudIntegrationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CloudIntegrationServiceImpl implements CloudIntegrationService {
    private EntityService entityService;
    private LocationService locationService;

    public CloudIntegrationServiceImpl(EntityService entityService, LocationService locationService) {
        this.entityService = entityService;
        this.locationService = locationService;
    }

    @Override
    public Response processWebServiceRequest(String id, String httpMethod, String path, String body, Map params, Map headers) {
        return entityService.processInstalledAutomationAppWebRequest(id, httpMethod, path, body, params, headers);
    }

    @Override
    public Map getOAuthDeviceConfigPageByClientId(String clientId) {
        try {
            InstalledAutomationApp installedAutomationApp = entityService
                    .getInstalledAutomationAppByClientId(clientId, true);
            Object configPage = entityService
                    .getInstalledAutomationAppConfigurationPage(installedAutomationApp.getId(), null);
            if (configPage instanceof Map) {
                return (Map) configPage;
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map getOAuthDeviceListByClientId(String clientId) {
        try {
            InstalledAutomationApp installedAutomationApp = entityService
                    .getInstalledAutomationAppByClientId(clientId, true);
            Object configPage = entityService
                    .getInstalledAutomationAppConfigurationPage(installedAutomationApp.getId(), null);
            // get devices
            Map<String, Object> devices = new HashMap<>();
            List<Map> sections = (List<Map>) ((Map) configPage).get("sections");
            for (Map section : sections) {
                List<Map> inputs = (List<Map>) section.get("input");
                for (Map input : inputs) {
                    String inputType = (String) input.get("type");
                    if (inputType != null && inputType.startsWith("capability")) {
                        if (!devices.containsKey(inputType)) {
                            devices.put(inputType, entityService
                                    .getDevicesByCapability(inputType.substring("capability.".length())).stream()
                                    .map(device -> Maps
                                            .of("id", device.getId(), "displayName", device.getDisplayName()))
                                    .collect(Collectors.toList()));
                        }
                    }
                }
            }
            return devices;
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map getOAuthSettingsByClientId(String clientId) {
        try {
            InstalledAutomationApp installedAutomationApp = entityService
                    .getInstalledAutomationAppByClientId(clientId, true);
            Object configPage = entityService
                    .getInstalledAutomationAppConfigurationPage(installedAutomationApp.getId(), null);
            if (configPage instanceof Map) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("layout", configPage);
                // get settings
                List<InstalledAutomationAppSetting> installedAutomationAppSettingList = installedAutomationApp
                        .getSettings();
                if (installedAutomationAppSettingList != null) {
                    Map<String, Map> settingsMap = installedAutomationAppSettingList.stream()
                            .collect(Collectors.toMap(data -> data.getName(), data -> data.toMap(true)));
                    return settingsMap;
                } else {
                    return new HashMap<>();
                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean updateAutomationAppSettings(String clientId, Map<String, Object> settings) {
        InstalledAutomationApp installedAutomationApp = null;
        try {
            installedAutomationApp = entityService
                    .getInstalledAutomationAppByClientId(clientId, true);
            entityService.updateInstalledAutomationAppSettings(installedAutomationApp.getId(), settings);
            return true;
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean authorizeAutomationApp(String clientId) {
        InstalledAutomationApp installedAutomationApp = null;
        try {
            installedAutomationApp = entityService
                    .getInstalledAutomationAppByClientId(clientId, true);

            //if not installed yet: mark as installed, run installed method else will run updated method
            entityService.updateOrInstallInstalledAutomationApp(installedAutomationApp.getId());
            return true;
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean denyAutomationApp(String clientId) {
        return false;
    }

    @Override
    public String getHubId() {
        return locationService.getHub().getId();
    }

    @Override
    public OAuthToken getOauthToken(String clientId, String secretKey) {
        return entityService.getOauthToken(clientId, secretKey);
    }

    @Override
    public Map<String, String> getLocationIdAndName() {
        Location location = locationService.getLocation();
        return Maps.of("id", location.getId(), "name", location.getName());
    }

    @Override
    public List<String> getInstalledAutomationAppsByToken(String token) {
        return entityService.getInstalledAutomationAppsByToken(token);
    }

    @Override
    public String getOAuthClientIdByToken(String token) {
        return entityService.getOAuthClientIdByToken(token);
    }
}
