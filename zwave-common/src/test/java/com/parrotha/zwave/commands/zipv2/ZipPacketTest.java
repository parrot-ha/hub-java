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
package com.parrotha.zwave.commands.zipv2;

import com.parrotha.internal.utils.HexUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZipPacketTest {

    @Test
    public void testSetPayload() {
        ZipPacket zipPacket = new ZipPacket();
        //230200D006000005840200012503FF
        zipPacket.setPayload(HexUtils.hexStringToShortList("00D006000005840200012503FF"));
        assertTrue(zipPacket.getHeaderExtIncluded());
        List<Short> headerExtension = zipPacket.getHeaderExtension();
        assertNotNull(headerExtension);
        assertEquals(5, headerExtension.size());

        assertTrue(zipPacket.getzWaveCmdIncluded());
        List<Short> zWaveCommand = zipPacket.getzWaveCommand();
        assertNotNull(zWaveCommand);
        assertEquals(3, zWaveCommand.size());
        assertEquals("2503FF", HexUtils.shortListToHexString(zWaveCommand));
    }
}
