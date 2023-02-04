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
package com.parrotha.internal.device;

import com.parrotha.internal.entity.EntityService;
import com.parrotha.service.DeviceIntegrationService;

import java.util.Map;

public class DeviceIntegrationServiceImpl implements DeviceIntegrationService {
    private DeviceService deviceService;
    private EntityService entityService;

    public DeviceIntegrationServiceImpl(DeviceService deviceService, EntityService entityService) {
        this.deviceService = deviceService;
        this.entityService = entityService;
    }

    @Override
    public String addDevice(String integrationId, String deviceHandlerId, String deviceName, String deviceNetworkId, Map<String, Object> deviceData, Map<String, String> additionalIntegrationParameters) {
        String deviceId = deviceService.addDevice(integrationId, deviceHandlerId, deviceName, deviceNetworkId, deviceData, additionalIntegrationParameters);
        entityService.runDeviceMethod(deviceId, "installed");
        return deviceId;
    }

    @Override
    public boolean deleteDevice(String integrationId, String deviceNetworkId) {
        return deviceService.deleteDevice(integrationId, deviceNetworkId);
    }

    @Override
    public boolean updateExistingDevice(String integrationId,
                                        String existingDeviceNetworkId,
                                        Map<String, String> existingIntegrationParameters,
                                        String updatedDeviceNetworkId) {
        return deviceService.updateExistingDevice(integrationId,
                existingDeviceNetworkId,
                existingIntegrationParameters,
                updatedDeviceNetworkId);
    }

    @Override
    public void runDeviceMethodByDNI(String integrationId, String deviceNetworkId, String methodName, Object... args) {
        entityService.runDeviceMethodByDNI(integrationId, deviceNetworkId, methodName, args);
    }

    @Override
    public boolean deviceExists(String integrationId, String deviceNetworkId, boolean includeUnaffiliated) {
        return deviceService.deviceExists(integrationId, deviceNetworkId, includeUnaffiliated);
    }

    @Override
    public boolean deviceExists(String integrationId, String deviceNetworkId, Map<String, String> additionalIntegrationParameters) {
        return deviceService.deviceExists(integrationId, deviceNetworkId, additionalIntegrationParameters);
    }

    @Override
    public String[] getDeviceHandlerByFingerprint(Map<String, String> deviceInfo) {
        return entityService.getDeviceHandlerByFingerprint(deviceInfo);
    }

    @Override
    public void sendHubEvent(Map properties) {
        entityService.sendHubEvent(properties);
    }
}
