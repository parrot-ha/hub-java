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
package com.parrotha.internal.device;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DevicePreferencesDelegate {

    void metadata(Closure closure) {
        closure.run();
    }

    void tiles(Closure closure) {
        // do nothing
    }

    void tiles(Map options, Closure closure) {
        // do nothing
    }

    void simulator(Closure closure) {
    }

    void definition(Map map, Closure closure) {
    }

    public Map<String, Object> preferences;
    private List<Map<String, Object>> input;
    private List<Map<String, Object>> body;

    void preferences(Closure closure) {
        body = new ArrayList<>();
        input = new ArrayList<>();

        closure.run();

        Map<String, List> section = new HashMap<>();
        section.put("input", input);
        section.put("body", body);

        List<Map> sections = new ArrayList<>();
        sections.add(section);

        preferences = new HashMap<>();
        preferences.put("sections", sections);
    }

    public void section(Map<String, Object> options, Closure closure) {
        // TODO: handle sections in ui, for now just ignore them and add all inputs to preferences by running closure
        if (closure != null) {
            closure.run();
        }
    }

    public void section(Map<String, Object> options, String sectionTitle, Closure closure) {
        if (options == null) {
            options = new HashMap<>();
        }
        if (sectionTitle != null) {
            options.put("title", sectionTitle);
        }
        section(options, closure);
    }

    public void section(String sectionTitle, Closure closure) {
        section(null, sectionTitle, closure);
    }

    public void section(Closure closure) {
        section(null, null, closure);
    }

    public void input(Map<String, Object> params, String name, String type) {
        HashMap<String, Object> tempInput = new HashMap<>(params);
        tempInput.put("name", name);
        tempInput.put("type", type);
        input.add(tempInput);
        body.add(tempInput);
    }

    void input(Map params) {
        HashMap<String, Object> tempInput = new HashMap<>(params);
        input.add(tempInput);
        body.add(tempInput);
    }
}
