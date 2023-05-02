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
package com.parrotha.internal.app;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurperClassic;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.beans.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class InstalledAutomationAppSetting implements Serializable, Cloneable {

    private String id;
    private String name;
    private String value;
    private String type;
    private boolean multiple;

    public InstalledAutomationAppSetting() {
    }

    public InstalledAutomationAppSetting(Map map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.value = (String) map.get("value");
        this.type = (String) map.get("type");
        this.multiple = map.get("multiple") != null ? (Boolean) map.get("multiple") : false;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public void processValueTypeAndMultiple(Object valueObject, String type, boolean multiple) {
        this.multiple = multiple;
        //TODO: if type changes, log a warning message.
        this.type = type;
        if (type != null) {
            if ("time".equals(getType())) {
                String value = null;
                if (valueObject != null) {
                    value = valueObject.toString();
                }
                if (StringUtils.isEmpty(value)) {
                    setValue(null);
                } else if (value.matches("[0-9]{2}:[0-9]{2}")) {
                    // format string as ISO
                    String[] timeArray = value.split(":");
                    LocalDateTime ldt = LocalDate.now()
                            .atTime(Integer.parseInt(timeArray[0]), Integer.parseInt(timeArray[1]));
                    String dateTime = ldt.atZone(TimeZone.getDefault().toZoneId())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
                    setValue(dateTime);
                } else if (value
                        .matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}-[0-9]{2}:[0-9]{2}")) {
                    //2020-11-29T21:26:00.000-06:00
                    String[] timeArray = value.split("T")[1].split(":");
                    LocalDateTime ldt = LocalDate.now()
                            .atTime(Integer.parseInt(timeArray[0]), Integer.parseInt(timeArray[1]));
                    String dateTime = ldt.atZone(TimeZone.getDefault().toZoneId())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
                    setValue(dateTime);
                }
            } else if (type.startsWith("capability") && isMultiple()) {
                if (valueObject != null) {
                    if (valueObject instanceof List) {
                        setValue(new JsonBuilder(valueObject).toString());
                    } else {
                        setValue(valueObject.toString());
                    }
                } else {
                    setValue(null);
                }
            } else if (type.equals("enum") && isMultiple()) {
                if (valueObject != null) {
                    if (valueObject instanceof List) {
                        setValue(new JsonBuilder(valueObject).toString());
                    } else {
                        setValue(valueObject.toString());
                    }
                } else {
                    setValue(null);
                }
            } else {
                if (valueObject != null) {
                    setValue(valueObject.toString());
                } else {
                    setValue(null);
                }
            }
        }
    }

    @Transient
    public Object getValueAsType() {
        if (getValue() != null) {
            if ("bool".equals(type)) {
                return Boolean.parseBoolean(getValue());
            } else if ("boolean".equals(type) || "email".equals(type) || "text".equals(type) || "time".equals(type) ||
                    "password".equals(type)) {
                return getValue();
            } else if ("decimal".equals(type)) {
                return new BigDecimal(getValue());
            } else if ("number".equals(type)) {
                return NumberUtils.createInteger(getValue());
            } else if ("enum".equals(type)) {
                if (isMultiple()) {
                    if (StringUtils.isNotBlank(getValue())) {
                        if(getValue().startsWith("[") || getValue().startsWith("{")) {
                            return new JsonSlurperClassic().parseText(getValue());
                        } else {
                            return new ArrayList<>(List.of(getValue()));
                        }
                    } else {
                        return new ArrayList<>();
                    }
                }
                return getValue();
            } else if (type != null && type.startsWith("capability")) {
                // returns the value as an array if multiple is true
                if (isMultiple()) {
                    if (StringUtils.isNotBlank(getValue())) {
                        return new JsonSlurperClassic().parseText(getValue());
                    } else {
                        return new ArrayList<>();
                    }
                }
                return getValue();
            }
        }
        //TODO; handle hub, icon, phone

        return null;
    }

    public Map<String, Object> toMap(boolean includeValueAsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("type", type);
        if (includeValueAsType) {
            map.put("value", getValueAsType());
        } else {
            map.put("value", value);
        }
        map.put("multiple", multiple);

        return map;
    }

    @Override
    public String toString() {
        return "InstalledAutomationAppSetting(" +
                "id: '" + id + '\'' +
                ", name: '" + name + '\'' +
                ", value: '" + value + '\'' +
                ", type: '" + type + '\'' +
                ", multiple: " + multiple +
                ')';
    }
}

