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

import com.google.common.collect.Maps;
import com.parrotha.internal.entity.EntityScriptDelegateUtils;
import com.parrotha.internal.http.ParrotHttpRequest;
import groovy.json.JsonSlurper;
import groovy.lang.Closure;
import groovy.lang.MetaMethod;
import groovyx.net.http.HttpResponseDecorator;
import groovyx.net.http.Method;
import groovyx.net.http.ParrotHubHTTPBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public abstract class EntityScriptDelegateCommon {
    private static final Logger logger = LoggerFactory.getLogger(EntityScriptDelegateCommon.class);

    protected abstract void runEntityMethod(String methodName, Object... args);

    private Asynchttp asynchttp = null;

    public void include(String namespace) {
        if ("asynchttp_v1".equals(namespace)) {
            asynchttp = new Asynchttp(this);
        }
    }

    public Asynchttp getAsynchttp_v1() {
        if (asynchttp == null) {
            throw new IllegalStateException("asynchttp is not imported");
        }
        return asynchttp;
    }

    /**
     * From docs.smartthings.com/en/latest/ref-docs/smartapp-ref.html#parsejson
     */
    public static Object parseJson(String stringToParse) {
        return new JsonSlurper().parseText(stringToParse);
    }

    public static Map parseLanMessage(String stringToParse) {
        return EntityScriptDelegateUtils.parseLanMessage(stringToParse);
    }

    public BigDecimal celsiusToFahrenheit(BigDecimal value) {
        return ((value.multiply(BigDecimal.valueOf(9))).divide(BigDecimal.valueOf(5))).add(BigDecimal.valueOf(32));
    }

    public Object httpGet(String uri, Closure closure) throws IOException, URISyntaxException {
        Map<String, Object> params = new HashMap<>();
        params.put("uri", uri);
        return httpGet(params, closure);
    }

    public Object httpGet(Map<String, Object> params, Closure closure) throws URISyntaxException, IOException {
        return httpRequest(Method.GET, params, closure);
    }

    public Object httpPost(Map params, Closure closure) throws IOException, URISyntaxException {
        return httpRequest(Method.POST, params, closure);
    }

    public void asynchttpGet(MetaMethod callbackMethod, Map params) {
        asynchttpGet(callbackMethod.getName(), params);
    }

    public void asynchttpGet(String callbackMethod, Map params) {
        asynchttpGet(callbackMethod, params, null);
    }

    public void asynchttpGet(MetaMethod callbackMethod, Map params, Map data) {
        asynchttpGet(callbackMethod.getName(), params, data);
    }

    public void asynchttpGet(String callbackMethod, Map params, Map data) {
        asyncHttpRequest("GET", callbackMethod, params, data);
    }

    public void asynchttpPost(MetaMethod callbackMethod, Map params) {
        asynchttpPost(callbackMethod.getName(), params);
    }

    public void asynchttpPost(String callbackMethod, Map params) {
        asynchttpPost(callbackMethod, params, null);
    }

    public void asynchttpPost(MetaMethod callbackMethod, Map params, Map data) {
        asynchttpPost(callbackMethod.getName(), params, data);
    }

    public void asynchttpPost(String callbackMethod, Map params, Map data) {
        asyncHttpRequest("POST", callbackMethod, params, data);
    }

    protected void asyncHttpRequest(String httpMethod, String callbackMethod, Map params, Map data) {
        new Thread(() -> {
            CloseableHttpResponse httpResponse = null;
            String body = null;
            try {
                httpResponse = ParrotHttpRequest.doApacheHttpRequest(httpMethod, params);
                if (httpResponse.getEntity().getContentLength() > 0) {
                    body = EntityUtils.toString(httpResponse.getEntity());
                    EntityUtils.consume(httpResponse.getEntity());
                }
            } catch (URISyntaxException | IOException e) {
                logger.warn("Exception with http request", e);
                //TODO: add to AsyncResponse
            }
            if (httpResponse != null && callbackMethod != null) {
                if (data != null) {
                    runEntityMethod(callbackMethod, new AsyncResponseImpl(httpResponse, body), data);
                } else {
                    runEntityMethod(callbackMethod, new AsyncResponseImpl(httpResponse, body));
                }
            }
        }).start();

    }

    private Object httpRequest(Method httpMethod, Map params, Closure closure) throws URISyntaxException, IOException {
        HttpResponseDecorator response = doHttpBuilderRequest(httpMethod, params);
        if (response != null) {
            return closure.call(response);
        }
        return null;
    }


    /**
     * Processes http request using old HttpBuilder library
     *
     * @param httpMethod
     * @param params
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @Deprecated
     */
    private HttpResponseDecorator doHttpBuilderRequest(Method httpMethod, Map params) throws URISyntaxException, IOException {
        ParrotHubHTTPBuilder httpBuilder = new ParrotHubHTTPBuilder();
        Map paramsCopy = Maps.newHashMap(params);
        if (paramsCopy.containsKey("timeout")) {
            Object timeoutObj = paramsCopy.remove("timeout");
            if (timeoutObj instanceof Number) {
                int timeout = ((Number) timeoutObj).intValue();
                //TODO: process timeout, set it on httpBuilder somehow.
            }
        }

        Object response = httpBuilder.request(httpMethod, paramsCopy, null);
        if (response != null && response instanceof HttpResponseDecorator) {
            return (HttpResponseDecorator) response;
        }

        return null;
    }
    //TODO: HTTPBuilder is very old and not maintained.  The only thing that needs to be kept is HttpResponseDecorator
    // the rest can be rewritten using apache httpclient
    // for now, using a copy of HTTPBuilder with fixes called ParrotHubHTTPBuilder


}
