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
package com.parrotha.integration.zigbee;

import com.zsmartsystems.zigbee.ZigBeeCommand;
import com.zsmartsystems.zigbee.serialization.DefaultSerializer;
import com.zsmartsystems.zigbee.transport.ZigBeePort;
import com.zsmartsystems.zigbee.zcl.ZclCommand;
import com.zsmartsystems.zigbee.zcl.ZclFieldSerializer;
import org.apache.commons.lang3.StringUtils;
import com.parrotha.internal.utils.HexUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZigBeeHandlerTest {
    @Test
    public void testProcessZigbeeCommand() {
        ZigBeeHandlerMock zigBeeHandlerMock = new ZigBeeHandlerMock(null, 0, null, null);

        zigBeeHandlerMock.payloadCompare = null;
        zigBeeHandlerMock.processZigbeeCommand("st cmd 0x0101 17 6 1 {}");

        zigBeeHandlerMock.payloadCompare = "0A0000";
        zigBeeHandlerMock.processZigbeeCommand("st cmd 0xABCD 01 8 4 {0A 0000}");
        zigBeeHandlerMock.processZigbeeCommand("st cmd 0xABCD 01 8 4 {0A 00 00}");
        zigBeeHandlerMock.processZigbeeCommand("st cmd 0xABCD 01 8 4 {0A0000}");
    }

    @Test
    public void testProcessRawCommand() {
        ZigBeeHandlerMock zigBeeHandlerMock = new ZigBeeHandlerMock(null, 0, null, null);

        zigBeeHandlerMock.payloadCompare = "04";
        zigBeeHandlerMock.manufacturerSpecific = false;
        zigBeeHandlerMock.commandId = 0;
        zigBeeHandlerMock.processZigbeeCommand("ph raw 0xABCD 1 1 0x0501 4 {09 01 00 04}");

        zigBeeHandlerMock.payloadCompare = "0102";
        zigBeeHandlerMock.manufacturerSpecific = true;
        zigBeeHandlerMock.commandId = 4;
        zigBeeHandlerMock.processZigbeeCommand("ph raw 0x1234 0x01 0x01 0x0006 { 05D204FF040102 }");

    }

    private class ZigBeeHandlerMock extends ZigBeeHandler {
        public ZigBeeHandlerMock(String serialPortName, int serialBaud, ZigBeePort.FlowControl flowControl, ZigBeeIntegration zigBeeIntegration) {
            super(serialPortName, serialBaud, flowControl, zigBeeIntegration);
        }

        public String payloadCompare = null;
        public boolean manufacturerSpecific = false;
        public Integer commandId = null;

        @Override
        public void sendZigBeeCommand(ZigBeeCommand command) {
            final ZclFieldSerializer fieldSerializer = new ZclFieldSerializer(new DefaultSerializer());

            if (command instanceof ZclCommand) {
                command.serialize(fieldSerializer);
                int[] payload = fieldSerializer.getPayload();
                if (StringUtils.isNotEmpty(payloadCompare)) {
                    assertTrue(payload.length > 0);
                    assertEquals(payloadCompare, HexUtils.intArrayToHexString(payload));
                    assertEquals(manufacturerSpecific, ((ZclCommand) command).isManufacturerSpecific());
                    if (commandId != null) {
                        assertEquals(commandId, ((ZclCommand) command).getCommandId());
                    }
                }
            }
        }
    }
}
