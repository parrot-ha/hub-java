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

import com.zsmartsystems.zigbee.IeeeAddress;
import com.zsmartsystems.zigbee.ZigBeeCommand;
import com.zsmartsystems.zigbee.ZigBeeEndpointAddress;
import com.zsmartsystems.zigbee.ZigBeeNetworkManager;
import com.zsmartsystems.zigbee.zcl.ZclCommand;
import com.zsmartsystems.zigbee.zcl.ZclFieldSerializer;
import com.zsmartsystems.zigbee.zcl.clusters.general.ConfigureReportingCommand;
import com.zsmartsystems.zigbee.zcl.clusters.general.ReadAttributesCommand;
import com.zsmartsystems.zigbee.zcl.field.AttributeReportingConfigurationRecord;
import com.zsmartsystems.zigbee.zcl.protocol.ZclCommandDirection;
import com.zsmartsystems.zigbee.zcl.protocol.ZclDataType;
import com.zsmartsystems.zigbee.zdo.command.BindRequest;
import org.apache.commons.lang3.StringUtils;
import com.parrotha.internal.utils.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

public class ZigBeeMessageTransformer {
    private static final Logger logger = LoggerFactory.getLogger(ZigBeeHandler.class);

    private static int getIntegerValueForString(String valueStr) {
        if (valueStr.startsWith("0x"))
            return HexUtils.hexStringToInt(valueStr);
        else
            return Integer.parseInt(valueStr);
    }

    private static int getIntegerValueForString(String valueStr, int defaultValue) {
        if(StringUtils.isEmpty(valueStr)) return defaultValue;
        if (valueStr.startsWith("0x"))
            return HexUtils.hexStringToInt(valueStr);
        else
            return Integer.parseInt(valueStr);
    }

    public static ZigBeeCommand createCommand(String msg, ZigBeeNetworkManager networkManager) {
        if (msg.startsWith("ph cmd") || msg.startsWith("st cmd") || msg.startsWith("he cmd")) {
            if (logger.isDebugEnabled()) logger.debug("Sending cmd! " + msg);
            msg = msg.substring("ph cmd ".length());
            String[] msgParts = msg.split(" ");
            int networkAddress = getIntegerValueForString(msgParts[0].trim());
            int endpoint = getIntegerValueForString(msgParts[1].trim());
            int cluster = getIntegerValueForString(msgParts[2].trim());
            int command = getIntegerValueForString(msgParts[3].trim());
            final String payload;
            if (msg.contains("{")) {
                payload = StringUtils.deleteWhitespace(msg.substring(msg.indexOf("{") + 1, msg.indexOf("}")));
            } else {
                payload = null;
            }

            //zigBeeService.getNode(networkAddress).getEndpoint(endpoint).getInputCluster(cluster).sendCommand()
            ZclCommand zclCommand = new ZclCommand() {
                {
                    clusterId = cluster;
                    commandId = command;
                    commandDirection = ZclCommandDirection.CLIENT_TO_SERVER;
                    destinationAddress = new ZigBeeEndpointAddress(networkAddress, endpoint);
                }

                @Override
                public void serialize(ZclFieldSerializer serializer) {
                    if (payload != null) {
                        int[] payloadIntArray = com.parrotha.internal.utils.HexUtils.hexStringToIntArray(payload);
                        for (int i : payloadIntArray) {
                            serializer.serialize(i, ZclDataType.DATA_8_BIT);
                        }
                    }
                }
            };
            return zclCommand;
        } else if (msg.startsWith("ph rattr") || msg.startsWith("st rattr") || msg.startsWith("he rattr")) {
            msg = msg.substring("ph rattr ".length());
            String[] msgParts = msg.split(" ");
            int networkAddress = getIntegerValueForString(msgParts[0].trim());
            int endpoint = getIntegerValueForString(msgParts[1].trim());
            int cluster = getIntegerValueForString(msgParts[2].trim());
            int attribute = getIntegerValueForString(msgParts[3].trim());
            ReadAttributesCommand readAttributesCommand = new ReadAttributesCommand(Arrays.asList(attribute));
            readAttributesCommand.setClusterId(cluster);
            readAttributesCommand.setDestinationAddress(new ZigBeeEndpointAddress(networkAddress, endpoint));

            return readAttributesCommand;
        } else if (msg.startsWith("zdo bind")) {
            IeeeAddress destAddress = networkManager.getNode(0).getIeeeAddress();

            // do a bind message
            msg = msg.substring("zdo bind ".length());
            String[] msgParts = msg.split(" ");
            int networkAddress = HexUtils.hexStringToInt(msgParts[0].trim());
            int endpoint = HexUtils.hexStringToInt(msgParts[1].trim());
            int destEndpoint = HexUtils.hexStringToInt(msgParts[2].trim());
            int cluster = HexUtils.hexStringToInt(msgParts[3].trim());

            final BindRequest command = new BindRequest(networkManager.getNode(networkAddress).getIeeeAddress(), endpoint,
                    cluster, 3, destAddress, destEndpoint);
            command.setDestinationAddress(new ZigBeeEndpointAddress(networkAddress));
            return command;
            //getNode(networkAddress).getEndpoint(endpoint).getInputCluster(cluster).bind(destAddress, destEndpoint);
        } else if (msg.startsWith("ph cr ") || msg.startsWith("st cr ") || msg.startsWith("he cr ")) {
            msg = msg.substring("ph cr ".length());
            String[] msgParts = msg.split(" ");
            int networkAddress = HexUtils.hexStringToInt(msgParts[0].trim());
            int endpoint = HexUtils.hexStringToInt(msgParts[1].trim());
            int cluster = getIntegerValueForString(msgParts[2].trim());
            int attributeId = getIntegerValueForString(msgParts[3].trim());
            int attributeDataType = getIntegerValueForString(msgParts[4].trim());
            int minInterval = getIntegerValueForString(msgParts[5].trim());
            int maxInterval = getIntegerValueForString(msgParts[6].trim());

            final AttributeReportingConfigurationRecord record = new AttributeReportingConfigurationRecord();
            record.setDirection(0);
            record.setAttributeIdentifier(attributeId);
            record.setAttributeDataType(ZclDataType.getType(attributeDataType));
            record.setMinimumReportingInterval(minInterval);
            record.setMaximumReportingInterval(maxInterval);

            String reportableChangeString = StringUtils.deleteWhitespace(msg.substring(msg.indexOf("{") + 1, msg.indexOf("}")));
            if(StringUtils.isNotEmpty(reportableChangeString)) {
                record.setReportableChange(getIntegerValueForString(reportableChangeString));
            }

            record.setTimeoutPeriod(0);

            final ConfigureReportingCommand command = new ConfigureReportingCommand(Collections.singletonList(record));
            command.setClusterId(cluster);

            command.setDestinationAddress(new ZigBeeEndpointAddress(networkAddress, endpoint));

            //networkManager.getNode(networkAddress).getEndpoint(endpoint).getInputCluster(cluster).setReporting(attributeId, minInterval, maxInterval);

            //TODO: how to handle manufacturer specific?
            //if (isManufacturerSpecific()) {
            //    command.setManufacturerCode(getManufacturerCode());
            //} else if (attribute.isManufacturerSpecific()) {
            //    command.setManufacturerCode(attribute.getManufacturerCode());
            //}

            return command;

        } else if (msg.startsWith("send ") || msg.startsWith("raw ")) {
            logger.warn("Need to handle raw / send zigbee message: " + msg);
        } else if (msg.startsWith("ph raw ") || msg.startsWith("he raw ")) {
            if (logger.isDebugEnabled()) logger.debug("Sending raw cmd! " + msg);
            msg = msg.substring("ph raw ".length());
            String[] msgParts = msg.split(" ");
            int networkAddress = getIntegerValueForString(msgParts[0].trim());
            int sourceEndpoint = getIntegerValueForString(msgParts[1].trim());
            int destinationEndpoint = getIntegerValueForString(msgParts[2].trim());
            int cluster = getIntegerValueForString(msgParts[3].trim());

            final String rawCommand;
            if (msg.contains("{")) {
                rawCommand = StringUtils.deleteWhitespace(msg.substring(msg.indexOf("{") + 1, msg.indexOf("}")));
            } else {
                rawCommand = null;
            }

            if (rawCommand != null) {
                int[] rawCommandIntArray = com.parrotha.internal.utils.HexUtils.hexStringToIntArray(rawCommand);
                int rawCommandIndex = 0;
                int frameControl = rawCommandIntArray[rawCommandIndex];
                final Integer manufacturer;
                if ((frameControl & 0x04) == 0x04) {
                    // manufacturer specific
                    manufacturer = rawCommandIntArray[rawCommandIndex + 2] << 2 + rawCommandIntArray[rawCommandIndex + 1];
                    rawCommandIndex += 3;
                } else {
                    manufacturer = null;
                    rawCommandIndex++;
                }
                // skip next value, it is sequence number and is handled by the framework
                rawCommandIndex++;

                int command = rawCommandIntArray[rawCommandIndex];
                rawCommandIndex++;
                int[] payload = Arrays.copyOfRange(rawCommandIntArray, rawCommandIndex, rawCommandIntArray.length);

                //zigBeeService.getNode(networkAddress).getEndpoint(endpoint).getInputCluster(cluster).sendCommand()
                ZclCommand zclCommand = new ZclCommand() {
                    {
                        clusterId = cluster;
                        commandId = command;

                        // frame control
                        genericCommand = (frameControl & 0x01) == 0x00;

                        if (manufacturer != null) {
                            setManufacturerCode(manufacturer);
                        }

                        if ((frameControl & 0x08) == 0x08) {
                            commandDirection = ZclCommandDirection.SERVER_TO_CLIENT;
                        } else {
                            commandDirection = ZclCommandDirection.CLIENT_TO_SERVER;
                        }
                        disableDefaultResponse = (frameControl & 0x10) == 0x10;

                        destinationAddress = new ZigBeeEndpointAddress(networkAddress, destinationEndpoint);
                    }

                    @Override
                    public void serialize(ZclFieldSerializer serializer) {
                        if (payload.length > 0) {
                            for (int i : payload) {
                                serializer.serialize(i, ZclDataType.DATA_8_BIT);
                            }
                        }
                    }
                };
                return zclCommand;
            }
            /*
            https://community.hubitat.com/t/need-help-converting-st-zigbee-dth-to-he/13658/2:

            ST raw zigbee frame
            List cmds = ["raw 0x501 {09 01 00 04}","send 0x${device.deviceNetworkId} 1 1"]

            HE raw zigbee frame (for the same command)
            List cmds = ["he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 00 04}"]

            he raw
            0x${device.deviceNetworkId} 16 bit hex address
            1							source endpoint, always one
            1 							destination endpoint, device dependent
            0x0501 						zigbee cluster id
            {09 						frame control
	            01 						sequence, always 01
		            00 					command
			            04}				command parameter(s)
             */
        } else {
            logger.warn("Unhandled msg: " + msg);
        }
        return null;
    }
}
