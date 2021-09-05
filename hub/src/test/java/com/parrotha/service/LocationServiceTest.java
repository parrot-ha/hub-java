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
package com.parrotha.service;

import com.parrotha.internal.hub.Location;
import com.parrotha.internal.hub.LocationServiceImpl;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LocationServiceTest {

    @Test
    public void testGetSunriseSunset() {
        Location location = new Location();
        location.setLatitude(new BigDecimal(36.161366897048346));
        location.setLongitude(new BigDecimal(-86.77837087608596));

        LocationServiceImpl locationService = new LocationServiceImpl(location);
        Map<String, Date> sunriseAndSunset = locationService.getSunriseAndSunset(new HashMap<>());
        Assert.assertNotNull(sunriseAndSunset);
        Assert.assertEquals(2, sunriseAndSunset.size());
    }
}
