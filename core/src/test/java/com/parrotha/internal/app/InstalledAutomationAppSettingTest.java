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
package com.parrotha.internal.app;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstalledAutomationAppSettingTest {
    @Test
    public void testGetValueAsTypeEnum() {
        InstalledAutomationAppSetting setting = new InstalledAutomationAppSetting();
        setting.setId(UUID.randomUUID().toString());
        setting.setName("testSetting");

        setting.processValueTypeAndMultiple("Test Value", "enum", true);
        assertEquals("Test Value", setting.getValue());
        assertNotNull(setting.getValueAsType());
        assertTrue(setting.getValueAsType() instanceof List);

        setting.processValueTypeAndMultiple(List.of("Test Value 1", "Test Value 2"), "enum", true);
        assertEquals("[\"Test Value 1\",\"Test Value 2\"]", setting.getValue());
        assertNotNull(setting.getValueAsType());
        assertTrue(setting.getValueAsType() instanceof List);

        setting.processValueTypeAndMultiple("Test Value 3", "enum", false);
        assertEquals("Test Value 3", setting.getValue());
        assertNotNull(setting.getValueAsType());
        assertTrue(setting.getValueAsType() instanceof String);
    }
}
