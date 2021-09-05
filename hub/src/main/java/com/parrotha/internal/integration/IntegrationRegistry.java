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

import com.parrotha.device.HubAction;
import com.parrotha.device.HubResponse;
import com.parrotha.device.Protocol;
import com.parrotha.integration.CloudIntegration;
import com.parrotha.integration.DeviceIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegrationRegistry {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationRegistry.class);

    private Map<AbstractIntegration.IntegrationType, List<AbstractIntegration>> integrationRegistry = new HashMap<>();
    private Map<String, AbstractIntegration> integrationIdMap = new HashMap<>();

    public void registerIntegration(AbstractIntegration integration) {
        integrationIdMap.put(integration.getId(), integration);

        if (integration instanceof DeviceIntegration) {
            if (((DeviceIntegration) integration).getProtocol() == Protocol.ZIGBEE) {
                registerIntegration(AbstractIntegration.IntegrationType.ZIGBEE, integration);
            } else if (((DeviceIntegration) integration).getProtocol() == Protocol.ZWAVE) {
                registerIntegration(AbstractIntegration.IntegrationType.ZWAVE, integration);
            } else if (((DeviceIntegration) integration).getProtocol() == Protocol.LAN) {
                registerIntegration(AbstractIntegration.IntegrationType.LAN, integration);
            } else {
                registerIntegration(AbstractIntegration.IntegrationType.DEVICE, integration);
            }
        }
        if (integration instanceof CloudIntegration) {
            registerIntegration(AbstractIntegration.IntegrationType.CLOUD, integration);
        }
    }

    private void registerIntegration(AbstractIntegration.IntegrationType integrationType, AbstractIntegration integration) {
        if (!integrationRegistry.containsKey(integrationType)) {
            integrationRegistry.put(integrationType, new ArrayList<>());
        }
        integrationRegistry.get(integrationType).add(integration);
    }

    public void unregisterIntegration(AbstractIntegration integration) {
        integrationIdMap.remove(integration.getId());

        if (integration instanceof DeviceIntegration) {
            if (((DeviceIntegration) integration).getProtocol() == Protocol.ZIGBEE) {
                unregisterIntegrationType(AbstractIntegration.IntegrationType.ZIGBEE, integration);
            } else if (((DeviceIntegration) integration).getProtocol() == Protocol.ZWAVE) {
                unregisterIntegrationType(AbstractIntegration.IntegrationType.ZWAVE, integration);
            } else if (((DeviceIntegration) integration).getProtocol() == Protocol.LAN) {
                unregisterIntegrationType(AbstractIntegration.IntegrationType.LAN, integration);
            } else {
                unregisterIntegrationType(AbstractIntegration.IntegrationType.DEVICE, integration);
            }
        }
        if (integration instanceof CloudIntegration) {
            unregisterIntegrationType(AbstractIntegration.IntegrationType.CLOUD, integration);
        }
    }

    private void unregisterIntegrationType(AbstractIntegration.IntegrationType integrationType, AbstractIntegration integration) {
        List<AbstractIntegration> integrations = integrationRegistry.get(integrationType);
        if (integrations != null) {
            integrations.remove(integration);
        }
    }

    public AbstractIntegration getIntegration(AbstractIntegration.IntegrationType integrationType) {
        List<AbstractIntegration> integrations = getIntegrations(integrationType);
        return (integrations != null && integrations.size() > 0) ? integrations.get(0) : null;
    }

    public List<AbstractIntegration> getIntegrations(AbstractIntegration.IntegrationType integrationType) {
        return integrationRegistry.get(integrationType);
    }

    public AbstractIntegration getIntegrationById(String id) {
        return integrationIdMap.get(id);
    }

    public DeviceIntegration getDeviceIntegrationById(String id) {
        AbstractIntegration abstractIntegration = getIntegrationById(id);
        if (abstractIntegration instanceof DeviceIntegration) {
            return (DeviceIntegration) abstractIntegration;
        }
        return null;
    }

    private List<AbstractIntegration> getIntegrationsByProtocol(Protocol protocol) {
        switch (protocol) {
            case ZIGBEE:
                return getIntegrations(AbstractIntegration.IntegrationType.ZIGBEE);
            case LAN:
                return getIntegrations(AbstractIntegration.IntegrationType.LAN);
            case ZWAVE:
                return getIntegrations(AbstractIntegration.IntegrationType.ZWAVE);
        }
        return null;
    }


    public HubResponse processAction(String integrationId, HubAction hubAction) {
        if (integrationId != null) {
            DeviceIntegration deviceIntegration = getDeviceIntegrationById(integrationId);
            if (deviceIntegration != null) {
                return deviceIntegration.processAction(hubAction);
            }
        } else if (hubAction.getProtocol() != null) {
            List<AbstractIntegration> integrations = getIntegrationsByProtocol(hubAction.getProtocol());
            // send message to all integrations that can handle it (usually only 1)
            if (integrations != null) {
                for (AbstractIntegration integration : integrations) {
                    if (integration instanceof DeviceIntegration) {
                        //TODO: how to handle multiple integrations? right now, just use first one.
                        return ((DeviceIntegration) integration).processAction(hubAction);
                    }
                }
            }
        }
        return null;
    }

    public boolean removeDevice(String integrationId, String deviceNetworkId) {
        DeviceIntegration deviceIntegration = getDeviceIntegrationById(integrationId);
        if (deviceIntegration != null) {
            return deviceIntegration.removeIntegrationDevice(deviceNetworkId);
        } else {
            logger.warn("Unknown integration: " + integrationId);
        }
        return false;
    }


}
