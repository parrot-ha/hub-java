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
import com.parrotha.internal.utils.HexUtils;
import com.parrotha.internal.utils.ObjectUtils;
import com.parrotha.zigbee.clusters.iaszone.ZoneStatus;
import com.parrotha.zigbee.zcl.DataType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Based on https://docs.smartthings.com/en/latest/ref-docs/zigbee-ref.html#zigbee-reference
 */
public class ZigBeeImpl implements ZigBee {

    private DeviceWrapper device;

    public ZigBeeImpl(DeviceWrapper device) {
        this.device = device;
    }

    public static final int DEFAULT_DELAY = 2000;

    public static final int IAS_ZONE_CLUSTER = 0x0500;
    public static final int ONOFF_CLUSTER = 0x0006;
    public static final int POWER_CONFIGURATION_CLUSTER = 0x0001;

    public static final int ATTRIBUTE_IAS_ZONE_STATUS = 0x0002;

    public static ZoneStatus parseZoneStatus(String description) {
        // example: zone status 0x0000 -- extended status 0x00
        String[] zoneStatusArray = StringUtils.split(description, "--");
        for (String zoneStatus : zoneStatusArray) {
            if (zoneStatus.trim().startsWith("zone status ")) {
                return new ZoneStatus(HexUtils.hexStringToInt(zoneStatus.trim().substring("zone status ".length())));
            }
        }
        return null;
    }

    // from existing ST device handlers:
    //zigbee.BASIC_CLUSTER
    //zigbee.COLOR_CONTROL_CLUSTER
    //zigbee.ELECTRICAL_MEASUREMENT_CLUSTER
    //zigbee.LEVEL_CONTROL_CLUSTER
    //zigbee.RELATIVE_HUMIDITY_CLUSTER
    //zigbee.SIMPLE_METERING_CLUSTER
    //zigbee.TEMPERATURE_MEASUREMENT_CLUSTER

    //read attr - raw: E6A20104020A0000299907, dni: E6A2, endpoint: 01, cluster: 0402, size: 0A, attrId: 0000, encoding: 29, command: 0A, value: 9907
    // [raw:E6A20104020A0000299907, dni:E6A2, endpoint:01, cluster:0402, size:0A, attrId:0000, encoding:29, command:0A, value:0799, clusterInt:1026, attrInt:0]
    //read attr - raw: E6A20104050A0000215E14, dni: E6A2, endpoint: 01, cluster: 0405, size: 0A, attrId: 0000, encoding: 21, command: 0A, value: 5E14
    //"catchall: 0104 0500 01 01 0040 00 ACFE 01 00 0000 00 01 010000000000"
    //[raw:0104 0500 01 01 0040 00 ACFE 01 00 0000 00 01 010000000000, profileId:0104, clusterId:0500, sourceEndpoint:01, destinationEndpoint:01, options:0040, messageType:00, dni:ACFE, isClusterSpecific:true, isManufacturerSpecific:false, manufacturerId:0000, command:00, direction:01, data:[01, 00, 00, 00, 00, 00], clusterInt:1280, commandInt:0]
    public static Map<String, Object> parseDescriptionAsMap(String description) {
        if (description == null) {
            return null;
        }
        if (description.startsWith("read attr - ")) {
            HashMap<String, Object> descriptionMap = new HashMap<>();
            String[] descriptionArray = description.substring("read attr -".length()).split(",");

            for (String arrayItem : descriptionArray) {
                String[] keyValue = arrayItem.split(":");
                descriptionMap.put(keyValue[0].trim(), keyValue[1].trim());
            }

            // need to flip the value for some encodings:
            // 29 = Signed 16 bit integer
            // 21 = Unsigned 16-bit integer
            //TODO: figure out all encodings that need to be flipped
            if ("21".equals(descriptionMap.get("encoding")) ||
                    "29".equals(descriptionMap.get("encoding"))) {
                descriptionMap.put("value", HexUtils.reverseHexString((String) descriptionMap.get("value")));
            }

            descriptionMap.put("clusterInt", Integer.valueOf((String) descriptionMap.get("cluster"), 16));
            descriptionMap.put("attrInt", Integer.valueOf((String) descriptionMap.get("attrId"), 16));

            return descriptionMap;
        } else if (description.startsWith("catchall: ")) {
            HashMap<String, Object> descriptionMap = new HashMap<>();
            String rawDescription = description.substring("catchall: ".length());
            String[] descriptionArray = rawDescription.split(" ");
            descriptionMap.put("raw", rawDescription);
            descriptionMap.put("profileId", descriptionArray[0].trim());
            descriptionMap.put("clusterId", descriptionArray[1].trim());
            descriptionMap.put("sourceEndpoint", descriptionArray[2].trim());
            descriptionMap.put("destinationEndpoint", descriptionArray[3].trim());
            descriptionMap.put("options", descriptionArray[4].trim());
            descriptionMap.put("messageType", descriptionArray[5].trim());
            descriptionMap.put("dni", descriptionArray[6].trim());
            descriptionMap.put("isClusterSpecific", descriptionArray[7].trim().equals("01"));
            descriptionMap.put("isManufacturerSpecific", descriptionArray[8].trim().equals("01"));
            descriptionMap.put("manufacturerId", descriptionArray[9].trim());
            descriptionMap.put("command", descriptionArray[10].trim());
            descriptionMap.put("direction", descriptionArray[11].trim());
            if (descriptionArray.length > 12) {
                List<String> dataList = Arrays.asList(descriptionArray[12].trim().split("(?<=\\G.{2})"));
                descriptionMap.put("data", dataList);
            } else {
                descriptionMap.put("data", new ArrayList<String>());
            }
            descriptionMap.put("clusterInt", HexUtils.hexStringToInt(descriptionArray[1].trim()));
            descriptionMap.put("commandInt", HexUtils.hexStringToInt(descriptionArray[10].trim()));

            return descriptionMap;
        } else {
            return null;
        }
    }

    //https://docs.smartthings.com/en/latest/ref-docs/zigbee-ref.html#zigbee-getevent
    // example to parse:
    // read attr - raw: 3F9A0A00080A000020FE, dni: 3F9A, endpoint: 0A, cluster: 0008, size: 0A, attrId: 0000, encoding: 20, command: 01, value: FE
    // [name:level, value:100]
    // read attr - raw: 3F9A0A00060A00001001, dni: 3F9A, endpoint: 0A, cluster: 0006, size: 0A, attrId: 0000, encoding: 10, command: 01, value: 01
    // [name:switch, value:on]
    public static Map<String, String> getEvent(String description) {
        Map event = new HashMap<>();
        if (StringUtils.isEmpty(description)) {
            return event;
        }

        if (description.startsWith("read attr - ")) {
            Map parsedDescription = parseDescriptionAsMap(description);

            if ((Integer) parsedDescription.get("clusterInt") == 8 &&
                    (Integer) parsedDescription.get("attrInt") == 0 &&
                    HexUtils.hexStringToInt((String) parsedDescription.get("encoding")) == 0x20) {
                int value = (int) Math.round(HexUtils.hexStringToInt((String) parsedDescription.get("value")) / 2.55);
                event.put("name", "level");
                event.put("value", value);
            } else if ((Integer) parsedDescription.get("clusterInt") == 6 &&
                    (Integer) parsedDescription.get("attrInt") == 0 &&
                    HexUtils.hexStringToInt((String) parsedDescription.get("encoding")) == 0x10) {
                int value = HexUtils.hexStringToInt((String) parsedDescription.get("value"));
                if (value == 0) {
                    event.put("name", "switch");
                    event.put("value", "off");
                } else if (value == 1) {
                    event.put("name", "switch");
                    event.put("value", "on");
                }
            }
        }

        return event;
    }

    public List<String> command(Integer cluster, Integer command) {
        return command(cluster, command, null, null, DEFAULT_DELAY);
    }

    List command(Integer cluster, Integer command, Map additionalParams, int delay, String... payload) {
        if (payload != null && payload.length > 0) {
            return command(cluster, command, String.join("", payload), additionalParams, delay);
        } else {
            return command(cluster, command, null, additionalParams, delay);
        }
    }

    // zigbee.command(0x0300, 0x06, "01", "02", "03")
    // [st cmd 0xFC6E 0x01 0x0300 0x06 {010203}, delay 2000]
    public List<String> command(Integer cluster, Integer command, String... payload) {
        if (payload != null && payload.length > 0) {
            return command(cluster, command, String.join("", payload), null);
        } else {
            return command(cluster, command, null, null);
        }
    }

    public List<String> command(Integer cluster, Integer command, String payload, Map<String, Object> additionalParams) {
        return command(cluster, command, payload, additionalParams, DEFAULT_DELAY);
    }

    // zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: 0x02])
    // [st cmd 0xFC6E 0x02 0x0006 0x00 {}, delay 2000]
    // zigbee.command(6, 4, "0102", [mfgCode:"1234"])
    // [raw 0x0006 { 05D204FF040102 }, delay 200, send 0xFC6E 0x01 0x01, delay 2000]
    public List<String> command(Integer cluster, Integer command, String payload, Map<String, Object> additionalParams, int delay) {
        ArrayList<String> arrayList = new ArrayList<>();
        Integer endpointId = device.getEndpointId();
        if (additionalParams != null && additionalParams.containsKey("destEndpoint")) {
            Object destEndpointObject = additionalParams.get("destEndpoint");
            if (destEndpointObject instanceof Integer) {
                endpointId = (Integer) destEndpointObject;
            } else {
                endpointId = NumberUtils.createInteger(destEndpointObject.toString());
            }
        }

        int mfgCode = getMfgCode(additionalParams);
        if (mfgCode > -1) {
            arrayList.add(String.format("ph cmd 0x%s 0x%02X 0x%04X 0x%02X {%s} {%04X}", device.getDeviceNetworkId(), endpointId, cluster, command,
                    payload != null ? payload : "", mfgCode));
        } else if (StringUtils.isNotEmpty(payload)) {
            arrayList.add(
                    String.format("ph cmd 0x%s 0x%02X 0x%04X 0x%02X {%s}", device.getDeviceNetworkId(), endpointId, cluster, command, payload));
        } else {
            arrayList.add(String.format("ph cmd 0x%s 0x%02X 0x%04X 0x%02X {}", device.getDeviceNetworkId(), endpointId, cluster, command));
        }
        arrayList.add("delay " + DEFAULT_DELAY);
        return arrayList;
    }

    public List<String> on() {
        return on(DEFAULT_DELAY);
    }

    public List<String> on(Object delay) {
        return on(ObjectUtils.objectToInt(delay));
    }

    private List<String> on(int delay) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(String.format("ph cmd 0x%s 0x%02X 6 1 {}", device.getDeviceNetworkId(), device.getEndpointId()));
        if (delay > 0) {
            arrayList.add("delay " + delay);
        }
        return arrayList;
    }


    public List<String> off() {
        return off(DEFAULT_DELAY);
    }

    public List<String> off(Object delay) {
        return off(ObjectUtils.objectToInt(delay));
    }

    private List<String> off(int delay) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(String.format("ph cmd 0x%s 0x%02X 6 0 {}", device.getDeviceNetworkId(), device.getEndpointId()));
        if (delay > 0) {
            arrayList.add("delay " + delay);
        }
        return arrayList;
    }

    //[st rattr 0xFC6E 0x01 0x0006 0x0000, delay 2000]
    public List<String> onOffRefresh() {
        return onOffRefresh(DEFAULT_DELAY);
    }

    public List<String> onOffRefresh(Object delay) {
        return onOffRefresh(ObjectUtils.objectToInt(delay));
    }

    private List<String> onOffRefresh(int delay) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(String.format("ph rattr 0x%s 0x%02X 0x0006 0x0000", device.getDeviceNetworkId(), device.getEndpointId()));
        if (delay > 0) {
            arrayList.add("delay " + delay);
        }
        return arrayList;
    }

    // [zdo bind 0xFC6E 0x01 0x01 0x0006 {000DFF0055DDFFAA} {}, delay 2000, st cr 0xFC6E 0x01 0x0006 0x0000 0x10 0x0000 0x0258 {}, delay 2000]
    // onOffConfig(0,300)
    //[zdo bind 0xFC6E 0x01 0x01 0x0006 {000D6F00055D8FA6} {}, delay 2000, st cr 0xFC6E 0x01 0x0006 0x0000 0x10 0x0000 0x012C {}, delay 2000]
    // onOffConfig(5,100)
    //[zdo bind 0xFC6E 0x01 0x01 0x0006 {000D6F00055D8FA6} {}, delay 2000, st cr 0xFC6E 0x01 0x0006 0x0000 0x10 0x0005 0x0064 {}, delay 2000]
    //onOffConfig(10)
    //[zdo bind 0xFC6E 0x01 0x01 0x0006 {000D6F00055D8FA6} {}, delay 2000, st cr 0xFC6E 0x01 0x0006 0x0000 0x10 0x000A 0x0258 {}, delay 2000]
    public List<String> onOffConfig() {
        return onOffConfig(0, 600, DEFAULT_DELAY);
    }

    public List<String> onOffConfig(Object minReportTime) {
        return onOffConfig(ObjectUtils.objectToInt(minReportTime), 600, DEFAULT_DELAY);
    }

    public List<String> onOffConfig(Object minReportTime, Object maxReportTime) {
        return onOffConfig(ObjectUtils.objectToInt(minReportTime), ObjectUtils.objectToInt(maxReportTime), DEFAULT_DELAY);
    }

    public List<String> onOffConfig(Object minReportTime, Object maxReportTime, Object delay) {
        return onOffConfig(ObjectUtils.objectToInt(minReportTime), ObjectUtils.objectToInt(maxReportTime), ObjectUtils.objectToInt(delay));
    }

    private List<String> onOffConfig(int minReportTime, int maxReportTime, int delay) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(
                String.format("zdo bind 0x%s 0x%02X 0x01 0x0006 {%s} {}", device.getDeviceNetworkId(), device.getEndpointId(), device.getZigbeeId()));
        if (delay > 0) {
            arrayList.add("delay " + delay);
        }
        arrayList.add(String.format("ph cr 0x%s 0x%02X 0x0006 0x0000 0x10 0x%04X 0x%04X {}", device.getDeviceNetworkId(), device.getEndpointId(),
                minReportTime, maxReportTime));
        if (delay > 0) {
            arrayList.add("delay " + delay);
        }
        return arrayList;
    }

    public List<String> levelConfig() {
        return levelConfig(1, 3600, 1, DEFAULT_DELAY);
    }

    public List<String> levelConfig(Object minReportTime) {
        return levelConfig(ObjectUtils.objectToInt(minReportTime), 3600, 1, DEFAULT_DELAY);
    }

    public List<String> levelConfig(Object minReportTime, Object maxReportTime) {
        return levelConfig(ObjectUtils.objectToInt(minReportTime), ObjectUtils.objectToInt(maxReportTime), 1, DEFAULT_DELAY);
    }

    public List<String> levelConfig(Object minReportTime, Object maxReportTime, Object reportableChange) {
        return levelConfig(ObjectUtils.objectToInt(minReportTime), ObjectUtils.objectToInt(maxReportTime), ObjectUtils.objectToInt(reportableChange),
                DEFAULT_DELAY);
    }

    // zigbee.levelConfig():
    // [zdo bind 0xFC6E 0x01 0x01 0x0008 {000D6F00055D8FA6} {}, delay 2000, st cr 0xFC6E 0x01 0x0008 0x0000 0x20 0x0001 0x0E10 {01}, delay 2000]
    //levelConfig(), levelConfig(java.lang.Object), levelConfig(java.lang.Object, java.lang.Object), levelConfig(java.lang.Object, java.lang.Object, java.lang.Object)
    //zigbee.levelConfig("ABC", "CDF","XYZ")
    //[zdo bind 0xFC6E 0x01 0x01 0x0008 {000D6F00055D8FA6} {}, delay 2000, st cr 0xFC6E 0x01 0x0008 0x0000 0x20 0x0ABC 0x0CDF {XYZ}, delay 2000]
    private List<String> levelConfig(int minReportTime, int maxReportTime, int reportableChange, int delay) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(
                String.format("zdo bind 0x%s 0x%02X 0x01 0x0008 {%s} {}", device.getDeviceNetworkId(), device.getEndpointId(), device.getZigbeeId()));
        if (delay > 0) {
            arrayList.add("delay " + delay);
        }
        if (reportableChange < 1) {
            reportableChange = 1;
        }
        arrayList.add(String.format("ph cr 0x%s 0x%02X 0x0008 0x0000 0x20 0x%04X 0x%04X {%s}", device.getDeviceNetworkId(), device.getEndpointId(),
                minReportTime, maxReportTime, DataType.pack(reportableChange, 0x20)));

        if (delay > 0) {
            arrayList.add("delay " + delay);
        }

        return arrayList;
    }

    //[st rattr 0xFC6E 0x01 0x0008 0x0000, delay 2000]
    public List<String> levelRefresh() {
        return levelRefresh(DEFAULT_DELAY);
    }

    public List<String> levelRefresh(Object delay) {
        return levelRefresh(ObjectUtils.objectToInt(delay));
    }

    private List<String> levelRefresh(int delay) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(String.format("ph rattr 0x%s 0x%02X 0x0008 0x0000", device.getDeviceNetworkId(), device.getEndpointId()));
        if (delay > 0) {
            arrayList.add("delay " + delay);
        }
        return arrayList;
    }

    public List<String> setLevel(Object level) {
        return setLevel(ObjectUtils.objectToInt(level), 0xffff, DEFAULT_DELAY);
    }

    public List<String> setLevel(Object level, Object rate) {
        if (rate == null) {
            return setLevel(ObjectUtils.objectToInt(level), 0xffff, DEFAULT_DELAY);
        } else {
            return setLevel(ObjectUtils.objectToInt(level), ObjectUtils.objectToInt(rate), DEFAULT_DELAY);
        }
    }

    private List<String> setLevel(int level, int rate, int delay) {
        ArrayList<String> arrayList = new ArrayList<>();
        // level is 0 - 254
        if (level > 100) {
            level = 100;
        } else if (level < 0) {
            level = 0;
        }
        level = (int) Math.round((level / 100.0) * 254.0);
        if (rate < 0) {
            rate = 0;
        }
        if (rate > 100 && rate != 0xffff) {
            rate = 100;
        }

        arrayList.add(String.format("ph cmd 0x%s 0x%02X 0x0008 0x04 {%02X %s}", device.getDeviceNetworkId(), device.getEndpointId(), level,
                DataType.pack(rate, DataType.UINT16, true)));
        if (delay > 0) {
            arrayList.add("delay " + delay);
        }
        return arrayList;
    }


    public List<String> readAttribute(Object cluster, Object attributeId) {
        return readAttribute(ObjectUtils.objectToInt(cluster),
                ObjectUtils.objectToInt(attributeId),
                null,
                DEFAULT_DELAY);
    }

    public List<String> readAttribute(Object cluster, Object attributeId, Map additionalParams) {
        return readAttribute(ObjectUtils.objectToInt(cluster),
                ObjectUtils.objectToInt(attributeId),
                additionalParams,
                DEFAULT_DELAY);
    }

    public List<String> readAttribute(Object cluster, Object attributeId, Map additionalParams, Object delay) {
        return readAttribute(ObjectUtils.objectToInt(cluster),
                ObjectUtils.objectToInt(attributeId),
                additionalParams,
                ObjectUtils.objectToInt(delay));
    }

    private List<String> readAttribute(int cluster, int attributeId, Map additionalParams, int delay) {
        ArrayList<String> arrayList = new ArrayList<>();

        int destEndpoint = device.getEndpointId();
        if (additionalParams != null && additionalParams.containsKey("destEndpoint")) {
            destEndpoint = ObjectUtils.objectToInt(additionalParams.get("destEndpoint"));
        }

        int mfgCode = getMfgCode(additionalParams);
        if (mfgCode > -1) {
            arrayList.add(String.format("ph rattr 0x%s 0x%02X 0x%04X 0x%04X {%04X}", device.getDeviceNetworkId(), destEndpoint, cluster, attributeId,
                    mfgCode));
        } else {
            arrayList.add(String.format("ph rattr 0x%s 0x%02X 0x%04X 0x%04X", device.getDeviceNetworkId(), destEndpoint, cluster, attributeId));
        }

        if (delay > 0) {
            arrayList.add("delay " + delay);
        }

        return arrayList;
    }

    public List<String> writeAttribute(Integer cluster, Integer attributeId, Integer dataType, Object value) {
        return writeAttribute(cluster, attributeId, dataType, value, null);
    }

    public List<String> writeAttribute(Integer cluster, Integer attributeId, Integer dataType, Object value, Map additionalParams) {
        return writeAttribute(cluster, attributeId, dataType, value, additionalParams, DEFAULT_DELAY);
    }

    //writeAttribute(0x05A3, 0x0015, 0x21, 0xffff, [mfgCode: '0x1234'])
    //ST: [zcl mfg-code 0x1234, delay 200, zcl global write 0x05A3 0x0015 0x21 {FFFF}, delay 200, send 0xFC6E 0x01 0x01, delay 2000]
    // the above message structure is defined in the document Application Framework Reference: For EmberZNet 4.7.2
    //zcl mfg-code [mfgSpecificId:2]
    //zcl global write [cluster:2] [attributeId:2] [type:4]
    //send [id:2] [src-endpoint:1] [dst-endpoint:1]
    //
    //HE: [he wattr 0xFC6E 0x01 0x05A3 0x0015 0x21 {FFFF} {1234}, delay 2000]
    // HE has decided to put all the commands into a single message, we should follow this same structure
    //
    public List<String> writeAttribute(Integer cluster, Integer attributeId, Integer dataType, Object value, Map additionalParams, int delay) {
        ArrayList<String> arrayList = new ArrayList<>();

        int destEndpoint = device.getEndpointId();
        if (additionalParams != null && additionalParams.containsKey("destEndpoint")) {
            destEndpoint = ObjectUtils.objectToInt(additionalParams.get("destEndpoint"));
        }
        int mfgCode = getMfgCode(additionalParams);

        // TODO: should this use DataType.pack() for the value instead?
        String stringValue;
        if (value instanceof Number) {
            stringValue = HexUtils.integerToHexString(((Number) value).intValue(), 1);
        } else {
            stringValue = value.toString();
        }

        if (mfgCode > -1) {
            arrayList.add(String.format("ph wattr 0x%s 0x%02X 0x%04X 0x%04X 0x%02X {%s} {%04X}", device.getDeviceNetworkId(), destEndpoint, cluster,
                    attributeId, dataType, stringValue, mfgCode));
        } else {
            arrayList.add(
                    String.format("ph wattr 0x%s 0x%02X 0x%04X 0x%04X 0x%02X {%s}", device.getDeviceNetworkId(), destEndpoint, cluster, attributeId,
                            dataType, stringValue));
        }

        if (delay > 0) {
            arrayList.add("delay " + delay);
        }

        return arrayList;
    }

    private int getMfgCode(Map additionalParams) {
        if (additionalParams != null && additionalParams.containsKey("mfgCode")) {
            return ObjectUtils.objectToInt(additionalParams.get("mfgCode"));
        }
        return -1;
    }

    //TODO: implement additional methods listed here: https://docs.smartthings.com/en/latest/ref-docs/zigbee-ref.html
}
