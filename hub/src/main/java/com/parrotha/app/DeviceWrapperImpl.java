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
package com.parrotha.app;

import com.parrotha.internal.device.Attribute;
import com.parrotha.internal.device.Device;
import com.parrotha.internal.device.DeviceService;
import com.parrotha.internal.device.State;
import com.parrotha.internal.entity.EntityService;
import com.parrotha.internal.hub.Hub;
import com.parrotha.internal.hub.LocationService;
import com.parrotha.internal.utils.HexUtils;
import com.parrotha.internal.utils.ObjectUtils;
import groovy.json.JsonSlurper;
import groovy.lang.GroovyObjectSupport;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class DeviceWrapperImpl extends GroovyObjectSupport implements DeviceWrapper {
    public DeviceWrapperImpl(Device device, DeviceService deviceService, EntityService entityService, LocationService locationService) {
        this.device = device;
        this.deviceService = deviceService;
        this.entityService = entityService;
        this.locationService = locationService;
    }

    public String getId() {
        return this.device.getId();
    }

    public String getIntegrationId() {
        return this.device.getIntegration() != null ? this.device.getIntegration().getId() : null;
    }

    public Object methodMissing(String methodName, Object arguments) {
        // TODO: check to see if the device supports this method. (capabilities or commands)
        if (this.device != null) {
            this.entityService.runDeviceMethod(this.device.getId(), methodName, arguments);
        } else {
            throw new RuntimeException("Device not found");
        }

        return null;
    }

    public Object propertyMissing(String name) {
        if (name == null) {
            return null;
        }
        if (name.startsWith("current")) {
            String attributeName = name.substring("current".length()).toLowerCase();
            return latestValue(attributeName);
        }
        return null;
    }

    public Map getSettings() {
        return device.getSettingsMap();
    }

    public String getDeviceNetworkId() {
        return device.getDeviceNetworkId();
    }

    @Override
    public void setDeviceNetworkId(String deviceNetworkId) {
        if (deviceService == null) {
            throw new IllegalStateException("DeviceNetworkId is currently not updatable");
        }
        device.setDeviceNetworkId(deviceNetworkId);
        deviceService.saveDevice(device);
    }

    public String getZigbeeId() {
        if (device.getIntegration() != null) {
            return (String) device.getIntegration().getOption("zigbeeId");
        }
        return null;
    }

    public Map getZwaveInfo() {
        if (device.getIntegration() != null) {
            String zwaveInfoStr = device.getIntegration().getOption("zwaveInfo");
            if (zwaveInfoStr != null) {
                Object jsonInfo = new JsonSlurper().parseText(zwaveInfoStr);
                if (jsonInfo instanceof Map) {
                    return (Map) jsonInfo;
                }
            }
        }
        return null;
    }

    public Integer getZwaveHubNodeId() {
        if (device.getIntegration() != null) {
            String zwaveHubNodeIdStr = device.getIntegration().getOption("zwaveHubNodeId");
            if (zwaveHubNodeIdStr != null) {
                return NumberUtils.createInteger(zwaveHubNodeIdStr);
            }
        }
        return null;
    }

    public Integer getEndpointId() {
        if (device.getIntegration() != null) {
            Object endpointIdObj = device.getIntegration().getOption("endpointId");
            if (endpointIdObj != null) {
                if (endpointIdObj instanceof String) {
                    return HexUtils.hexStringToInt((String) endpointIdObj);
                } else if (endpointIdObj instanceof Integer) {
                    return (Integer) endpointIdObj;
                }
            }
        }
        return null;
    }

    public State currentState(String attributeName) {
        if (device.getCurrentStates() == null) {
            return null;
        }
        return device.getCurrentStates().get(attributeName);
    }

    public Object latestValue(String attributeName) {
        if (attributeName == null) {
            return null;
        }

        State state = currentState(attributeName);
        if (state == null) {
            return null;
        }
        Attribute attribute = deviceService.getAttributeForDeviceHandler(this.device.getDeviceHandlerId(), attributeName);
        if (attribute == null) {
            return null;
        }
        String dataType = attribute.getDataType();
        if ("STRING".equalsIgnoreCase(dataType) || "ENUM".equalsIgnoreCase(dataType)) {
            return state.getStringValue();
        } else if ("NUMBER".equalsIgnoreCase(dataType)) {
            return state.getNumberValue();
        } else if ("DATE".equalsIgnoreCase(dataType)) {
            return state.getDateValue();
        }
        return null;
    }

    @Override
    public Object currentValue(String attributeName) {
        return latestValue(attributeName);
    }

    @Override
    public List getSupportedAttributes() {
        return null;
    }

    public String getLabel() {
        return device.getLabel();
    }

    public String getDisplayName() {
        if (device.getLabel() == null) {
            return device.getName();
        }
        return device.getLabel();
    }

    public String getName() {
        return device.getName();
    }

    public String getTypeName() {
        return deviceService.getDeviceHandler(this.device.getDeviceHandlerId()).getName();
    }

    public Hub getHub() {
        if (hub == null) {
            hub = locationService.getHub();
        }
        return hub;
    }

    @Override
    public Map getData() {
        return device.getData();
    }

    @Override
    public Object getDataValue(String key) {
        return device.getDataValue(key);
    }

    @Override
    public void updateDataValue(String key, Object value) {
        device.getData().put(key, value);
        deviceService.saveDeviceData(device.getId(), device.getData());
    }

    @Override
    public void updateSetting(String inputName, Object value) {
        deviceService.updateDeviceSetting(this.device.getId(), inputName, value);
    }

    @Override
    public void updateSetting(String inputName, Map options) {
        deviceService.updateDeviceSetting(this.device.getId(), inputName, (String) options.get("type"), options.get("value"));
    }

    @Override
    public List<EventWrapper> eventsSince(Date date) {
        return eventsSince(date, null);
    }

    @Override
    public List<EventWrapper> eventsSince(Date date, Map options) {
        int maxEvents = 10;
        if (options != null) {
            maxEvents = ObjectUtils.objectToInt(options.get("max"));
        }
        return entityService.eventsSince("DEVICE", device.getId(), date, maxEvents);
    }

    @Override
    public List<EventWrapper> eventsBetween(Date startDate, Date endDate) {
        return eventsBetween(startDate, endDate, null);
    }

    @Override
    public List<EventWrapper> eventsBetween(Date startDate, Date endDate, Map options) {
        int maxEvents = 10;
        if (options != null) {
            maxEvents = ObjectUtils.objectToInt(options.get("max"));
        }
        return entityService.eventsBetween("DEVICE", device.getId(), startDate, endDate, maxEvents);
    }

    @Override
    public String toString() {
        return device.getDisplayName();
    }

    private Device device;
    private DeviceService deviceService;
    private EntityService entityService;
    private LocationService locationService;
    private Hub hub;
}
