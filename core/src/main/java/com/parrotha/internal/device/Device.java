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

import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Device implements Serializable {
    private String id;
    private String deviceHandlerId;
    private String name;
    private String label;
    private String modelName;
    private String manufacturerName;
    private String deviceNetworkId;
    private String parentDeviceId;
    private String parentInstalledAutomationAppId;
    private Integration integration;
    private Map state;
    private Map data;
    private Map<String, State> currentStates;
    private List<DeviceSetting> settings;
    private Date created = new Date();
    private Date updated = new Date();

    private transient Map<String, DeviceSetting> nameToSettingMap;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceHandlerId() {
        return deviceHandlerId;
    }

    public void setDeviceHandlerId(String deviceHandlerId) {
        this.deviceHandlerId = deviceHandlerId;
    }

    public String getDeviceNetworkId() {
        return deviceNetworkId;
    }

    public void setDeviceNetworkId(String deviceNetworkId) {
        this.deviceNetworkId = deviceNetworkId;
    }

    public String getParentDeviceId() {
        return parentDeviceId;
    }

    public void setParentDeviceId(String parentDeviceId) {
        this.parentDeviceId = parentDeviceId;
    }

    public String getParentInstalledAutomationAppId() {
        return parentInstalledAutomationAppId;
    }

    public void setParentInstalledAutomationAppId(String parentInstalledAutomationAppId) {
        this.parentInstalledAutomationAppId = parentInstalledAutomationAppId;
    }

    public void setZigbeeId(String zigbeeId) {
        if (integration == null) {
            integration = new Integration();
        }
        integration.setOption("zigbeeId", zigbeeId);
    }

    public void setEndpointId(Integer endpointId) {
        if (integration == null) {
            integration = new Integration();
        }
        integration.setOption("endpointId", endpointId.toString());
    }

    @Transient
    public Map getState() {
        return state;
    }

    public void setState(Map state) {
        this.state = state != null ? new LinkedHashMap(state) : null;
    }

    public Map getData() {
        if (data == null) {
            data = new HashMap();
        }
        return data;
    }

    @Transient
    public Object getDataValue(String key) {
        return getData().get(key);
    }

    public void setData(Map data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Transient
    public String getDisplayName() {
        if (getLabel() == null) {
            return getName();
        }
        return getLabel();
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public Integration getIntegration() {
        if (integration == null) {
            integration = new Integration();
        }
        return integration;
    }

    public void setIntegration(Integration integration) {
        this.integration = integration;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Map<String, State> getCurrentStates() {
        return currentStates;
    }

    public void setCurrentStates(Map<String, State> currentStates) {
        this.currentStates = currentStates;
    }

    public State currentState(String attributeName) {
        if (currentStates == null) {
            return null;
        }
        return currentStates.get(attributeName);
    }

    public void setCurrentState(State state) {
        if (currentStates == null) {
            currentStates = new HashMap<>();
        }
        currentStates.put(state.getName(), state);
    }

    public List<DeviceSetting> getSettings() {
        return settings;
    }

    @Transient
    public Map<String, Object> getSettingsMap() {
        Map<String, Object> settingsMap = new HashMap<>();
        if (settings != null) {
            for (DeviceSetting setting : settings) {
                settingsMap.put(setting.getName(), setting.getValueAsType());
            }
        }
        return settingsMap;
    }

    public void setSettings(List<DeviceSetting> settings) {
        this.settings = settings;
    }

    public void addSetting(DeviceSetting setting) {
        if (this.settings == null) {
            this.settings = new ArrayList<>();
        }
        if (nameToSettingMap == null) {
            this.nameToSettingMap = new HashMap<>();
        }
        DeviceSetting deviceSetting = getSettingByName(setting.getName());
        if (deviceSetting == null) {
            this.settings.add(setting);
            this.nameToSettingMap.put(setting.getName(), setting);
        } else {
            deviceSetting.setMultiple(setting.isMultiple());
            deviceSetting.setType(setting.getType());
            deviceSetting.setValue(setting.getValue());
        }
    }

    @Transient
    public DeviceSetting getSettingByName(String name) {
        if (getNameToSettingMap() != null) {
            return getNameToSettingMap().get(name);
        } else {
            return null;
        }
    }

    @Transient
    public synchronized Map<String, DeviceSetting> getNameToSettingMap() {
        if (nameToSettingMap == null && settings != null) {
            Map<String, DeviceSetting> newNameToSettingMap = new HashMap<>();
            for (DeviceSetting setting : this.settings) {
                newNameToSettingMap.put(setting.getName(), setting);
            }
            nameToSettingMap = newNameToSettingMap;
        }
        return nameToSettingMap;
    }
}
