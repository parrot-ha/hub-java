/**
 * Copyright (c) 2021-2023 by the respective copyright holders.
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
package com.parrotha.service;

import com.parrotha.api.Response;
import com.parrotha.internal.system.OAuthToken;

import java.util.List;
import java.util.Map;

public interface CloudIntegrationService {
    Response processWebServiceRequest(String id, String httpMethod, String path, String body, Map params, Map headers);

    Map getOAuthDeviceConfigPageByClientId(String clientId);

    Map getOAuthDeviceListByClientId(String clientId);

    Map getOAuthSettingsByClientId(String clientId);

    boolean updateAutomationAppSettings(String clientId, Map<String, Object> settings);

    boolean authorizeAutomationApp(String clientId);

    boolean denyAutomationApp(String clientId);

    String getHubId();

    OAuthToken getOauthToken(String clientId, String secretKey);

    Map<String, String> getLocationIdAndName();

    List<String> getInstalledAutomationAppsByToken(String token);

    String getOAuthClientIdByToken(String token);
}
