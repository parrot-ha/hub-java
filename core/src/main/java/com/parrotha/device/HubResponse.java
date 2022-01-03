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

import groovy.xml.XmlSlurper;
import groovy.xml.slurpersupport.GPathResult;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class HubResponse {
    private String index;
    private String port;
    private String ip;
    private String requestId;
    private String hubId;
    private String body;
    private String callback;

    public HubResponse() {
    }

    public HubResponse(String body) {

    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    private GPathResult xml;
    public GPathResult getXml() {
        if(xml == null && body != null) {
            try {
                this.xml = new XmlSlurper().parseText(body);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return xml;
    }

    // getXml() -> groovy.util.slurpersupport.NodeChild
    // getHeaders() -> org.apache.commons.collections.map.CaseInsensitiveMap
}
