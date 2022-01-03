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
package com.parrotha.device;

import groovy.json.JsonBuilder;
import groovy.lang.GString;
import groovy.lang.MetaMethod;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//https://docs.smartthings.com/en/latest/ref-docs/hubaction-ref.html#hubaction
public class HubAction {
    private String action;
    private String dni;
    private String responseType;  //used for z-wave to specify the command class and command response type
    private Protocol protocol;
    private Map options;

    public HubAction() {
    }

    public HubAction(String action) {
        this.action = action;
    }

    public HubAction(String action, Protocol protocol) {
        this.action = action;
        this.protocol = protocol;
    }

    public HubAction(String action, Protocol protocol, String dni, Map options) {
        this.action = action;
        this.protocol = protocol;
        this.dni = dni;
        this.options = options;
    }

    public HubAction(Map params, String dni) {
        this(params, dni, null);
    }
    public HubAction(Map params, String dni, Map options) {
        this.protocol = (Protocol) params.getOrDefault("protocol", Protocol.LAN);
        this.options = options;


        StringBuilder actionStringBuilder = new StringBuilder();
        actionStringBuilder.append(params.getOrDefault("method", "POST")).append(" ");
        actionStringBuilder.append(params.getOrDefault("path", "/")).append(" HTTP/1.1\r\n");

        Map<String, Object> headers = new LinkedHashMap<>();

        String body = null;
        if (params.containsKey("headers") && params.get("headers") instanceof Map) {
            Map<String, Object> paramsHeaders = (Map) params.get("headers");

            // remove content-length before we start adding headers so that we can add it in at the end.
            Object contentLengthHeaderValue = paramsHeaders.remove("Content-Length");

            headers.put("Accept", Objects.requireNonNullElse(paramsHeaders.remove("Accept"), "*/*"));
            headers.put("User-Agent", Objects.requireNonNullElse(paramsHeaders.remove("User-Agent"), "Linux UPnP/1.0 ParrotHub"));
            headers.putAll(paramsHeaders);
            Object bodyObject = paramsHeaders.get("body");

            if (!paramsHeaders.containsKey("Content-Type") && bodyObject != null) {
                if (bodyObject instanceof Map) {
                    headers.put("Content-Type", "application/json");
                } else {
                    headers.put("Content-Type", "text/xml; charset=\"utf-8\"");
                }
            }

            if (bodyObject != null) {
                if (bodyObject instanceof String || bodyObject instanceof GString) {
                    body = bodyObject.toString();
                } else if (((String) headers.get("Content-Type")).contains("json") && bodyObject instanceof Map || bodyObject instanceof List) {
                    body = new JsonBuilder(params.get("body")).toString();
                } else if (((String) headers.get("Content-Type")).contains("xml")) {
                    //TODO: format xml
                }
                headers.put("Content-Length", Objects.requireNonNullElseGet(contentLengthHeaderValue, body::length));
            }
        } else {
            headers.put("Accept", "*/*");
            headers.put("User-Agent", "Linux UPnP/1.0 ParrotHub");
        }

        for (String key : headers.keySet()) {
            actionStringBuilder.append(key).append(": ").append(headers.get(key)).append("\r\n");
        }
        actionStringBuilder.append("\r\n");
        if (body != null) {
            actionStringBuilder.append(body);
        } else {
            actionStringBuilder.append("\r\n");
        }
        action = actionStringBuilder.toString();
    }

    public String getCallback() {
        if(options != null && options.containsKey("callback")) {
            Object callbackObject = options.get("callback");
            if(callbackObject instanceof MetaMethod) {
                return ((MetaMethod) callbackObject).getName();
            } else {
                return callbackObject.toString();
            }
        }
        return null;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Map getOptions() {
        return options;
    }

    public void setOptions(Map options) {
        this.options = options;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    @Override
    public String toString() {
        return action;
    }


    //    def hostAddress = "1.2.3.4"
    //            def ha = new physicalgraph.device.HubAction(
    //            [
    //                method: 'GET',
    //                path: '/setup.xml',
    //                headers: [ HOST: hostAddress ],
    //            ],
    //            null,
    //            [ callback: handleSetupXml ]
    //        )
    //
    //        log.debug ha.action
    //        log.debug ha.options

    //GET /setup.xml HTTP/1.1
    //Accept: */*
    //User-Agent: Linux UPnP/1.0 SmartThings
    //HOST: 1.2.3.4
}
