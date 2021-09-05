/**
 * Copyright (c) 2021 by the respective copyright holders.
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DeviceDataStore {
    Collection<Device> getAllDevices();

    Collection<Device> getDevicesByCapability(String capability);

    Device getDeviceById(String id);

    Device getDeviceByIntegrationAndDNI(String integrationId, String deviceNetworkId);

    String createDevice(Device device);

    List<Device> getDeviceChildDevices(String parentDeviceId);

    List<Device> getInstalledAutomationAppIdChildDevices(String parentInstalledAutomationAppId);

    Device getInstalledAutomationAppChildDevice(String parentInstalledAutomationAppId, String deviceNetworkId);

    String createDevice(String deviceHandlerId, String deviceName, String deviceNetworkId, String integrationId, Map<String, Object> deviceData, Map<String, String> additionalIntegrationParameters);

    boolean updateDevice(Device device);

    boolean deleteDevice(String id);

    boolean updateDeviceState(String deviceId, Map deviceState);

    Collection<DeviceHandler> getAllDeviceHandlers();

    DeviceHandler getDeviceHandler(String id);

    DeviceHandler getDeviceHandlerByNamespaceAndName(String namespace, String name);

    DeviceHandler getDeviceHandlerByName(String name);

    void updateDeviceHandler(DeviceHandler deviceHandler);

    void addDeviceHandler(DeviceHandler deviceHandler);

    String getDeviceHandlerSourceCode(String id);

    boolean updateDeviceHandlerSourceCode(String id, String sourceCode);
}
