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

import com.parrotha.internal.BaseApiHandler;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import io.javalin.Javalin;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LocationApiHandler extends BaseApiHandler {
    private LocationService locationService;

    public LocationApiHandler(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void setupApi(Javalin app) {
        app.put("/api/location", ctx -> {
            boolean locationSaved = false;
            String body = ctx.body();

            if (StringUtils.isNotBlank(body)) {
                Object jsonBodyObj = new JsonSlurper().parseText(body);

                if (jsonBodyObj instanceof Map) {
                    //TODO: validate these values

                    Map<String, Object> jsonBodyMap = (Map<String, Object>) jsonBodyObj;
                    Location location = locationService.getLocation();
                    location.setName((String) jsonBodyMap.get("name"));
                    location.setTemperatureScale((String) jsonBodyMap.get("temperatureScale"));
                    location.setZipCode((String) jsonBodyMap.get("zipCode"));
                    location.setLatitude((BigDecimal) jsonBodyMap.get("latitude"));
                    location.setLongitude((BigDecimal) jsonBodyMap.get("longitude"));
                    String modeId = (String) ((Map) jsonBodyMap.get("currentMode")).get("id");
                    location.setModeId(modeId);
                    locationSaved = locationService.saveLocation();
                }
            }

            Map<String, Object> model = new HashMap<>();
            model.put("success", locationSaved);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        app.get("/api/location", ctx -> {
            Map<String, Object> model = new HashMap<>();
            Location location = locationService.getLocation();
            model.put("id", location.getId());
            model.put("name", location.getName());
            model.put("latitude", location.getLatitude());
            model.put("longitude", location.getLongitude());
            model.put("zipCode", location.getZipCode());
            model.put("timeZone", location.getTimeZone().getID());
            Map<String, Date> sunriseSunset = locationService.getSunriseAndSunset(new HashMap<>());
            model.put("sunrise", SimpleDateFormat.getTimeInstance().format(sunriseSunset.get("sunrise")));
            model.put("sunset", SimpleDateFormat.getTimeInstance().format(sunriseSunset.get("sunset")));
            model.put("temperatureScale", location.getTemperatureScale());
            model.put("currentMode", location.getCurrentMode());
            model.put("modes", location.getModes());

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });

        app.get("/api/hub", ctx -> {
            Map<String, Object> model = new HashMap<>();
            Hub hub = locationService.getHub();
            model.put("id", hub.getId());
            model.put("name", hub.getName());
            model.put("version", HubUtils.getHubVersion());
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(model).toString());
        });
    }
}
