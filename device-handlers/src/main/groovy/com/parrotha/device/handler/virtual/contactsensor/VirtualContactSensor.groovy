/*
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
package com.parrotha.device.handler.virtual.contactsensor

metadata {
    definition (name: "Virtual Contact Sensor", namespace: "com.parrotha.device.handler.virtual.contactsensor", author: "parrot ha") {
        capability "Contact Sensor"

        command "open"
        command "close"
    }

    tiles {}
}

def parse(String description) {
    log.debug "parse called with ${description}"
}

def open() {
    sendEvent(name: "contact", value: "open")
}

def close() {
    sendEvent(name: "contact", value: "closed")
}
