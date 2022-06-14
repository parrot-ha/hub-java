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
import com.parrotha.integration.CloudIntegration;
import com.parrotha.integration.DeviceIntegration;
import com.parrotha.internal.Main;
import com.parrotha.internal.entity.CloudIntegrationServiceImpl;
import com.parrotha.internal.entity.EntityService;
import com.parrotha.internal.extension.ExtensionService;
import com.parrotha.internal.extension.ExtensionState;
import com.parrotha.internal.extension.ExtensionStateListener;
import com.parrotha.internal.hub.LocationService;
import com.parrotha.service.DeviceIntegrationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class IntegrationService implements ExtensionStateListener {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationService.class);
    IntegrationRegistry integrationRegistry;
    ExtensionService extensionService;
    ConfigurationService configurationService;
    DeviceIntegrationService deviceIntegrationService;
    EntityService entityService;
    LocationService locationService;
    private Map<String, AbstractIntegration> integrationMap;
    private Map<Protocol, List<String>> protocolListMap = null;

    private Map<String, Map<String, Object>> integrationTypeMap;

    public IntegrationService(IntegrationRegistry integrationRegistry, ConfigurationService configurationService,
                              ExtensionService extensionService, DeviceIntegrationService deviceIntegrationService,
                              EntityService entityService, LocationService locationService) {
        this.integrationRegistry = integrationRegistry;
        this.configurationService = configurationService;
        this.extensionService = extensionService;
        this.deviceIntegrationService = deviceIntegrationService;
        this.entityService = entityService;
        this.locationService = locationService;

        loadIntegrationTypes();
    }

    private void loadIntegrationTypes() {
        Map<String, Map<String, Object>> integrations = new HashMap<>();

        // load integrations built in
        for (Map<String, Object> sysIntegration : getIntegrationsFromClassloader(Main.class.getClassLoader(), "SYSTEM")) {
            integrations.put((String) sysIntegration.get("id"), sysIntegration);
        }

        // load integrations from extensions
        Map<String, ClassLoader> extensionClassloaders = extensionService.getExtensionClassloaders();
        for (String extensionId : extensionClassloaders.keySet()) {
            List<Map<String, Object>> extIntegrations = getIntegrationsFromClassloader(extensionClassloaders.get(extensionId), "EXTENSION");
            for (Map<String, Object> extIntegration : extIntegrations) {
                extIntegration.put("extensionId", extensionId);
                integrations.put((String) extIntegration.get("id"), extIntegration);
            }
        }

        integrationTypeMap = integrations;
    }

    private List<Map<String, Object>> getIntegrationsFromClassloader(ClassLoader classLoader, String type) {
        List<Map<String, Object>> availableIntegrations = new ArrayList<>();

        try {
            Enumeration<URL> resources = classLoader.getResources("parrotIntegration.yaml");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Yaml yaml = new Yaml();
                Map integrationInformation = yaml.load(url.openStream());
                String className = (String) integrationInformation.get("className");
                String id = (String) integrationInformation.get("id");

                Map<String, Object> integration = new HashMap<>();
                try {
                    Class<? extends AbstractIntegration> integrationClass = Class.forName(className, true, classLoader)
                            .asSubclass(AbstractIntegration.class);
                    AbstractIntegration abstractIntegration = integrationClass.getDeclaredConstructor().newInstance();
                    integration.put("id", id);
                    integration.put("type", type);
                    integration.put("name", abstractIntegration.getName());
                    integration.put("className", className);
                    integration.put("description", abstractIntegration.getDescription());
                    integration.put("classLoader", classLoader);
                    availableIntegrations.add(integration);
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException exception) {
                    logger.warn("Exception occurred while processing integration {} with className {}", id, className, exception);
                }
            }
        } catch (IOException e) {
            logger.warn("Exception occurred while processing classLoader {} with type {}", classLoader.toString(), type, e);
        }
        return availableIntegrations;
    }

    public boolean removeIntegration(String id) {
        AbstractIntegration integration = getIntegrationById(id);
        if (integration != null) {
            integration.stop();
        }
        boolean integrationConfigurationRemoved = configurationService.removeIntegration(id);

        if (integrationConfigurationRemoved) {
            getIntegrationMap().remove(id);

            // remove integration from any protocol lists
            if (protocolListMap != null) {
                for (List<String> protocolList : protocolListMap.values()) {
                    if (protocolList != null) {
                        protocolList.remove(id);
                    }
                }
            }

            // remove integration from registry
            integrationRegistry.unregisterIntegration(integration);

            return true;
        }

        return false;
    }

    public void updateIntegrationSettings(String id, Map<String, Object> settingsMap) {
        IntegrationConfiguration integrationConfiguration = configurationService.getIntegrationById(id);

        List<String> changedKeys = new ArrayList<>();
        for (String key : settingsMap.keySet()) {
            Map setting = (Map) settingsMap.get(key);


            if ("label".equals(key)) {
                integrationConfiguration.setLabel((String) setting.get("value"));
            } else {
                IntegrationSetting existingSetting = integrationConfiguration.getSettingByName(key);
                if (existingSetting != null) {
                    Object value = setting.get("value");
                    // update existing setting
                    // TODO: create method on IntegrationSetting to check for changes.
                    if ((existingSetting.getValue() == null && value != null) || (existingSetting.getValue() != null && value == null) ||
                            existingSetting.getValue() != null && value != null && !existingSetting.getValue().equals(value.toString())) {
                        existingSetting.processValueTypeAndMultiple(setting.get("value"), (String) setting.get("type"),
                                (Boolean) setting.get("multiple"));

                        // add to list of changed fields
                        changedKeys.add(key);
                    }
                } else {
                    // create new setting
                    existingSetting = new IntegrationSetting();
                    existingSetting.setId(UUID.randomUUID().toString());
                    existingSetting.setName(key);
                    existingSetting.processValueTypeAndMultiple(setting.get("value"), (String) setting.get("type"),
                            (Boolean) setting.get("multiple"));
                    integrationConfiguration.addSetting(existingSetting);

                    // add to list of changed fields
                    changedKeys.add(key);
                }
            }
        }
        configurationService.updateIntegration(integrationConfiguration);

        // send list of changed keys to the integration
        if (changedKeys.size() > 0) {
            getIntegrationById(id).settingValueChanged(changedKeys);
        }
    }

    public void start() {
        configurationService.initialize();

        Map<String, AbstractIntegration> integrationMap = getIntegrationMap();
        if (integrationMap != null) {
            IntegrationConfigurationServiceImpl integrationConfigurationService = new IntegrationConfigurationServiceImpl(
                    configurationService);
            for (String integrationId : integrationMap.keySet()) {
                AbstractIntegration abstractIntegration = integrationMap.get(integrationId);
                if (abstractIntegration != null) {
                    new Thread(() -> {
                        try {
                            abstractIntegration.setConfigurationService(integrationConfigurationService);
                            initializeIntegration(abstractIntegration);
                            integrationRegistry.registerIntegration(abstractIntegration);
                            abstractIntegration.setId(integrationId);
                            abstractIntegration.start();
                        } catch (Exception e) {
                            logger.warn("Exception while starting integration", e);
                        }
                    }).start();
                }
            }
        }
    }

    private void initializeIntegration(AbstractIntegration abstractIntegration) {
        if (abstractIntegration instanceof DeviceIntegration) {
            ((DeviceIntegration) abstractIntegration)
                    .setDeviceIntegrationService(deviceIntegrationService);
        }
        if (abstractIntegration instanceof CloudIntegration) {
            ((CloudIntegration) abstractIntegration).setCloudIntegrationService(
                    new CloudIntegrationServiceImpl(entityService, locationService));
        }
    }

    public void stop() {
        if (integrationMap != null) {
            for (AbstractIntegration abstractIntegration : integrationMap.values()) {
                abstractIntegration.stop();
            }
        }
    }

    private Map<String, AbstractIntegration> getIntegrationMap() {
        if (integrationMap == null) {
            loadIntegrationMap();
        }
        return integrationMap;
    }

    private synchronized void loadIntegrationMap() {
        if (integrationMap != null) {
            return;
        }

        Map<String, AbstractIntegration> temporaryIntegrationMap = new HashMap<>();
        Map<Protocol, List<String>> temporaryProtocolListMap = new HashMap<>();

        Collection<IntegrationConfiguration> integrationConfigurations = configurationService.getIntegrations();
        if (integrationConfigurations != null) {
            for (IntegrationConfiguration integrationConfiguration : integrationConfigurations) {
                AbstractIntegration abstractIntegration = getAbstractIntegrationFromConfiguration(
                        integrationConfiguration);
                if (abstractIntegration != null) {
                    temporaryIntegrationMap.put(integrationConfiguration.getId(), abstractIntegration);
                    temporaryProtocolListMap
                            .computeIfAbsent(integrationConfiguration.getProtocol(),
                                    k -> new ArrayList<>()).add(integrationConfiguration.getId());
                }
            }
        }
        integrationMap = temporaryIntegrationMap;
        protocolListMap = temporaryProtocolListMap;
    }

    private ClassLoader getClassLoaderForIntegration(String integrationTypeId) {
        ClassLoader cl = null;
        Map<String, Object> integrationInfo = integrationTypeMap.get(integrationTypeId);
        if (integrationInfo != null) {
            cl = (ClassLoader) integrationInfo.get("classLoader");
        }

        if (cl == null) {
            cl = Main.class.getClassLoader();
        }
        return cl;
    }

    private AbstractIntegration getIntegrationClass(String integrationTypeId) {
        AbstractIntegration abstractIntegration = null;
        ClassLoader integrationClassLoader = getClassLoaderForIntegration(integrationTypeId);
        if (integrationClassLoader != null) {
            Class<? extends AbstractIntegration> integrationClass = null;
            try {
                integrationClass = Class
                        .forName((String) integrationTypeMap.get(integrationTypeId).get("className"), true, integrationClassLoader)
                        .asSubclass(AbstractIntegration.class);
                abstractIntegration = integrationClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.warn("Exception occurred while loading integration id {}", integrationTypeId);
                e.printStackTrace();
            }
        }
        return abstractIntegration;
    }

    private AbstractIntegration getAbstractIntegrationFromConfiguration(IntegrationConfiguration integrationConfiguration) {
        AbstractIntegration abstractIntegration = null;
        if (StringUtils.isNotBlank(integrationConfiguration.getIntegrationTypeId())) {
            abstractIntegration = getIntegrationClass(integrationConfiguration.getIntegrationTypeId());
        } else {
            Optional<Map<String, Object>> integrationInfo = integrationTypeMap.values().stream()
                    .filter(m -> integrationConfiguration.getClassName().equals(m.get("className"))).findFirst();
            if (integrationInfo.isPresent()) {
                abstractIntegration = getIntegrationClass((String) integrationInfo.get().get("id"));
            }
        }

        if (abstractIntegration != null) {
            abstractIntegration.setId(integrationConfiguration.getId());
        }

        return abstractIntegration;
    }

    public Collection<IntegrationConfiguration> getIntegrations() {
        Collection<IntegrationConfiguration> integrations = configurationService.getIntegrations();
        for (IntegrationConfiguration integrationConfiguration : integrations) {
            AbstractIntegration abstractIntegration = getIntegrationById(integrationConfiguration.getId());
            if (abstractIntegration != null) {
                integrationConfiguration.setName(abstractIntegration.getName());
            } else {
                integrationConfiguration.setName("UNKNOWN");
            }
        }
        return integrations;
    }

    public String createIntegration(String integrationTypeId) {

        AbstractIntegration integration = getIntegrationClass(integrationTypeId);
        String integrationId;
        if (integration instanceof DeviceIntegration) {
            integrationId = configurationService
                    .addIntegration(((DeviceIntegration) integration).getProtocol(), integrationTypeId,
                            integration.getDefaultSettings());
        } else {
            integrationId = configurationService
                    .addIntegration(null, integrationTypeId, integration.getDefaultSettings());
        }

        IntegrationConfiguration integrationConfiguration = configurationService.getIntegrationById(integrationId);
        AbstractIntegration abstractIntegration = getAbstractIntegrationFromConfiguration(integrationConfiguration);
        if (integrationMap == null) {
            integrationMap = new HashMap<>();
        }
        integrationMap.put(integrationConfiguration.getId(), abstractIntegration);

        if (protocolListMap == null) {
            protocolListMap = new HashMap<>();
        }
        protocolListMap
                .computeIfAbsent(integrationConfiguration.getProtocol(),
                        k -> new ArrayList<>()).add(integrationConfiguration.getId());

        abstractIntegration.setConfigurationService(new IntegrationConfigurationServiceImpl(configurationService));

        abstractIntegration.setId(integrationId);
        abstractIntegration.start();

        initializeIntegration(abstractIntegration);
        integrationRegistry.registerIntegration(abstractIntegration);

        return integrationId;
    }

    public AbstractIntegration getIntegrationById(String id) {
        return getIntegrationMap().get(id);
    }


    public List<Map<String, String>> getIntegrationTypes() {

        List<Map<String, String>> availableIntegrations = new ArrayList<>();
        for (Map<String, Object> integrationType : integrationTypeMap.values()) {
            Map<String, String> integration = new HashMap<>();
            integration.put("id", (String) integrationType.get("id"));
            integration.put("name", (String) integrationType.get("name"));
            integration.put("description", (String) integrationType.get("description"));
            availableIntegrations.add(integration);
        }
        return availableIntegrations;
    }

    @Override
    public void stateUpdated(ExtensionState state) {
        String extensionId = state.getId();
        if (ExtensionState.StateType.INSTALLED.equals(state.getState()) || ExtensionState.StateType.UPDATED.equals(state.getState())) {
            // TODO: should we be checking existing integrations and loading or unloading them as we go?
            //  also should we check for an integration disappearing in a new version of the extension.
            loadIntegrationTypes();
        } else if (ExtensionState.StateType.DELETED.equals(state.getState())) {
            if (isExtensionInUse(state.getId()).getLeft()) {
                throw new RuntimeException("Integration still in use");
            }
            //remove old integration
            integrationTypeMap.entrySet()
                    .removeIf(entry -> "EXTENSION".equals(entry.getValue().get("type")) && extensionId.equals(entry.getValue().get("extensionId")));
        }
    }

    @Override
    public Pair<Boolean, String> isExtensionInUse(String extensionId) {
        StringBuilder sb = new StringBuilder();
        boolean inUse = false;
        for (IntegrationConfiguration integrationConfiguration : getIntegrations()) {
            Map<String, Object> integrationType = integrationTypeMap.get(integrationConfiguration.getIntegrationTypeId());
            if (extensionId.equals(integrationType.get("id"))) {
                inUse = true;
                sb.append("Integration ").append(integrationConfiguration.getDisplayName()).append("\n");
            }
        }

        return new ImmutablePair<>(inUse, sb.toString());
    }
}
