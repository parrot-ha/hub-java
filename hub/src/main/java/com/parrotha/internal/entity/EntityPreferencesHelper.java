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
package com.parrotha.internal.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EntityPreferencesHelper {
    //[description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor]
    //[element:input, description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor]
    //input title: "No settings"
    //input:[[description:Tap to set, multiple:false, title:No settings, required:true]],
    //body:[[element:input, description:Tap to set, multiple:false, title:No settings, required:true]]
    //input type: "enum"
    //input:[[description:Tap to set, multiple:false, title:Which?, required:true, type:enum]],
    //body:[[element:input, description:Tap to set, multiple:false, title:Which?, required:true, type:enum]]
    //input type: "decimal"
    //input:[[description:Tap to set, multiple:false, title:Number, required:true, type:decimal]], body:[[element:input, description:Tap to set, multiple:false, title:Number, required:true, type:decimal]]
    public static LinkedHashMap<String, Object> input(Map<String, Object> params) {
        // create a standard input with default values
        LinkedHashMap<String, Object> tempInput = new LinkedHashMap<>();
        tempInput.put("description", "Tap to set");
        tempInput.put("title", "Which?");
        tempInput.put("multiple", false);
        tempInput.put("required", true);

        tempInput.putAll(params);

        return tempInput;
    }

    public static Map<String, Object> createStandardPage() {
        Map<String, Object> tempPage = new HashMap<>();

        tempPage.put("name", null);
        tempPage.put("nextPage", null);
        tempPage.put("previousPage", null);
        tempPage.put("content", null);
        tempPage.put("install", false);
        tempPage.put("uninstall", false);
        tempPage.put("refreshInterval", -1);
        tempPage.put("sections", new ArrayList());
        tempPage.put("popToAncestor", null);
        tempPage.put("onUpdate", null);

        return tempPage;
    }

    public static LinkedHashMap<String, Object> createLabel(Map<String, Object> params) {
        LinkedHashMap<String, Object> tempLabel = new LinkedHashMap<>();
        tempLabel.put("name", "label");
        tempLabel.put("title", "Add a name");
        tempLabel.put("description", "Tap to set");
        tempLabel.put("element", "label");
        tempLabel.put("type", "text");
        tempLabel.put("required", true);
        tempLabel.putAll(params);
        return tempLabel;
    }
}
