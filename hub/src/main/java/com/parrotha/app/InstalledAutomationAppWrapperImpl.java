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
package com.parrotha.app;

import groovy.lang.GroovyObjectSupport;
import com.parrotha.internal.app.AutomationAppService;
import com.parrotha.internal.app.InstalledAutomationApp;
import com.parrotha.internal.entity.EntityService;

import java.util.Map;

public class InstalledAutomationAppWrapperImpl extends GroovyObjectSupport implements InstalledAutomationAppWrapper {
    private InstalledAutomationApp installedAutomationApp;
    private EntityService entityService;
    private AutomationAppService automationAppService;

    public InstalledAutomationAppWrapperImpl(InstalledAutomationApp installedAutomationApp,
                                             EntityService entityService,
                                             AutomationAppService automationAppService) {
        this.installedAutomationApp = installedAutomationApp;
        this.entityService = entityService;
        this.automationAppService = automationAppService;
    }

    public Object methodMissing(String methodName, Object arguments) throws Exception {
        if (this.installedAutomationApp != null) {
            return this.entityService.runInstalledAutomationAppMethodAndReturn(this.installedAutomationApp.getId(), methodName, arguments);
        } else {
            throw new RuntimeException("Installed Automation App not found");
        }
    }

    public Object propertyMissing(String property) {
        // look for property in settings
        Map settings = installedAutomationApp.getNameToSettingMap();
        if (settings != null) {
            Object propertyObj = settings.get(property);
            if (propertyObj != null)
                return propertyObj;
        }
        //it appears that ST will return null if property is not found, so do not throw mpe
        return null;
    }

    @Override
    public String getId() {
        return installedAutomationApp.getId();
    }

    @Override
    public String getInstallationState() {
        return installedAutomationApp.isInstalled() ? "COMPLETE" : "INCOMPLETE";
    }

    @Override
    public String getLabel() {
        return installedAutomationApp.getLabel();
    }

    @Override
    public String getName() {
        return installedAutomationApp.getName();
    }

    //https://community.smartthings.com/t/update-preferences-field-from-code/21219/5
    //app.updateSetting(inputName, [type: type, value: value])
    //app.updateSetting(inputName, value)
    //// or for DTHs:
    //device.updateSetting(inputName, [type: type, value: value])
    //device.updateSetting(inputName, value)
    @Override
    public void updateSetting(String inputName, Object value) {
        automationAppService.updateInstalledAutomationAppSetting(this.installedAutomationApp.getId(), inputName, value);
    }

    @Override
    public void updateSetting(String inputName, Map options) {
        automationAppService.updateInstalledAutomationAppSetting(this.installedAutomationApp.getId(), inputName,
                (String) options.get("type"), options.get("value"));
    }
}
