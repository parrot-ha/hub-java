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
package com.parrotha.integration;

import com.parrotha.api.Response;
import com.parrotha.internal.integration.AbstractIntegration;
import com.parrotha.internal.system.OAuthToken;
import com.parrotha.service.CloudIntegrationService;

import java.util.List;
import java.util.Map;

public abstract class CloudIntegration extends AbstractIntegration {
    private CloudIntegrationService cloudIntegrationService;

    public void setCloudIntegrationService(CloudIntegrationService cloudIntegrationService) {
        this.cloudIntegrationService = cloudIntegrationService;
    }

    public Response processWebServiceRequest(String id, String httpMethod, String path, String body, Map params, Map headers) {
        return cloudIntegrationService.processWebServiceRequest(id, httpMethod, path, body, params, headers);
    }

    public Map getOAuthDeviceConfigPageByClientId(String clientId) {
        return cloudIntegrationService.getOAuthDeviceConfigPageByClientId(clientId);
    }

    public Map getOAuthDeviceListByClientId(String clientId) {
        return cloudIntegrationService.getOAuthDeviceListByClientId(clientId);
    }

    public Map getOAuthSettingsByClientId(String clientId) {
        return cloudIntegrationService.getOAuthSettingsByClientId(clientId);
    }

    public boolean updateAutomationAppSettings(String clientId, Map<String, Object> settings) {
        return cloudIntegrationService.updateAutomationAppSettings(clientId, settings);
    }

    public boolean authorizeAutomationApp(String clientId) {
        return cloudIntegrationService.authorizeAutomationApp(clientId);
    }

    public boolean denyAutomationApp(String clientId) {
        return cloudIntegrationService.denyAutomationApp(clientId);
    }

    public String getHubId() {
        return cloudIntegrationService.getHubId();
    }

    public Map<String, String> getLocationIdAndName() {
        return cloudIntegrationService.getLocationIdAndName();
    }

    public OAuthToken getOauthToken(String clientId, String secretKey) {
        return cloudIntegrationService.getOauthToken(clientId, secretKey);
    }

    public List<String> getInstalledAutomationAppsByToken(String token) {
        return cloudIntegrationService.getInstalledAutomationAppsByToken(token);
    }

    public String getOAuthClientIdByToken(String token) {
        return cloudIntegrationService.getOAuthClientIdByToken(token);
    }

    public abstract String getApiServerUrl();
}
