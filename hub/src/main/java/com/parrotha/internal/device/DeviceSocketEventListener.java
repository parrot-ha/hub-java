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
package com.parrotha.internal.device;

import groovy.json.JsonBuilder;
import io.javalin.websocket.WsConnectContext;
import org.apache.groovy.util.Maps;
import com.parrotha.device.Event;
import com.parrotha.internal.entity.EventListener;

public class DeviceSocketEventListener implements EventListener {
    private String deviceId;
    private WsConnectContext wsConnectContext;
    private boolean registered = false;

    public void registerCtx(String deviceId, WsConnectContext wsConnectContext) {
        this.deviceId = deviceId;
        this.wsConnectContext = wsConnectContext;
        registered = true;
    }

    public void unregisterCtx() {
        this.registered = false;
        this.deviceId = null;
        this.wsConnectContext = null;
    }

    public void eventReceived(Event event) {
        if (registered && event != null && "DEVICE".equals(event.getSource()) && event.getSourceId() != null &&
                event.getSourceId().equals(deviceId)) {
            // process event
            String eventMessage = new JsonBuilder(
                    Maps.of("name", event.getName(), "value", event.getValue(), "unit", event.getUnit())).toString();

            wsConnectContext.send(eventMessage);
        }
    }
}
