/**
 * Copyright (c) 2021 by the respective copyright holders.
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
package com.parrotha.zigbee.clusters.iaszone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZoneStatusTest {

    @Test
    public void testZoneStatus() {
        ZoneStatus zoneStatus = new ZoneStatus(0x00);
        assertEquals(0, zoneStatus.getAlarm1());
        assertFalse(zoneStatus.isAlarm1Set());
        assertEquals(0, zoneStatus.getAlarm2());
        assertFalse(zoneStatus.isAlarm2Set());
        assertEquals(0, zoneStatus.getTamper());
        assertFalse(zoneStatus.isTamperSet());
        assertEquals(0, zoneStatus.getBattery());
        assertFalse(zoneStatus.isBatterySet());
        assertEquals(0, zoneStatus.getSupervisionReports());
        assertFalse(zoneStatus.isSupervisionReportsSet());
        assertEquals(0, zoneStatus.getRestoreReports());
        assertFalse(zoneStatus.isRestoreReportsSet());
        assertEquals(0, zoneStatus.getTrouble());
        assertFalse(zoneStatus.isTroubleSet());
        assertEquals(0, zoneStatus.getAc());
        assertFalse(zoneStatus.isAcSet());
        assertEquals(0, zoneStatus.getTest());
        assertFalse(zoneStatus.isTestSet());
        assertEquals(0, zoneStatus.getBatteryDefect());
        assertFalse(zoneStatus.isBatteryDefectSet());

        zoneStatus = new ZoneStatus(0x3FF);
        assertEquals(1, zoneStatus.getAlarm1());
        assertTrue(zoneStatus.isAlarm1Set());
        assertEquals(1, zoneStatus.getAlarm2());
        assertTrue(zoneStatus.isAlarm2Set());
        assertEquals(1, zoneStatus.getTamper());
        assertTrue(zoneStatus.isTamperSet());
        assertEquals(1, zoneStatus.getBattery());
        assertTrue(zoneStatus.isBatterySet());
        assertEquals(1, zoneStatus.getSupervisionReports());
        assertTrue(zoneStatus.isSupervisionReportsSet());
        assertEquals(1, zoneStatus.getRestoreReports());
        assertTrue(zoneStatus.isRestoreReportsSet());
        assertEquals(1, zoneStatus.getTrouble());
        assertTrue(zoneStatus.isTroubleSet());
        assertEquals(1, zoneStatus.getAc());
        assertTrue(zoneStatus.isAcSet());
        assertEquals(1, zoneStatus.getTest());
        assertTrue(zoneStatus.isTestSet());
        assertEquals(1, zoneStatus.getBatteryDefect());
        assertTrue(zoneStatus.isBatteryDefectSet());

        zoneStatus = new ZoneStatus(0x01);
        assertEquals(1, zoneStatus.getAlarm1());
        assertTrue(zoneStatus.isAlarm1Set());
        assertEquals(0, zoneStatus.getAlarm2());
        assertFalse(zoneStatus.isAlarm2Set());
        assertEquals(0, zoneStatus.getTamper());
        assertFalse(zoneStatus.isTamperSet());
        assertEquals(0, zoneStatus.getBattery());
        assertFalse(zoneStatus.isBatterySet());
        assertEquals(0, zoneStatus.getSupervisionReports());
        assertFalse(zoneStatus.isSupervisionReportsSet());
        assertEquals(0, zoneStatus.getRestoreReports());
        assertFalse(zoneStatus.isRestoreReportsSet());
        assertEquals(0, zoneStatus.getTrouble());
        assertFalse(zoneStatus.isTroubleSet());
        assertEquals(0, zoneStatus.getAc());
        assertFalse(zoneStatus.isAcSet());
        assertEquals(0, zoneStatus.getTest());
        assertFalse(zoneStatus.isTestSet());
        assertEquals(0, zoneStatus.getBatteryDefect());
        assertFalse(zoneStatus.isBatteryDefectSet());


        zoneStatus = new ZoneStatus(0x41);
        assertEquals(1, zoneStatus.getAlarm1());
        assertTrue(zoneStatus.isAlarm1Set());
        assertEquals(1, zoneStatus.getTrouble());
        assertTrue(zoneStatus.isTroubleSet());

    }
}
