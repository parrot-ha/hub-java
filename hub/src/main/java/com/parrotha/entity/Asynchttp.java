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
package com.parrotha.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Implementation of smartthings asynchttp_v1
 */
public class Asynchttp {
    private static final Logger logger = LoggerFactory.getLogger(Asynchttp.class);

    private EntityScriptDelegateCommon entityScriptDelegateCommon;

    public Asynchttp(EntityScriptDelegateCommon entityScriptDelegateCommon) {
        this.entityScriptDelegateCommon = entityScriptDelegateCommon;
    }

    public void get(String callbackMethod, Map params, Map data) {
        this.entityScriptDelegateCommon.asyncHttpRequest("GET", callbackMethod, params, data);
    }

    public void get(String callbackMethod, Map params) {
        get(callbackMethod, params, null);
    }

    public void get(Map params, Map data) {
        get(null, params, data);
    }

    public void get(Map params) {
        get(null, params, null);
    }

    public void post(String callbackMethod, Map params, Map data) {
        this.entityScriptDelegateCommon.asyncHttpRequest("GET", callbackMethod, params, data);
    }

    public void post(String callbackMethod, Map params) {
        post(callbackMethod, params, null);
    }

    public void post(Map params, Map data) {
        post(null, params, data);
    }

    public void post(Map params) {
        post(null, params, null);
    }
}
