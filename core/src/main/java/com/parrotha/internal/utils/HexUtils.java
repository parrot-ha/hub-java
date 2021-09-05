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
package com.parrotha.internal.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class HexUtils {
    // Constants.
    private static final String HEXES = "0123456789ABCDEF";
    private static final String HEX_HEADER = "0x";

    /**
     * Converts the given hex string into a int array.
     *
     * @param value Hex string to convert to int array.
     * @return int array of the given hex string.
     * @throws NullPointerException if {@code value == null}.
     * @see #intArrayToHexString(int[])
     */
    public static int[] hexStringToIntArray(String value) {

        if (value == null)
            throw new NullPointerException("Value to convert cannot be null.");

        value = value.trim();
        if (value.startsWith(HEX_HEADER))
            value = value.substring((HEX_HEADER).length());
        int len = value.length();
        if (len % 2 != 0) {
            value = "0" + value;
            len = value.length();
        }
        int[] data = new int[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (int) ((Character.digit(value.charAt(i), 16) << 4)
                    + Character.digit(value.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Converts the given int array into a hex string.
     *
     * @param value int array to convert to hex string.
     * @return Converted int array to hex string.
     * @throws NullPointerException if {@code value == null}.
     * @see #hexStringToIntArray(String)
     */
    public static String intArrayToHexString(int[] value) {
        if (value == null)
            throw new NullPointerException("Value to convert cannot be null.");

        final StringBuilder hex = new StringBuilder(2 * value.length);
        for (final int b : value) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    /**
     * Converts the given short list into a hex string.
     *
     * @param value short list to convert to hex string.
     * @return Converted short list to hex string.
     * @throws NullPointerException if {@code value == null}.
     */
    public static String shortListToHexString(List<Short> value) {
        return shortListToHexString(value, false);
    }

    /**
     * Converts the given short list into a hex string.
     *
     * @param value short list to convert to hex string.
     * @return Converted short list to hex string.
     * @throws NullPointerException if {@code value == null}.
     */
    public static String shortListToHexString(List<Short> value, boolean withCommas) {
        if (value == null)
            throw new NullPointerException("Value to convert cannot be null.");

        final StringBuilder hex = new StringBuilder(2 * value.size());
        int index = 0;

        for (final Object o : value) {
            short b;
            // it's possible for groovy to send us an Integer in a List<Short>, it doesn't make sense, but we need to handle it.
            if(o instanceof Integer) {
                b = ((Integer) o).shortValue();
            } else if (o instanceof Short) {
                b = (short) o;
            } else {
                b = Short.parseShort(o.toString());
            }

            if(index > 0 && withCommas) {
                hex.append(',');
            }
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
            index++;
        }
        return hex.toString();
    }

    /**
     * Converts a hex string into an int
     *
     * @param hexString The hex string to convert
     * @return int from the hex string.
     * @throws NullPointerException if {@code value == null}.
     */
    public static int hexStringToInt(String hexString) {
        if (hexString == null)
            throw new NullPointerException("Value to convert cannot be null.");

        hexString = hexString.trim();
        if (hexString.startsWith(HEX_HEADER))
            hexString = hexString.substring((HEX_HEADER).length());

        return Integer.parseInt(hexString, 16);
    }

    public static String reverseHexString(String hexString) {
        if (hexString == null) return null;
        if (hexString.length() < 3) return hexString;
        hexString = hexString.trim();
        if (hexString.length() % 2 != 0) throw new IllegalArgumentException("hexString must be an even length");
        StringBuilder sb = new StringBuilder("");
        for (int i = hexString.length() - 2; i >= 0; i -= 2) {
            sb.append(hexString.substring(i, i + 2));
        }
        return sb.toString();
    }

    public static String integerArrayToHexStringCommaDelimited(Collection<Integer> valueArray, int minBytes) {
        StringBuilder sb = new StringBuilder();
        for (int intVal : valueArray) {
            sb.append(integerToHexString(intVal, minBytes)).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Converts the given long into an hexadecimal string.
     *
     * @param value    The long value to convert to hexadecimal string.
     * @param minBytes The minimum number of bytes to be represented.
     * @return The long value as hexadecimal string.
     * @throws IllegalArgumentException if {@code minBytes <= 0}.
     */
    public static String longToHexString(long value, int minBytes) {
        if (minBytes <= 0)
            throw new IllegalArgumentException("Minimum number of bytes must be greater than 0.");

        String f = String.format("%%0%dX", minBytes * 2);
        return String.format(f, value);
    }

    public static List<Short> hexStringToShortList(String hexString) {
        List<Short> shortList = Arrays.stream(HexUtils.hexStringToIntArray(hexString)).mapToObj(s -> Short.valueOf((short) s)).collect(Collectors.toList());
        return shortList;
    }

    public static List<Short> integerToShortList(int value, int minBytes) {
        if (minBytes <= 0)
            throw new IllegalArgumentException("Minimum number of bytes must be greater than 0.");
        String f = String.format("%%0%dX", minBytes * 2);
        return hexStringToShortList(String.format(f, value));
    }

    public static int shortListToInteger(List<Short> value) {
        return hexStringToInt(shortListToHexString(value));
    }

    // https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
    /* s must be an even-length string. */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        return byteArrayToHexString(bytes, false);
    }

    public static String byteArrayToHexString(byte[] bytes, boolean withSpaces) {
        if (bytes == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String format = withSpaces ? "%02X " : "%02X";
        for (int i = 0; i < bytes.length; i++) {
            result.append(String.format(format, bytes[i]));
        }
        return result.toString();
    }

/**
 * The following code is based on the Digi XBee Library hex utils, which had the original license:
 * <p>
 * Copyright 2017, Digi International Inc.
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

    /**
     * Converts the given integer into an hexadecimal string.
     *
     * @param value    The integer value to convert to hexadecimal string.
     * @param minBytes The minimum number of bytes to be represented.
     * @return The integer value as hexadecimal string.
     * @throws IllegalArgumentException if {@code minBytes <= 0}.
     */
    public static String integerToHexString(int value, int minBytes) {
        if (minBytes <= 0)
            throw new IllegalArgumentException("Minimum number of bytes must be greater than 0.");

        String f = String.format("%%0%dX", minBytes * 2);
        return String.format(f, value);
    }
}

