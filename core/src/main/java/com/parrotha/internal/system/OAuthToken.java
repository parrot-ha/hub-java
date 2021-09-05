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
package com.parrotha.internal.system;

import java.util.UUID;

public class OAuthToken {
    private String accessToken;
    private String refreshToken;
    private String type;
    private long expiration;

    public OAuthToken() {
        this(false, null);
    }

    public OAuthToken(boolean autoPopulate, String type) {
        if (autoPopulate) {
            this.accessToken = UUID.randomUUID().toString();
            this.refreshToken = UUID.randomUUID().toString();
            this.type = type;
            //TODO: ST returns an expires_in ~50 years in the future, should we shorten it?
            this.expiration = 60 * 60 * 24 * 365 * 49;
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
