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
package com.parrotha.internal.integration;

import com.parrotha.service.IntegrationConfigurationService;

import java.util.List;

public class IntegrationConfigurationServiceImpl implements IntegrationConfigurationService {
    ConfigurationService configurationService;

    @Override
    public String getLabel(String integrationId) {
        return this.configurationService.getIntegrationById(integrationId).getLabel();
    }

    public IntegrationConfigurationServiceImpl(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public List<IntegrationSetting> getConfiguration(String integrationId) {
        return configurationService.getIntegrationConfiguration(integrationId);
    }

    @Override
    public String getConfigurationValue(String integrationId, String configurationKey) {
        return configurationService.getIntegrationConfigurationValue(integrationId, configurationKey);
    }

    @Override
    public void updateConfigurationValue(String integrationId, String configurationKey, Object configurationValue, String type, boolean multiple) {
        configurationService.updateIntegrationConfigurationValue(integrationId, configurationKey, configurationValue, type, multiple);
    }
}
