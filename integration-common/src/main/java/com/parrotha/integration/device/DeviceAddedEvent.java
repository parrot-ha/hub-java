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

import java.util.Map;

public class DeviceAddedEvent extends DeviceEvent {
    private boolean userInitiatedAdd;

    public DeviceAddedEvent(String deviceNetworkId, boolean userInitiatedAdd, Map<String, String> fingerprint, Map<String, Object> data,
                            Map<String, String> parameters) {
        super(deviceNetworkId, DeviceStatusType.ADDED, Map.of("fingerprint", fingerprint, "data", data, "parameters", parameters));
        this.userInitiatedAdd = userInitiatedAdd;
    }

    public Map getFingerprint() {
        return (Map) ((Map) getEvent()).get("fingerprint");
    }

    public Map getAdditionalParameters() {
        return (Map) ((Map) getEvent()).get("parameters");
    }

    public Map getData() {
        return (Map) ((Map) getEvent()).get("data");
    }

    public boolean isUserInitiatedAdd() {
        return userInitiatedAdd;
    }
}
