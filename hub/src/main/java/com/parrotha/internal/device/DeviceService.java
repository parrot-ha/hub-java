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

import com.parrotha.app.exception.UnknownDeviceTypeException;
import com.parrotha.device.Event;
import com.parrotha.device.HubAction;
import com.parrotha.device.HubMultiAction;
import com.parrotha.device.HubResponse;
import com.parrotha.device.Protocol;
import com.parrotha.exception.DeviceHandlerInUseException;
import com.parrotha.internal.ChangeTrackingMap;
import com.parrotha.internal.Main;
import com.parrotha.internal.extension.ExtensionService;
import com.parrotha.internal.extension.ExtensionState;
import com.parrotha.internal.extension.ExtensionStateListener;
import com.parrotha.internal.integration.Integration;
import com.parrotha.internal.integration.IntegrationRegistry;
import com.parrotha.internal.script.ParrotHubDelegatingScript;
import groovy.lang.GString;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class DeviceService implements ExtensionStateListener {
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    public static final String PARENT_TYPE_IA_APP = "IAA";
    public static final String PARENT_TYPE_DEVICE = "DEV";

    private IntegrationRegistry integrationRegistry;
    private DeviceDataStore deviceDataStore;
    private ExtensionService extensionService;

    public DeviceService(DeviceDataStore deviceDataStore, IntegrationRegistry integrationRegistry, ExtensionService extensionService) {
        this.deviceDataStore = deviceDataStore;
        this.integrationRegistry = integrationRegistry;
        this.extensionService = extensionService;
    }

    public DeviceService(IntegrationRegistry integrationRegistry, ExtensionService extensionService) {
        this.deviceDataStore = new DeviceYamlDataStore();
        this.integrationRegistry = integrationRegistry;
        this.extensionService = extensionService;
    }

    /**
     * @param parentType
     * @param parentType
     * @param namespace
     * @param typeName
     * @param deviceNetworkId
     * @param properties
     * @return
     * @throws UnknownDeviceTypeException - If a Device Handler with the specified name and namespace is not found.
     * @throws IllegalArgumentException   - If the deviceNetworkId is not specified.
     */
    public Device addChildDevice(String parentId, String parentType,
                                 String namespace,
                                 String typeName,
                                 String deviceNetworkId,
                                 Map properties)
            throws UnknownDeviceTypeException, IllegalArgumentException {
        if (StringUtils.isBlank(deviceNetworkId)) {
            throw new IllegalArgumentException("Device Network ID not specified.");
        }

        DeviceHandler deviceHandler;
        if (StringUtils.isBlank(namespace)) {
            deviceHandler = deviceDataStore.getDeviceHandlerByName(typeName);
        } else {
            deviceHandler = deviceDataStore.getDeviceHandlerByNamespaceAndName(namespace, typeName);
        }

        if (deviceHandler == null) {
            throw new UnknownDeviceTypeException("Unable to find device type for namespace: " + namespace + " and name: " + typeName);
        }

        Device device = new Device();
        device.setDeviceHandlerId(deviceHandler.getId());
        device.setName(deviceHandler.getName());
        if (properties != null) {
            if (properties.get("label") != null) {
                device.setLabel(properties.get("label").toString());
            }
            if (properties.get("name") != null) {
                device.setName(properties.get("name").toString());
            }
            if (properties.get("data") != null && properties.get("data") instanceof Map) {
                device.setData((Map) properties.get("data"));
            }
        }
        device.setDeviceNetworkId(deviceNetworkId);
        if (parentType.equals(PARENT_TYPE_IA_APP)) {
            device.setParentInstalledAutomationAppId(parentId);
        } else if (parentType.equals(PARENT_TYPE_DEVICE)) {
            device.setParentDeviceId(parentId);
        }

        String deviceId = deviceDataStore.createDevice(device);

        return deviceDataStore.getDeviceById(deviceId);
    }

    public List<Device> getChildDevicesForDevice(String parentDeviceId) {
        return deviceDataStore.getDeviceChildDevices(parentDeviceId);
    }

    public List<Device> getInstalledAutomationAppChildDevices(String parentInstalledAutomationAppId) {
        return deviceDataStore.getInstalledAutomationAppIdChildDevices(parentInstalledAutomationAppId);
    }

    public Device getInstalledAutomationAppChildDevice(String parentInstalledAutomationAppId, String deviceNetworkId) {
        return deviceDataStore.getInstalledAutomationAppChildDevice(parentInstalledAutomationAppId, deviceNetworkId);
    }

    public void processReturnObj(Device device, Object retObj) {
        new Thread(() -> {
            if (retObj instanceof List) {
                for (Object obj : (List) retObj) {
                    if (obj instanceof String || obj instanceof GString) {
                        processStringRetObj(device, obj.toString());
                    } else if (obj instanceof HubAction) {
                        HubAction hubAction = (HubAction) retObj;
                        if (hubAction.getDni() == null) {
                            hubAction.setDni(device.getDeviceNetworkId());
                        }
                        processHubAction(device.getIntegration().getId(), hubAction);
                    } else {
                        logger.warn("TODO: process this: " + obj.getClass().getName());
                    }
                }
            } else if (retObj instanceof String || retObj instanceof GString) {
                processStringRetObj(device, retObj.toString());
            } else if (retObj instanceof HubAction) {
                String integrationId = null;
                if (device.getIntegration() != null) {
                    integrationId = device.getIntegration().getId();
                }
                HubAction hubAction = (HubAction) retObj;
                if (hubAction.getDni() == null) {
                    hubAction.setDni(device.getDeviceNetworkId());
                }
                processHubAction(integrationId, hubAction);
            } else if (retObj instanceof HubMultiAction) {
                String integrationId = null;
                if (device.getIntegration() != null) {
                    integrationId = device.getIntegration().getId();
                }
                for (HubAction hubAction : ((HubMultiAction) retObj).getActions()) {
                    if (hubAction.getDni() == null) {
                        hubAction.setDni(device.getDeviceNetworkId());
                    }
                    processHubAction(integrationId, hubAction);
                }
            } else {
                if (retObj != null) {
                    logger.warn("TODO: process this retObj: " + retObj.getClass().getName());
                }
            }
        }).start();
    }

    private void processStringRetObj(Device device, String obj) {
        String msg = obj.toString();
        // st = smartthings, ph = Parrot Hub
        if (msg.matches("(st |he |ph |raw |zdo ).*")) {
            String integrationId = null;
            if (device.getIntegration() != null) {
                integrationId = device.getIntegration().getId();
            }
            // send to integration or zigbee network.
            integrationRegistry.processAction(integrationId, new HubAction(msg, Protocol.ZIGBEE, device.getDeviceNetworkId(), null));
        } else if (msg.startsWith("delay")) {
            // delay for amount of time specified
            long delay = Long.parseLong(msg.substring("delay".length()).trim());
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Delay for " + delay);
                }
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                logger.warn("Interrupted Exception in delay process", e);
            }
        } else {
            // we don't have a protocol, so send to integration if it exists
            if (device.getIntegration() != null) {
                String integrationId = device.getIntegration().getId();
                integrationRegistry.processAction(integrationId, new HubAction(msg, Protocol.OTHER, device.getDeviceNetworkId(), null));
            }
        }
        //TODO: process other types of messages
    }

    public HubResponse processHubAction(String integrationId, HubAction action) {
        if (action != null) {
            //TODO: use regex for match
            if (action.getAction() != null && action.getAction().startsWith("delay")) {
                long delay = Long.parseLong(action.getAction().substring("delay".length()).trim());
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Delay for " + delay);
                    }
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted Exception in delay", e);
                }
            } else {
                return integrationRegistry.processAction(integrationId, action);
            }
        }
        return null;
    }

    public boolean saveDevice(Device device) {
        return deviceDataStore.updateDevice(device);
    }

    public boolean updateDevice(String id, Map<String, Object> deviceMap, Map<String, Object> settingsMap) {
        Device device = getDeviceById(id);

        if (deviceMap.containsKey("name")) {
            device.setName((String) deviceMap.get("name"));
        }
        if (deviceMap.containsKey("label")) {
            device.setLabel((String) deviceMap.get("label"));
        }
        if (deviceMap.containsKey("deviceHandlerId")) {
            device.setDeviceHandlerId((String) deviceMap.get("deviceHandlerId"));
        }
        if (deviceMap.containsKey("deviceNetworkId")) {
            device.setDeviceNetworkId((String) deviceMap.get("deviceNetworkId"));
        }
        if (deviceMap.containsKey("integrationId")) {
            String integrationId = (String) deviceMap.get("integrationId");
            if (StringUtils.isNotBlank(integrationId)) {
                if (device.getIntegration() == null) {
                    device.setIntegration(new Integration());
                }
                device.getIntegration().setId(integrationId);
            } else {
                // we are clearing the integration
                device.setIntegration(null);
            }
        }

        for (String key : settingsMap.keySet()) {
            Map setting = (Map) settingsMap.get(key);
            DeviceSetting deviceSetting = device.getSettingByName(key);
            if (deviceSetting != null) {
                // update existing setting
                deviceSetting.processValueTypeAndMultiple(setting.get("valueAsType"), (String) setting.get("type"),
                        (Boolean) setting.get("multiple"));
            } else {
                // create new setting
                deviceSetting = new DeviceSetting();
                deviceSetting.setId(UUID.randomUUID().toString());
                deviceSetting.setName(key);
                deviceSetting.processValueTypeAndMultiple(setting.get("valueAsType"), (String) setting.get("type"),
                        (Boolean) setting.get("multiple"));
                device.addSetting(deviceSetting);
            }
        }
        return deviceDataStore.updateDevice(device);
    }

    public boolean saveDeviceState(String deviceId, ChangeTrackingMap deviceState) {
        Device existingDevice = deviceDataStore.getDeviceById(deviceId);
        if (existingDevice != null) {
            Map existingState = existingDevice.getState();
            if (existingState != null) {
                ChangeTrackingMap.ChangeSet stateChanges = deviceState.changes();
                for (Object key : stateChanges.getRemoved()) {
                    existingState.remove(key);
                }
                existingState.putAll(stateChanges.getUpdated());
                existingState.putAll(stateChanges.getAdded());
                deviceDataStore.updateDeviceState(deviceId, existingState);
            } else {
                deviceDataStore.updateDeviceState(deviceId, deviceState);
            }
        }
        return true;
    }

    public boolean saveDeviceData(String deviceId, Map data) {
        Device existingDevice = deviceDataStore.getDeviceById(deviceId);
        if (existingDevice != null) {
            existingDevice.setData(data);
            return deviceDataStore.updateDevice(existingDevice);
        }
        return true;
    }

    public String addDevice(String integrationId, String deviceHandlerId, String deviceName, String deviceNetworkId, Map<String, Object> deviceData,
                            Map<String, String> additionalIntegrationParameters) {
        if (deviceName == null) {
            deviceName = deviceDataStore.getDeviceHandler(deviceHandlerId).getName();
        }
        return deviceDataStore.createDevice(deviceHandlerId, deviceName, deviceNetworkId, integrationId, deviceData, additionalIntegrationParameters);
    }

    public boolean updateExistingDevice(String integrationId,
                                        String existingDeviceNetworkId,
                                        Map<String, String> existingIntegrationParameters,
                                        String updatedDeviceNetworkId) {
        Device device = null;
        if (existingDeviceNetworkId != null) {
            Device tempDevice = getDeviceByIntegrationAndDNI(integrationId, existingDeviceNetworkId);
            if (existingIntegrationParameters != null) {
                if (deviceMatchesIntegrationParameters(tempDevice, existingIntegrationParameters)) {
                    device = tempDevice;
                }
            } else {
                device = tempDevice;
            }
        } else if (existingIntegrationParameters != null) {
            device = getDeviceByIntegrationParameters(integrationId, existingIntegrationParameters);
        }

        if (device != null) {
            device.setDeviceNetworkId(updatedDeviceNetworkId);
            return saveDevice(device);
        }
        return false;
    }

    Map<String, Future<Boolean>> devicesToRemove = new HashMap<>();

    public Future<Boolean> removeDeviceAsync(String id, boolean force) {
        Future<Boolean> deviceToRemoveFuture = devicesToRemove.get(id);
        if (deviceToRemoveFuture != null) {
            if (deviceToRemoveFuture.isDone() || deviceToRemoveFuture.isCancelled()) {
                devicesToRemove.remove(id);
            }
            return deviceToRemoveFuture;
        }
        Device device = deviceDataStore.getDeviceById(id);
        if(device == null) {
            // device is already removed.
            return CompletableFuture.completedFuture(true);
        }
        String integrationId = device.getIntegration().getId();
        String deviceNetworkId = device.getDeviceNetworkId();

        if (integrationId != null) {
            Future<Boolean> removeDeviceFuture = integrationRegistry.removeDeviceAsync(integrationId, deviceNetworkId, force);
            if (removeDeviceFuture.isDone()) {
                deviceDataStore.deleteDevice(id);
            } else {
                devicesToRemove.put(id, removeDeviceFuture);
                //wait for future to resolve then remove device from db
                new Thread(() -> {
                    while (!removeDeviceFuture.isDone() && !removeDeviceFuture.isCancelled()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            logger.info("Interrupted while waiting for remove device future to complete", e);
                        }
                    }
                    if (removeDeviceFuture.isDone() && !removeDeviceFuture.isCancelled()) {
                        try {
                            Boolean result = removeDeviceFuture.get(5, TimeUnit.SECONDS);
                            if (result != null && result.booleanValue()) {
                                deviceDataStore.deleteDevice(id);
                            }
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            logger.warn("Exception while getting device future", e);
                        }
                    }
                    devicesToRemove.remove(id);
                }).start();
            }
            return removeDeviceFuture;
        } else {
            // there is no integration, so just remove device.
            boolean removedDeviceStatus = deviceDataStore.deleteDevice(id);
            return CompletableFuture.completedFuture(removedDeviceStatus);
        }
    }

    public void cancelRemoveDeviceAsync(String id) {
        Future deviceToRemoveFuture = devicesToRemove.remove(id);
        if (deviceToRemoveFuture != null) {
            if (!deviceToRemoveFuture.isCancelled() && !deviceToRemoveFuture.isDone()) {
                deviceToRemoveFuture.cancel(true);
            }
        }
    }

    public boolean deleteDevice(String integrationId, String deviceNetworkId) {
        Device device = deviceDataStore.getDeviceByIntegrationAndDNI(integrationId, deviceNetworkId);
        if (device == null) {
            return true;
        }
        if (!integrationId.equals(device.getIntegration().getId())) {
            return false;
        }

        return deviceDataStore.deleteDevice(device.getId());
    }

    public Collection<Device> getAllDevices() {
        return deviceDataStore.getAllDevices();
    }

    public Collection<Device> getDevicesByCapability(String capability) {
        return deviceDataStore.getDevicesByCapability(capability);
    }

    public Collection<Device> getDevicesByDeviceHandler(String deviceHandlerId) {
        return deviceDataStore.getDevicesByDeviceHandler(deviceHandlerId);
    }

    public Device getDeviceById(String id) {
        return deviceDataStore.getDeviceById(id);
    }

    public Device getDeviceByIntegrationAndDNI(String integrationId, String deviceNetworkId) {
        return deviceDataStore.getDeviceByIntegrationAndDNI(integrationId, deviceNetworkId);
    }

    public Device getDeviceByIntegrationParameters(String integrationId, Map<String, String> integrationParameters) {
        for (Device device : getAllDevices()) {
            if (device.getIntegration() != null
                    && device.getIntegration().getId() != null
                    && device.getIntegration().getId().equals(integrationId)
                    && deviceMatchesIntegrationParameters(device, integrationParameters)) {
                return device;
            }
        }
        return null;
    }

    public boolean deviceExists(String integrationId, String deviceNetworkId, boolean includeUnaffiliated) {
        return getDeviceByIntegrationAndDNI(includeUnaffiliated ? null : integrationId, deviceNetworkId) != null;
    }

    public boolean deviceExists(String integrationId, String deviceNetworkId, Map<String, String> additionalIntegrationParameters) {
        if (deviceNetworkId != null) {
            Device device = getDeviceByIntegrationAndDNI(integrationId, deviceNetworkId);
            if (device != null) {
                // check additional integration options
                return deviceMatchesIntegrationParameters(device, additionalIntegrationParameters);
            }
        } else {
            for (Device device : getAllDevices()) {
                if (device.getIntegration() != null
                        && device.getIntegration().getId() != null
                        && device.getIntegration().getId().equals(integrationId)
                        && deviceMatchesIntegrationParameters(device, additionalIntegrationParameters)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean deviceMatchesIntegrationParameters(Device device, Map<String, String> additionalIntegrationParameters) {
        if (device != null && device.getIntegration() != null) {
            //check additional integration options
            if (additionalIntegrationParameters != null) {
                for (String key : additionalIntegrationParameters.keySet()) {
                    Object option = additionalIntegrationParameters.get(key);
                    if (option == null && device.getIntegration().getOption(key) != null) {
                        return false;
                    }
                    if (option != null && !option.equals(device.getIntegration().getOption(key))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void updateDeviceState(Event event) {
        Device d = deviceDataStore.getDeviceById(event.getDevice().getId());
        State s = new State(UUID.randomUUID().toString(), event.getName(), event.getValue(), event.getUnit(), event.getDate());
        d.setCurrentState(s);
        //TODO: store state history in database
        //TODO: use write behind cache for saving device
        deviceDataStore.updateDevice(d);
    }

    public void updateDeviceSetting(String id, String name, Object value) {
        updateDeviceSetting(id, name, null, value);
    }

    public void updateDeviceSetting(String id, String name, String type, Object value) {
        Device device = getDeviceById(id);
        DeviceSetting deviceSetting = device.getSettingByName(name);
        if (deviceSetting != null) {
            deviceSetting.processValueTypeAndMultiple(value, type != null ? type : deviceSetting.getType(), deviceSetting.isMultiple());
            deviceDataStore.updateDevice(device);
        }
    }

    public void initialize() {
        reprocessDeviceHandlers();
        if (extensionService != null) {
            extensionService.registerStateListener(this);
        }
    }

    public void shutdown() {
        if (extensionService != null) {
            extensionService.unregisterStateListener(this);
        }
    }

    public Collection<DeviceHandler> getAllDeviceHandlers() {
        return deviceDataStore.getAllDeviceHandlers();
    }

    public DeviceHandler getDeviceHandlerByNameAndNamespace(String name, String namespace) {
        for (DeviceHandler deviceHandler : getAllDeviceHandlers()) {
            if (deviceHandler.getName() != null && deviceHandler.getName().equals(name) &&
                    deviceHandler.getNamespace() != null && deviceHandler.getNamespace().equals(namespace)) {
                return deviceHandler;
            }
        }
        return null;
    }

    public DeviceHandler getDeviceHandler(String id) {
        return deviceDataStore.getDeviceHandler(id);
    }

    public Attribute getAttributeForDeviceHandler(String deviceHandlerId, String attributeName) {
        DeviceHandler deviceHandler = getDeviceHandler(deviceHandlerId);
        if (deviceHandler == null) {
            return null;
        }

        if (deviceHandler.getAttributeList() != null) {
            for (Attribute attribute : deviceHandler.getAttributeList()) {
                if (attributeName.equals(attribute.getName())) {
                    return attribute;
                }
            }
        }
        if (deviceHandler.getCapabilityList() != null) {
            for (String capabilityName : deviceHandler.getCapabilityList()) {
                Capability capability = Capabilities.getCapability(capabilityName);
                if (capability != null) {
                    if (capability.getAttributes() != null) {
                        for (Attribute attribute : capability.getAttributes()) {
                            if (attributeName.equalsIgnoreCase(attribute.getName())) {
                                return attribute;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public ClassLoader getClassLoaderForDeviceHandler(String deviceHandlerId) {
        DeviceHandler deviceHandler = getDeviceHandler(deviceHandlerId);
        if (deviceHandler.getType() == DeviceHandler.Type.EXTENSION) {
            return extensionService.getExtensionClassloader(deviceHandler.getExtensionId());
        } else if (deviceHandler.getType() == DeviceHandler.Type.SYSTEM) {
            return Main.class.getClassLoader();
        } else {
            return null;
        }
    }

    public void reprocessDeviceHandler(String id) {
        DeviceHandler existingDeviceHandler = getDeviceHandler(id);
        String fileName = existingDeviceHandler.getFile();
        if (!fileName.startsWith("class")) {
            File f = new File(fileName);
            try {
                String scriptCode = IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8);
                Map metadata = extractDeviceHandlerMetadata(scriptCode);
                metadata.put("type", existingDeviceHandler.getType());
                metadata.put("extensionId", existingDeviceHandler.getExtensionId());
                DeviceHandler newDeviceHandler = new DeviceHandler(id, fileName, metadata);
                if (!newDeviceHandler.equalsIgnoreId(existingDeviceHandler)) {
                    deviceDataStore.updateDeviceHandler(newDeviceHandler);
                }
            } catch (IOException e) {
                logger.warn("IOException while attempting to load file " + fileName, e);
            }
        }
    }


    public void reprocessDeviceHandlers() {
        // run this process in the background, allows quicker start up of system at the
        // expense of system starting up with possibly old device handler definition, however
        // this should be quickly rectified once system is fully running
        new Thread(() -> {
            Collection<DeviceHandler> deviceHandlers = deviceDataStore.getAllDeviceHandlers();
            Map<String, DeviceHandler> newDeviceHandlerInfoMap = processDeviceHandlerInfo();

            if (deviceHandlers != null && newDeviceHandlerInfoMap != null) {
                // check each device handler info against what is in the config file.
                compareNewAndExistingDeviceHandlers(deviceHandlers, newDeviceHandlerInfoMap.values());
            }
        }).start();
    }

    private void compareNewAndExistingDeviceHandlers(Collection<DeviceHandler> existingDeviceHandlers, Collection<DeviceHandler> newDeviceHandlers) {
        // check each device handler info against what is in the config file.
        Iterator<DeviceHandler> newDHInfoIter = newDeviceHandlers.iterator();

        while (newDHInfoIter.hasNext()) {
            DeviceHandler newDHInfo = newDHInfoIter.next();
            String fileName = newDHInfo.getFile();
            Iterator<DeviceHandler> oldDHInfoIter = existingDeviceHandlers.iterator();

            boolean foundExistingDH = false;
            while (oldDHInfoIter.hasNext()) {
                DeviceHandler oldDHInfo = oldDHInfoIter.next();
                if (fileName.equals(oldDHInfo.getFile())) {
                    foundExistingDH = true;
                    // the file name matches, let see if any of the values have changed.
                    //TODO: this check is only if the file name stays the same, add another check in case all the contents stay the same, but the file name changed.
                    if (newDHInfo.equalsIgnoreId(oldDHInfo)) {
                        // only difference is the id,, so no changes
                        logger.debug("No changes for file " + fileName);
                    } else {
                        logger.debug("Changes for file " + fileName);
                        newDHInfo.setId(oldDHInfo.getId());
                        deviceDataStore.updateDeviceHandler(newDHInfo);
                    }
                }
            }
            if (!foundExistingDH) {
                // we have a new device handler.
                deviceDataStore.addDeviceHandler(newDHInfo);
            }
        }
    }

    private Map<String, DeviceHandler> processDeviceHandlerInfo() {
        // we need to process device handlers
        Map<String, DeviceHandler> deviceHandlerInfo = new HashMap<>();

        // load built in device handlers (pre-compiled)
        try {
            ClassLoader classLoader = Main.class.getClassLoader();
            Enumeration<URL> resources = classLoader.getResources("deviceHandlerClasses.yaml");
            deviceHandlerInfo.putAll(getDeviceHandlersFromResources(resources, DeviceHandler.Type.SYSTEM, classLoader, null));
        } catch (IOException e) {
            logger.warn("IO Exception loading precompiled device handler", e);
        }

        // load device handlers from data store
        Map<String, InputStream> dhSources = deviceDataStore.getDeviceHandlerSources();
        deviceHandlerInfo.putAll(createDeviceHandlersFromSource(dhSources, DeviceHandler.Type.USER, null));

        // load device handler sources from extensions
        Map<String, Map<String, InputStream>> extDHSources = extensionService.getDeviceHandlerSources();
        for (String extensionId : extDHSources.keySet()) {
            deviceHandlerInfo.putAll(
                    createDeviceHandlersFromSource(extDHSources.get(extensionId), DeviceHandler.Type.EXTENSION_SOURCE, extensionId));
        }

        // load device handlers from extension classpaths (pre-compiled)
        Map<String, Pair<Enumeration<URL>, ClassLoader>> extensionResources = extensionService.getResourcesFromExtensions(
                "deviceHandlerClasses.yaml");
        for (String extensionId : extensionResources.keySet()) {
            Pair<Enumeration<URL>, ClassLoader> resource = extensionResources.get(extensionId);
            deviceHandlerInfo.putAll(
                    getDeviceHandlersFromResources(resource.getLeft(), DeviceHandler.Type.EXTENSION, resource.getRight(), extensionId));
        }

        return deviceHandlerInfo;
    }

    private Map<String, DeviceHandler> createDeviceHandlersFromSource(Map<String, InputStream> dhSources, DeviceHandler.Type type,
                                                                      String extensionId) {
        Map<String, DeviceHandler> deviceHandlers = new HashMap<>();
        if (dhSources != null && dhSources.size() > 0) {
            for (String dhSourceKey : dhSources.keySet()) {
                try {
                    String scriptCode = IOUtils.toString(dhSources.get(dhSourceKey), StandardCharsets.UTF_8);
                    Map metadata = extractDeviceHandlerMetadata(scriptCode);
                    metadata.put("type", type);
                    metadata.put("extensionId", extensionId);

                    DeviceHandler deviceHandler = new DeviceHandler(UUID.randomUUID().toString(), dhSourceKey, metadata);
                    deviceHandlers.put(deviceHandler.getId(), deviceHandler);
                } catch (Exception exception) {
                    logger.warn(String.format("Caught exception while processing %s", dhSourceKey), exception);
                }
            }
        }
        return deviceHandlers;
    }

    private Map<String, DeviceHandler> getDeviceHandlersFromResources(Enumeration<URL> resources, DeviceHandler.Type deviceHandlerType,
                                                                      ClassLoader classLoader, String extensionId) {
        Map<String, DeviceHandler> deviceHandlerInfo = new HashMap<>();
        if (resources == null || classLoader == null) {
            return deviceHandlerInfo;
        }
        try {
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                List<Map> list = yaml.load(url.openStream());
                for (Map m : list) {
                    String deviceHandlerId = (String) m.get("id");
                    String className = (String) m.get("className");
                    Class<ParrotHubDelegatingScript> deviceHandlerScriptClass = (Class<ParrotHubDelegatingScript>) Class.forName(className, false,
                            classLoader);
                    ParrotHubDelegatingScript deviceHandlerScript = deviceHandlerScriptClass.getDeclaredConstructor().newInstance();
                    Map dhi = extractDeviceHandlerMetadata(deviceHandlerScript);
                    dhi.put("type", deviceHandlerType);
                    dhi.put("extensionId", extensionId);
                    deviceHandlerInfo.put(deviceHandlerId, new DeviceHandler(deviceHandlerId, "class:" + className, dhi));
                }
            }
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            logger.warn("Exception in loading device handlers from resources", e);
        }

        return deviceHandlerInfo;
    }

    private Map extractDeviceHandlerMetadata(String deviceHandlerScript) {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass("com.parrotha.internal.script.ParrotHubDelegatingScript");
        GroovyShell shell = new GroovyShell(compilerConfiguration);

        ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) shell.parse(deviceHandlerScript);
        return extractDeviceHandlerMetadata(parrotHubDelegatingScript);
    }

    private Map extractDeviceHandlerMetadata(DelegatingScript deviceHandlerScript) {
        deviceHandlerScript.setDelegate(new DeviceDefinitionDelegate());

        deviceHandlerScript.invokeMethod("run", null);
        DeviceDefinitionDelegate deviceDefinitionDelegate = (DeviceDefinitionDelegate) deviceHandlerScript.getDelegate();

        return deviceDefinitionDelegate.metadataValue;
    }

    public Collection<DeviceHandler> getUserDeviceHandlers() {
        return deviceDataStore.getAllDeviceHandlers().stream()
                .filter(dh -> dh.isUserType()).collect(Collectors.toList());
    }

    public String getDeviceHandlerSourceCode(String id) {
        return deviceDataStore.getDeviceHandlerSourceCode(id);
    }

    public boolean updateDeviceHandlerSourceCode(String id, String sourceCode) {
        extractDeviceHandlerMetadata(sourceCode);
        return deviceDataStore.updateDeviceHandlerSourceCode(id, sourceCode);
    }

    public boolean removeDeviceHandler(String id) {
        Collection<Device> devicesInUse = getDevicesByDeviceHandler(id);
        if (devicesInUse.size() > 0) {
            throw new DeviceHandlerInUseException("Device Handler in use", devicesInUse);
        } else {
            return deviceDataStore.deleteDeviceHandler(id);
        }
    }

    public String addDeviceHandlerSourceCode(String sourceCode) {
        Map metadata = extractDeviceHandlerMetadata(sourceCode);
        if (metadata == null) {
            throw new IllegalArgumentException("No metadata found.");
        }
        metadata.put("type", DeviceHandler.Type.USER);
        String dhId = deviceDataStore
                .addDeviceHandlerSourceCode(sourceCode, new DeviceHandler(null, null, metadata));
        return dhId;
    }

    @Override
    public void stateUpdated(ExtensionState state) {
        String extensionId = state.getId();
        if (ExtensionState.StateType.INSTALLED.equals(state.getState()) || ExtensionState.StateType.UPDATED.equals(state.getState())) {
            // we need to process device handlers
            Map<String, DeviceHandler> newDeviceHandlerInfo = new HashMap<>();

            // load device handler sources from extensions
            Map<String, InputStream> extDHSources = extensionService.getDeviceHandlerSources(extensionId);
            newDeviceHandlerInfo.putAll(createDeviceHandlersFromSource(extDHSources, DeviceHandler.Type.EXTENSION_SOURCE, extensionId));

            // load device handlers from extension classpaths (pre-compiled)
            Pair<Enumeration<URL>, ClassLoader> extensionResources = extensionService.getResourcesFromExtension(extensionId,
                    "deviceHandlerClasses.yaml");
            if (extensionResources != null) {
                newDeviceHandlerInfo.putAll(getDeviceHandlersFromResources(extensionResources.getLeft(), DeviceHandler.Type.EXTENSION,
                        extensionResources.getRight(), extensionId));
            }

            Collection<DeviceHandler> deviceHandlers = deviceDataStore.getAllDeviceHandlers();

            // check each device handler info against what is in the config file.
            compareNewAndExistingDeviceHandlers(deviceHandlers, newDeviceHandlerInfo.values());
        } else if (ExtensionState.StateType.DELETED.equals(state.getState())) {
            if (isExtensionInUse(state.getId()).getLeft()) {
                throw new RuntimeException("Devices still in use");
            }
            // delete all device handlers that are a part of this extension
            List<String> dhIds = getAllDeviceHandlers().stream().filter(dh -> extensionId.equals(dh.getExtensionId())).map(DeviceHandler::getId)
                    .collect(Collectors.toList());
            for (String dhId : dhIds) {
                deviceDataStore.deleteDeviceHandler(dhId);
            }
        }
    }

    @Override
    public Pair<Boolean, String> isExtensionInUse(String extensionId) {
        Collection<Device> devices = deviceDataStore.getDevicesByExtension(extensionId);
        if (devices.size() == 0) {
            return new ImmutablePair<>(false, "");
        } else {
            boolean inUse = false;
            StringBuilder sb = new StringBuilder();
            for (Device device : devices) {
                DeviceHandler dh = getDeviceHandler(device.getDeviceHandlerId());
                if (dh != null && extensionId.equals(dh.getExtensionId())) {
                    inUse = true;
                    sb.append("Device ").append(device.getDisplayName()).append("\n");
                }
            }
            return new ImmutablePair<>(inUse, sb.toString());
        }
    }
}
