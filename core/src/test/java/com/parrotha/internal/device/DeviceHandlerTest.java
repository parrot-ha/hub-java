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
package com.parrotha.internal.device;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceHandlerTest {
    /**
     * Method under test: {@link DeviceHandler#equalsIgnoreId(DeviceHandler)}
     */
    @Test
    void testEqualsIgnoreId() {
        DeviceHandler deviceHandler = new DeviceHandler();
        DeviceHandler deviceHandler2 = new DeviceHandler();

        assertFalse(deviceHandler.equalsIgnoreId(null));
        assertTrue(deviceHandler.equalsIgnoreId(deviceHandler));
        assertTrue(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler.setFile("FileName");
        deviceHandler2.setFile("FileName2");
        assertFalse(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler2.setFile("FileName");
        deviceHandler.setName("DeviceHandlerName");
        deviceHandler2.setName("DeviceHandlerName2");
        assertFalse(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler2.setName("DeviceHandlerName");
        deviceHandler.setNamespace("Namespace");
        deviceHandler2.setNamespace("Namespace2");
        assertFalse(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler2.setNamespace("Namespace");
        deviceHandler.setAuthor("Author");
        deviceHandler2.setAuthor("Author2");
        assertFalse(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler2.setAuthor("Author");

        //Test tags
        deviceHandler.setTags(List.of("tag1", "tag2"));
        assertFalse(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler.setTags(null);
        deviceHandler2.setTags(List.of("tag3", "tag2"));
        assertFalse(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler.setTags(List.of("tag1", "tag2"));
        assertFalse(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler2.setTags(List.of("tag2", "tag1"));
        assertTrue(deviceHandler.equalsIgnoreId(deviceHandler2));

        //Test Commands
        deviceHandler.setCommandList(List.of(new Command("open"), new Command("close")));
        assertFalse(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler.setCommandList(null);
        deviceHandler2.setCommandList(List.of(new Command("refresh"), new Command("open")));
        assertFalse(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler.setCommandList(List.of(new Command("open"), new Command("close")));
        assertFalse(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler2.setCommandList(List.of(new Command("close"), new Command("open")));
        assertTrue(deviceHandler.equalsIgnoreId(deviceHandler2));

        deviceHandler.setCommandList(List.of(new Command("close"), new Command("open"), new Command("open", List.of("time"))));
        deviceHandler2.setCommandList(List.of(new Command("open"), new Command("close"), new Command("open", List.of("time"))));
        assertTrue(deviceHandler.equalsIgnoreId(deviceHandler2));
    }
}

