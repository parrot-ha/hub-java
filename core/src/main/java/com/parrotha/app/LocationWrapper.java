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
package com.parrotha.app;

import com.parrotha.internal.hub.Hub;
import com.parrotha.internal.hub.Location;
import com.parrotha.internal.hub.Mode;

import java.util.List;
import java.util.TimeZone;

public class LocationWrapper {
    private Location location;

    public LocationWrapper(Location location) {
        this.location = location;
    }

    public TimeZone getTimeZone() {
        return location.getTimeZone();
    }

    public String getId() {
        return location.getId();
    }

    public String getZipCode() {
        return location.getZipCode();
    }

    public String getName() {
        return location.getName();
    }

    public String getTemperatureScale() {
        return location.getTemperatureScale();
    }

    public List<Hub> getHubs() {
        return location.getHubs();
    }

    // TODO: should be similar to ST: physicalgraph.app.ModeWrapper instead of Mode
    public List<Mode> getModes() {
        return location.getModes();
    }

    public String getMode() {
        return location.getMode();
    }

    //TODO: what is this? what does it do?
    public Object getHelloHome() {
        return null;
    }

    @Override
    public String toString() {
        return location.getName();
    }
}
