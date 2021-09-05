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
package com.parrotha.internal.app;

import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InstalledAutomationApp implements Serializable, Cloneable {
    private String id;
    private String label;
    private String automationAppId;
    private boolean installed = false;
    private ArrayList<InstalledAutomationAppSetting> settings;
    private LinkedHashMap state;
    private String parentInstalledAutomationAppId;

    private transient LinkedHashMap<String, InstalledAutomationAppSetting> nameToSettingMap;
    private transient String name;
    private transient String namespace;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        InstalledAutomationApp cloned = (InstalledAutomationApp) super.clone();
        if (state != null)
            cloned.setState((LinkedHashMap) state.clone());
        if (settings != null)
            cloned.setSettings((ArrayList<InstalledAutomationAppSetting>) settings.clone());
        return cloned;
    }

    /*
    - id: b71b2aa3-7d8a-4d3b-9eae-b1f132a92c7d
  name: "Most Useless Machine"
  automationAppId: 999b825d-d047-405b-93c7-c8af51eaef78
  settings:
    - id: 5d4a2e52-398a-4974-8268-5047b187ac38
      name: switch1
      value: 544fcfaf-d52f-4a0f-86c0-0137d1828702
      type: capability.switch
      multiple: false
     */
    public InstalledAutomationApp() {
    }

    public InstalledAutomationApp(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAutomationAppId() {
        return automationAppId;
    }

    public void setAutomationAppId(String automationAppId) {
        this.automationAppId = automationAppId;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public Map getState() {
        return state;
    }

    public void setState(Map state) {
        this.state = state != null ? new LinkedHashMap(state) : null;
    }

    public String getParentInstalledAutomationAppId() {
        return parentInstalledAutomationAppId;
    }

    public void setParentInstalledAutomationAppId(String parentInstalledAutomationAppId) {
        this.parentInstalledAutomationAppId = parentInstalledAutomationAppId;
    }

    public List<InstalledAutomationAppSetting> getSettings() {
        return settings;
    }

    public void setSettings(List<InstalledAutomationAppSetting> settings) {
        if(settings != null) {
            this.settings = new ArrayList<>(settings);
        }
    }

    public void addSetting(InstalledAutomationAppSetting setting) {
        if (this.settings == null) {
            this.settings = new ArrayList<>();
        }
        if (nameToSettingMap == null) {
            this.nameToSettingMap = new LinkedHashMap<>();
        }
        this.settings.add(setting);
        this.nameToSettingMap.put(setting.getName(), setting);
    }

    @Transient
    public String getDisplayName() {
        if (getLabel() == null)
            return getName();
        return getLabel();
    }

    @Transient
    public InstalledAutomationAppSetting getSettingByName(String name) {
        if (getNameToSettingMap() != null) {
            return getNameToSettingMap().get(name);
        } else {
            return null;
        }
    }

    @Transient
    public synchronized LinkedHashMap<String, InstalledAutomationAppSetting> getNameToSettingMap() {
        if (nameToSettingMap == null && settings != null) {
            LinkedHashMap<String, InstalledAutomationAppSetting> newNameToSettingMap = new LinkedHashMap<>();
            for (InstalledAutomationAppSetting setting : this.settings) {
                newNameToSettingMap.put(setting.getName(), setting);
            }
            nameToSettingMap = newNameToSettingMap;
        } else if (settings == null) {
            nameToSettingMap = new LinkedHashMap<>();
        }
        return nameToSettingMap;
    }

    @Transient
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Transient
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return "InstalledAutomationApp(" +
                "id: '" + id + '\'' +
                ", label: '" + label + '\'' +
                ", automationAppId: '" + automationAppId + '\'' +
                ", settings: " + settings +
                ')';
    }
}
