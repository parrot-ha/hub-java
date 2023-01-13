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
package com.parrotha.api;

import java.util.Map;

public class Response {
    private String contentType = "application/json";
    private int status = 200;
    private String data;

    public Response(Map params) {
        if(params == null) return;
        if(params.get("contentType") != null) {
            contentType = params.get("contentType").toString();
        }
        if(params.get("status") != null) {
            status = Integer.parseInt(params.get("status").toString());
        }
        if(params.get("data") != null) {
            data = params.get("data").toString();
        }
    }

    public String getContentType() {
        return contentType;
    }

    public int getStatus() {
        return status;
    }

    public String getData() {
        return data;
    }
}
