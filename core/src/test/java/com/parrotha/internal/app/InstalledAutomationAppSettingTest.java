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
