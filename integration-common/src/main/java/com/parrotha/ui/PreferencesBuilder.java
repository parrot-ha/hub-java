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
package com.parrotha.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PreferencesBuilder {
    private List<Map<String, Object>> inputList = new ArrayList<>();
    private List<Map<String, Object>> bodyList = new ArrayList<>();

    public PreferencesBuilder withInput(String type, String name, String title, String description, List<String> options, boolean multiple,
                                        boolean required, boolean displayDuringSetup) {

        Map<String, Object> input = Stream.of(new Object[][]{
                {"description", description},
                {"multiple", multiple},
                {"title", title},
                {"required", required},
                {"name", name},
                {"type", type},
                {"options", options},
                {"displayDuringSetup", displayDuringSetup},
        }).filter(o -> o[1] != null).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1]));
        inputList.add(input);

        Map<String, Object> body = Stream.of(new Object[][]{
                {"element", "input"},
                {"description", description},
                {"multiple", multiple},
                {"title", title},
                {"required", required},
                {"name", name},
                {"type", type},
                {"options", options},
                {"displayDuringSetup", displayDuringSetup},
        }).filter(o -> o[1] != null).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1]));
        bodyList.add(body);

        return this;
    }

    public PreferencesBuilder withTextInput(String name, String title, String description, boolean required, boolean displayDuringSetup) {
        return withInput("text", name, title, description, null, false, required, displayDuringSetup);
    }

    public PreferencesBuilder withEnumInput(String name, String title, String description, List<String> options, boolean multiple, boolean required,
                                            boolean displayDuringSetup) {
        return withInput("enum", name, title, description, options, multiple, required, displayDuringSetup);
    }

    public PreferencesBuilder withBoolInput(String name, String title, String description, boolean required, boolean displayDuringSetup) {
        return withInput("bool", name, title, description, null, false, required, displayDuringSetup);
    }

    public Map<String, Object> build() {
        Map<String, Object> preferencesMap = new LinkedHashMap<>();
        Map<String, List<Map<String, Object>>> section = new LinkedHashMap<>();
        section.put("input", inputList);
        section.put("body", bodyList);
        List<Map<String, List<Map<String, Object>>>> sections = new ArrayList<>();
        sections.add(section);
        preferencesMap.put("sections", sections);
        preferencesMap.put("defaults", true);
        return preferencesMap;
    }
}
