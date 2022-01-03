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
package com.parrotha.internal.device;

import org.apache.commons.lang3.math.NumberUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class State implements Serializable {
    String id;
    String name;
    String value;
    String unit;
    Date date;

    public State() {
    }

    public State(Map map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.value = (String) map.get("value");
        this.unit = (String) map.get("unit");
        if (map.get("date") != null) {
            if (map.get("date") instanceof Long) {
                this.date = new Date((Long) map.get("date"));
            } else {
                this.date = new Date(NumberUtils.createLong(map.get("date").toString()));
            }
        }
    }

    public State(String id, String name, String value, String unit, Date date) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.date = date;
    }

    public String getStringValue() {
        return value;
    }

    public Date getDateValue() {
        TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(getStringValue());
        Instant i = Instant.from(ta);
        return Date.from(i);
    }

    public BigDecimal getNumberValue() {
        return new BigDecimal(getStringValue());
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

    public String getUnit() {
        return unit;
    }

    public Date getDate() {
        return date;
    }

    public Map toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("value", value);
        map.put("unit", unit);
        map.put("date", date.getTime());

        return map;
    }
}
