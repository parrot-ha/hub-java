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

import java.util.Map;

public interface DeviceIntegrationService {
    @Deprecated
    String addDevice(String integrationId, String deviceHandlerId, String deviceName, String deviceNetworkId, Map<String, Object> deviceData,
                     Map<String, String> additionalIntegrationParameters);

    String addDevice(String integrationId, String deviceNetworkId, Map<String, String> fingerprint, Map<String, Object> deviceData,
                     Map<String, String> additionalIntegrationParameters);

    boolean deleteDevice(String integrationId, String deviceNetworkId);

    void runDeviceMethodByDNI(String integrationId, String deviceNetworkId, String methodName, Object... args);

    boolean deviceExists(String integrationId, String deviceNetworkId, boolean includeUnaffiliated);

    boolean deviceExists(String integrationId, String deviceNetworkId, Map<String, String> additionalIntegrationParameters);

    boolean updateExistingDevice(String integrationId, String existingDeviceNetworkId,
                                 Map<String, String> existingIntegrationParameters,
                                 String updatedDeviceNetworkId);

    String[] getDeviceHandlerByFingerprint(Map<String, String> deviceInfo);

    void sendHubEvent(Map properties);
}
