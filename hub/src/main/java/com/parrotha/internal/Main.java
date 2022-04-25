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
package com.parrotha.internal;

import com.parrotha.internal.app.AutomationAppApiHandler;
import com.parrotha.internal.device.DeviceApiHandler;
import com.parrotha.internal.device.DeviceService;
import com.parrotha.internal.entity.EntityService;
import com.parrotha.internal.extension.ExtensionApiHandler;
import com.parrotha.internal.hub.LocationApiHandler;
import com.parrotha.internal.hub.ScheduleService;
import com.parrotha.internal.hub.SettingsApiHandler;
import com.parrotha.internal.integration.IntegrationApiHandler;
import com.parrotha.internal.ui.UIFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // initialize directories and load configuration
        ServiceFactory.getConfigurationService().initialize();

        // try building database
        new DatabaseService().createDatabase();

        // start UI
        UIFramework uiFramework = new UIFramework();
        uiFramework.start();

        EntityService entityService = ServiceFactory.getEntityService();
        entityService.initialize();

        DeviceService deviceService = ServiceFactory.getDeviceService();

        ServiceFactory.getIntegrationService().start();

        // start scheduler
        ScheduleService scheduleService = ServiceFactory.getScheduleService();
        scheduleService.start();

        // configure api
        new DeviceApiHandler(deviceService, entityService).setupApi(uiFramework.getApp());
        new AutomationAppApiHandler(ServiceFactory.getAutomationAppService(), entityService, scheduleService).setupApi(uiFramework.getApp());
        new IntegrationApiHandler(ServiceFactory.getIntegrationService()).setupApi(uiFramework.getApp());
        new LocationApiHandler(ServiceFactory.getLocationService()).setupApi(uiFramework.getApp());
        new SettingsApiHandler(entityService).setupApi(uiFramework.getApp());
        new ExtensionApiHandler(ServiceFactory.getExtensionService()).setupApi(uiFramework.getApp());

        Thread myShutdownHook = new Thread(() -> {
            logger.info("In the middle of a shutdown");

            ServiceFactory.getScheduleService().shutdown();
            uiFramework.stop();
            ServiceFactory.getIntegrationService().stop();
        }
        );

        Runtime.getRuntime().addShutdownHook(myShutdownHook);

        logger.trace("End");
    }
}