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

import com.zsmartsystems.zigbee.ZigBeeChannel;
import com.zsmartsystems.zigbee.dongle.ember.EmberSerialProtocol;
import com.zsmartsystems.zigbee.dongle.ember.ZigBeeDongleEzsp;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.command.EzspSetRadioChannelRequest;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.command.EzspSetRadioChannelResponse;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.structure.EmberStatus;
import com.zsmartsystems.zigbee.dongle.ember.internal.EzspProtocolHandler;
import com.zsmartsystems.zigbee.dongle.ember.internal.transaction.EzspSingleResponseTransaction;
import com.zsmartsystems.zigbee.dongle.ember.internal.transaction.EzspTransaction;
import com.zsmartsystems.zigbee.transport.ZigBeePort;

public class ParrotHubZigBeeDongleEzsp extends ZigBeeDongleEzsp {

    public ParrotHubZigBeeDongleEzsp(ZigBeePort serialPort) {
        super(serialPort);
    }

    public ParrotHubZigBeeDongleEzsp(ZigBeePort serialPort, EmberSerialProtocol protocol) {
        super(serialPort, protocol);
    }

    public EmberStatus setRadioChannel(int channel) {
        EzspProtocolHandler protocolHandler = super.getProtocolHandler();
        EzspSetRadioChannelRequest request = new EzspSetRadioChannelRequest();
        request.setChannel(channel);

        EzspTransaction transaction = protocolHandler.sendEzspTransaction(
                new EzspSingleResponseTransaction(request, EzspSetRadioChannelResponse.class));
        EzspSetRadioChannelResponse response = (EzspSetRadioChannelResponse) transaction.getResponse();

        // Update the channel settings so that the zigbee ui displays correctly
        if(response.getStatus() == EmberStatus.EMBER_SUCCESS) {
            setZigBeeChannel(ZigBeeChannel.create(channel));
        }

        return response.getStatus();
    }
}
