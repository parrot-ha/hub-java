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
package com.parrotha.internal;

import groovy.json.JsonBuilder;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseApiHandler {
    public abstract void setupApi(Javalin app);

    public void buildStandardJsonResponse(Context ctx, boolean success) {
        buildStandardJsonResponse(ctx, 200, success, null);
    }

    public void buildStandardJsonResponse(Context ctx, boolean success, String message) {
        buildStandardJsonResponse(ctx, 500, success, message);
    }

    public void buildStandardJsonResponse(Context ctx, int status, boolean success, String message) {
        Map<String, Object> model = new HashMap<>();
        model.put("success", success);
        if (message != null)
            model.put("message", message);
        ctx.status(status);
        ctx.contentType("application/json");
        ctx.result(new JsonBuilder(model).toString());
    }
}
