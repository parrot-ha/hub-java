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
package com.parrotha.device;

import groovy.lang.GString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HubSoapAction extends HubAction {

    public HubSoapAction(String action, Protocol protocol) {
        super(action, protocol);
    }

    public HubSoapAction(Map options) {
        StringBuilder stringBuilder = new StringBuilder();

        String urn = getOption(options, "urn", null);
        String soapAction = getOption(options, "action", null);
        String soapActionBody = "";
        Object bodyObj = options.get("body");
        if (bodyObj != null) {
            if (bodyObj instanceof String || bodyObj instanceof GString) {
                soapActionBody = bodyObj.toString();
            } else if (bodyObj instanceof Map) {
                soapActionBody = ((Map<String, Object>) bodyObj).keySet().stream()
                        .map(key -> "<" + key + ">" + ((Map<String, Object>) bodyObj).get(key) + "</" + key + ">")
                        .collect(Collectors.joining(""));
            }
        }
        String methodPathVersion = String.format("POST %1$s HTTP/1.1", getOption(options, "path", "/"));
        String soapBody = String.format("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                        "<s:Body><u:%1$s xmlns:u=\"%2$s\">%3$s</u:%1$s></s:Body></s:Envelope>",
                soapAction, urn, soapActionBody);

        /* after a bunch of iterations of changing header values on ST, it appears that they
           use a case sensitive check and add the headers in a specific order:
           Accept,
           User-Agent,
           other headers,
           SOAPAction (if not in other headers)
           Content-Type (if not in other headers)
           Content-Length
         */
        Map<String, Object> headers = new LinkedHashMap<>();
        if (options.containsKey("headers") && options.get("headers") instanceof Map) {
            Map<String, Object> optionsHeaders = (Map) options.get("headers");

            // remove content-length before we start adding headers so that we can add it in at the end.
            Object contentLengthHeaderValue = optionsHeaders.remove("Content-Length");

            headers.put("Accept", Objects.requireNonNullElse(optionsHeaders.remove("Accept"), "*/*"));
            headers.put("User-Agent", Objects.requireNonNullElse(optionsHeaders.remove("User-Agent"), "Linux UPnP/1.0 ParrotHub"));
            headers.putAll(optionsHeaders);
            if (!optionsHeaders.containsKey("SOAPAction")) {
                headers.put("SOAPAction", String.format("\"%1$s#%2$s\"", urn, soapAction));
            }
            if (!optionsHeaders.containsKey("Content-Type")) {
                headers.put("Content-Type", "text/xml; charset=\"utf-8\"");
            }
            headers.put("Content-Length", Objects.requireNonNullElseGet(contentLengthHeaderValue, soapBody::length));
        } else {
            headers.put("Accept", "*/*");
            headers.put("User-Agent", "Linux UPnP/1.0 ParrotHub");
            headers.put("SOAPAction", String.format("%1$s#%2$s", urn, soapAction));
            headers.put("Content-Type", "text/xml; charset=\"utf-8\"");
            headers.put("Content-Length", soapBody.length());
        }

        stringBuilder.append(methodPathVersion).append("\r\n");
        for (String headerName : headers.keySet()) {
            if (headerName != null && headers.get(headerName) != null) {
                stringBuilder.append(headerName).append(": ").append(headers.get(headerName).toString()).append("\r\n");
            }
        }
        stringBuilder.append("\r\n");
        stringBuilder.append(soapBody);

        this.setAction(stringBuilder.toString());
        this.setProtocol(Protocol.LAN);
    }

    private String getOption(Map options, String key, String defaultValue) {
        if (options == null || key == null) return defaultValue;
        if (options.get(key) != null) return options.get(key).toString();
        return defaultValue;
    }
}
