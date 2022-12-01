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
package com.parrotha.internal.integration;

import com.parrotha.device.Protocol;
import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IntegrationConfiguration {
    private String id;
    private String integrationTypeId;
    private String label;
    private String className;
    private Protocol protocol;

    private ArrayList<IntegrationSetting> settings;

    private transient String name;
    private transient String description;
    private transient LinkedHashMap<String, IntegrationSetting> nameToSettingMap;


    @Transient
    public synchronized LinkedHashMap<String, IntegrationSetting> getNameToSettingMap() {
        if (nameToSettingMap == null && settings != null) {
            LinkedHashMap<String, IntegrationSetting> newNameToSettingMap = new LinkedHashMap<>();
            for (IntegrationSetting setting : this.settings) {
                newNameToSettingMap.put(setting.getName(), setting);
            }
            nameToSettingMap = newNameToSettingMap;
        } else if (settings == null) {
            nameToSettingMap = new LinkedHashMap<>();
        }
        return nameToSettingMap;
    }

    @Transient
    public IntegrationSetting getSettingByName(String name) {
        if (getNameToSettingMap() != null) {
            return getNameToSettingMap().get(name);
        } else {
            return null;
        }
    }

    @Transient
    public Map<String, Object> getDisplayValues() {
        Map<String, Object> integrationMap = new HashMap<>();
        integrationMap.put("id", getId());
        integrationMap.put("name", getName());
        integrationMap.put("label", getLabel() != null ? getLabel() : getName());
        if (getSettings() != null) {
            Map<String, String> settingsMap = new HashMap<>();
            for (IntegrationSetting integrationSetting : getSettings()) {
                if (!"password".equals(integrationSetting.getType())) {
                    settingsMap.put(integrationSetting.getName(), integrationSetting.getValue());
                } else {
                    settingsMap.put(integrationSetting.getName(), "********");
                }
            }
            integrationMap.put("settings", settingsMap);
        }
        return integrationMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIntegrationTypeId() {
        return integrationTypeId;
    }

    public void setIntegrationTypeId(String integrationTypeId) {
        this.integrationTypeId = integrationTypeId;
    }

    public String getLabel() {
        return label;
    }

    public String getDisplayName() {
        return StringUtils.isNotBlank(label) ? label : name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public List<IntegrationSetting> getSettings() {
        return settings;
    }

    public void setSettings(List<IntegrationSetting> settings) {
        if (settings != null) {
            this.settings = new ArrayList<>(settings);
        }
    }

    public void addSetting(IntegrationSetting setting) {
        if (this.settings == null) {
            this.settings = new ArrayList<>();
        }
        if (nameToSettingMap == null) {
            this.nameToSettingMap = new LinkedHashMap<>();
        }
        this.settings.add(setting);
        this.nameToSettingMap.put(setting.getName(), setting);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
