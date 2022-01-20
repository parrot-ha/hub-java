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
package com.parrotha.internal.groovy.extension.module;

import groovy.json.JsonOutput;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.groovy.runtime.NullObject;

import java.io.UnsupportedEncodingException;

public class EncodingExtensionMethods {
    public static String encodeAsBase64(Object targetObject) throws UnsupportedEncodingException {
        if (targetObject == null || targetObject instanceof NullObject) {
            return null;
        } else if (targetObject instanceof Byte[] || targetObject instanceof byte[]) {
            return new String(Base64.encodeBase64((byte[]) targetObject));
        } else {
            return new String(Base64.encodeBase64(targetObject.toString().getBytes("UTF-8")));
        }
    }

    public static byte[] decodeBase64(Object targetObject) throws UnsupportedEncodingException {
        if (targetObject == null || targetObject instanceof NullObject) {
            return null;
        } else if (targetObject instanceof Byte[] || targetObject instanceof byte[]) {
            return Base64.decodeBase64((byte[]) targetObject);
        } else {
            return Base64.decodeBase64(targetObject.toString().getBytes("UTF-8"));
        }
    }

    public static String encodeAsJSON(Object targetObject) throws UnsupportedEncodingException {
        if (targetObject == null || targetObject instanceof NullObject) {
            return null;
        } else {
            return JsonOutput.toJson(targetObject);
        }
    }

    public static String encodeAsJson(Object targetObject) throws UnsupportedEncodingException {
        return encodeAsJSON(targetObject);
    }

}
