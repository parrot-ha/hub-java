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

//https://docs.smartthings.com/en/latest/ref-docs/zigbee-ref.html#zonestatus
// 0x41 = 0000 0000 0100 0001  (Trouble and Alarm1
public class ZoneStatus {
    private int zonestatus;

    public ZoneStatus(int zonestatus) {
        this.zonestatus = zonestatus;
    }

    public int getAlarm1() {
        return zonestatus & 0x01;
    }

    public boolean isAlarm1Set() {
        return getAlarm1() == 1;
    }

    public int getAlarm2() {
        return (zonestatus & 0x02) >> 1;
    }

    public boolean isAlarm2Set() {
        return getAlarm2() == 1;
    }

    public int getTamper() {
        return (zonestatus & 0x04) >> 2;
    }

    public boolean isTamperSet() {
        return getTamper() == 1;
    }

    public int getBattery() {
        return (zonestatus & 0x08) >> 3;
    }

    public boolean isBatterySet() {
        return getBattery() == 1;
    }

    public int getSupervisionReports() {
        return (zonestatus & 0x10) >> 4;
    }

    public boolean isSupervisionReportsSet() {
        return getSupervisionReports() == 1;
    }

    public int getRestoreReports() {
        return (zonestatus & 0x20) >> 5;
    }

    public boolean isRestoreReportsSet() {
        return getRestoreReports() == 1;
    }

    public int getTrouble() {
        return (zonestatus & 0x40) >> 6;
    }

    public boolean isTroubleSet() {
        return getTrouble() == 1;
    }

    public int getAc() {
        return (zonestatus & 0x80) >> 7;
    }

    public boolean isAcSet() {
        return getAc() == 1;
    }

    public int getTest() {
        return (zonestatus & 0x100) >> 8;
    }

    public boolean isTestSet() {
        return getTest() == 1;
    }

    public int getBatteryDefect() {
        return (zonestatus & 0x200) >> 9;
    }

    public boolean isBatteryDefectSet() {
        return getBatteryDefect() == 1;
    }
}
