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
package com.parrotha.internal.hub;

import org.apache.commons.lang3.StringUtils;
import com.parrotha.exception.NotYetImplementedException;
import org.shredzone.commons.suncalc.SunTimes;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LocationServiceImpl implements LocationService {

    private Location location;
    private Hub hub;

    public LocationServiceImpl() {
    }

    public LocationServiceImpl(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        if (location == null) {
            loadLocation();
        }
        return location;
    }

    public Hub getHub() {
        if (hub == null) {
            loadHub();
        }
        return hub;
    }

    private void loadLocation() {
        Yaml yaml = new Yaml();
        try {
            File locationConfig = new File("config/location.yaml");
            if (locationConfig.exists()) {
                Map settings = yaml.load(new FileInputStream(locationConfig));
                String locationId = (String) settings.get("id");
                String temperatureScale = (String) settings.get("temperatureScale");
                String currentModeStr = (String) settings.get("currentMode");
                BigDecimal latitude;
                try {
                    latitude = new BigDecimal((String) settings.get("latitude"));
                } catch (NumberFormatException nfe) {
                    latitude = new BigDecimal(0.0);
                }
                BigDecimal longitude;
                try {
                    longitude = new BigDecimal((String) settings.get("longitude"));
                } catch (NumberFormatException nfe) {
                    longitude = new BigDecimal(0.0);
                }
                String name = (String) settings.get("name");
                String zipCode = (String) settings.get("zipCode");
                List<Map<String, String>> modesListMap = (List) settings.get("modes");
                List<Mode> modes = new ArrayList<>();
                for (Map<String, String> modeListMapEntry : modesListMap) {
                    Mode mode = new Mode(modeListMapEntry.get("id"), modeListMapEntry.get("name"));
                    modes.add(mode);
                }
                Mode currentMode = null;
                for (Mode mode : modes) {
                    if (mode.getName().equalsIgnoreCase(currentModeStr)) {
                        currentMode = mode;
                    }
                }
                this.location = new Location(locationId, getHub(), temperatureScale, currentMode, latitude, longitude, name, zipCode, modes);
            } else {
                createDefaultLocation();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void loadHub() {
        Yaml yaml = new Yaml();
        try {
            File hubConfig = new File("config/hub.yaml");
            if (hubConfig.exists()) {
                Map settings = yaml.load(new FileInputStream(hubConfig));
                String hubID = (String) settings.get("id");
                String name = (String) settings.get("name");
                String hardwareID = (String) settings.get("hardwareID");
                String type = (String) settings.get("type");

                this.hub = new Hub(hubID, name, type, hardwareID);
            } else {
                createDefaultHub();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void createDefaultLocation() {
        String locationId = UUID.randomUUID().toString();
        String temperatureScale = "F";
        BigDecimal latitude = new BigDecimal("40.748267");
        BigDecimal longitude = new BigDecimal("-73.985472");
        String zipCode = "10001";
        String name = "Default";
        Mode currentMode = new Mode(UUID.randomUUID().toString(), "Day");
        List<Mode> modes = new ArrayList<>();
        modes.add(currentMode);
        modes.add(new Mode(UUID.randomUUID().toString(), "Evening"));
        modes.add(new Mode(UUID.randomUUID().toString(), "Night"));
        modes.add(new Mode(UUID.randomUUID().toString(), "Away"));

        this.location = new Location(locationId, temperatureScale, currentMode, latitude, longitude, name, zipCode, modes);
        saveLocation();
    }

    private void createDefaultHub() {
        String hubId = UUID.randomUUID().toString();
        String name = getLocation().getName();
        String type = "PHYSICAL";
        String hardwareID = "UNKNOWN";

        this.hub = new Hub(hubId, name, type, hardwareID);
        saveHub();
    }

    public boolean saveLocation() {
        try {
            Yaml yaml = new Yaml();
            File deviceConfig = new File("config/location.yaml");
            FileWriter fileWriter = new FileWriter(deviceConfig);
            Map locationMap = this.location.toMap();
            yaml.dump(locationMap, fileWriter);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveHub() {
        try {
            Yaml yaml = new Yaml();
            File hubConfig = new File("config/hub.yaml");
            FileWriter fileWriter = new FileWriter(hubConfig);
            Map hubMap = this.hub.toMap();
            yaml.dump(hubMap, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Date> getSunriseAndSunset(Map<String, Object> options) {
        Double latitude = null;
        Double longitude = null;

        Object zipCode = options.get("zipCode");
        if (zipCode != null && StringUtils.isNotBlank(zipCode.toString())) {
            // TODO: use a map of zip codes to lat/long to get date time.
            throw new NotYetImplementedException();
        }
        Long sunriseOffset = HubUtils.timeOffset(options.get("sunriseOffset"));
        Long sunsetOffset = HubUtils.timeOffset(options.get("sunsetOffset"));

        if (latitude == null && longitude == null && getLocation() != null) {
            latitude = getLocation().getLatitude().doubleValue();
            longitude = getLocation().getLongitude().doubleValue();
        }

        HashMap<String, Date> sunriseSunsetMap = new HashMap<>();

        if (latitude != null && longitude != null) {
            ZonedDateTime dateTime = ZonedDateTime.now().withHour(0).withMinute(0);// date, time and timezone of calculation
            SunTimes times = SunTimes.compute()
                    .on(dateTime)   // set a date
                    .at(getLocation().getLatitude().doubleValue(), getLocation().getLongitude().doubleValue())   // set a location
                    .execute();     // get the results
            if (times.getRise() != null)
                sunriseSunsetMap.put("sunrise", new Date(times.getRise().toInstant().toEpochMilli() + sunriseOffset));
            if (times.getSet() != null)
                sunriseSunsetMap.put("sunset", new Date(times.getSet().toInstant().toEpochMilli() + sunsetOffset));
        } else {
            sunriseSunsetMap.put("sunrise", null);
            sunriseSunsetMap.put("sunset", null);
        }
        return sunriseSunsetMap;
    }

    public void calculateSunriseAndSunset() {
        ZonedDateTime dateTime = ZonedDateTime.now();// date, time and timezone of calculation
        dateTime = dateTime.withHour(0);

        if (getLocation() != null && getLocation().getLatitude() != null && getLocation().getLongitude() != null) {
            //double lat, lng = // geolocation
            SunTimes times = SunTimes.compute()
                    .on(dateTime)   // set a date
                    .at(getLocation().getLatitude().doubleValue(), getLocation().getLongitude().doubleValue())   // set a location
                    .execute();     // get the results
        }
    }
}
