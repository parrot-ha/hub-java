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
package com.parrotha.zigbee.zcl;

import com.parrotha.internal.utils.HexUtils;

/**
 * Based on https://docs.smartthings.com/en/latest/ref-docs/zigbee-ref.html#datatype
 * and com.zsmartsystems.zigbee.zcl.protocol.ZclDataType
 * and ZigBee Cluster Library Specification 07-5123-06 https://zigbeealliance.org/
 */
public class DataType {

    // ZigBee Cluster Library Specification Revision 6, 07-5123-06 2.6.2 Table 2-10
    public static Integer getLength(int type) {
        switch (type) {
            case NO_DATA:
            case UNKNOWN:
                return 0;
            case DATA8:
            case BOOLEAN:
            case BITMAP8:
            case UINT8:
            case INT8:
            case ENUM8:
            case FLOAT8:
                return 1;
            case DATA16:
            case BITMAP16:
            case UINT16:
            case INT16:
            case ENUM16:
            case FLOAT2:
            case CLUSTER_ID:
            case ATTRIBUTE_ID:
                return 2;
            case DATA24:
            case BITMAP24:
            case UINT24:
            case INT24:
                return 3;
            case DATA32:
            case BITMAP32:
            case UINT32:
            case INT32:
            case FLOAT4:
            case TIME_OF_DAY:
            case DATE:
            case UTCTIME:
            case BACNET_OID:
                return 4;
            case DATA40:
            case BITMAP40:
            case UINT40:
            case INT40:
                return 5;
            case DATA48:
            case BITMAP48:
            case UINT48:
            case INT48:
                return 6;
            case DATA56:
            case BITMAP56:
            case UINT56:
            case INT56:
                return 7;
            case DATA64:
            case BITMAP64:
            case UINT64:
            case INT64:
            case IEEE_ADDRESS:
                return 8;
            case SECKEY128:
                return 16;
            case STRING_OCTET:
            case STRING_CHAR:
            case STRING_LONG_OCTET:
            case STRING_LONG_CHAR:
            case ARRAY:
            case STRUCTURE:
            case SET:
            case BAG:
                return -1;
        }
        return 0;
    }

    // ZigBee Cluster Library Specification Revision 6, 07-5123-06 2.6.2 Table 2-10
    public static boolean isDiscrete(int type) {
        return ((type >= 0x08 && type <= 0x1F) ||
                (type >= 0x30 && type <= 0x31) ||
                (type >= 0x41 && type <= 0x44) ||
                (type == 0x48) ||
                (type == 0x4C) ||
                (type >= 0x50 && type <= 0x51) ||
                (type >= 0xE8 && type <= 0xEA) ||
                (type >= 0xF0 && type <= 0xF1));
    }

    //https://docs.smartthings.com/en/latest/ref-docs/zigbee-ref.html#datatype-constants
    public static final int NO_DATA = 0x00; //No Data
    public static final int DATA8 = 0x08; //8-bit data
    public static final int DATA16 = 0x09; //16-bit data
    public static final int DATA24 = 0x0a; //24-bit data
    public static final int DATA32 = 0x0b; //32-bit data
    public static final int DATA40 = 0x0c; //40-bit data
    public static final int DATA48 = 0x0d; //48-bit data
    public static final int DATA56 = 0x0e; //56-bit data
    public static final int DATA64 = 0x0f; //64-bit data
    public static final int BOOLEAN = 0x10; //Boolean
    public static final int BITMAP8 = 0x18; //8-bit bitmap
    public static final int BITMAP16 = 0x19; //16-bit bitmap
    public static final int BITMAP24 = 0x1a; //24-bit bitmap
    public static final int BITMAP32 = 0x1b; //32-bit bitmap
    public static final int BITMAP40 = 0x1c; //40-bit bitmap
    public static final int BITMAP48 = 0x1d; //48-bit bitmap
    public static final int BITMAP56 = 0x1e; //56-bit bitmap
    public static final int BITMAP64 = 0x1f; //64-bit bitmap
    public static final int UINT8 = 0x20; //Unsigned 8-bit int
    public static final int UINT16 = 0x21; //Unsigned 16-bit int
    public static final int UINT24 = 0x22; //Unsigned 24-bit int
    public static final int UINT32 = 0x23; //Unsigned 32-bit int
    public static final int UINT40 = 0x24; //Unsigned 40-bit int
    public static final int UINT48 = 0x25; //Unsigned 48-bit int
    public static final int UINT56 = 0x26; //Unsigned 56-bit int
    public static final int UINT64 = 0x27; //Unsigned 64-bit int
    public static final int INT8 = 0x28; //Signed 8-bit int
    public static final int INT16 = 0x29; //Signed 16-bit int
    public static final int INT24 = 0x2a; //Signed 24-bit int
    public static final int INT32 = 0x2b; //Signed 32-bit int
    public static final int INT40 = 0x2c; //Signed 40-bit int
    public static final int INT48 = 0x2d; //Signed 48-bit int
    public static final int INT56 = 0x2e; //Signed 56-bit int
    public static final int INT64 = 0x2f; //Signed 64-bit int
    public static final int ENUM8 = 0x30; //8-bit enumeration
    public static final int ENUM16 = 0x31; //16-bit enumeration
    public static final int FLOAT2 = 0x38; //Semi-precision
    public static final int FLOAT4 = 0x39; //Single precision
    public static final int FLOAT8 = 0x3a; //Double precision
    public static final int STRING_OCTET = 0x41; //Octet String
    public static final int STRING_CHAR = 0x42; //Character String
    public static final int STRING_LONG_OCTET = 0x43; //Long Octet String
    public static final int STRING_LONG_CHAR = 0x44; //Long Character String
    public static final int ARRAY = 0x48; //Array
    public static final int STRUCTURE = 0x4c; //Structure
    public static final int SET = 0x50; //Set
    public static final int BAG = 0x51; //Bag
    public static final int TIME_OF_DAY = 0xe0; //Time of day
    public static final int DATE = 0xe1; //Date
    public static final int UTCTIME = 0xe2; //UTCTime
    public static final int CLUSTER_ID = 0xe8; //Cluster ID
    public static final int ATTRIBUTE_ID = 0xe9; //Attribute ID
    public static final int BACNET_OID = 0xea; //BACnet OID
    public static final int IEEE_ADDRESS = 0xf0; //IEEE address
    public static final int SECKEY128 = 0xf1; //128-bit security key
    public static final int UNKNOWN = 0xff; //Unknown


    //TODO: implement these
    public static String pack(String data, int type) {
        return pack(data, type, false);
    }

    public static String pack(String data, int type, boolean littleEndian) {
        return "";
    }

    public static String pack(Long data, int type) {
        return pack(data, type, false);
    }

    public static String pack(Long data, int type, boolean littleEndian) {
        String hexString = HexUtils.longToHexString(data, getLength(type));
        if (hexString.length() > (getLength(type) * 2))
            throw new IllegalArgumentException("Data too large for data type");
        if (littleEndian) return HexUtils.reverseHexString(hexString);
        else return hexString;
    }

    public static String pack(Integer data, int type) {
        return pack(data, type, false);
    }

    //log.debug DataType.pack(100, DataType.UINT16, false)
    //6400
    public static String pack(Integer data, int type, boolean littleEndian) {
        return pack(data.longValue(), type, littleEndian);
    }
}
