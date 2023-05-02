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
package com.parrotha.internal;

import com.parrotha.internal.app.AutomationAppService;
import com.parrotha.internal.device.DeviceIntegrationServiceImpl;
import com.parrotha.internal.device.DeviceService;
import com.parrotha.internal.entity.EntityService;
import com.parrotha.internal.entity.EntityServiceImpl;
import com.parrotha.internal.extension.ExtensionService;
import com.parrotha.internal.hub.EventService;
import com.parrotha.internal.hub.LocationService;
import com.parrotha.internal.hub.LocationServiceImpl;
import com.parrotha.internal.hub.ScheduleService;
import com.parrotha.internal.integration.ConfigurationService;
import com.parrotha.internal.integration.ConfigurationServiceImpl;
import com.parrotha.internal.integration.IntegrationRegistry;
import com.parrotha.internal.integration.IntegrationService;

public class ServiceFactory {
    private static ConfigurationService configurationService;

    public static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = new ConfigurationServiceImpl();
        }
        return configurationService;
    }

    private static LocationService locationService;

    public static LocationService getLocationService() {
        if (locationService == null) {
            locationService = new LocationServiceImpl();
        }
        return locationService;
    }

    private static DeviceService deviceService;

    public static DeviceService getDeviceService() {
        if (deviceService == null) {
            deviceService = new DeviceService(getIntegrationRegistry(), getExtensionService());
        }
        return deviceService;
    }

    private static EventService eventService;

    public static EventService getEventService() {
        if (eventService == null) {
            eventService = new EventService(getLocationService());
        }
        return eventService;
    }

    private static AutomationAppService automationAppService;

    public static AutomationAppService getAutomationAppService() {
        if (automationAppService == null) {
            automationAppService = new AutomationAppService(getExtensionService());
        }
        return automationAppService;
    }

    private static EntityService entityService;

    public static EntityService getEntityService() {
        if (entityService == null) {
            entityService = new EntityServiceImpl(getDeviceService(), getAutomationAppService(), getEventService(),
                    getLocationService(), getScheduleService(), getIntegrationRegistry());
        }
        return entityService;
    }

    private static IntegrationService integrationService;

    public static IntegrationService getIntegrationService() {
        if (integrationService == null) {
            integrationService = new IntegrationService(getIntegrationRegistry(), getConfigurationService(), getExtensionService(),
                    getDeviceIntegrationService(), getDeviceService(), getEntityService(), getLocationService());
        }
        return integrationService;
    }

    private static ExtensionService extensionService;

    public static ExtensionService getExtensionService() {
        if (extensionService == null) {
            extensionService = new ExtensionService();
        }
        return extensionService;
    }

    private static ScheduleService scheduleService;

    public static ScheduleService getScheduleService() {
        if (scheduleService == null) {
            scheduleService = new ScheduleService();
        }
        return scheduleService;
    }

    private static DeviceIntegrationServiceImpl deviceIntegrationService;

    public static DeviceIntegrationServiceImpl getDeviceIntegrationService() {
        if (deviceIntegrationService == null) {
            deviceIntegrationService = new DeviceIntegrationServiceImpl(getDeviceService(), getEntityService());
        }
        return deviceIntegrationService;
    }

    private static IntegrationRegistry integrationRegistry;

    public static IntegrationRegistry getIntegrationRegistry() {
        if (integrationRegistry == null) {
            integrationRegistry = new IntegrationRegistry();
        }
        return integrationRegistry;
    }
}
