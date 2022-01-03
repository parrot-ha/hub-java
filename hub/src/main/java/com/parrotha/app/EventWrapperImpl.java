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

import groovy.json.JsonSlurper;
import com.parrotha.device.Event;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class EventWrapperImpl implements EventWrapper {
    private Event event;

    public EventWrapperImpl(Event event) {
        this.event = event;
    }

    /**
     * A map of any additional data on the Event.
     *
     * @return String - A JSON string representing a map of the additional data (if any) on the Event.
     */
    public String getData() {
        return event.getData();
    }

    public String getId() {
        return event.getId();
    }

    public String getName() {
        return event.getName();
    }

    public String getValue() {
        return event.getValue();
    }

    public Date getDate() {
        return event.getDate();
    }

    public String getDescriptionText() {
        return event.getDescriptionText();
    }

    public boolean isDisplayed() {
        return event.isDisplayed();
    }

    public String getDisplayName() {
        return event.getDisplayName();
    }

    public boolean isStateChange() {
        return event.isStateChange();
    }

    public String getUnit() {
        return event.getUnit();
    }

    public Date getDateValue() {
        //TODO: parse value into date
        throw new UnsupportedOperationException();
    }

    public String getDescription() {
        return event.getDescription();
    }

    public DeviceWrapper getDevice() {
        return event.getDevice();
    }

    public String getDeviceId() {
        if ("DEVICE".equals(event.getSource()))
            return event.getSourceId();
        return null;
    }

    public Double getDoubleValue() {
        return Double.valueOf(event.getValue());
    }

    public Float getFloatValue() {
        return Float.valueOf(event.getValue());
    }

    public String getHubId() {
        return event.getHubId();
    }

    public String getInstalledSmartAppId() {
        return getInstalledAutomationAppId();
    }

    // parrot hub calls them Automation Apps
    public String getInstalledAutomationAppId() {
        return event.getInstalledAutomationAppId();
    }

    public Integer getIntegerValue() {
        return Integer.valueOf(event.getValue());
    }

    public String getIsoDate() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(event.getDate());
    }

    public Object getJsonValue() {
        return new JsonSlurper().parseText(event.getValue());
    }

    public String getLinkText() {
        return getDisplayName();
    }

    public Object getLocation() {
        //TODO: implement
        throw new UnsupportedOperationException();
    }


    public String getLocationId() {
        return event.getLocationId();
    }

    public Long getLongValue() {
        return Long.valueOf(event.getValue());
    }

    public Number getNumberValue() throws ParseException {
        return NumberFormat.getInstance().parse(event.getValue());
    }

    public Number getNumericValue() throws ParseException {
        return getNumberValue();
    }

    public String getSource() {
        return event.getSource();
    }

    public String getSourceId() {
        return event.getSourceId();
    }

    public String getStringValue() {
        return event.getValue();
    }

    public Map<String, BigDecimal> getXyzValue() {
        //TODO: implement
        throw new UnsupportedOperationException();
    }

    public boolean isDigital() {
        return event.isDigital();
    }

    public boolean isPhysical() {
        return !isDigital();
    }
}
