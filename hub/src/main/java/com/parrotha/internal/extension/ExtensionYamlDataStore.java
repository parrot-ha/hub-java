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
package com.parrotha.internal.extension;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExtensionYamlDataStore implements ExtensionDataStore {
    private Map<String, Map> extensionLocations;

    private final static String EXTENSION_CONFIG_FILE = "config/extensionLocations.yaml";

    @Override
    public Map<String, Map> getExtensionLocations() {
        if (extensionLocations == null) {
            synchronized (this) {
                // check again once we are inside the synchronized block in case another thread has initialized the extension locations.
                if (extensionLocations == null) {
                    extensionLocations = loadExtensionLocations();
                }
            }
        }

        return extensionLocations;
    }

    @Override
    public String addLocation(String name, String type, String location) {
        String id = UUID.randomUUID().toString();
        getExtensionLocations().put(id, new HashMap<>(Map.of("id", id,
                "name", name,
                "type", type,
                "location", location)));
        return saveExtensionLocations() ? id : null;
    }

    @Override
    public boolean updateLocation(String id, String name, String type, String location) {
        Map extensionLocation = getExtensionLocations().get(id);
        if (extensionLocation != null) {
            extensionLocation.put("name", name);
            extensionLocation.put("type", type);
            extensionLocation.put("location", location);
        }
        return saveExtensionLocations();
    }

    @Override
    public boolean deleteLocation(String id) {
        this.extensionLocations.remove(id);
        return saveExtensionLocations();
    }

    @Override
    public Map getLocationById(String id) {
        return getExtensionLocations().get(id);
    }

    private boolean saveExtensionLocations() {
        if (extensionLocations != null) {
            synchronized (this) {
                try {
                    Yaml yaml = new Yaml();
                    File extensionLocationsFile = new File(EXTENSION_CONFIG_FILE);
                    FileWriter fileWriter = new FileWriter(extensionLocationsFile);
                    yaml.dump(new ArrayList<>(extensionLocations.values()), fileWriter);
                    fileWriter.close();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private Map<String, Map> loadExtensionLocations() {
        Map<String, Map> extensionLocations = new HashMap<>();
        File extensionLocationsFile = new File(EXTENSION_CONFIG_FILE);
        if (extensionLocationsFile.exists()) {
            try {
                Yaml yaml = new Yaml();
                List<Map> listObj = yaml.load(new FileInputStream(extensionLocationsFile));
                if (listObj != null) {
                    for (Map extensionLocation : listObj) {
                        extensionLocations.put((String) extensionLocation.get("id"), extensionLocation);
                    }
                }
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        } else {
            extensionLocations.put("ada38365-1d40-44a0-8208-8395b6ecca53", new HashMap<>(Map.of("id", "ada38365-1d40-44a0-8208-8395b6ecca53",
                    "name", "Zwave-JS",
                    "type", "GithubRelease",
                    "location", "parrot-ha/zwavejs-integration")));
        }

        return extensionLocations;
    }
}
