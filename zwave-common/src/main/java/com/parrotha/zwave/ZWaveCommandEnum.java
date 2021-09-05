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
package com.parrotha.zwave;

import org.apache.commons.lang3.StringUtils;
import com.parrotha.internal.utils.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Note: This code is autogenerated, changes will be overwritten.
 */
public enum ZWaveCommandEnum {
    BasicSet("2001", 0x20, 0x01, "basic", "BasicSet", 2),
    BasicGet("2002", 0x20, 0x02, "basic", "BasicGet", 2),
    BasicReport("2003", 0x20, 0x03, "basic", "BasicReport", 2),
    ApplicationBusy("2201", 0x22, 0x01, "applicationstatus", "ApplicationBusy", 1),
    ApplicationRejectedRequest("2202", 0x22, 0x02, "applicationstatus", "ApplicationRejectedRequest", 1),
    ZipPacket("2302", 0x23, 0x02, "zip", "ZipPacket", 4),
    ZipKeepAlive("2303", 0x23, 0x03, "zip", "ZipKeepAlive", 4),
    SwitchBinarySet("2501", 0x25, 0x01, "switchbinary", "SwitchBinarySet", 2),
    SwitchBinaryGet("2502", 0x25, 0x02, "switchbinary", "SwitchBinaryGet", 2),
    SwitchBinaryReport("2503", 0x25, 0x03, "switchbinary", "SwitchBinaryReport", 2),
    SwitchMultilevelSet("2601", 0x26, 0x01, "switchmultilevel", "SwitchMultilevelSet", 3),
    SwitchMultilevelGet("2602", 0x26, 0x02, "switchmultilevel", "SwitchMultilevelGet", 3),
    SwitchMultilevelReport("2603", 0x26, 0x03, "switchmultilevel", "SwitchMultilevelReport", 3),
    NodeAdd("3401", 0x34, 0x01, "networkmanagementinclusion", "NodeAdd", 3),
    NodeAddStatus("3402", 0x34, 0x02, "networkmanagementinclusion", "NodeAddStatus", 3),
    NodeRemove("3403", 0x34, 0x03, "networkmanagementinclusion", "NodeRemove", 3),
    NodeRemoveStatus("3404", 0x34, 0x04, "networkmanagementinclusion", "NodeRemoveStatus", 3),
    ThermostatFanModeSet("4401", 0x44, 0x01, "thermostatfanmode", "ThermostatFanModeSet", 2),
    ThermostatFanModeGet("4402", 0x44, 0x02, "thermostatfanmode", "ThermostatFanModeGet", 2),
    ThermostatFanModeReport("4403", 0x44, 0x03, "thermostatfanmode", "ThermostatFanModeReport", 2),
    ThermostatFanModeSupportedGet("4404", 0x44, 0x04, "thermostatfanmode", "ThermostatFanModeSupportedGet", 2),
    ThermostatFanModeSupportedReport("4405", 0x44, 0x05, "thermostatfanmode", "ThermostatFanModeSupportedReport", 2),
    LearnModeSet("4D01", 0x4D, 0x01, "networkmanagementbasic", "LearnModeSet", 2),
    LearnModeSetStatus("4D02", 0x4D, 0x02, "networkmanagementbasic", "LearnModeSetStatus", 2),
    NetworkUpdateRequest("4D03", 0x4D, 0x03, "networkmanagementbasic", "NetworkUpdateRequest", 2),
    NetworkUpdateRequestStatus("4D04", 0x4D, 0x04, "networkmanagementbasic", "NetworkUpdateRequestStatus", 2),
    NodeInformationSend("4D05", 0x4D, 0x05, "networkmanagementbasic", "NodeInformationSend", 2),
    DefaultSet("4D06", 0x4D, 0x06, "networkmanagementbasic", "DefaultSet", 2),
    DefaultSetComplete("4D07", 0x4D, 0x07, "networkmanagementbasic", "DefaultSetComplete", 2),
    DSKGet("4D08", 0x4D, 0x08, "networkmanagementbasic", "DSKGet", 2),
    DSKReport("4D09", 0x4D, 0x09, "networkmanagementbasic", "DSKReport", 2),
    NodeListGet("5201", 0x52, 0x01, "networkmanagementproxy", "NodeListGet", 3),
    NodeListReport("5202", 0x52, 0x02, "networkmanagementproxy", "NodeListReport", 3),
    NodeInfoCachedGet("5203", 0x52, 0x03, "networkmanagementproxy", "NodeInfoCachedGet", 3),
    NodeInfoCachedReport("5204", 0x52, 0x04, "networkmanagementproxy", "NodeInfoCachedReport", 3),
    FailedNodeListGet("520B", 0x52, 0x0B, "networkmanagementproxy", "FailedNodeListGet", 3),
    FailedNodeListReport("520C", 0x52, 0x0C, "networkmanagementproxy", "FailedNodeListReport", 3),
    Crc16Encap("5601", 0x56, 0x01, "crc16encap", "Crc16Encap", 1),
    ZipNodeAdvertisement("5801", 0x58, 0x01, "zipnd", "ZipNodeAdvertisement", 1),
    ZipNodeSolicitation("5803", 0x58, 0x03, "zipnd", "ZipNodeSolicitation", 1),
    ZipInvNodeSolicitation("5804", 0x58, 0x04, "zipnd", "ZipInvNodeSolicitation", 1),
    UnsolicitedDestinationSet("5F08", 0x5F, 0x08, "zipgateway", "UnsolicitedDestinationSet", 1),
    UnsolicitedDestinationGet("5F09", 0x5F, 0x09, "zipgateway", "UnsolicitedDestinationGet", 1),
    UnsolicitedDestinationReport("5F0A", 0x5F, 0x0A, "zipgateway", "UnsolicitedDestinationReport", 1),
    DoorLockOperationSet("6201", 0x62, 0x01, "doorlock", "DoorLockOperationSet", 1),
    DoorLockOperationGet("6202", 0x62, 0x02, "doorlock", "DoorLockOperationGet", 1),
    DoorLockOperationReport("6203", 0x62, 0x03, "doorlock", "DoorLockOperationReport", 1),
    UserCodeSet("6301", 0x63, 0x01, "usercode", "UserCodeSet", 2),
    UserCodeGet("6302", 0x63, 0x02, "usercode", "UserCodeGet", 2),
    UserCodeReport("6303", 0x63, 0x03, "usercode", "UserCodeReport", 2),
    UsersNumberGet("6304", 0x63, 0x04, "usercode", "UsersNumberGet", 2),
    UsersNumberReport("6305", 0x63, 0x05, "usercode", "UsersNumberReport", 2),
    UserCodeCapabilitiesGet("6306", 0x63, 0x06, "usercode", "UserCodeCapabilitiesGet", 2),
    ConfigurationSet("7004", 0x70, 0x04, "configuration", "ConfigurationSet", 2),
    ConfigurationGet("7005", 0x70, 0x05, "configuration", "ConfigurationGet", 2),
    ConfigurationReport("7006", 0x70, 0x06, "configuration", "ConfigurationReport", 2),
    AlarmGet("7104", 0x71, 0x04, "alarm", "AlarmGet", 2),
    AlarmReport("7105", 0x71, 0x05, "alarm", "AlarmReport", 2),
    AlarmSet("7106", 0x71, 0x06, "alarm", "AlarmSet", 2),
    AlarmTypeSupportedGet("7107", 0x71, 0x07, "alarm", "AlarmTypeSupportedGet", 2),
    AlarmTypeSupportedReport("7108", 0x71, 0x08, "alarm", "AlarmTypeSupportedReport", 2),
    ManufacturerSpecificGet("7204", 0x72, 0x04, "manufacturerspecific", "ManufacturerSpecificGet", 2),
    ManufacturerSpecificReport("7205", 0x72, 0x05, "manufacturerspecific", "ManufacturerSpecificReport", 2),
    DeviceSpecificGet("7206", 0x72, 0x06, "manufacturerspecific", "DeviceSpecificGet", 2),
    DeviceSpecificReport("7207", 0x72, 0x07, "manufacturerspecific", "DeviceSpecificReport", 2),
    BatteryGet("8002", 0x80, 0x02, "battery", "BatteryGet", 1),
    BatteryReport("8003", 0x80, 0x03, "battery", "BatteryReport", 1),
    Hail("8201", 0x82, 0x01, "hail", "Hail", 1),
    AssociationSet("8501", 0x85, 0x01, "association", "AssociationSet", 3),
    AssociationGet("8502", 0x85, 0x02, "association", "AssociationGet", 3),
    AssociationReport("8503", 0x85, 0x03, "association", "AssociationReport", 3),
    AssociationRemove("8504", 0x85, 0x04, "association", "AssociationRemove", 3),
    AssociationGroupingsGet("8505", 0x85, 0x05, "association", "AssociationGroupingsGet", 3),
    AssociationGroupingsReport("8506", 0x85, 0x06, "association", "AssociationGroupingsReport", 3),
    AssociationSpecificGroupGet("850B", 0x85, 0x0B, "association", "AssociationSpecificGroupGet", 3),
    AssociationSpecificGroupReport("850C", 0x85, 0x0C, "association", "AssociationSpecificGroupReport", 3),
    VersionGet("8611", 0x86, 0x11, "version", "VersionGet", 3),
    VersionReport("8612", 0x86, 0x12, "version", "VersionReport", 3),
    VersionCommandClassGet("8613", 0x86, 0x13, "version", "VersionCommandClassGet", 3),
    VersionCommandClassReport("8614", 0x86, 0x14, "version", "VersionCommandClassReport", 3),
    VersionCapabilitiesGet("8615", 0x86, 0x15, "version", "VersionCapabilitiesGet", 3),
    VersionCapabilitiesReport("8616", 0x86, 0x16, "version", "VersionCapabilitiesReport", 3),
    VersionZWaveSoftwareGet("8617", 0x86, 0x17, "version", "VersionZWaveSoftwareGet", 3),
    TimeGet("8A01", 0x8A, 0x01, "time", "TimeGet", 1),
    TimeReport("8A02", 0x8A, 0x02, "time", "TimeReport", 1),
    DateGet("8A03", 0x8A, 0x03, "time", "DateGet", 1),
    DateReport("8A04", 0x8A, 0x04, "time", "DateReport", 1),
    SecurityCommandsSupportedReport("9803", 0x98, 0x03, "security", "SecurityCommandsSupportedReport", 1),
    NetworkKeyVerify("9807", 0x98, 0x07, "security", "NetworkKeyVerify", 1),
    SecurityMessageEncapsulation("9881", 0x98, 0x81, "security", "SecurityMessageEncapsulation", 1);

    private static Map<String, ZWaveCommandEnum> codeToCommandClassMapping;

    private String key;
    private int commandClass;
    private int command;
    private String packageName;
    private String className;
    private int maxVersion;

    ZWaveCommandEnum(String key, int commandClass, int command, String packageName, String className, int maxVersion) {
        this.key = key;
        this.commandClass = commandClass;
        this.command = command;
        this.packageName = packageName;
        this.className = className;
        this.maxVersion = maxVersion;
    }

    private static void initMapping() {
        codeToCommandClassMapping = new HashMap<String, ZWaveCommandEnum>();
        for (ZWaveCommandEnum s : values()) {
            codeToCommandClassMapping.put(s.key, s);
        }
    }

    public static ZWaveCommandEnum getZWaveClass(String s) {
        if (codeToCommandClassMapping == null) {
            initMapping();
        }
        return codeToCommandClassMapping.get(s);
    }

    public String getKey() {
        return key;
    }

    public int getCommandClass() {
        return commandClass;
    }

    public int getCommand() {
        return command;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public int getMaxVersion() {
        return maxVersion;
    }
}
