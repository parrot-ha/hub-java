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
package com.parrotha.service;

import com.parrotha.internal.app.AutomationAppService;
import com.parrotha.internal.device.DeviceHandler;
import com.parrotha.internal.device.DeviceService;
import com.parrotha.internal.device.Fingerprint;
import com.parrotha.internal.entity.EntityServiceImpl;
import com.parrotha.internal.hub.EventService;
import com.parrotha.internal.hub.LocationService;
import com.parrotha.internal.hub.ScheduleService;
import com.parrotha.internal.integration.IntegrationRegistry;
import com.parrotha.internal.entity.EntityService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntityServiceTest {

    @Test
    public void testGetDeviceHandlerByFingerprintWithProfileIdUnsortedInClustersAndOutClusters() {
        DeviceService mockDeviceService = mock(DeviceService.class);
        AutomationAppService mockAutomationAppService = mock(AutomationAppService.class);
        EventService mockEventService = mock(EventService.class);
        LocationService mockLocationService = mock(LocationService.class);
        ScheduleService mockScheduleService = mock(ScheduleService.class);
        IntegrationRegistry mockIntegrationRegistry = mock(IntegrationRegistry.class);
        EntityService entityService = new EntityServiceImpl(mockDeviceService, mockAutomationAppService, mockEventService, mockLocationService,
                mockScheduleService, mockIntegrationRegistry);

        Fingerprint fp = new Fingerprint();
        fp.setProfileId("0104");
        fp.setInClusters("0000,0001,0003,0020,0006,0201");
        fp.setOutClusters("000A,0019");
        String deviceHandlerId = "afeafea";
        DeviceHandler dh = new DeviceHandler();
        dh.setId(deviceHandlerId);
        dh.setFingerprints(Collections.singletonList(fp));
        Collection<DeviceHandler> dhCollection = new ArrayList<>();
        dhCollection.add(dh);

        when(mockDeviceService.getAllDeviceHandlers()).thenReturn(dhCollection);
        Map<String, String> deviceInfo = Stream.of(new String[][]{
                {"profileId", "0104"},
                {"inClusters", "0000,0001,0003,0006,0020,0201"},
                {"outClusters", "000A,0019"},
                {"model", "HT8-ZB"},
                {"manufacturer", "Orbit"},
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        String[] foundDH = entityService.getDeviceHandlerByFingerprint(deviceInfo);
        assertNotNull(foundDH);
        assertEquals(2, foundDH.length);
        assertEquals(deviceHandlerId, foundDH[0]);
        assertNull(foundDH[1]);
    }

    @Test
    public void testGetDeviceHandlerByFingerprint() {
        DeviceService mockDeviceService = mock(DeviceService.class);
        AutomationAppService mockAutomationAppService = mock(AutomationAppService.class);
        EventService mockEventService = mock(EventService.class);
        LocationService mockLocationService = mock(LocationService.class);
        ScheduleService mockScheduleService = mock(ScheduleService.class);
        IntegrationRegistry mockIntegrationRegistry = mock(IntegrationRegistry.class);
        EntityService entityService = new EntityServiceImpl(mockDeviceService, mockAutomationAppService, mockEventService, mockLocationService,
                mockScheduleService, mockIntegrationRegistry);


        Fingerprint fp = new Fingerprint();
        fp.setProfileId("0104");
        fp.setInClusters("0000,0003,0004,0005,0006,0B05,FC01,FC08");
        fp.setOutClusters("0003,0019");
        fp.setModel("PLUG");
        fp.setManufacturer("LEDVANCE");
        fp.setDeviceJoinName("Sylvania Outlet");
        String deviceHandlerId = "abc123";
        DeviceHandler dh = new DeviceHandler();
        dh.setId(deviceHandlerId);
        dh.setFingerprints(Collections.singletonList(fp));
        Collection<DeviceHandler> dhCollection = new ArrayList<>();
        dhCollection.add(dh);

        when(mockDeviceService.getAllDeviceHandlers()).thenReturn(dhCollection);
        Map<String, String> deviceInfo = Stream.of(new String[][]{
                {"profileId", "0104"},
                {"inClusters", "0000,0003,0004,0005,0006,0B05,FC01,FC08"},
                {"outClusters", "0003,0019"},
                {"model", "PLUG"},
                {"manufacturer", "LEDVANCE"},
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        String[] foundDH = entityService.getDeviceHandlerByFingerprint(deviceInfo);
        assertNotNull(foundDH);
        assertEquals(2, foundDH.length);
        assertEquals(deviceHandlerId, foundDH[0]);
        assertEquals("Sylvania Outlet", foundDH[1]);
    }

    @Test
    public void testGetDeviceHandlerByFingerprintWithInClustersAndNoOutClusters() {
        DeviceService mockDeviceService = mock(DeviceService.class);
        AutomationAppService mockAutomationAppService = mock(AutomationAppService.class);
        EventService mockEventService = mock(EventService.class);
        LocationService mockLocationService = mock(LocationService.class);
        ScheduleService mockScheduleService = mock(ScheduleService.class);
        IntegrationRegistry mockIntegrationRegistry = mock(IntegrationRegistry.class);
        EntityService entityService = new EntityServiceImpl(mockDeviceService, mockAutomationAppService, mockEventService, mockLocationService,
                mockScheduleService, mockIntegrationRegistry);

        Fingerprint fp = new Fingerprint();
        fp.setProfileId("0104");
        fp.setInClusters("0000,0001,0003,0406,0500,0020,0402,0405");
        fp.setModel("3041");
        fp.setManufacturer("NYCE");
        fp.setDeviceJoinName("NYCE Motion Sensor");
        String deviceHandlerId = "abc123";
        DeviceHandler dh = new DeviceHandler();
        dh.setId(deviceHandlerId);
        dh.setFingerprints(Collections.singletonList(fp));
        Collection<DeviceHandler> dhCollection = new ArrayList<>();
        dhCollection.add(dh);

        when(mockDeviceService.getAllDeviceHandlers()).thenReturn(dhCollection);
        Map<String, String> deviceInfo = Stream.of(new String[][]{
                {"profileId", "0104"},
                {"inClusters", "0000,0001,0003,0406,0500,0020,0402,0405"},
                {"outClusters", ""},
                {"model", "3041"},
                {"manufacturer", "NYCE"},
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        String[] foundDH = entityService.getDeviceHandlerByFingerprint(deviceInfo);
        assertNotNull(foundDH);
        assertEquals(2, foundDH.length);
        assertEquals(deviceHandlerId, foundDH[0]);
        assertEquals("NYCE Motion Sensor", foundDH[1]);
    }

    @Test
    public void testGetDeviceHandlerByFingerprintWithMfrProdModel() {
        DeviceService mockDeviceService = mock(DeviceService.class);
        AutomationAppService mockAutomationAppService = mock(AutomationAppService.class);
        EventService mockEventService = mock(EventService.class);
        LocationService mockLocationService = mock(LocationService.class);
        ScheduleService mockScheduleService = mock(ScheduleService.class);
        IntegrationRegistry mockIntegrationRegistry = mock(IntegrationRegistry.class);
        EntityService entityService = new EntityServiceImpl(mockDeviceService, mockAutomationAppService, mockEventService, mockLocationService,
                mockScheduleService, mockIntegrationRegistry);

        Fingerprint fp = new Fingerprint();
        fp.setProfileId(null);
        fp.setModel("3034");
        fp.setMfr("0063");
        fp.setProd("4944");
        fp.setDeviceJoinName("GE Fan");
        String deviceHandlerId = "abc123";
        DeviceHandler dh = new DeviceHandler();
        dh.setId(deviceHandlerId);
        dh.setFingerprints(Collections.singletonList(fp));
        Collection<DeviceHandler> dhCollection = new ArrayList<>();
        dhCollection.add(dh);

        when(mockDeviceService.getAllDeviceHandlers()).thenReturn(dhCollection);
        Map<String, String> deviceInfo = Stream.of(new String[][]{
                {"prod", "4944"},
                {"mfr", "0063"},
                {"model", "3034"},
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        String[] foundDH = entityService.getDeviceHandlerByFingerprint(deviceInfo);
        assertNotNull(foundDH);
        assertEquals(2, foundDH.length);
        assertEquals(deviceHandlerId, foundDH[0]);
        assertEquals("GE Fan", foundDH[1]);
    }

    @Test
    public void testGetDeviceHandlerByFingerprintWithMfrProdModel2() {
        DeviceService mockDeviceService = mock(DeviceService.class);
        AutomationAppService mockAutomationAppService = mock(AutomationAppService.class);
        EventService mockEventService = mock(EventService.class);
        LocationService mockLocationService = mock(LocationService.class);
        ScheduleService mockScheduleService = mock(ScheduleService.class);
        IntegrationRegistry mockIntegrationRegistry = mock(IntegrationRegistry.class);
        EntityService entityService = new EntityServiceImpl(mockDeviceService, mockAutomationAppService, mockEventService, mockLocationService,
                mockScheduleService, mockIntegrationRegistry);

        Fingerprint fp = new Fingerprint();
        fp.setProfileId(null);
        fp.setProfileId("0104");
        fp.setInClusters("0000,0001,0003,0020,0006,0201");
        fp.setOutClusters("000A,0019");
        String deviceHandlerId = "abc123";
        DeviceHandler dh = new DeviceHandler();
        dh.setId(deviceHandlerId);
        dh.setFingerprints(Collections.singletonList(fp));
        Collection<DeviceHandler> dhCollection = new ArrayList<>();
        dhCollection.add(dh);

        when(mockDeviceService.getAllDeviceHandlers()).thenReturn(dhCollection);
        Map<String, String> deviceInfo = Stream.of(new String[][]{
                {"prod", "0001"},
                {"mfr", "003B"},
                {"model", "0469"},
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        String[] foundDH = entityService.getDeviceHandlerByFingerprint(deviceInfo);
        assertNull(foundDH);
    }
}
