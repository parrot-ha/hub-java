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
package com.parrotha.internal.app;

import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AutomationAppServiceTest {

    @Test
    public void testReprocessAutomationAppWithChange() {
        Map definition = Maps.of("name", "appName", "namespace", "appNamespace");
        AutomationAppDataStore mockAutomationAppDataStore = createMockAutomationAppDataStore("automationAppTest.groovy", definition);
        AutomationAppService automationAppService = new AutomationAppService(mockAutomationAppDataStore);

        automationAppService.reprocessAutomationApp("1");

        ArgumentCaptor<AutomationApp> automationAppCaptor = ArgumentCaptor.forClass(AutomationApp.class);
        verify(mockAutomationAppDataStore, times(1)).updateAutomationApp(automationAppCaptor.capture());

        assertEquals("Automation App Test", automationAppCaptor.getValue().getName());
        assertEquals("5aec4155-3470-406e-93eb-cbcc087cb088", automationAppCaptor.getValue().getoAuthClientId());
    }

    @Test
    public void testReprocessAutomationAppWithoutChange() {
        Map definition = Maps.of("name", "Automation App Test",
                "namespace", "com.parrotha",
                "author", "Anonymous",
                "description", "Automation App Test",
                "category", "My Apps",
                "iconUrl", "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
                "iconX2Url", "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
                "type", AutomationApp.Type.USER,
                "oauth", Maps.of("displayName", "Automation App Test",
                        "displayLink", "",
                        "clientId", "5aec4155-3470-406e-93eb-cbcc087cb088",
                        "clientSecret", "f30ff9d9-1cf3-47b1-a112-ef0172464d92"));

        AutomationAppDataStore mockAutomationAppDataStore = createMockAutomationAppDataStore("automationAppTest.groovy", definition);
        AutomationAppService automationAppService = new AutomationAppService(mockAutomationAppDataStore);

        automationAppService.reprocessAutomationApp("1");
        verify(mockAutomationAppDataStore, times(0)).updateAutomationApp(any(AutomationApp.class));
    }

    @Test
    public void testReprocessAutomationAppWithoutOAuthClientSecret() {
        Map definition = Maps.of("name", "Automation App Test",
                "namespace", "com.parrotha",
                "author", "Anonymous",
                "description", "Automation App Test",
                "category", "My Apps",
                "iconUrl", "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
                "iconX2Url", "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
                "type", AutomationApp.Type.USER,
                "oauth", Maps.of("displayName", "Automation App Test",
                        "displayLink", "",
                        "clientId", "5aec4155-3470-406e-93eb-cbcc087cb088",
                        "clientSecret", "f30ff9d9-1cf3-47b1-a112-ef0172464d92"));
        AutomationAppDataStore mockAutomationAppDataStore = createMockAutomationAppDataStore("automationAppTestWithoutOAuthClient.groovy",
                definition);
        AutomationAppService automationAppService = new AutomationAppService(mockAutomationAppDataStore);

        automationAppService.reprocessAutomationApp("1");
        verify(mockAutomationAppDataStore, times(0)).updateAutomationApp(any(AutomationApp.class));
    }

    private AutomationAppDataStore createMockAutomationAppDataStore(String fileName, Map definition) {
        ClassLoader classLoader = getClass().getClassLoader();
        String resourceFileName = classLoader.getResource(fileName).getFile();
        AutomationApp automationApp = new AutomationApp("1", resourceFileName, definition);

        AutomationAppDataStore mockAutomationAppDataStore = mock(AutomationAppDataStore.class);

        when(mockAutomationAppDataStore.getAutomationAppById(ArgumentMatchers.eq("1"))).thenReturn(automationApp);
        return mockAutomationAppDataStore;
    }
}
