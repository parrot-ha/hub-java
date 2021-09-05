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
package com.parrotha.api;

import groovy.json.JsonSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web Service Request from the cloud endpoint or local endpoint for Automation Apps
 */
public class Request {
    private static final Logger logger = LoggerFactory.getLogger(Request.class);

    private final String body;
    private final String method;
    private Object JSON;
    private final Map<String, List<String>> headers;

    public Request() {
        this.body = null;
        this.method = null;
        this.headers = null;
    }

    public Request(String method, Map headers, String body) {
        this.method = method;
        this.headers = headers;
        this.body = body;
    }

    public Object getJSON() {
        if (JSON == null) {
            try {
                JSON = new JsonSlurper().parseText(body);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Unable to parse body [%s] to JSON", body), e);
                }
                JSON = new HashMap<>();
            }
        }
        return JSON;
    }
}
