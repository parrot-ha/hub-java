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
package com.parrotha.internal.device;

import com.parrotha.internal.integration.Integration;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurperClassic;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DeviceYamlDataStore implements DeviceDataStore {
    private static final Logger logger = LoggerFactory.getLogger(DeviceYamlDataStore.class);
    private Map<String, Device> devices;

    private Map<String, String> deviceDNItoIdMap;
    private Map<String, Set<String>> childDeviceMap;
    private Map<String, Set<String>> appChildDeviceMap;

    @Override
    public Collection<Device> getAllDevices() {
        return getDevices().values();
    }

    @Override
    public Collection<Device> getDevicesByCapability(String capability) {
        Collection<Device> devices = new ArrayList<>();
        if (StringUtils.isBlank(capability)) {
            return devices;
        }
        for (Device device : getAllDevices()) {
            List<String> capabilityList = getDeviceHandler(device.getDeviceHandlerId()).getCapabilityList();
            if (capabilityList != null) {
                for (String deviceCapability : capabilityList) {
                    if (capability.equalsIgnoreCase(StringUtils.deleteWhitespace(deviceCapability))) {
                        devices.add(device);
                    }
                }
            }
        }
        return devices;
    }

    @Override
    public Collection<Device> getDevicesByExtension(String extensionId) {
        Collection<Device> devices = new HashSet<>();
        for (DeviceHandler deviceHandler : getAllDeviceHandlers()) {
            for (Device device : getAllDevices()) {
                if (deviceHandler.getId() != null && deviceHandler.getId().equals(device.getDeviceHandlerId())) {
                    devices.add(device);
                }
            }
        }
        return devices;
    }

    @Override
    public Device getDeviceById(String id) {
        Device device = getDevices().get(id);
        if (device != null) {
            return SerializationUtils.clone(device);
        }
        return null;
    }

    @Override
    public Device getDeviceByIntegrationAndDNI(String integrationId, String deviceNetworkId) {
        Device device = getDevices().get(getDeviceDNItoIDMap().get((integrationId != null ? integrationId : "null") + ":" + deviceNetworkId));
        if (device != null) {
            return SerializationUtils.clone(device);
        }
        return null;
    }

    @Override
    public List<Device> getDeviceChildDevices(String parentDeviceId) {
        List<Device> childDevices = new ArrayList<>();
        Set<String> childDeviceIds = getChildDeviceMap().get(parentDeviceId);

        if (childDeviceIds != null && childDeviceIds.size() > 0) {
            for (String childDeviceId : childDeviceIds) {
                Device childDevice = getDeviceById(childDeviceId);
                if (childDevice != null) {
                    childDevices.add(childDevice);
                }
            }
        }
        return childDevices;
    }

    @Override
    public List<Device> getInstalledAutomationAppIdChildDevices(String parentInstalledAutomationAppId) {
        List<Device> childDevices = new ArrayList<>();
        Set<String> childDeviceIds = getAppChildDeviceMap().get(parentInstalledAutomationAppId);

        if (childDeviceIds != null && childDeviceIds.size() > 0) {
            for (String childDeviceId : childDeviceIds) {
                Device childDevice = getDeviceById(childDeviceId);
                if (childDevice != null) {
                    childDevices.add(childDevice);
                }
            }
        }
        return childDevices;
    }

    @Override
    public Device getInstalledAutomationAppChildDevice(String parentInstalledAutomationAppId, String deviceNetworkId) {
        Device device = getDeviceByIntegrationAndDNI(null, deviceNetworkId);
        if (device != null && parentInstalledAutomationAppId.equals(device.getParentInstalledAutomationAppId())) {
            return device;
        }
        return null;
    }

    public String createDevice(Device device) {
        String deviceId = UUID.randomUUID().toString();
        device.setId(deviceId);
        getDevices().put(deviceId, device);
        if (StringUtils.isNotEmpty(device.getParentDeviceId())) {
            addChildDevice(getChildDeviceMap(), device.getParentDeviceId(), device.getId());
        } else if (StringUtils.isNotEmpty(device.getParentInstalledAutomationAppId())) {
            addChildDevice(getAppChildDeviceMap(), device.getParentInstalledAutomationAppId(), device.getId());
        }
        saveDevice(deviceId);

        return deviceId;
    }

    @Override
    public String createDevice(String deviceHandlerId, String deviceName, String deviceNetworkId, String integrationId,
                               Map<String, Object> deviceData, Map<String, String> additionalIntegrationParameters) {
        Map<String, Device> deviceSettings = getDevices();

        String deviceId = UUID.randomUUID().toString();

        Device d = new Device();
        d.getIntegration().setId(integrationId);
        d.getIntegration().setOptions(additionalIntegrationParameters);

        d.setData(deviceData);

        d.setDeviceNetworkId(deviceNetworkId);
        d.setName(deviceName);
        d.setDeviceHandlerId(deviceHandlerId);
        d.setId(deviceId);

        deviceSettings.put(deviceId, d);
        getDeviceDNItoIDMap().put((integrationId != null ? integrationId : "null") + ":" + deviceNetworkId, deviceId);

        saveDevice(deviceId);

        return deviceId;
    }

    @Override
    public boolean updateDevice(Device device) {
        Device existingDevice = getDevices().get(device.getId());
        if (existingDevice != null) {
            // TODO: check for changes instead of assigning all values and writing
            existingDevice.setDeviceHandlerId(device.getDeviceHandlerId());
            existingDevice.setDeviceNetworkId(device.getDeviceNetworkId());
            existingDevice.setName(device.getName());
            existingDevice.setCurrentStates(device.getCurrentStates());
            existingDevice.setLabel(device.getLabel());
            existingDevice.setSettings(device.getSettings());
            existingDevice.setState(device.getState());
            existingDevice.setData(device.getData());
            existingDevice.setUpdated(new Date());

            if (device.getIntegration() != null) {
                if (existingDevice.getIntegration() == null) {
                    existingDevice.setIntegration(new Integration());
                }
                existingDevice.getIntegration().setId(device.getIntegration().getId());
            }

            saveDevice(existingDevice.getId());
        } else {
            throw new IllegalArgumentException("Device does not exist");
        }

        return true;
    }

    @Override
    public boolean updateDeviceState(String deviceId, Map deviceState) {
        Device existingDevice = getDevices().get(deviceId);
        if (existingDevice != null) {
            existingDevice.setState(new LinkedHashMap(deviceState));
            saveDevice(existingDevice.getId());
        } else {
            throw new IllegalArgumentException("Device does not exist");
        }

        return true;
    }

    @Override
    public boolean deleteDevice(String id) {
        //delete file in devices
        File deviceConfig = new File("config/devices/" + id + ".yaml");
        boolean deleted = deviceConfig.delete();
        if (!deleted) {
            logger.warn("Unable to remove device config file for " + id);
            return false;
        }

        getDevices().remove(id);

        return true;
    }

    private Map<String, Device> getDevices() {
        if (devices == null) {
            loadDevices();
        }
        return devices;
    }

    private Map<String, String> getDeviceDNItoIDMap() {
        if (deviceDNItoIdMap == null) {
            loadDevices();
        }
        return deviceDNItoIdMap;
    }

    private Map<String, Set<String>> getAppChildDeviceMap() {
        if (appChildDeviceMap == null) {
            loadDevices();
        }
        return appChildDeviceMap;
    }

    private Map<String, Set<String>> getChildDeviceMap() {
        if (childDeviceMap == null) {
            loadDevices();
        }
        return childDeviceMap;
    }

    synchronized private void saveDevice(String deviceId) {
        Device d = getDevices().get(deviceId);

        //synchronize this on each device, don't need to synchronize for all
        synchronized (d) {
            try {
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                File deviceConfig = new File("config/devices/" + deviceId + ".yaml");
                FileWriter fileWriter = new FileWriter(deviceConfig);
                yaml.dump(convertDeviceToMap(d), fileWriter);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized public void loadDevices() {

        if (devices != null) {
            return;
        }
        Map<String, Device> newDevices = new HashMap<>();
        Map<String, String> newDeviceDNItoIdMap = new HashMap<>();
        Map<String, Set<String>> newChildDeviceMap = new HashMap<>();
        Map<String, Set<String>> newAppChildDeviceMap = new HashMap<>();

        File deviceConfigDir = new File("config/devices/");
        if (deviceConfigDir.exists() && deviceConfigDir.isDirectory()) {
            File[] deviceConfigFiles = deviceConfigDir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".yaml"));

            if (deviceConfigFiles != null && deviceConfigFiles.length > 0) {
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                for (File f : deviceConfigFiles) {
                    try {
                        Map deviceMap = yaml.load(new FileInputStream(f));
                        Device d = createDeviceFromMap(deviceMap);
                        newDevices.put(d.getId(), d);
                        newDeviceDNItoIdMap.put((d.getIntegration() != null ? d.getIntegration().getId() : "null") + ":" + d.getDeviceNetworkId(),
                                d.getId());
                        if (StringUtils.isNotEmpty(d.getParentDeviceId())) {
                            addChildDevice(newChildDeviceMap, d.getParentDeviceId(), d.getId());
                        } else if (StringUtils.isNotEmpty(d.getParentInstalledAutomationAppId())) {
                            addChildDevice(newAppChildDeviceMap, d.getParentInstalledAutomationAppId(), d.getId());
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        devices = newDevices;
        deviceDNItoIdMap = newDeviceDNItoIdMap;
        childDeviceMap = newChildDeviceMap;
        this.appChildDeviceMap = newAppChildDeviceMap;
    }


    private Device createDeviceFromMap(Map<String, Object> map) {
        Device device = new Device();
        device.setId((String) map.get("id"));
        device.setDeviceHandlerId((String) map.get("deviceHandlerId"));
        device.setName((String) map.get("name"));
        device.setLabel((String) map.get("label"));
        device.setModelName((String) map.get("modelName"));
        device.setManufacturerName((String) map.get("manufacturerName"));
        device.setDeviceNetworkId((String) map.get("deviceNetworkId"));
        device.setParentDeviceId((String) map.get("parentDeviceId"));
        device.setParentInstalledAutomationAppId((String) map.get("parentInstalledAutomationAppId"));
        if (map.get("integration") != null) {
            device.setIntegration(new Integration((Map) map.get("integration")));
        }
        if (map.get("state") != null && map.get("state") instanceof String && StringUtils.isNotEmpty((String) map.get("state"))) {
            // need to use classic json slurper so we don't end up with LazyMap that can't be serialized.
            device.setState((Map) new JsonSlurperClassic().parseText((String) map.get("state")));
        }
        if (map.get("data") != null && map.get("data") instanceof String && StringUtils.isNotEmpty((String) map.get("data"))) {
            // need to use classic json slurper so we don't end up with LazyMap that can't be serialized.
            device.setData((Map) new JsonSlurperClassic().parseText((String) map.get("data")));
        }

        if (map.get("currentStates") != null && map.get("currentStates") instanceof Map) {
            Map<String, State> currentStates = new HashMap<>();
            Map<String, Map> mapCurrentStates = ((Map<String, Map>) map.get("currentStates"));
            for (String key : mapCurrentStates.keySet()) {
                currentStates.put(key, new State(mapCurrentStates.get(key)));
            }

            device.setCurrentStates(currentStates);
        }

        Object settings = map.get("settings");
        if (settings instanceof List) {
            List<DeviceSetting> deviceSettings = new ArrayList<>();
            for (Object setting : (List) settings) {
                deviceSettings.add(new DeviceSetting((Map) setting));
            }
            device.setSettings(deviceSettings);
        }

        if (map.get("created") != null && map.get("created") instanceof Integer) {
            device.setCreated(new Date((Integer) map.get("created")));
        }
        if (map.get("updated") != null && map.get("updated") instanceof Integer) {
            device.setUpdated(new Date((Integer) map.get("updated")));
        }

        return device;
    }

    private Map convertDeviceToMap(Device device) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", device.getId());
        map.put("deviceHandlerId", device.getDeviceHandlerId());
        map.put("name", device.getName());
        map.put("label", device.getLabel());
        map.put("modelName", device.getModelName());
        map.put("manufacturerName", device.getManufacturerName());
        map.put("deviceNetworkId", device.getDeviceNetworkId());
        map.put("parentDeviceId", device.getParentDeviceId());
        map.put("parentInstalledAutomationAppId", device.getParentInstalledAutomationAppId());
        map.put("integration", device.getIntegration().toMap());
        //serialize state to json and back to filter out any bad values
        //https://docs.smartthings.com/en/latest/smartapp-developers-guide/state.html#persistence-model
        if (device.getState() != null) {
            map.put("state", new JsonBuilder(device.getState()).toString());
            device.setState((Map) new JsonSlurperClassic().parseText((String) map.get("state")));
        }
        if (device.getData() != null) {
            map.put("data", new JsonBuilder(device.getData()).toString());
        }

        if (device.getCurrentStates() != null) {
            Map<String, Map<String, Object>> currentStatesMap = new HashMap<>();
            for (String key : device.getCurrentStates().keySet()) {
                currentStatesMap.put(key, device.getCurrentStates().get(key).toMap());
            }
            map.put("currentStates", currentStatesMap);
        }

        if (device.getSettings() != null) {
            ArrayList<Map> mapSettings = new ArrayList<>();
            for (DeviceSetting setting : device.getSettings()) {
                mapSettings.add(setting.toMap());
            }
            map.put("settings", mapSettings);
        }

        map.put("created", device.getCreated().getTime());
        map.put("updated", device.getUpdated().getTime());

        return map;
    }

    private void addChildDevice(Map<String, Set<String>> childDeviceMap, String parentId, String childDeviceId) {
        if (childDeviceMap.containsKey(parentId)) {
            childDeviceMap.get(parentId).add(childDeviceId);
        } else {
            childDeviceMap.put(parentId, new HashSet<>(Collections.singleton(childDeviceId)));
        }
    }

    private Map<String, DeviceHandler> deviceHandlerInfo;

    public Collection<DeviceHandler> getAllDeviceHandlers() {
        return getDeviceHandlerInfo().values();
    }

    public DeviceHandler getDeviceHandler(String id) {
        return getDeviceHandlerInfo().get(id);
    }

    @Override
    public DeviceHandler getDeviceHandlerByNamespaceAndName(String namespace, String name) {
        for (DeviceHandler deviceHandler : getDeviceHandlerInfo().values()) {
            if (deviceHandler.getNamespace().equals(namespace) && deviceHandler.getName().equals(name)) {
                return deviceHandler;
            }
        }
        return null;
    }

    @Override
    public DeviceHandler getDeviceHandlerByName(String name) {
        for (DeviceHandler deviceHandler : getDeviceHandlerInfo().values()) {
            if (deviceHandler.getName().equals(name)) {
                return deviceHandler;
            }
        }
        return null;
    }

    private Map<String, DeviceHandler> getDeviceHandlerInfo() {
        if (deviceHandlerInfo == null) {
            deviceHandlerInfo = loadDeviceHandlerConfig();
        }
        return deviceHandlerInfo;
    }

    @Override
    public void updateDeviceHandler(DeviceHandler deviceHandler) {
        getDeviceHandlerInfo().put(deviceHandler.getId(), deviceHandler);
        saveDeviceHandlers();
    }

    @Override
    public void addDeviceHandler(DeviceHandler deviceHandler) {
        getDeviceHandlerInfo().put(deviceHandler.getId(), deviceHandler);
        saveDeviceHandlers();
    }

    public void saveDeviceHandlers() {
        if (deviceHandlerInfo != null && deviceHandlerInfo.size() > 0) {
            try {
                Yaml yaml = new Yaml();
                File deviceHandlerConfig = new File("config/deviceHandlers.yaml");
                FileWriter fileWriter = new FileWriter(deviceHandlerConfig);
                yaml.dump(new ArrayList<>(deviceHandlerInfo.values()), fileWriter);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // load config file on file system instead of processing through the device handler files
    private synchronized Map<String, DeviceHandler> loadDeviceHandlerConfig() {
        Map<String, DeviceHandler> deviceHandlerInfo = new HashMap<>();
        try {
            File deviceHandlersConfigFile = new File("config/deviceHandlers.yaml");
            if (deviceHandlersConfigFile.exists()) {
                Yaml yaml = new Yaml();
                List<DeviceHandler> listObj = yaml.load(new FileInputStream(deviceHandlersConfigFile));
                if (listObj != null) {
                    for (DeviceHandler dh : listObj) {
                        deviceHandlerInfo.put(dh.getId(), dh);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return deviceHandlerInfo;
    }

    @Override
    public String getDeviceHandlerSourceCode(String id) {
        DeviceHandler deviceHandler = getDeviceHandlerInfo().get(id);
        if (deviceHandler != null && deviceHandler.isUserType()) {
            File f = new File(deviceHandler.getFile());
            try {
                String scriptCode = IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8);
                return scriptCode;
            } catch (IOException e) {
                logger.warn("Exception loading device handler source code", e);
            }
        }

        return null;
    }

    @Override
    public Map<String, InputStream> getDeviceHandlerSources() {
        Map<String, InputStream> deviceHandlerSourceList = new HashMap<>();

        // load device handlers from text files on local file system
        try {
            final String dhFilePath = "deviceHandlers/";
            File devicehandlerDir = new File(dhFilePath);
            if (!devicehandlerDir.exists()) {
                devicehandlerDir.mkdir();
            }
            if (devicehandlerDir.exists() && devicehandlerDir.isDirectory()) {
                File[] deviceHandlerFiles = devicehandlerDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && pathname.getName().endsWith(".groovy");
                    }
                });

                if (deviceHandlerFiles != null && deviceHandlerFiles.length > 0) {
                    for (File f : deviceHandlerFiles) {
                        deviceHandlerSourceList.put(dhFilePath + f.getName(), new FileInputStream(f));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deviceHandlerSourceList;
    }

    @Override
    public boolean updateDeviceHandlerSourceCode(String id, String sourceCode) {
        DeviceHandler deviceHandler = getDeviceHandlerInfo().get(id);
        if (deviceHandler != null && deviceHandler.isUserType()) {
            File f = new File(deviceHandler.getFile());
            try {
                IOUtils.write(sourceCode, new FileOutputStream(f), StandardCharsets.UTF_8);
                return true;
            } catch (IOException e) {
                logger.warn("Exception saving device handler source code", e);
            }
        }

        return false;
    }

    @Override
    public String addDeviceHandlerSourceCode(String sourceCode, DeviceHandler deviceHandler) {
        String dhId = UUID.randomUUID().toString();
        String fileName = "deviceHandlers/" + dhId + ".groovy";

        deviceHandler.setId(dhId);
        deviceHandler.setFile(fileName);

        File f = new File(fileName);
        try {
            IOUtils.write(sourceCode, new FileOutputStream(f), StandardCharsets.UTF_8);
            addDeviceHandler(deviceHandler);
            return dhId;
        } catch (IOException e) {
            logger.warn("Exception saving device handler source code", e);
        }
        return null;
    }

}
