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
package com.parrotha.internal.entity;

import com.parrotha.api.Response;
import com.parrotha.app.DeviceWrapper;
import com.parrotha.app.EventWrapper;
import com.parrotha.exception.NotFoundException;
import com.parrotha.internal.app.InstalledAutomationApp;
import com.parrotha.internal.device.Device;
import com.parrotha.internal.system.OAuthToken;
import groovy.lang.Script;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

//This is a service that provides access to run methods and interact with devices and automation apps
public interface EntityService {
    void sendEvent(Map properties, DeviceWrapper deviceWrapper);

    void sendEvent(Map properties, InstalledAutomationApp installedAutomationApp);

    void sendHubEvent(Map properties);

    void sendLocationEvent(Map properties);

    List<EventWrapper> eventsSince(String source, String sourceId, Date date, int maxEvents);

    List<EventWrapper> eventsBetween(String source, String sourceId, Date startDate, Date endDate, int maxEvents);

    void registerEventListener(EventListener eventListener);

    void unregisterEventListener(EventListener eventListener);

    void runDeviceMethodByDNI(String integrationId, String deviceNetworkId, String methodName, Object... args);

    void runDeviceMethod(String id, String methodName, Object... args);

    Object runDeviceMethodAndReturn(String id, String methodName, Object... args);

    void reprocessAutomationApps();

    void reprocessDeviceHandlers();

    void initialize();

    String[] getDeviceHandlerByFingerprint(Map<String, String> deviceInfo);

    Collection<Device> getDevicesByCapability(String capability);

    InstalledAutomationApp getInstalledAutomationAppByClientId(String clientId, boolean createIfMissing)
            throws NotFoundException;

    OAuthToken getOauthToken(String clientId, String secretKey);

    List<String> getInstalledAutomationAppsByToken(String token);

    String getOAuthClientIdByToken(String token);

    Response processInstalledAutomationAppWebRequest(String id, String httpMethod, String path, String body, Map params, Map headers);

    void runInstalledAutomationAppMethod(String id, String methodName, Object... args);

    void runInstalledAutomationAppMethodWithException(String id, String methodName, Object... args) throws Exception;

    Object runInstalledAutomationAppMethodAndReturn(String id, String methodName, Object... args) throws Exception;

    void updateInstalledAutomationAppSettings(String id, Map<String, Object> settingsMap);

    /**
     * Check if automation app is installed already and run correct method.
     * If not installed, mark it as installed and run the installed method.
     * If installed, run the updated method.
     *
     * @param id The installed automation app id to use.
     */
    void updateOrInstallInstalledAutomationApp(String id);

    void removeInstalledAutomationAppSetting(String id, String name);

    void clearAutomationAppScripts();

    void clearDeviceHandlerScripts();

    Class<Script> getScriptForInstalledAutomationApp(String id);

    Object getInstalledAutomationAppConfigurationPage(String id, String pageName);

    Map<String, Object> getDeviceTileLayout(String id);

    Map<String, Object> getDevicePreferencesLayout(String id);

    Map<String, Object> getDeviceHandlerPreferencesLayout(String deviceHandlerId);

    Object getParentForDevice(String deviceId);

    void updateInstalledAutomationAppState(String id, Map state);

    Map getInstalledAutomationAppState(String id);

    boolean updateAutomationAppSourceCode(String id, String sourceCode);

    boolean removeAutomationApp(String id);

    boolean updateDeviceHandlerSourceCode(String id, String sourceCode);

    boolean removeDeviceHandler(String id);
}
