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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HubSoapActionTest {
// ST CODE:
//def hsa = new physicalgraph.device.HubSoapAction(
//    path: '/upnp/control/basicevent1',
//    urn: 'urn:Belkin:service:basicevent:1',
//    action: 'GetBinaryState',
//    headers: [
//        HOST: "TEST"
//    ]
//)
//log.debug hsa
// ST OUTPUT:
//POST /upnp/control/basicevent1 HTTP/1.1
//Accept: */*
//User-Agent: Linux UPnP/1.0 SmartThings
//HOST: TEST
//SOAPAction: "urn:Belkin:service:basicevent:1#GetBinaryState"
//Content-Type: text/xml; charset="utf-8"
//Content-Length: 272
//
//<?xml version="1.0" encoding="utf-8"?>
//<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body><u:GetBinaryState xmlns:u="urn:Belkin:service:basicevent:1"></u:GetBinaryState></s:Body></s:Envelope>

// ST CODE:
//log.debug hsa instanceof physicalgraph.device.HubSoapAction
// OUTPUT:
//debug true

// ST CODE:
//log.debug hsa instanceof physicalgraph.device.HubAction
// OUTPUT:
//debug true

// ST CODE:
//log.debug hsa.protocol
// OUTPUT:
//debug LAN

// ST CODE:
//def hsa = new physicalgraph.device.HubSoapAction(
//    path: '/upnp/control/basicevent1',
//    urn: 'urn:Belkin:service:basicevent:1',
//    action: 'GetBinaryState',
//    headers: [
//        HOST: "TEST"
//    ]
//)
//log.debug hsa
// ST OUTPUT:
//POST /upnp/control/basicevent1 HTTP/1.1
//Accept: */*
//User-Agent: Linux UPnP/1.0 SmartThings
//HOST: TEST
//SOAPAction: "urn:Belkin:service:basicevent:1#GetBinaryState"
//Content-Type: text/xml; charset="utf-8"
//Content-Length: 272
//
//<?xml version="1.0" encoding="utf-8"?>
//<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body><u:GetBinaryState xmlns:u="urn:Belkin:service:basicevent:1"></u:GetBinaryState></s:Body></s:Envelope>

    // ST CODE:
    //log.debug new physicalgraph.device.HubSoapAction([:])
    // ST OUTPUT:
    //POST / HTTP/1.1
    //Accept: */*
    //User-Agent: Linux UPnP/1.0 SmartThings
    //SOAPAction: "null#null"
    //Content-Type: text/xml; charset="utf-8"
    //Content-Length: 225
    //
    //<?xml version="1.0" encoding="utf-8"?>
    //<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body><u:null xmlns:u="null"></u:null></s:Body></s:Envelope>
    @Test
    public void testConstructor() {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("path", "/upnp/control/basicevent1");
        options.put("urn", "urn:Belkin:service:basicevent:1");
        options.put("action", "GetBinaryState");
        Map<String, Object> headers = new HashMap();
        headers.put("HOST", "TEST");
        options.put("headers", headers);

        HubSoapAction hsa = new HubSoapAction(options);

        assertNotNull(hsa);
        assertNotNull(hsa.getAction());
        assertEquals(Protocol.LAN, hsa.getProtocol());
    }

    @Test
    public void testConstructorWithBody() {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("path", "/upnp/control/basicevent1");
        options.put("urn", "urn:Belkin:service:basicevent:1");
        options.put("action", "SetBinaryState");
        Map<String, Object> body = new HashMap();
        body.put("BinaryState", "1");
        options.put("body", body);
        Map<String, Object> headers = new HashMap();
        headers.put("HOST", "TEST2");
        options.put("headers", headers);

        HubSoapAction hsa = new com.parrotha.device.HubSoapAction(options);

        assertNotNull(hsa);
        assertNotNull(hsa.getAction());
        assertEquals(Protocol.LAN, hsa.getProtocol());

        assertEquals("POST /upnp/control/basicevent1 HTTP/1.1\r\n" +
                        "Accept: */*\r\n" +
                        "User-Agent: Linux UPnP/1.0 ParrotHub\r\n" +
                        "HOST: TEST2\r\n" +
                        "SOAPAction: \"urn:Belkin:service:basicevent:1#SetBinaryState\"\r\n" +
                        "Content-Type: text/xml; charset=\"utf-8\"\r\n" +
                        "Content-Length: 300\r\n" +
                        "\r\n" +
                        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body><u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\"><BinaryState>1</BinaryState></u:SetBinaryState></s:Body></s:Envelope>",
                hsa.getAction());
    }
}
