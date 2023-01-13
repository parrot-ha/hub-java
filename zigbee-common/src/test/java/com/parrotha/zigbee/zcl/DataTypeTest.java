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
package com.parrotha.zigbee.zcl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DataTypeTest {

    @Test
    public void testPack() {
        String packedData = DataType.pack(100, DataType.UINT16, true);
        assertNotNull(packedData);
        assertEquals("6400", packedData);

        packedData = DataType.pack(100, 0x23, true);
        assertNotNull(packedData);
        assertEquals("64000000", packedData);

        packedData = DataType.pack(Long.valueOf(665768019), 0x23, true);
        assertNotNull(packedData);
        assertEquals("53D0AE27", packedData);

        try {
            DataType.pack(600, 0x20);
            fail();
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().startsWith("Data too large for data type"));
        }
    }
}
