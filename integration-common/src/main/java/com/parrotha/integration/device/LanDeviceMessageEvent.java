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
package com.parrotha.integration.device;

public class LanDeviceMessageEvent extends DeviceMessageEvent {
    public LanDeviceMessageEvent(String deviceNetworkId, String message) {
        super(deviceNetworkId, message);
    }

    public LanDeviceMessageEvent(String macAddress, String remoteAddress, int remotePort, String message) {
        super(macAddress, message);
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    private String remoteAddress;
    private int remotePort;


    public String getRemoteAddress() {
        return remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getMacAddress() {
        return getDeviceNetworkId();
    }
}
