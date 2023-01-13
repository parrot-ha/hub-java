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
package com.parrotha.integration.zigbee;

import com.zsmartsystems.zigbee.dongle.ember.EmberNcpResetProvider;
import com.zsmartsystems.zigbee.serial.ZigBeeSerialPort;
import com.zsmartsystems.zigbee.transport.ZigBeePort;

/**
 * Class to perform reset using DTR
 *
 * @author Chris Jackson
 *
 */
public class EmberNcpHardwareReset implements EmberNcpResetProvider {

    @Override
    public void emberNcpReset(ZigBeePort port) {
        ZigBeeSerialPort serialPort = (ZigBeeSerialPort) port;

        try {
            serialPort.setRts(false);
            serialPort.setDtr(false);
            Thread.sleep(50);
            serialPort.setRts(true);
            serialPort.setDtr(true);
        } catch (InterruptedException e) {
        }
    }

}