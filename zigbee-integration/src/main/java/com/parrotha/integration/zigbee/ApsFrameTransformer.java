/**
 * Copyright (c) 2021-2022 by the respective copyright holders.
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
import org.apache.commons.lang3.ArrayUtils;
import com.parrotha.internal.utils.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ApsFrameTransformer {
    private static final Logger logger = LoggerFactory.getLogger(ApsFrameTransformer.class);

    public static String transformApsFrame(final ZigBeeApsFrame incomingApsFrame) {

        if (incomingApsFrame.getProfile() == 0x0104) {
            int currentIndex = 0;

            int[] payload = incomingApsFrame.getPayload();
            int firstFrameVal = incomingApsFrame.getPayload()[currentIndex];
            int clusterSpecific = (firstFrameVal & 0x3);
            int manufacturerSpecific = ((firstFrameVal >> 2) & 1);

            String manufacturerId = "0000";
            if (manufacturerSpecific == 1) {
                manufacturerId = HexUtils.intArrayToHexString(new int[]{payload[currentIndex + 1], payload[currentIndex]});
                currentIndex += 3;
            } else {
                currentIndex++;
            }

            // the next index is the sequence number
            int sequenceNumber = payload[currentIndex++];

            String commandIdentifier = HexUtils.integerToHexString(incomingApsFrame.getPayload()[currentIndex++], 1);

            if (incomingApsFrame.getProfile() == 0x0104 && incomingApsFrame.getCluster() == 0x0500 &&
                    clusterSpecific == 1 && "00".equals(commandIdentifier)) {
                // Zone Status Change Notification (zigbee cluster library specification 8.2.2.4.1)
                // 01 00 00 00 00 00
                // first 2 bytes are zone status
                String zoneStatus = HexUtils.intArrayToHexString(new int[]{payload[currentIndex + 1], payload[currentIndex]});
                currentIndex += 2;
                String extendedStatus = HexUtils.integerToHexString(payload[currentIndex++], 1);
                if(payload.length >= currentIndex+3) {
                    String zoneID = HexUtils.integerToHexString(payload[currentIndex++], 1);
                    String delay = HexUtils.intArrayToHexString(new int[]{payload[currentIndex + 1], payload[currentIndex]});
                    return String.format("zone status 0x%s -- extended status 0x%s -- zone ID 0x%s -- delay 0x%s", zoneStatus, extendedStatus, zoneID, delay);
                }
                return String.format("zone status 0x%s -- extended status 0x%s", zoneStatus, extendedStatus);
            } else if (incomingApsFrame.getProfile() == 0x0104 && clusterSpecific == 0 && ("0A".equals(commandIdentifier) || "01".equals(commandIdentifier))) {

                String dni = String.format("%04X", incomingApsFrame.getSourceAddress());

                String size = HexUtils.integerToHexString((incomingApsFrame.getPayload().length - currentIndex) * 2, 1);

                int[] attrId = new int[]{incomingApsFrame.getPayload()[currentIndex], incomingApsFrame.getPayload()[currentIndex + 1]};
                // reverse the array for the raw message

                int[] reverseAttrId = Arrays.copyOf(attrId, attrId.length);
                ArrayUtils.reverse(reverseAttrId);

                currentIndex += 2;

                String cluster = HexUtils.integerToHexString(incomingApsFrame.getCluster(), 2);
                String endpoint = HexUtils.integerToHexString(incomingApsFrame.getSourceEndpoint(), 1);

                int status = 0;
                if ("01".equals(commandIdentifier)) {
                    // lets get the status
                    status = payload[currentIndex++];
                }
                if (status != 0) {
                    return createCatchAllMessage(incomingApsFrame, firstFrameVal, clusterSpecific, manufacturerSpecific, manufacturerId, commandIdentifier);
                }

                int encoding = incomingApsFrame.getPayload()[currentIndex++];
                int[] value = Arrays.copyOfRange(incomingApsFrame.getPayload(), currentIndex, incomingApsFrame.getPayload().length);


                return "read attr - raw: " + dni + endpoint + cluster + size + HexUtils.intArrayToHexString(attrId) +
                        HexUtils.integerToHexString(encoding, 1) + HexUtils.intArrayToHexString(value) +
                        ", dni: " + dni +
                        ", endpoint: " + String.format("%02X", incomingApsFrame.getSourceEndpoint()) +
                        ", cluster: " + cluster +
                        ", size: " + size +
                        ", attrId: " + HexUtils.intArrayToHexString(reverseAttrId) +
                        ", encoding: " + HexUtils.integerToHexString(encoding, 1) +
                        ", command: " + commandIdentifier +
                        ", value: " + HexUtils.intArrayToHexString(value);
            } else {
                return createCatchAllMessage(incomingApsFrame, firstFrameVal, clusterSpecific, manufacturerSpecific, manufacturerId, commandIdentifier);
            }
        }
        return null;
    }

    private static String createCatchAllMessage(final ZigBeeApsFrame incomingApsFrame, int firstFrameVal,
                                                int clusterSpecific, int manufacturerSpecific, String manufacturerId,
                                                String commandIdentifier) {
        String direction = HexUtils.integerToHexString((firstFrameVal >> 3) & 1, 1);

        // return a catch all
        // catchall: 0104 0006 01 01 0040 00 2A7F 00 00 0000 0B 01 0000
        // catchall: 0104        0006         01               01                    0040        00            3F21  00                   00                       0000               0B         01             0000
        //           [profileId] [clusterId] [sourceEndpoint] [destinationEndpoint] [options]   [messageType] [dni] [isClusterSpecific]  [isManufacturerSpecific]  [manufacturerId]  [command]  [direction]    [data]
        //
        // run in ST:
        //def str = "catchall: 0104 0006 01 01 0040 00 3F21 00 00 0000 0B 01 0000"
        //log.debug zigbee.parseDescriptionAsMap(str);
        //output:
        //[raw:0104 0006 01 01 0040 00 2A7F 00 00 0000 0B 01 0000, profileId:0104, clusterId:0006, sourceEndpoint:01, destinationEndpoint:01, options:0040, messageType:00, dni:3F21, isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0000, command:0B, direction:01, data:[00, 00], clusterInt:6, commandInt:11]
        //[sourceAddress=6EF8/1, destinationAddress=0000/1, profile=0104, cluster=0006, addressMode=DEVICE, radius=0, apsSecurity=false, ackRequest=false, apsCounter=88, rssi=-52, lqi=FF, payload=08 08 0B 00 00]
        return String.format("catchall: %04X %04X %02X %02X 0040 00 %04X %02X %02X %s %s %s %s",
                incomingApsFrame.getProfile(),
                incomingApsFrame.getCluster(),
                incomingApsFrame.getSourceEndpoint(),
                incomingApsFrame.getDestinationEndpoint(),
                incomingApsFrame.getSourceAddress(),
                clusterSpecific,
                manufacturerSpecific,
                manufacturerId,
                commandIdentifier,
                direction,
                HexUtils.intArrayToHexString(ArrayUtils.subarray(incomingApsFrame.getPayload(), 3, incomingApsFrame.getPayload().length)));

    }

}
