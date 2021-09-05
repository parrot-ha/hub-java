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
package com.parrotha.internal.integration;

import com.parrotha.device.Protocol;
import com.parrotha.integration.CloudIntegration;
import com.parrotha.integration.DeviceIntegration;
import com.parrotha.internal.Main;
import com.parrotha.internal.device.DeviceIntegrationServiceImpl;
import com.parrotha.internal.device.DeviceService;
import com.parrotha.internal.entity.CloudIntegrationServiceImpl;
import com.parrotha.internal.hub.LocationService;
import com.parrotha.internal.entity.EntityService;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IntegrationService {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationService.class);
    IntegrationRegistry integrationRegistry;
    DeviceService deviceService;
    EntityService scriptService;
    ConfigurationService configurationService;
    DeviceIntegrationServiceImpl deviceIntegrationService;
    EntityService entityService;
    LocationService locationService;
    private Map<String, AbstractIntegration> integrationMap;
    private Map<Protocol, List<String>> protocolListMap = null;

    public IntegrationService(IntegrationRegistry integrationRegistry, ConfigurationService configurationService, DeviceService deviceService,
                              EntityService scriptService, DeviceIntegrationServiceImpl deviceIntegrationService,
                              EntityService entityService, LocationService locationService) {
        this.integrationRegistry = integrationRegistry;
        this.configurationService = configurationService;
        this.deviceService = deviceService;
        this.scriptService = scriptService;
        this.deviceIntegrationService = deviceIntegrationService;
        this.entityService = entityService;
        this.locationService = locationService;
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
            Object value = settingsMap.get(key);
            if ("label".equals(key)) {
                integrationConfiguration.setLabel((String) value);
            } else {
                Object existingSetting = integrationConfiguration.getSettings().get(key);
                if (existingSetting != null) {
                    // update existing setting
                    if (!existingSetting.toString().equals(value.toString())) {
                        integrationConfiguration.addSetting(key, value);
                        // add to list of changed fields
                        changedKeys.add(key);
                    }
                } else {
                    // create new setting
                    integrationConfiguration.addSetting(key, value);
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
                    try {
                        abstractIntegration.setConfigurationService(integrationConfigurationService);
                        initializeIntegration(abstractIntegration);
                        integrationRegistry.registerIntegration(abstractIntegration);
                        abstractIntegration.setId(integrationId);
                        abstractIntegration.start();
                    } catch (Exception e) {
                        logger.warn("Exception while starting integration", e);
                    }
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

        Collection<IntegrationConfiguration> integrationConfigurations = getIntegrations();
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

    private AbstractIntegration getAbstractIntegrationFromConfiguration(
            IntegrationConfiguration integrationConfiguration) {
        AbstractIntegration abstractIntegration = null;
        try {
            Class<? extends AbstractIntegration> integrationClass = Class
                    .forName(integrationConfiguration.getClassName()).asSubclass(AbstractIntegration.class);
            abstractIntegration = integrationClass.getDeclaredConstructor().newInstance();
            abstractIntegration.setId(integrationConfiguration.getId());
            //abstractIntegration.setLabel(integrationConfiguration.getLabel());
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        } catch (InstantiationException instantiationException) {
            instantiationException.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return abstractIntegration;
    }

    public Collection<IntegrationConfiguration> getIntegrations() {
        return configurationService.getIntegrations();
    }

    public String createIntegration(String integrationClassName) {
        //TODO: validate class name is in a list of allowable classes
        try {
            Class<? extends AbstractIntegration> integrationClass = Class.forName(integrationClassName)
                    .asSubclass(AbstractIntegration.class);
            AbstractIntegration integration = integrationClass.getDeclaredConstructor().newInstance();
            String integrationId;
            if (integration instanceof DeviceIntegration) {
                integrationId = configurationService
                        .addIntegration(((DeviceIntegration) integration).getProtocol(), integrationClassName,
                                integration.getDefaultSettings());
            } else {
                integrationId = configurationService
                        .addIntegration(null, integrationClassName, integration.getDefaultSettings());
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
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        } catch (InstantiationException instantiationException) {
            instantiationException.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AbstractIntegration getIntegrationById(String id) {
        return getIntegrationMap().get(id);
    }

    public List<Map<String, String>> getAvailableIntegrations() {
        List<String> integrationClasses = Stream.of(
                "com.parrotha.integration.zigbee.ZigBeeIntegration",
                "com.parrotha.integration.lan.LanIntegration").collect(Collectors.toList());

        try {
            Enumeration<URL> resources = Main.class.getClassLoader().getResources("integrationInformation.yaml");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Yaml yaml = new Yaml();
                Map integrationInformation = yaml.load(url.openStream());
                String className = (String) integrationInformation.get("className");
                integrationClasses.add(className);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Map<String, String>> availableIntegrations = new ArrayList<>();
        for (String integrationClassName : integrationClasses) {
            Map<String, String> integration = new HashMap<>();
            try {
                Class<? extends AbstractIntegration> integrationClass = Class.forName(integrationClassName)
                        .asSubclass(AbstractIntegration.class);
                AbstractIntegration abstractIntegration = integrationClass.getDeclaredConstructor().newInstance();
                integration.put("name", abstractIntegration.getName());
                integration.put("className", integrationClassName);
                integration.put("description", abstractIntegration.getDescription());
                availableIntegrations.add(integration);
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            } catch (IllegalAccessException illegalAccessException) {
                illegalAccessException.printStackTrace();
            } catch (InstantiationException instantiationException) {
                instantiationException.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return availableIntegrations;
    }
}
