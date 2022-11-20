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

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class DeviceSetting implements Serializable {
    private String id;
    private String name;
    private String value;
    private String type;
    private boolean multiple;

    public DeviceSetting() {
    }

    public DeviceSetting(Map settingMap) {
        this.id = (String) settingMap.get("id");
        this.name = (String) settingMap.get("name");
        this.value = (String) settingMap.get("value");
        this.type = (String) settingMap.get("type");
        if (settingMap.containsKey("multiple") && settingMap.get("multiple") != null) {
            this.multiple = (boolean) settingMap.get("multiple");
        } else {
            this.multiple = false;
        }
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

    public Object getValueAsType() {
        if ("bool".equals(type)) {
            return Boolean.parseBoolean(getValue());
        } else if ("boolean".equals(type) || "email".equals(type) || "text".equals(type) || "string".equals(type) || "enum".equals(type) ||
                "time".equals(type)) {
            return getValue();
        } else if ("decimal".equals(type)) {
            return new BigDecimal(getValue());
        } else if ("number".equals(type)) {
            if (getValue() != null) {
                return Integer.valueOf(getValue());
            }
            //TODO; handle hub, icon, password, phone
        } else {
            return getValue();
        }


        return null;
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
                    LocalDateTime ldt = LocalDate.now().atTime(Integer.parseInt(timeArray[0]), Integer.parseInt(timeArray[1]));
                    String dateTime = ldt.atZone(TimeZone.getDefault().toZoneId()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
                    setValue(dateTime);
                } else if (value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}-[0-9]{2}:[0-9]{2}")) {
                    //2020-11-29T21:26:00.000-06:00
                    String[] timeArray = value.split("T")[1].split(":");
                    LocalDateTime ldt = LocalDate.now().atTime(Integer.parseInt(timeArray[0]), Integer.parseInt(timeArray[1]));
                    String dateTime = ldt.atZone(TimeZone.getDefault().toZoneId()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
                    setValue(dateTime);
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

    public Map toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("type", type);
        map.put("value", value);
        map.put("multiple", multiple);

        return map;
    }

    @Override
    public String toString() {
        return "DeviceSetting(" +
                "id: '" + id + '\'' +
                ", name: '" + name + '\'' +
                ", value: '" + value + '\'' +
                ", type: '" + type + '\'' +
                ", multiple: " + multiple +
                ')';
    }
}
