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
package com.parrotha.helper;

import com.parrotha.exception.NotYetImplementedException;

// this is a helper that is used by some deviceHandlers
public class HexUtils {
    public static String integerToHexString(int value, int minBytes) {
        return com.parrotha.internal.utils.HexUtils.integerToHexString(value, minBytes);
    }

    public static int hexStringToInt(String value) {
        return com.parrotha.internal.utils.HexUtils.hexStringToInt(value);
    }

    public static String byteArrayToHexString(byte[] value) {
        return com.parrotha.internal.utils.HexUtils.byteArrayToHexString(value);
    }

    public static byte[] hexStringToByteArray(String value) {
        return com.parrotha.internal.utils.HexUtils.hexStringToByteArray(value);
    }

    public static String intArrayToHexString(int[] value) {
        return com.parrotha.internal.utils.HexUtils.intArrayToHexString(value);
    }

    public static int[] hexStringToIntArray(String value) {
        return com.parrotha.internal.utils.HexUtils.hexStringToIntArray(value);
    }
}
