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
package com.parrotha.internal.hub;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility methods that are used in multiple hub code locations
 */
public class HubUtils {
    private static final Logger logger = LoggerFactory.getLogger(HubUtils.class);

    public static Long timeOffset(Object object) {
        if (object == null) return 0L;
        if (object instanceof Number) return timeOffset((Number)object);
        else return timeOffset(object.toString());
    }

    public static Long timeOffset(Number minutes) {
        if (minutes == null) return 0L;
        return minutes.longValue() * 60 * 1000;
    }

    public static Long timeOffset(String hoursAndMinutesString) {
        if (StringUtils.isBlank(hoursAndMinutesString)) return 0L;
        hoursAndMinutesString = hoursAndMinutesString.trim();
        if (hoursAndMinutesString.matches("[-]{0,1}[0-9]{1,2}:[0-9]{1,2}")) {
            String[] hoursAndMinutesArray = hoursAndMinutesString.split(":");
            long hoursInMillis = Long.parseLong(hoursAndMinutesArray[0])* 60 * 60 * 1000;
            long minutesInMillis = Long.parseLong(hoursAndMinutesArray[1]) * 60 * 1000;

            if (hoursAndMinutesString.startsWith("-")) {
                // we have a negative number
                return hoursInMillis - minutesInMillis;
            } else {
                return hoursInMillis + minutesInMillis;
            }
        }
        return 0L;
    }

    private static String hubVersion = null;
    public static String getHubVersion() {
        if(hubVersion == null) {
            ClassLoader loader = HubUtils.class.getClassLoader();
            Properties props = new Properties();
            try (InputStream resourceStream = loader.getResourceAsStream("parrot-hub-version.properties")) {
                if(resourceStream != null) {
                    props.load(resourceStream);
                    hubVersion = props.getProperty("version");
                }
            } catch (IOException e) {
                logger.warn("Unable to load parrot-hub-version.properties file", e);
            }
            if(hubVersion == null) {
                hubVersion = "DEVELOPMENT";
            }
        }
        return hubVersion;
    }
}
