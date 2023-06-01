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

import com.parrotha.exception.NotYetImplementedException;
import groovy.json.JsonSlurper;
import groovy.xml.slurpersupport.GPathResult;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.util.List;
import java.util.Map;

public class AsyncResponseImpl implements AsyncResponse {
    private CloseableHttpResponse closeableHttpResponse;
    private String body;
    private Object jsonData;

    public AsyncResponseImpl(CloseableHttpResponse closeableHttpResponse, String body) {
        this.closeableHttpResponse = closeableHttpResponse;
        this.body = body;
    }

    @Override
    public String getData() {
        return body;
    }

    @Override
    public String getErrorData() {
        //TODO: implement
        throw new NotYetImplementedException();
    }

    @Override
    public Object getErrorJson() {
        //TODO: implement
        throw new NotYetImplementedException();
    }

    @Override
    public String getErrorMessage() {
        //TODO: implement
        throw new NotYetImplementedException();
    }

    @Override
    public GPathResult getErrorXml() {
        //TODO: implement
        throw new NotYetImplementedException();
    }

    @Override
    public Map<String, String> getHeaders() {
        //TODO: implement
        throw new NotYetImplementedException();
    }

    @Override
    public Object getJson() {
        int status = getStatus();
        if(status < 200 || status > 299) {
            throw new IllegalStateException("Error response from http call");
        }
        if(jsonData == null) {
            this.jsonData = new JsonSlurper().parseText(this.body);
        }
        return this.jsonData;
    }

    @Override
    public int getStatus() {
        return closeableHttpResponse.getStatusLine().getStatusCode();
    }

    @Override
    public List<String> getWarningMessages() {
        //TODO: implement
        throw new NotYetImplementedException();
    }

    @Override
    public GPathResult getXml() {
        //TODO: implement
        throw new NotYetImplementedException();
    }

    @Override
    public boolean hasError() {
        //TODO: implement
        throw new NotYetImplementedException();
    }
}
