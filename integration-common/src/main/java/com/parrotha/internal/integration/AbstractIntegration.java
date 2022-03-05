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

import com.parrotha.service.IntegrationConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractIntegration {
    public enum IntegrationType {
        CLOUD,
        DEVICE,
        LAN,
        ZIGBEE,
        ZWAVE
    }

    private String id = null;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        String label = configurationService.getLabel(id);
        return label != null ? label : getName();
    }

    public abstract void start();

    public abstract void stop();

    public abstract String getName();

    public abstract String getDescription();

    public abstract Map<String, String> getDisplayInformation();

    // override this method if you want to provide a default configuration
    public List<IntegrationSetting> getDefaultSettings() {
        return new ArrayList<>();
    }

    // override this method if integration provides options to configure
    // It should match what comes from a device preferences
    public Map<String, Object> getPreferencesLayout() {
        return new HashMap<>();
    }

    // override this if you want to be informed of configuration changes
    public void settingValueChanged(List<String> keys) {
    }

    // override this method if you want to provide a custom layout for the integration
    public List<Map<String, Object>> getPageLayout() {
        return new ArrayList<>();
    }

    // override this method if you want to provide data for the custom layout for the integration
    public Map<String, Object> getPageData() {
        return new HashMap<>();
    }


    private IntegrationConfigurationService configurationService;

    public void setConfigurationService(IntegrationConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public String getSettingAsString(String key) {
        return configurationService.getConfigurationValue(id, key);
    }

    public String getSettingAsString(String key, String defaultValue) {
        String value = configurationService.getConfigurationValue(id, key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return defaultValue;
    }

    public void updateSetting(String key, String value, String type, boolean multiple) {
        configurationService.updateConfigurationValue(id, key, value, type, multiple);
    }

    @Deprecated
    public void updateSetting(String key, String value) {
        updateSetting(key, value, "text", false);
    }

    public Integer getSettingAsInteger(String key) {
        String settingObj = getSettingAsString(key);
        Integer settingInteger = null;
        if (settingObj instanceof String && NumberUtils.isCreatable(settingObj)) {
            settingInteger = Integer.parseInt(settingObj);
        }

        return settingInteger;
    }

    public Integer getSettingAsInteger(String key, int defaultValue) {
        String settingObj = getSettingAsString(key);
        Integer settingInteger = null;
        if (settingObj instanceof String && NumberUtils.isCreatable(settingObj)) {
            settingInteger = Integer.parseInt(settingObj);
        }

        if (settingInteger == null) {
            return defaultValue;
        }

        return settingInteger;
    }

    public List<IntegrationSetting> getSettings() {
        return configurationService.getConfiguration(id);
    }
}
