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
package com.parrotha.internal.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class HexUtilsTest {
    @Test
    public void testSwapHexString() {

        assertNull(HexUtils.reverseHexString(null));
        assertEquals("", HexUtils.reverseHexString(""));
        assertEquals("AF12", HexUtils.reverseHexString("12AF"));
        assertEquals("EEDDCCBBAA", HexUtils.reverseHexString("AABBCCDDEE"));

        try {
            HexUtils.reverseHexString("AB0");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

    }

    @Test
    public void testIntegerArrayToHexStringCommaDelimited() {
        Integer[] intArray = new Integer[]{0, 3, 4, 5, 6, 2821, 64513, 64520};
        assertEquals("0000,0003,0004,0005,0006,0B05,FC01,FC08", HexUtils.integerArrayToHexStringCommaDelimited(Arrays.asList(intArray), 2));
    }

    @Test
    public void testHexStringToInt() {
        assertEquals(255, HexUtils.hexStringToInt("FF"));
    }

    @Test
    public void testShortListToHexString() {
        assertEquals("5E,86,72,98,84", HexUtils.shortListToHexString(new ArrayList<>(Arrays.asList((short) 0x5E, (short) 0x86, (short) 0x72, (short) 0x98, (short) 0x84)), true));
        assertEquals("5A", HexUtils.shortListToHexString(new ArrayList<>(Arrays.asList((short) 0x5A)), true));
    }
}
