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
package groovyx.net.http;

import groovy.json.JsonBuilder;
import org.apache.http.HttpEntity;

import java.io.UnsupportedEncodingException;

public class ParrotHubEncoderRegistry extends EncoderRegistry {
    @Override
    public HttpEntity encodeJSON(Object model, Object contentType) throws UnsupportedEncodingException {
        JsonBuilder json = new JsonBuilder(model);

        if (contentType == null) {
            contentType = ContentType.JSON;
        }
        return this.createEntity(contentType, json.toString());
    }
}
