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
package com.parrotha.internal.entity;

import org.apache.commons.lang.StringUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EntityScriptDelegateUtils {
    public static Map parseLanMessage(String stringToParse) {
        if (stringToParse == null) {
            return null;
        }
        if (StringUtils.isBlank(stringToParse)) {
            return new HashMap();
        }
        Map<String, Object> lanMessageMap = new HashMap<>();

        Map<String, String> lanMessageInterim = stringToMap(stringToParse, ",",
                ":"); //Splitter.on(',').trimResults().withKeyValueSeparator(':').split(stringToParse);

        for (String key : lanMessageInterim.keySet()) {
            if (StringUtils.isEmpty(lanMessageInterim.get(key))) {
                lanMessageMap.put(key, null);
            } else if ("headers".equals(key)) {
                // base 64 decode the headers
                String header = new String(Base64.getDecoder().decode(lanMessageInterim.get(key)));
                lanMessageMap.put("header", header);

                // TODO: is there a library we can use to do this?
                Map headerMap = stringToMap(header, "\n", ":");
                lanMessageMap.put("headers", headerMap);
            } else if ("body".equals(key)) {
                // base 64 decode the message
                lanMessageMap.put("body", new String(Base64.getDecoder().decode(lanMessageInterim.get(key))));
            } else {
                lanMessageMap.put(key, lanMessageInterim.get(key));
            }
        }
        return lanMessageMap;
    }

    public static Map stringToMap(String stringToSplit, String entrySeparator, String keyValueSeparator) {
        Map<String, Object> map = new HashMap<>();
        String[] stringToSplitArray = stringToSplit.split(entrySeparator);
        for (String mapEntryString : stringToSplitArray) {
            if (mapEntryString.contains(keyValueSeparator)) {

                String[] mapEntryStringArray = mapEntryString.split(keyValueSeparator);

                if (mapEntryStringArray.length > 1) {
                    if (StringUtils.isBlank(mapEntryStringArray[1])) {
                        map.put(mapEntryStringArray[0], null);
                    } else {
                        map.put(mapEntryStringArray[0].trim(), mapEntryStringArray[1].trim());
                    }
                } else {
                    map.put(mapEntryStringArray[0].trim(), null);
                }
            } else {
                map.put(mapEntryString.trim(), null);
            }
        }

        return map;
    }
}
