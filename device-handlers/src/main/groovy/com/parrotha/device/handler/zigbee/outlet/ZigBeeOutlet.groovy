/*
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
package com.parrotha.device.handler.zigbee.outlet

/*
Zigbee Outlet
*/
metadata {
    definition(name: "ZigBee Outlet", namespace: "com.parrotha.device.handler.zigbee.outlet", author: "parrot ha") {
        capability "Switch"
        capability "Outlet"
        capability "Refresh"
        capability "Configuration"

        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B05,FC01,FC08", outClusters: "0003,0019", manufacturer: "LEDVANCE", model: "PLUG", deviceJoinName: "SYLVANIA Outlet" //SYLVANIA SMART+ Smart Plug
    }
    preferences {}
}

def updated() {
}

void parse(String description) {
    //log.debug "description ${description}"
    if (description.startsWith("catchall")) return
    Map descMap = zigbee.parseDescriptionAsMap(description)
    //log.debug "descMap:${descMap}"

    if (descMap.cluster == "0006" && descMap.attrId == "0000") {
        String value = descMap.value == "01" ? "on" : "off"
        //log.warn "switch is ${value}"
        sendEvent(name: "switch", value: value);
    }
}

def off() {
    return zigbee.off()
}

def on() {
    return zigbee.on()
}

def refresh() {
    return ["ph raw 0x${device.deviceNetworkId} 1 0x01 0x0006 {10 00 00 00 00}"]
}

def configure() {
    return ["zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}", "delay 300", "ph cr 0x${device.deviceNetworkId} 0x01 6 0 16 1 65534 {} {}", "delay 300"]
}

