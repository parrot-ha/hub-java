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
package com.parrotha.internal.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionYamlDataStore implements ExtensionDataStore {
    private Map<String, Map> extensionSettings;

    @Override
    public List getExtensionSettings() {
        synchronized (this) {
            if (extensionSettings == null) {
                extensionSettings = loadExtensionSettings();
            }
        }

        return new ArrayList<>(extensionSettings.values());
    }

    @Override
    public String addSetting(String name, String type, String location) {
        return null;
    }

    @Override
    public boolean updateSetting(String id, String name, String type, String location) {
        return false;
    }

    @Override
    public Map getSettingById(String id) {
        return null;
    }

    private Map<String, Map> loadExtensionSettings() {
        //TODO: make this configurable from UI
        Map<String, Map> extensionLocations = new HashMap<>();
        extensionLocations.put("ada38365-1d40-44a0-8208-8395b6ecca53", Map.of("id", "ada38365-1d40-44a0-8208-8395b6ecca53",
                "name", "Zwave-JS",
                "type", "GithubRelease",
                "location", "parrot-ha/zwavejs-integration"));
        extensionLocations.put("34b4a309-9bda-4bf1-a439-b43abcdde972", Map.of("id", "34b4a309-9bda-4bf1-a439-b43abcdde972",
                "name", "Test Source Ext",
                "type", "URL",
                "location", "https://raw.githubusercontent.com/parrot-ha/testSourceExt/main/parrotExtension.yaml"));

        return extensionLocations;
    }
}
