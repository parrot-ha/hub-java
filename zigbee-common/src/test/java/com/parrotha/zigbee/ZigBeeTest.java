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
package com.parrotha.zigbee;

import com.parrotha.app.DeviceWrapper;
import com.parrotha.zigbee.clusters.iaszone.ZoneStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ZigBeeTest {
    @Test
    public void testLevelConfig() {
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        when(deviceWrapperMock.getDeviceNetworkId()).thenReturn("1234");
        when(deviceWrapperMock.getEndpointId()).thenReturn(1);
        when(deviceWrapperMock.getZigbeeId()).thenReturn("0123456789");

        ZigBeeImpl zigbee = new ZigBeeImpl(deviceWrapperMock);
        List<String> cmds = zigbee.levelConfig();
        assertNotNull(cmds);
        assertEquals(4, cmds.size());
        assertEquals("zdo bind 0x1234 0x01 0x01 0x0008 {0123456789} {}", cmds.get(0));
        assertEquals("ph cr 0x1234 0x01 0x0008 0x0000 0x20 0x0001 0x0E10 {01}", cmds.get(2));

        cmds = zigbee.levelConfig(100);
        assertNotNull(cmds);
        assertEquals(4, cmds.size());
        assertEquals("zdo bind 0x1234 0x01 0x01 0x0008 {0123456789} {}", cmds.get(0));
        assertEquals("ph cr 0x1234 0x01 0x0008 0x0000 0x20 0x0064 0x0E10 {01}", cmds.get(2));

        cmds = zigbee.levelConfig(5, 20);
        assertNotNull(cmds);
        assertEquals(4, cmds.size());
        assertEquals("zdo bind 0x1234 0x01 0x01 0x0008 {0123456789} {}", cmds.get(0));
        assertEquals("ph cr 0x1234 0x01 0x0008 0x0000 0x20 0x0005 0x0014 {01}", cmds.get(2));

        cmds = zigbee.levelConfig(8, 25, 2);
        assertNotNull(cmds);
        assertEquals(4, cmds.size());
        assertEquals("zdo bind 0x1234 0x01 0x01 0x0008 {0123456789} {}", cmds.get(0));
        assertEquals("ph cr 0x1234 0x01 0x0008 0x0000 0x20 0x0008 0x0019 {02}", cmds.get(2));
    }


    @Test
    public void testGetEvent() {
        Map event = ZigBeeImpl.getEvent(
                "read attr - raw: 3F9A0A00060A00001001, dni: 3F9A, endpoint: 0A, cluster: 0006, size: 0A, attrId: 0000, encoding: 10, command: 01, value: 01");
        assertNotNull(event);
        assertEquals(2, event.size());
        assertEquals("switch", event.get("name"));
        assertEquals("on", event.get("value"));

        event = ZigBeeImpl.getEvent(
                "read attr - raw: 3F9A0A00080A000020FE, dni: 3F9A, endpoint: 0A, cluster: 0008, size: 0A, attrId: 0000, encoding: 20, command: 01, value: FE");
        assertNotNull(event);
        assertEquals(2, event.size());
        assertEquals("level", event.get("name"));
        assertEquals(100, event.get("value"));
    }

    @Test
    public void testParseReadAttributeDescription() {
        //read attr - raw: E6A20104020A0000299907, dni: E6A2, endpoint: 01, cluster: 0402, size: 0A, attrId: 0000, encoding: 29, command: 0A, value: 9907
        //read attr - raw: E6A20104050A0000215E14, dni: E6A2, endpoint: 01, cluster: 0405, size: 0A, attrId: 0000, encoding: 21, command: 0A, value: 5E14
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        Map parsedDesc = new ZigBeeImpl(deviceWrapperMock).parseDescriptionAsMap(
                "read attr - raw: E6A20104020A0000299907, dni: E6A2, endpoint: 01, cluster: 0402, size: 0A, attrId: 0000, encoding: 29, command: 0A, value: 9907");
        //[raw:E6A20104020A0000299907, dni:E6A2, endpoint:01, cluster:0402, size:0A, attrId:0000, encoding:29, command:0A, value:0799, clusterInt:1026, attrInt:0]
        assertEquals("E6A20104020A0000299907", parsedDesc.get("raw"));
        assertEquals("E6A2", parsedDesc.get("dni"));
        assertEquals("01", parsedDesc.get("endpoint"));
        assertEquals("0402", parsedDesc.get("cluster"));
        assertEquals("0A", parsedDesc.get("size"));
        assertEquals("0000", parsedDesc.get("attrId"));
        assertEquals("29", parsedDesc.get("encoding"));
        assertEquals("0A", parsedDesc.get("command"));
        assertEquals("0799", parsedDesc.get("value"));
        assertEquals(1026, parsedDesc.get("clusterInt"));
        assertEquals(0, parsedDesc.get("attrInt"));


        //read attr - raw: E6A201000108210020C8, dni: E6A2, endpoint: 01, cluster: 0001, size: 08, attrId: 0021, encoding: 20, command: 0A, value: C8
    }

    @Test
    public void testParseCatchallDescription() {
        //"catchall: 0104 0500 01 01 0040 00 ACFE 01 00 0000 00 01 010000000000"
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        Map parsedDesc = new ZigBeeImpl(deviceWrapperMock).parseDescriptionAsMap(
                "catchall: 0104 0500 01 01 0040 00 ACFE 01 00 0000 00 01 010000000000");
        //[raw:0104 0500 01 01 0040 00 ACFE 01 00 0000 00 01 010000000000, profileId:0104, clusterId:0500, sourceEndpoint:01, destinationEndpoint:01, options:0040, messageType:00, dni:ACFE, isClusterSpecific:true, isManufacturerSpecific:false, manufacturerId:0000, command:00, direction:01, data:[01, 00, 00, 00, 00, 00], clusterInt:1280, commandInt:0]
        assertEquals("0104 0500 01 01 0040 00 ACFE 01 00 0000 00 01 010000000000", parsedDesc.get("raw"));
        assertEquals("0104", parsedDesc.get("profileId"));
        assertEquals("0500", parsedDesc.get("clusterId"));
        assertEquals("01", parsedDesc.get("sourceEndpoint"));
        assertEquals("01", parsedDesc.get("destinationEndpoint"));
        assertEquals("0040", parsedDesc.get("options"));
        assertEquals("00", parsedDesc.get("messageType"));
        assertEquals("ACFE", parsedDesc.get("dni"));

        assertTrue((boolean) parsedDesc.get("isClusterSpecific"));
        assertFalse((boolean) parsedDesc.get("isManufacturerSpecific"));

        assertEquals("0000", parsedDesc.get("manufacturerId"));
        assertEquals("00", parsedDesc.get("command"));
        assertEquals("01", parsedDesc.get("direction"));
        assertEquals(1280, parsedDesc.get("clusterInt"));
        assertEquals(0, parsedDesc.get("commandInt"));

        assertNotNull(parsedDesc.get("data"));
        assertTrue(parsedDesc.get("data") instanceof List);
        List data = (List) parsedDesc.get("data");
        assertEquals(6, data.size());
        assertEquals("01", data.get(0));
        assertEquals("00", data.get(1));
    }

    // zigbee.setLevel("12") 1e = 30
    // [st cmd 0xFC6E 0x01 0x0008 0x04 {1e FFFF}, delay 2000]
    // zigbee.setLevel(20) 33 = 51
    // [st cmd 0xFC6E 0x01 0x0008 0x04 {33 FFFF}, delay 2000]
    // zigbee.setLevel(50) 7f = 127
    // [st cmd 0xFC6E 0x01 0x0008 0x04 {7f FFFF}, delay 2000]
    // zigbee.setLevel(100) fe = 254
    // [st cmd 0xFC6E 0x01 0x0008 0x04 {fe FFFF}, delay 2000]
    // zigbee.setLevel(100, 100)
    // [st cmd 0xFC6E 0x01 0x0008 0x04 {fe 6400}, delay 2000]
    // zigbee.setLevel(90, 0)
    // [st cmd 0xFC6E 0x01 0x0008 0x04 {e5 0000}, delay 2000]
    @Test
    public void testSetLevel() {
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        when(deviceWrapperMock.getDeviceNetworkId()).thenReturn("1234");
        when(deviceWrapperMock.getEndpointId()).thenReturn(1);
        when(deviceWrapperMock.getZigbeeId()).thenReturn("0123456789");

        ZigBeeImpl zigbee = new ZigBeeImpl(deviceWrapperMock);
        List<String> cmds = zigbee.setLevel("100");
        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph cmd 0x1234 0x01 0x0008 0x04 {FE FFFF}", cmds.get(0));

        cmds = zigbee.setLevel(12);
        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph cmd 0x1234 0x01 0x0008 0x04 {1E FFFF}", cmds.get(0));

        cmds = zigbee.setLevel(100, 100);
        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph cmd 0x1234 0x01 0x0008 0x04 {FE 6400}", cmds.get(0));

        cmds = zigbee.setLevel(1.0);
        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph cmd 0x1234 0x01 0x0008 0x04 {03 FFFF}", cmds.get(0));
    }

    @Test
    public void testCommandWithDestEndpoint() {
        // zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: 0x02])
        // [st cmd 0xFC6E 0x02 0x0006 0x00 {}, delay 2000]

        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        when(deviceWrapperMock.getDeviceNetworkId()).thenReturn("1234");
        when(deviceWrapperMock.getEndpointId()).thenReturn(1);
        when(deviceWrapperMock.getZigbeeId()).thenReturn("0123456789");

        ZigBeeImpl zigbee = new ZigBeeImpl(deviceWrapperMock);
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("destEndpoint", 0x02);
        List<String> cmds = zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", additionalParams);
        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph cmd 0x1234 0x02 0x0006 0x00 {}", cmds.get(0));


    }

    @Test
    public void testCommandWithMfgCode() {
        // zigbee.command(6, 4, "0102", [mfgCode:"1234"])
        // [raw 0x0006 { 05D204FF040102 }, delay 200, send 0xFC6E 0x01 0x01, delay 2000]
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        when(deviceWrapperMock.getDeviceNetworkId()).thenReturn("1234");
        when(deviceWrapperMock.getEndpointId()).thenReturn(1);
        when(deviceWrapperMock.getZigbeeId()).thenReturn("0123456789");

        ZigBeeImpl zigbee = new ZigBeeImpl(deviceWrapperMock);
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("mfgCode", 1234);
        List<String> cmds = zigbee.command(6, 4, "0102", additionalParams);
        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph cmd 0x1234 0x01 0x0006 0x04 {0102} {04D2}", cmds.get(0));

        additionalParams.put("mfgCode", "0x1234");
        cmds = zigbee.command(6, 4, "0102", additionalParams);
        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph cmd 0x1234 0x01 0x0006 0x04 {0102} {1234}", cmds.get(0));
    }

    @Test
    public void testReadAttribute() {
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        when(deviceWrapperMock.getDeviceNetworkId()).thenReturn("1234");
        when(deviceWrapperMock.getEndpointId()).thenReturn(1);
        when(deviceWrapperMock.getZigbeeId()).thenReturn("0123456789");
        ZigBeeImpl zigbee = new ZigBeeImpl(deviceWrapperMock);
        Map<String, Object> additionalParams = new HashMap<>();
        List<String> cmds = zigbee.readAttribute(0x0101, 0x0000, additionalParams, 200);
        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph rattr 0x1234 0x01 0x0101 0x0000", cmds.get(0));
        assertEquals("delay 200", cmds.get(1));

        cmds = zigbee.readAttribute(0x0001, 0x0021, additionalParams, 0);
        assertNotNull(cmds);
        assertEquals(1, cmds.size());
        assertEquals("ph rattr 0x1234 0x01 0x0001 0x0021", cmds.get(0));
    }

    @Test
    public void testReadAttributeWithManufacturerSpecific() {
        // zigbee.readAttribute(0x0511, 0x0023, [mfgCode: '0x1A34'])
        // [zcl mfg-code 0x1039, delay 200, zcl global read 0x0201 0x0023, delay 200, send 0xFC6E 0x01 0x01, delay 2000]
        // ph rattr 0x1234 0x01 0x0511, 0x0023 {1A34}
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        when(deviceWrapperMock.getDeviceNetworkId()).thenReturn("1234");
        when(deviceWrapperMock.getEndpointId()).thenReturn(1);
        when(deviceWrapperMock.getZigbeeId()).thenReturn("0123456789");
        ZigBeeImpl zigbee = new ZigBeeImpl(deviceWrapperMock);
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("mfgCode", "0x1A34");
        List<String> cmds = zigbee.readAttribute(0x0511, 0x0023, additionalParams);
        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph rattr 0x1234 0x01 0x0511 0x0023 {1A34}", cmds.get(0));
        assertEquals("delay 2000", cmds.get(1));

        cmds = zigbee.readAttribute(0x0001, 0x0021, additionalParams, 0);
        assertNotNull(cmds);
        assertEquals(1, cmds.size());
        assertEquals("ph rattr 0x1234 0x01 0x0001 0x0021 {1A34}", cmds.get(0));
    }

    @Test
    public void testWriteAttribute() {
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        when(deviceWrapperMock.getDeviceNetworkId()).thenReturn("1234");
        when(deviceWrapperMock.getEndpointId()).thenReturn(1);
        when(deviceWrapperMock.getZigbeeId()).thenReturn("0123456789");
        ZigBeeImpl zigbee = new ZigBeeImpl(deviceWrapperMock);
        List<String> cmds = zigbee.writeAttribute(0x05A3, 0x0015, 0x21, 0xffff);

        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph wattr 0x1234 0x01 0x05A3 0x0015 0x21 {FFFF}", cmds.get(0));
        assertEquals("delay 2000", cmds.get(1));
    }

    @Test
    public void testWriteAttributeWithMfgCode() {
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        when(deviceWrapperMock.getDeviceNetworkId()).thenReturn("1234");
        when(deviceWrapperMock.getEndpointId()).thenReturn(1);
        when(deviceWrapperMock.getZigbeeId()).thenReturn("0123456789");
        ZigBeeImpl zigbee = new ZigBeeImpl(deviceWrapperMock);
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("mfgCode", "0xA156");
        List<String> cmds = zigbee.writeAttribute(0x05A3, 0x0015, 0x21, 0xffff, additionalParams);

        assertNotNull(cmds);
        assertEquals(2, cmds.size());
        assertEquals("ph wattr 0x1234 0x01 0x05A3 0x0015 0x21 {FFFF} {A156}", cmds.get(0));
        assertEquals("delay 2000", cmds.get(1));
    }

    @Test
    public void testParseZoneStatus() {
        //zone status 0x0001 -- extended status 0x00 -- zone ID 0x00 -- delay 0x0000
        ZoneStatus zoneStatus = ZigBeeImpl.parseZoneStatus("zone status 0x0001 -- extended status 0x00 -- zone ID 0x00 -- delay 0x0000");
        assertNotNull(zoneStatus);
        Assertions.assertTrue(zoneStatus.isAlarm1Set());
    }

    @Test
    public void testConfigureReporting() {
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        when(deviceWrapperMock.getDeviceNetworkId()).thenReturn("1234");
        when(deviceWrapperMock.getEndpointId()).thenReturn(1);
        when(deviceWrapperMock.getZigbeeId()).thenReturn("0123456789");

        ZigBeeImpl zigbee = new ZigBeeImpl(deviceWrapperMock);
        List<String> cmds = zigbee.configureReporting(0x0405, 0x0000, 0x29, 60, 3600, 200, new HashMap(), 53);
        assertNotNull(cmds);
        assertEquals(4, cmds.size());
        assertEquals("zdo bind 0x1234 0x01 0x01 0x0405 {0123456789} {}", cmds.get(0));
        assertEquals("ph cr 0x1234 0x01 0x0405 0x0000 0x29 0x003C 0x0E10 {C800} {}", cmds.get(2));

        cmds = zigbee.configureReporting(0x0201, 0x0023, 0x30, 0,    600,   null,   Map.of("mfgCode","1039"), 500);
        System.out.println(cmds);
        assertEquals("zdo bind 0x1234 0x01 0x01 0x0201 {0123456789} {}", cmds.get(0));
        assertEquals("ph cr 0x1234 0x01 0x0201 0x0023 0x30 0x0000 0x0258 {} {1039}", cmds.get(2));

        assertEquals(4, cmds.size());
    }

    @Test
    public void testBatteryConfig() {
        DeviceWrapper deviceWrapperMock = Mockito.mock(DeviceWrapper.class);
        when(deviceWrapperMock.getDeviceNetworkId()).thenReturn("1234");
        when(deviceWrapperMock.getEndpointId()).thenReturn(1);
        when(deviceWrapperMock.getZigbeeId()).thenReturn("0123456789");

        ZigBeeImpl zigbee = new ZigBeeImpl(deviceWrapperMock);
        List<String> cmds = zigbee.batteryConfig();
        assertNotNull(cmds);
        assertEquals(4, cmds.size());
        assertEquals("zdo bind 0x1234 0x01 0x01 1 {0123456789} {}", cmds.get(0));
        assertEquals("ph cr 0x1234 0x01 0x0001 0x0020 0x20 0x001E 0x5460 {01}", cmds.get(2));
        assertEquals("delay 2000", cmds.get(1));

        cmds = zigbee.batteryConfig(200);
        assertNotNull(cmds);
        assertEquals(4, cmds.size());
        assertEquals("zdo bind 0x1234 0x01 0x01 1 {0123456789} {}", cmds.get(0));
        assertEquals("ph cr 0x1234 0x01 0x0001 0x0020 0x20 0x001E 0x5460 {01}", cmds.get(2));
        assertEquals("delay 200", cmds.get(1));
    }
}
