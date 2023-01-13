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
package com.parrotha.internal.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Integration implements Serializable {
    private String id;
    private Map<String, String> options;

    public Integration() {
    }

    public Integration(Map map) {
        this.id = (String) map.get("id");
        this.options = (Map<String, String>) map.get("options");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOption(String option) {
        if (options == null) {
            return null;
        }
        return options.get(option);
    }

    public void setOption(String option, String value) {
        if (options == null) {
            options = new HashMap<>();
        }
        options.put(option, value);
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public Map toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("options", options);
        return map;
    }
}
