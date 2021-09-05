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
package com.parrotha.app;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public interface EventWrapper {
    public String getData();

    public String getId();

    public String getName();

    public String getValue();

    public Date getDate();

    public String getDescriptionText();

    public boolean isDisplayed();

    public String getDisplayName();

    public boolean isStateChange();

    public String getUnit();

    public Date getDateValue();

    public String getDescription();

    public DeviceWrapper getDevice();

    public String getDeviceId();

    public Double getDoubleValue();

    public Float getFloatValue();

    public String getHubId();

    public String getInstalledSmartAppId();

    // parrot ha calls them Automation Apps
    public String getInstalledAutomationAppId();

    public Integer getIntegerValue();

    public String getIsoDate();

    public Object getJsonValue();

    public String getLinkText();

    public Object getLocation();

    public String getLocationId();

    public Long getLongValue();

    public Number getNumberValue() throws ParseException;

    public Number getNumericValue() throws ParseException;

    public String getSource();

    public String getSourceId();

    public String getStringValue();

    public Map<String, BigDecimal> getXyzValue();

    public boolean isDigital();

    public boolean isPhysical();
}
