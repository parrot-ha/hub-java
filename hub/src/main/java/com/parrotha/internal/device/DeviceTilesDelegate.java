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

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceTilesDelegate {
    Map tiles;

    public Map getTiles() {
        return tiles;
    }

    public DeviceTilesDelegate(Map options) {
        tiles = new HashMap();
        tiles.put("definitions", new ArrayList());
        tiles.put("main", new ArrayList());
        tiles.put("details", new ArrayList());

        if (options != null)
            tiles.putAll(options);
    }

    private Map tempTile;

    void standardTile(Map options, String name, String attribute, Closure closure) {
        tempTile = new HashMap();
        tempTile.put("type", "standard");
        tempTile.put("name", name);
        tempTile.put("attribute", attribute);
        tempTile.put("inactiveLabel", true);
        tempTile.put("canChangeBackground", false);
        tempTile.put("states", new ArrayList());
        if (options != null)
            tempTile.putAll(options);
        if (closure != null) {
            closure.setDelegate(this);
            closure.run();
        }

        ((List) tiles.get("definitions")).add(tempTile);
    }

    void standardTile(String name, String attribute, Closure closure) {
        standardTile(null, name, attribute, closure);
    }

    void multiAttributeTile(Map options, Closure closure) {
        tempTile = new HashMap();
        tempTile.put("type", "multi." + options.remove("type"));
        tempTile.put("attribute", "multi");
        tempTile.put("inactiveLabel", true);
        tempTile.put("canChangeBackground", false);
        tempTile.put("states", new ArrayList());
        if (options != null)
            tempTile.putAll(options);

        if (closure != null) {
            closure.setDelegate(this);
            closure.setResolveStrategy(Closure.DELEGATE_ONLY);
            closure.run();
        }

        ((List) tiles.get("definitions")).add(tempTile);
    }

    void tileAttribute(Map options, Closure closure) {
        tempTile.put("attributes", new ArrayList());
        if (closure != null) {
            closure.setDelegate(this);
            closure.run();
        }
    }

    void tileAttribute(Map options, String name, Closure closure) {
        if (closure != null) {
            closure.setDelegate(this);
            closure.run();
        }
    }

    void attributeState(Map options, String name) {
    }

    public void main(String mainTileName) {
    }

    public void main(List mainTileList) {
    }

    void details(List list) {
    }

    void state(Map options, String name) {
    }
}
