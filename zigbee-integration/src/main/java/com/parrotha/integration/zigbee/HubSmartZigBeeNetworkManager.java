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
package com.parrotha.integration.zigbee;

import com.zsmartsystems.zigbee.ZigBeeNetworkManager;
import com.zsmartsystems.zigbee.aps.ZigBeeApsFrame;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.structure.EmberStatus;
import com.zsmartsystems.zigbee.transport.ZigBeeTransportTransmit;
import com.parrotha.internal.utils.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParrotHubZigBeeNetworkManager extends ZigBeeNetworkManager {
    private static final Logger logger = LoggerFactory.getLogger(ParrotHubZigBeeNetworkManager.class);

    ZigBeeIntegration zigBeeIntegration;

    /**
     * Constructor which configures serial port and ZigBee network.
     *
     * @param transport the dongle providing the {@link ZigBeeTransportTransmit}
     */
    public ParrotHubZigBeeNetworkManager(ZigBeeTransportTransmit transport, ZigBeeIntegration zigBeeIntegration) {
        super(transport);
        this.zigBeeIntegration = zigBeeIntegration;
    }

    @Override
    public void receiveCommand(final ZigBeeApsFrame incomingApsFrame) {
        if (logger.isDebugEnabled()) logger.debug("Recieved APS Frame in Parrot Hub " + incomingApsFrame.toString());

        // don't transform messages from the radio
        if (incomingApsFrame.getSourceAddress() != 0) {
            try {
                String description = ApsFrameTransformer.transformApsFrame(incomingApsFrame);
                if (description != null) {
                    // send message to user code
                    zigBeeIntegration.sendDeviceMessage(HexUtils.integerToHexString(incomingApsFrame.getSourceAddress(), 2), description);
                }
            } catch (Exception e) {
                logger.warn(String.format("Caught exception while processing zigbee message: {%s}", incomingApsFrame.toString()), e);
            }
        }

        super.receiveCommand(incomingApsFrame);
    }

    public EmberStatus changeChannel(int channel) {

        //TODO: send message to all nodes that the channel is going to change.
        return ((ParrotHubZigBeeDongleEzsp) getZigBeeTransport()).setRadioChannel(channel);
    }
}
