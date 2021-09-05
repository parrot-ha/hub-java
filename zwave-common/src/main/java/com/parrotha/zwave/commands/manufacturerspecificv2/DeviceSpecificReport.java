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
package com.parrotha.zwave.commands.manufacturerspecificv2;

import com.parrotha.zwave.Command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Note: This code is autogenerated, changes will be overwritten.
 */
public class DeviceSpecificReport extends Command {
    public static final Short DEVICE_ID_DATA_FORMAT_UTF8 = 0;
    public static final Short DEVICE_ID_DATA_FORMAT_BINARY = 1;
    public static final Short DEVICE_ID_TYPE_OEM = 0;
    public static final Short DEVICE_ID_TYPE_SERIAL_NUMBER = 1;
    public static final Short DEVICE_ID_TYPE_PSEUDO_RANDOM = 2;

    public String getCMD() {
        return "7207";
    }

    private Short deviceIdType = 0;
    private Short deviceIdDataFormat = 0;
    private Short deviceIdDataLength = 0;
    private List<Short> deviceIdData;

    public Short getDeviceIdType() {
        return deviceIdType;
    }

    public void setDeviceIdType(Short deviceIdType) {
        this.deviceIdType = deviceIdType;
    }

    public Short getDeviceIdDataFormat() {
        return deviceIdDataFormat;
    }

    public void setDeviceIdDataFormat(Short deviceIdDataFormat) {
        this.deviceIdDataFormat = deviceIdDataFormat;
    }

    public Short getDeviceIdDataLength() {
        return deviceIdDataLength;
    }

    public void setDeviceIdDataLength(Short deviceIdDataLength) {
        this.deviceIdDataLength = deviceIdDataLength;
    }

    public List<Short> getDeviceIdData() {
        return deviceIdData;
    }

    public void setDeviceIdData(List<Short> deviceIdData) {
        this.deviceIdData = deviceIdData;
    }

    public List<Short> getPayload() {
        Short data0 = (short) ((deviceIdType & 7));
        Short data1 = (short) (((deviceIdDataFormat & 7) << 5) | 
                (deviceIdDataLength & 15));

        List<Short> retList = Stream.of(data0, data1).collect(Collectors.toList());
        retList.addAll(deviceIdData);
        return retList;
    }

    public void setPayload(List<Short> payload) {
        if (payload == null) return;
        if (payload.size() > 0) {
            deviceIdType = (short) (payload.get(0) & 7);
        }
        if (payload.size() > 1) {
            deviceIdDataFormat = (short) ((payload.get(1) & 224) >> 5);
            deviceIdDataLength = (short) (payload.get(1) & 15);
        }
        if (payload.size() > (deviceIdDataLength + 1)) {
            deviceIdData = payload.subList(2, (deviceIdDataLength + 1));
        }
    }

    @Override
    public String toString() {
        return "DeviceSpecificReport(" +
                "deviceIdType: " + deviceIdType +
                ", deviceIdDataFormat: " + deviceIdDataFormat +
                ", deviceIdDataLength: " + deviceIdDataLength +
                ", deviceIdData: " + deviceIdData +
                ')';
    }
}
