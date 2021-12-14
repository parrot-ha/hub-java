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

import groovy.lang.GString;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import com.parrotha.app.exception.UnknownDeviceTypeException;
import com.parrotha.device.Event;
import com.parrotha.device.HubAction;
import com.parrotha.device.HubResponse;
import com.parrotha.device.Protocol;
import com.parrotha.internal.ChangeTrackingMap;
import com.parrotha.internal.Main;
import com.parrotha.internal.integration.Integration;
import com.parrotha.internal.integration.IntegrationRegistry;
import com.parrotha.internal.script.ParrotHubDelegatingScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
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
import java.util.stream.Collectors;

public class DeviceService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    public static final String PARENT_TYPE_IA_APP = "IAA";
    public static final String PARENT_TYPE_DEVICE = "DEV";

    private IntegrationRegistry integrationRegistry;

    private DeviceDataStore deviceDataStore;

    public DeviceService(DeviceDataStore deviceDataStore, IntegrationRegistry integrationRegistry) {
        this.deviceDataStore = deviceDataStore;
        this.integrationRegistry = integrationRegistry;
    }

    public DeviceService(IntegrationRegistry integrationRegistry) {
        this.deviceDataStore = new DeviceYamlDataStore();
        this.integrationRegistry = integrationRegistry;
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
                        processHubAction(device.getIntegration().getId(), (HubAction) obj);
                    } else {
                        logger.warn("TODO: process this: " + obj.getClass().getName());
                    }
                    //TODO: process HubAction
                }
            } else if (retObj instanceof String || retObj instanceof GString) {
                processStringRetObj(device, retObj.toString());
            } else if (retObj instanceof HubAction) {
                String integrationId = null;
                if (device.getIntegration() != null) {
                    integrationId = device.getIntegration().getId();
                }
                processHubAction(integrationId, (HubAction) retObj);
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
                e.printStackTrace();
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
                    e.printStackTrace();
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
                deviceSetting.processValueTypeAndMultiple(setting.get("value"), (String) setting.get("type"), (Boolean) setting.get("multiple"));
            } else {
                // create new setting
                deviceSetting = new DeviceSetting();
                deviceSetting.setId(UUID.randomUUID().toString());
                deviceSetting.setName(key);
                deviceSetting.processValueTypeAndMultiple(setting.get("value"), (String) setting.get("type"), (Boolean) setting.get("multiple"));
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

    public boolean removeDevice(String id) {
        return removeDevice(id, false);
    }

    public boolean removeDevice(String id, boolean force) {
        Device device = deviceDataStore.getDeviceById(id);
        String integrationId = device.getIntegration().getId();
        String deviceNetworkId = device.getDeviceNetworkId();

        boolean removedFromIntegration = false;
        if (integrationId != null) {
            removedFromIntegration = integrationRegistry.removeDevice(integrationId, deviceNetworkId);
        } else {
            // there is no integration, so just mark it as successful.
            removedFromIntegration = true;
        }

        if (!removedFromIntegration && !force) {
            return false;
        }

        return deviceDataStore.deleteDevice(id);
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

    public void initialize() {
        reprocessDeviceHandlers();
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

    public void reprocessDeviceHandler(String id) {
        DeviceHandler existingDeviceHandler = getDeviceHandler(id);
        String fileName = existingDeviceHandler.getFile();
        if (!fileName.startsWith("class")) {
            File f = new File(fileName);
            try {
                String scriptCode = IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8);
                Map definition = extractDeviceHandlerInformation(scriptCode);
                DeviceHandler newDeviceHandler = new DeviceHandler(id, fileName, definition);
                if (!newDeviceHandler.equalsIgnoreId(existingDeviceHandler)) {
                    deviceDataStore.updateDeviceHandler(newDeviceHandler);
                }
            } catch (IOException e) {
                logger.warn("IOException while attempting to load file " + fileName, e);
            }
        }
    }


    public void reprocessDeviceHandlers() {
        Collection<DeviceHandler> deviceHandlers = deviceDataStore.getAllDeviceHandlers();

        // run this process in the background, allows quicker start up of system at the
        // expense of system starting up with possibly old device handler definition, however
        // this should be quickly rectified once system is fully running
        new Thread(() -> {
            Map<String, DeviceHandler> newDeviceHandlerInfoMap = processDeviceHandlerInfo();

            // check each device handler info against what is in the config file.
            Iterator<DeviceHandler> newDHInfoIter = newDeviceHandlerInfoMap.values().iterator();
            while (newDHInfoIter.hasNext()) {
                DeviceHandler newDHInfo = newDHInfoIter.next();
                String fileName = newDHInfo.getFile();
                Iterator<DeviceHandler> oldDHInfoIter = deviceHandlers.iterator();

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
        }).start();
    }

    private Map<String, DeviceHandler> processDeviceHandlerInfo() {
        // we need to process device handlers
        Map<String, DeviceHandler> deviceHandlerInfo = new HashMap<>();

        // load device handlers from jar files (pre-compiled)
        try {
            Enumeration<URL> resources = Main.class.getClassLoader().getResources("deviceHandlerClasses.yaml");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                List<Map> list = yaml.load(url.openStream());
                for (Map m : list) {
                    String deviceHandlerId = (String) m.get("id");
                    String className = (String) m.get("className");

                    Class<ParrotHubDelegatingScript> deviceHandlerScriptClass = (Class<ParrotHubDelegatingScript>) Class.forName(className);
                    ParrotHubDelegatingScript deviceHandlerScript = deviceHandlerScriptClass.getDeclaredConstructor().newInstance();
                    Map dhi = extractDeviceHandlerInformation(deviceHandlerScript);
                    deviceHandlerInfo.put(deviceHandlerId, new DeviceHandler(deviceHandlerId, "class:" + className, dhi));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        } catch (InstantiationException instantiationException) {
            instantiationException.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // load device handlers from text files on local file system
        File deviceHandlerDir = new File("deviceHandlers/");
        if (!deviceHandlerDir.exists()) {
            deviceHandlerDir.mkdir();
        }
        if (deviceHandlerDir.exists() && deviceHandlerDir.isDirectory()) {
            File[] deviceHandlerFiles = deviceHandlerDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".groovy");
                }
            });

            if (deviceHandlerFiles != null && deviceHandlerFiles.length > 0) {
                for (File f : deviceHandlerFiles) {
                    try {
                        String deviceHandlerId = UUID.randomUUID().toString();

                        String scriptCode = IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8);
                        Map dhi = extractDeviceHandlerInformation(scriptCode);

                        deviceHandlerInfo.put(deviceHandlerId, new DeviceHandler(deviceHandlerId, "deviceHandlers/" + f.getName(), dhi));
                    } catch (Exception e) {
                        logger.warn(String.format("Caught exception while processing %s", f.getName()), e);
                    }
                }
                return deviceHandlerInfo;
            }
        }
        return deviceHandlerInfo;
    }

    private Map extractDeviceHandlerInformation(String deviceHandlerScript) {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass("com.parrotha.internal.script.ParrotHubDelegatingScript");
        GroovyShell shell = new GroovyShell(compilerConfiguration);

        ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) shell.parse(deviceHandlerScript);
        return extractDeviceHandlerInformation(parrotHubDelegatingScript);
    }

    private Map extractDeviceHandlerInformation(DelegatingScript deviceHandlerScript) {
        deviceHandlerScript.setDelegate(new DeviceDefinitionDelegate());

        deviceHandlerScript.invokeMethod("run", null);
        DeviceDefinitionDelegate deviceDefinitionDelegate = (DeviceDefinitionDelegate) deviceHandlerScript.getDelegate();

        return deviceDefinitionDelegate.metadataValue;
    }

    public Collection<DeviceHandler> getUserDeviceHandlers() {
        return deviceDataStore.getAllDeviceHandlers().stream()
                .filter(dh -> !dh.getFile().startsWith("class")).collect(Collectors.toList());
    }

    public String getDeviceHandlerSourceCode(String id) {
        return deviceDataStore.getDeviceHandlerSourceCode(id);
    }

    public boolean updateDeviceHandlerSourceCode(String id, String sourceCode) {
        extractDeviceHandlerInformation(sourceCode);
        return deviceDataStore.updateDeviceHandlerSourceCode(id, sourceCode);
    }

}
