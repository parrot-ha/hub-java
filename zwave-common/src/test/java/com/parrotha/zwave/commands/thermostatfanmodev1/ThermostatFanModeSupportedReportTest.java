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
package com.parrotha.zwave.commands.thermostatfanmodev1;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ThermostatFanModeSupportedReportTest {
    @Test
    public void testSetPayload() {
        ThermostatFanModeSupportedReport thermostatFanModeSupportedReport = new ThermostatFanModeSupportedReport();
        List<Short> shortList = new ArrayList<>(Arrays.asList((short) 0x0));
        thermostatFanModeSupportedReport.setPayload(shortList);
        assertFalse(thermostatFanModeSupportedReport.getAuto());
        assertFalse(thermostatFanModeSupportedReport.getLow());
        assertFalse(thermostatFanModeSupportedReport.getAutoHigh());
        assertFalse(thermostatFanModeSupportedReport.getHigh());

        shortList = new ArrayList<>(Arrays.asList((short) 0xF));
        thermostatFanModeSupportedReport.setPayload(shortList);
        assertTrue(thermostatFanModeSupportedReport.getAuto());
        assertTrue(thermostatFanModeSupportedReport.getLow());
        assertTrue(thermostatFanModeSupportedReport.getAutoHigh());
        assertTrue(thermostatFanModeSupportedReport.getHigh());
    }

    @Test
    public void testGetPayload() {
        ThermostatFanModeSupportedReport thermostatFanModeSupportedReport = new ThermostatFanModeSupportedReport();
        thermostatFanModeSupportedReport.setAuto(true);
        assertEquals((short) 1, thermostatFanModeSupportedReport.getPayload().get(0));
        thermostatFanModeSupportedReport.setLow(true);
        assertEquals((short) 3, thermostatFanModeSupportedReport.getPayload().get(0));
        thermostatFanModeSupportedReport.setAutoHigh(true);
        assertEquals((short) 7, thermostatFanModeSupportedReport.getPayload().get(0));
        thermostatFanModeSupportedReport.setHigh(true);
        assertEquals((short) 15, thermostatFanModeSupportedReport.getPayload().get(0));
    }
}
