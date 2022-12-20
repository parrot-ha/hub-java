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
package com.parrotha.device;

/*
An example event from ST:
Name	Value
archivable	true
date	2020-10-14 13:56:34.186 PM EDT (2020-10-14T17:56:34.186Z)
description	WeMo Switch device watch device status is online
deviceId	abc
deviceTypeId	def
displayed	false
eventSource	DEVICE
hubId	123
id	xyz
isStateChange	true
isVirtualHub	false
linkText	WeMo Switch
locationId	456
name	DeviceWatch-DeviceStatus
rawDescription
translatable	false
unixTime	1602695963268
value	online
viewed	false
 */

import com.parrotha.app.DeviceWrapper;
import com.parrotha.internal.app.InstalledAutomationApp;
import com.parrotha.internal.device.State;
import com.parrotha.internal.hub.Hub;
import com.parrotha.internal.hub.Location;
import com.parrotha.internal.hub.LocationService;
import groovy.json.JsonBuilder;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

//From https://docs.smartthings.com/en/latest/ref-docs/event-ref.html
public class Event {
    private String id;
    private String name;
    private String value;
    private String descriptionText;
    private boolean displayed;
    private String displayName;
    private boolean isStateChange;
    private transient DeviceWrapper deviceWrapper;
    private String unit;
    private String data;
    private Date date;
    private String source;
    private String sourceId;
    private boolean isDigital;

    private LocationService locationService;

    public Event(String id, String name, String value, String descriptionText, boolean displayed, String displayName, boolean isStateChange,
                 String unit, String data, Date date, String source, String sourceId, boolean isDigital) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.descriptionText = descriptionText;
        this.displayed = displayed;
        this.displayName = displayName;
        this.isStateChange = isStateChange;
        this.unit = unit;
        this.data = data;
        this.date = date;
        this.source = source;
        this.sourceId = sourceId;
        this.isDigital = isDigital;
        this.locationService = null;
    }

    public Event(Map properties) {
        this.id = UUID.randomUUID().toString();
        this.date = new Date();
        if (properties != null) {
            if (properties.get("name") != null) {
                this.name = properties.get("name").toString();
            }
            if (properties.get("value") != null) {
                this.value = properties.get("value").toString();
            }
        }
    }

    public Event(Map properties, DeviceWrapper deviceWrapper, LocationService locationService) {
        this.locationService = locationService;
        this.deviceWrapper = deviceWrapper;
        if (deviceWrapper != null) {
            this.sourceId = deviceWrapper.getId();
        }
        this.id = UUID.randomUUID().toString();
        this.date = new Date();
        this.displayName = deviceWrapper.getDisplayName();
        if (properties != null) {
            if (properties.get("name") != null) {
                this.name = properties.get("name").toString();
            }
            if (properties.get("value") != null) {
                this.value = properties.get("value").toString();
            }
            if (properties.get("source") != null) {
                this.source = properties.get("source").toString();
            } else {
                this.source = "DEVICE";
            }

            Object dataObj = properties.get("data");
            if (dataObj != null && dataObj instanceof Map) {
                data = new JsonBuilder(dataObj).toString();
            }

            if (!properties.containsKey("isStateChange")) {
                // populate is state change
                State currentState = deviceWrapper.currentState(this.name);
                if (currentState == null) {
                    isStateChange = true;
                } else if (value != null) {
                    isStateChange = !value.equals(currentState.getValue());
                } else {
                    isStateChange = false;
                }
            } else {
                isStateChange = (boolean) properties.get("isStateChange");
            }
        }
    }

    public Event(Map properties, InstalledAutomationApp installedAutomationApp, LocationService locationService) {
        this.locationService = locationService;
        if (installedAutomationApp != null) {
            this.sourceId = installedAutomationApp.getId();
        }
        this.id = UUID.randomUUID().toString();
        this.date = new Date();
        if (properties != null) {
            if (properties.get("name") != null) {
                this.name = properties.get("name").toString();
            }
            if (properties.get("value") != null) {
                this.value = properties.get("value").toString();
            }
            if (properties.get("source") != null) {
                this.source = properties.get("source").toString();
            } else {
                this.source = "IAA";
            }

            Object dataObj = properties.get("data");
            if (dataObj != null && dataObj instanceof Map) {
                data = new JsonBuilder(dataObj).toString();
            }

            if (!properties.containsKey("isStateChange")) {
                isStateChange = true;
            } else {
                isStateChange = (boolean) properties.get("isStateChange");
            }
        }
    }

    public Event(Map properties, Hub Hub, LocationService locationService) {
        this(properties, Hub != null ? Hub.getId() : null, "HUB", locationService);
    }

    public Event(Map properties, Location location, LocationService locationService) {
        this(properties, location != null ? location.getId() : null, "LOCATION", locationService);
    }

    public Event(Map properties, String sourceId, String source, LocationService locationService) {
        this.locationService = locationService;
        this.sourceId = sourceId;
        this.id = UUID.randomUUID().toString();
        this.date = new Date();
        if (properties != null) {
            if (properties.get("name") != null) {
                this.name = properties.get("name").toString();
            }
            if (properties.get("value") != null) {
                this.value = properties.get("value").toString();
            }
            if (properties.get("source") != null) {
                this.source = properties.get("source").toString();
            } else {
                this.source = source;
            }
            if (properties.get("description") != null) {
                this.descriptionText = properties.get("description").toString();
            }

            Object dataObj = properties.get("data");
            if (dataObj != null && dataObj instanceof Map) {
                data = new JsonBuilder(dataObj).toString();
            }

            if (!properties.containsKey("isStateChange")) {
                isStateChange = true;
            } else {
                isStateChange = (boolean) properties.get("isStateChange");
            }
        }
    }

    @Override
    public String toString() {
        //TODO: build rest of event
        return "Event(name: " + name + " value: " + value + ")";
    }

    /**
     * A map of any additional data on the Event.
     *
     * @return String - A JSON string representing a map of the additional data (if any) on the Event.
     */
    public String getData() {
        return data;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Date getDate() {
        return date;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isStateChange() {
        return isStateChange;
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() {
        return descriptionText;
    }

    public DeviceWrapper getDevice() {
        return deviceWrapper;
    }

    public String getHubId() {
        return locationService.getHub().getId();
    }

    // parrot ha calls them Automation Apps
    public String getInstalledAutomationAppId() {
        //TODO: implement
        throw new UnsupportedOperationException();
    }

    public String getLocationId() {
        return locationService.getLocation().getId();
    }

    public String getSource() {
        return source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public boolean isDigital() {
        return isDigital;
    }
}
