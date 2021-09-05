/**
 * Copyright (c) 2021 by the respective copyright holders.
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
package com.parrotha.internal.ui;

import io.javalin.Javalin;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UIFramework {
    private static final Logger logger = LoggerFactory.getLogger(UIFramework.class);

    private Javalin app;

    public void start() {
        this.app = Javalin.create(config -> {
            config.addStaticFiles("/web");
            config.addSinglePageRoot("/", "/web/index.html");
            config.accessManager((handler, ctx, permittedRoles) -> {
                UIRole userRole = (UIRole) getUserRole(ctx);
                if (permittedRoles.size() == 0) {
                    // assume everyone can access
                    logger.warn("There are no permitted roles for {}, everyone can access", ctx.matchedPath());
                    handler.handle(ctx);
                } else if(permittedRoles.contains(UIRole.ANYONE) || permittedRoles.contains(userRole)) {
                    handler.handle(ctx);
                } else {
                    ctx.status(401).result("Unauthorized");
                }
            });
        }).start(7000);
    }

    public void stop() {
        if (this.app != null) {
            this.app.stop();
        }
    }

    public Javalin getApp() {
        return this.app;
    }

    private Role getUserRole(Context ctx) {
        // determine user role based on request
        // typically done by inspecting headers

        //TODO: figure out role, for now everyone is administrator
        return UIRole.ADMIN;
    }

    public enum UIRole implements Role {
        ANYONE,
        ADMIN,
        POWER_USER,
        USER;
    }

}
