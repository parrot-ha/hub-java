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
package com.parrotha.integration.zigbee;

import com.zsmartsystems.zigbee.ZigBeeCommand;
import com.zsmartsystems.zigbee.zcl.ZclCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZigBeeMessageTransformerTest {
    @Test
    public void testManufacturerSpecificRawCommand() {
        ZigBeeCommand zigBeeCommand = ZigBeeMessageTransformer.createCommand("ph raw 0x99A2 0x01 0x01 0x0006 { 053412FF030A00 }", null);
        assertNotNull(zigBeeCommand);
        assertTrue(zigBeeCommand instanceof ZclCommand);
        ZclCommand zclCommand = (ZclCommand) zigBeeCommand;
        assertTrue(zclCommand.isManufacturerSpecific());
        assertEquals(4660, zclCommand.getManufacturerCode());
    }

    @Test
    public void testManufacturerSpecificConfigureReportingCommand() {
        ZigBeeCommand zigBeeCommand = ZigBeeMessageTransformer.createCommand("ph cr 0x1234 0x01 0x0201 0x0023 0x30 0x0000 0x0258 {} {1039}", null);
        assertNotNull(zigBeeCommand);
        assertTrue(zigBeeCommand instanceof ZclCommand);
        ZclCommand zclCommand = (ZclCommand) zigBeeCommand;
        assertTrue(zclCommand.isManufacturerSpecific());
        assertEquals(0x1039, zclCommand.getManufacturerCode());
    }
}
