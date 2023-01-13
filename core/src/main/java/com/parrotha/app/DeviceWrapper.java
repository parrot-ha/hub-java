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
package com.parrotha.app;

import com.parrotha.internal.device.State;
import com.parrotha.internal.hub.Hub;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DeviceWrapper {
    String getId();

    String getIntegrationId();

    Object methodMissing(String methodName, Object arguments);

    Map getSettings();

    Object latestValue(String attributeName);

    Object currentValue(String attributeName);

    List getSupportedAttributes();

    String getDeviceNetworkId();

    void setDeviceNetworkId(String deviceNetworkId);

    String getZigbeeId();

    Integer getEndpointId();

    Map getZwaveInfo();

    Integer getZwaveHubNodeId();

    State currentState(String attributeName);

    String getLabel();

    String getDisplayName();

    String getName();

    String getTypeName();

    Hub getHub();

    Map getData();

    Object getDataValue(String key);

    void updateDataValue(String key, Object value);

    void updateSetting(String inputName, Object value);

    void updateSetting(String inputName, Map options);

    List<EventWrapper> eventsSince(Date date);

    List<EventWrapper> eventsSince(Date date, Map options);

    List<EventWrapper> eventsBetween(Date startDate, Date endDate);

    List<EventWrapper> eventsBetween(Date startDate, Date endDate, Map options);
}
