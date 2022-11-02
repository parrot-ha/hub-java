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

import com.google.common.collect.Maps;
import groovy.json.JsonSlurper;
import groovy.lang.Closure;
import groovyx.net.http.HttpResponseDecorator;
import groovyx.net.http.ParrotHubHTTPBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class EntityScriptDelegateCommon {
    /**
     * From docs.smartthings.com/en/latest/ref-docs/smartapp-ref.html#parsejson
     */
    public Object parseJson(String stringToParse) {
        return new JsonSlurper().parseText(stringToParse);
    }

    public Object httpGet(String uri, Closure closure) throws IOException, URISyntaxException {
        Map params = new HashMap();
        params.put("uri", uri);
        return httpGet(params, closure);
    }

    public Object httpGet(Map params, Closure closure) throws IOException, URISyntaxException {
        ParrotHubHTTPBuilder httpBuilder = new ParrotHubHTTPBuilder();
        Map paramsCopy = Maps.newHashMap(params);
        Object response = httpBuilder.get(paramsCopy);
        if (response != null && response instanceof HttpResponseDecorator) {
            return closure.call(response);
        }
        return null;
    }

    public Object httpPost(Map params, Closure closure) throws IOException, URISyntaxException {
        ParrotHubHTTPBuilder httpBuilder = new ParrotHubHTTPBuilder();
        Map paramsCopy = Maps.newHashMap(params);
        Object response = httpBuilder.post(paramsCopy);
        if (response != null && response instanceof HttpResponseDecorator) {
            return closure.call(response);
        }
        return null;
    }

    //TODO: HTTPBuilder is very old and not maintained.  The only thing that needs to be kept is HttpResponseDecorator
    // the rest can be rewritten using apache httpclient
    // for now, using a copy of HTTPBuilder with fixes called ParrotHubHTTPBuilder
}
