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
package com.parrotha.internal.hub;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Location {
    private String id;
    private String temperatureScale;
    private Mode currentMode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String name;
    private String zipCode;
    private List<Hub> hubs;
    List<Mode> modes;

    public Location() {
    }

    public Location(String id, Hub hub, String temperatureScale, Mode currentMode, BigDecimal latitude, BigDecimal longitude, String name, String zipCode, List<Mode> modes) {
        this.id = id;
        this.hubs = Collections.singletonList(hub);
        this.temperatureScale = temperatureScale;
        this.currentMode = currentMode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.zipCode = zipCode;
        this.modes = modes;
    }

    public Location(String id, String temperatureScale, Mode currentMode, BigDecimal latitude, BigDecimal longitude, String name, String zipCode, List<Mode> modes) {
        this.id = id;
        this.temperatureScale = temperatureScale;
        this.currentMode = currentMode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.zipCode = zipCode;
        this.modes = modes;
    }

    public Map toMap() {
        Map<String, Object> locationMap = new HashMap();
        locationMap.put("id", id);
        locationMap.put("temperatureScale", temperatureScale);
        locationMap.put("currentMode", currentMode.getName());
        locationMap.put("latitude", this.latitude.toString());
        locationMap.put("longitude", this.longitude.toString());
        locationMap.put("name", this.name);
        locationMap.put("zipCode", this.zipCode);
        List<Map<String, String>> listModes = new ArrayList<>();
        for (Mode mode : modes) {
            listModes.add(mode.toMap());
        }
        locationMap.put("modes", listModes);
        return locationMap;
    }

    public String getId() {
        return id;
    }

    public String getTemperatureScale() {
        return temperatureScale;
    }

    public void setTemperatureScale(String temperatureScale) {
        this.temperatureScale = temperatureScale;
    }

    public Boolean getContactBookEnabled() {
        return Boolean.FALSE;
    }

    public Mode getCurrentMode() {
        return currentMode;
    }

    public List<Hub> getHubs() {
        return hubs;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getMode() {
        return currentMode.getName();
    }

    public void setMode(String mode) {
        if (mode == null) return;
        for (Mode modeItem : modes) {
            if (mode.toLowerCase().equals(modeItem.getName().toLowerCase())) {
                this.currentMode = modeItem;
                return;
            }
        }
    }

    public void setModeId(String modeId) {
        if (modeId == null) return;
        for (Mode modeItem : modes) {
            if (modeId.equals(modeItem.getId())) {
                this.currentMode = modeItem;
                return;
            }
        }
    }

    public List<Mode> getModes() {
        return modes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimeZone getTimeZone() {
        //TODO: implement
        return TimeZone.getDefault();
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
