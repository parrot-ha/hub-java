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
package com.parrotha.integration;

import com.parrotha.device.HubAction;
import com.parrotha.device.HubResponse;
import com.parrotha.device.Protocol;
import com.parrotha.integration.device.DeviceAddedEvent;
import com.parrotha.integration.device.DeviceMessageEvent;
import com.parrotha.internal.integration.AbstractIntegration;
import com.parrotha.service.DeviceIntegrationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public abstract class DeviceIntegration extends AbstractIntegration {
    /**
     * Deprecated, override to provide system with a way to remove devices
     *
     * @param deviceNetworkId
     * @return
     * @Deprecated use removeIntegrationDeviceAsync(String, boolean)
     */
    @Deprecated
    public boolean removeIntegrationDevice(String deviceNetworkId) {
        return removeIntegrationDevice(deviceNetworkId, false);
    }

    /**
     * Implement to provide system with a way to remove devices
     *
     * @param deviceNetworkId
     * @param force
     * @return
     * @Deprecated use removeIntegrationDeviceAsync(String, boolean)
     */
    public boolean removeIntegrationDevice(String deviceNetworkId, boolean force) {
        return false;
    }

    public abstract Future<Boolean> removeIntegrationDeviceAsync(String deviceNetworkId, boolean force);

    public abstract HubResponse processAction(HubAction hubAction);

    // override if you want to specify the protocol that this integration supports
    // in general, it should be other, but if you want a specific system-wide handling of
    // a protocol, this is where it is specified.
    public Protocol getProtocol() {
        return Protocol.OTHER;
    }

    // override if you want to provide tags to filter device handlers by
    public List<String> getTags() {
        return new ArrayList<>();
    }

    private DeviceIntegrationService deviceIntegrationService;

    public void setDeviceIntegrationService(DeviceIntegrationService deviceIntegrationService) {
        this.deviceIntegrationService = deviceIntegrationService;
    }

    @Deprecated
    public void sendDeviceMessage(String deviceNetworkId, String message) {
        sendEvent(new DeviceMessageEvent(deviceNetworkId, message));
    }

    @Deprecated
    public void sendDeviceMessage(String deviceNetworkId, String message, boolean unaffiliated) {
        sendEvent(new DeviceMessageEvent(deviceNetworkId, message));
    }

    @Deprecated
    public boolean deviceExists(String deviceNetworkId) {
        return deviceExists(deviceNetworkId, false);
    }

    @Deprecated
    public boolean deviceExists(String deviceNetworkId, boolean includeUnaffiliated) {
        return deviceIntegrationService.deviceExists(getId(), deviceNetworkId, includeUnaffiliated);
    }

    @Deprecated
    public boolean deviceExists(String deviceNetworkId, Map<String, String> integrationParameters) {
        return deviceIntegrationService.deviceExists(getId(), deviceNetworkId, integrationParameters);
    }

    /**
     * @param existingDeviceNetworkId       If null, device will only be matched with integration parameters
     * @param existingIntegrationParameters If null, device will only be matched with device network id
     * @param updatedDeviceNetworkId        New device network id to assign to device
     * @return status of update
     * @Deprecated
     */
    @Deprecated
    public boolean updateExistingDevice(String existingDeviceNetworkId,
                                        Map<String, String> existingIntegrationParameters,
                                        String updatedDeviceNetworkId) {
        return deviceIntegrationService.updateExistingDevice(getId(), existingDeviceNetworkId, existingIntegrationParameters, updatedDeviceNetworkId);
    }

    @Deprecated
    public String[] getDeviceHandlerByFingerprint(Map<String, String> fingerprint) {
        return deviceIntegrationService.getDeviceHandlerByFingerprint(fingerprint);
    }

    @Deprecated
    public void addDevice(String deviceHandlerId, String deviceName, String deviceNetworkId, Map<String, Object> deviceData,
                          Map<String, String> additionalIntegrationParameters) {
        deviceIntegrationService.addDevice(getId(), deviceHandlerId, deviceName, deviceNetworkId, deviceData, additionalIntegrationParameters);
    }

    @Deprecated
    public void addDevice(String deviceNetworkId, Map<String, String> fingerprint, Map<String, Object> deviceData,
                          Map<String, String> additionalIntegrationParameters) {
        sendEvent(new DeviceAddedEvent(deviceNetworkId, true, fingerprint, deviceData, additionalIntegrationParameters));
    }

    /**
     * @param deviceNetworkId
     * @return
     * @Deprecated send device removed event
     */
    @Deprecated
    public boolean deleteItem(String deviceNetworkId) {
        return deviceIntegrationService.deleteDevice(getId(), deviceNetworkId);
    }

    @Deprecated
    public void sendHubEvent(Map properties) {
        deviceIntegrationService.sendHubEvent(properties);
    }
}
