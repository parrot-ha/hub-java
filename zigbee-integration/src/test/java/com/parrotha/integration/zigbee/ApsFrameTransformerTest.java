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

import com.zsmartsystems.zigbee.aps.ZigBeeApsFrame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApsFrameTransformerTest {

    @Test
    public void testIASZoneMessage() {
        //{ZigBeeApsFrame [sourceAddress=453E/1, destinationAddress=0000/1, profile=0104, cluster=0500, addressMode=DEVICE, radius=0, apsSecurity=false, ackRequest=false, apsCounter=4C, rssi=-32, lqi=FF, payload=19 01 00 30 00 00]}
        ZigBeeApsFrame apsFrame = new ZigBeeApsFrame();
        apsFrame.setSourceAddress(0x453E);
        apsFrame.setSourceEndpoint(0x01);
        apsFrame.setDestinationAddress(0x0000);
        apsFrame.setDestinationEndpoint(0x01);
        apsFrame.setProfile(0x0104);
        apsFrame.setCluster(0x0500);
        apsFrame.setPayload(new int[]{0x19, 0x01, 0x00, 0x30, 0x00, 0x00});
        String transformedStr = ApsFrameTransformer.transformApsFrame(apsFrame);
        assertNotNull(transformedStr);
        assertEquals("zone status 0x0030 -- extended status 0x00", transformedStr);

        // additional info
        apsFrame.setPayload(new int[]{0x19, 0x01, 0x00, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00});
        transformedStr = ApsFrameTransformer.transformApsFrame(apsFrame);
        assertNotNull(transformedStr);
        assertEquals("zone status 0x0030 -- extended status 0x00 -- zone ID 0x00 -- delay 0x0000", transformedStr);
    }

    @Test
    public void testReadAttributeResponse() {
        //frame control 0x18, command 0x01
        ZigBeeApsFrame apsFrame = new ZigBeeApsFrame();
        apsFrame.setSourceAddress(0x1B84);
        apsFrame.setSourceEndpoint(0x01);
        apsFrame.setCluster(0x0201);
        apsFrame.setProfile(0x0104);
        apsFrame.setPayload(new int[]{0x1C, 0x39, 0x10, 0xBC, 0x0A, 0x23, 0x00, 0x30, 0x00, 0x24, 0x00, 0x21, 0x00, 0x00, 0x02, 0x01, 0x21, 0x00, 0x00});

        String transformedStr = ApsFrameTransformer.transformApsFrame(apsFrame);
        assertEquals("read attr - raw: 1B840102011C2300300024002100000201210000, dni: 1B84, endpoint: 01, cluster: 0201, size: 1C, attrId: 0023, encoding: 30, command: 0A, value: 0024002100000201210000", transformedStr);

        apsFrame = new ZigBeeApsFrame();
        apsFrame.setSourceAddress(0x1B84);
        apsFrame.setSourceEndpoint(0x01);
        apsFrame.setCluster(0x0201);
        apsFrame.setProfile(0x0104);
        apsFrame.setPayload(new int[]{0x1C, 0x39, 0x10, 0xBD, 0x0A, 0x00, 0x01, 0x30, 0x02});

        transformedStr = ApsFrameTransformer.transformApsFrame(apsFrame);
        assertEquals("read attr - raw: 1B840102010800013002, dni: 1B84, endpoint: 01, cluster: 0201, size: 08, attrId: 0100, encoding: 30, command: 0A, value: 02", transformedStr);

        apsFrame = new ZigBeeApsFrame();
        apsFrame.setPayload(new int[]{0x18, 0xF1, 0x0A, 0x00, 0x00, 0x29, 0x64, 0x07});
        apsFrame.setSourceAddress(0xE6A2);
        apsFrame.setProfile(0x0104);
        apsFrame.setCluster(0x0402);
        apsFrame.setSourceEndpoint(0x01);
        transformedStr = ApsFrameTransformer.transformApsFrame(apsFrame);
        assertEquals("read attr - raw: E6A20104020A0000296407, dni: E6A2, endpoint: 01, cluster: 0402, size: 0A, attrId: 0000, encoding: 29, command: 0A, value: 6407", transformedStr);
    }

    @Test
    public void testCatchAllResponse() {
        ZigBeeApsFrame apsFrame = new ZigBeeApsFrame();
        apsFrame.setPayload(new int[]{0x08, 0x08, 0x0B, 0x00, 0x00});
        apsFrame.setSourceAddress(0xE6A2);
        apsFrame.setProfile(0x0104);
        apsFrame.setCluster(0x0006);
        apsFrame.setSourceEndpoint(0x01);
        apsFrame.setDestinationEndpoint(0x01);
        String transformedStr = ApsFrameTransformer.transformApsFrame(apsFrame);
        assertEquals("catchall: 0104 0006 01 01 0040 00 E6A2 00 00 0000 0B 01 0000", transformedStr);
    }

    @Test
    public void testReadAttributesResponseWithUnsupportedAttributeStatus() {
        //{ZigBeeApsFrame [sourceAddress=9750/1, destinationAddress=0000/1, profile=0104, cluster=0001, addressMode=DEVICE, radius=0, apsSecurity=false, ackRequest=false, apsCounter=34, rssi=-44, lqi=FF, payload=18 25 01 21 00 86]}
        ZigBeeApsFrame apsFrame = new ZigBeeApsFrame();
        apsFrame.setSourceAddress(0x9750);
        apsFrame.setSourceEndpoint(0x01);
        apsFrame.setDestinationAddress(0x0000);
        apsFrame.setDestinationEndpoint(0x01);
        apsFrame.setProfile(0x0104);
        apsFrame.setCluster(0x0001);
        apsFrame.setPayload(new int[]{0x18, 0x25, 0x01, 0x21, 0x00, 0x86});
        String transformedStr = ApsFrameTransformer.transformApsFrame(apsFrame);
        assertNotNull(transformedStr);
        assertEquals("catchall: 0104 0001 01 01 0040 00 9750 00 00 0000 01 01 210086", transformedStr);
    }

    @Test
    public void testClusterSpecificFrame() {
        //{ZigBeeApsFrame [sourceAddress=5C36/1, destinationAddress=0000/1, profile=0104, cluster=0101, addressMode=DEVICE, radius=0, apsSecurity=false, ackRequest=false, apsCounter=8E, rssi=-56, lqi=A1, payload=19 17 01 00]}
        ZigBeeApsFrame apsFrame = new ZigBeeApsFrame();
        apsFrame.setSourceAddress(0x5C36);
        apsFrame.setSourceEndpoint(0x01);
        apsFrame.setDestinationAddress(0x0000);
        apsFrame.setDestinationEndpoint(0x01);
        apsFrame.setProfile(0x0104);
        apsFrame.setCluster(0x0101);
        apsFrame.setPayload(new int[]{0x19, 0x17, 0x01, 0x00});
        String transformedStr = ApsFrameTransformer.transformApsFrame(apsFrame);
        assertNotNull(transformedStr);
        assertEquals("catchall: 0104 0101 01 01 0040 00 5C36 01 00 0000 01 01 00", transformedStr);
    }
}
