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
package com.parrotha.zwave.commands.networkmanagementinclusionv2;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NodeAddStatusTest {
    @Test
    public void testSetPayload() {
        List<Short> shortList = new ArrayList<>(Arrays.asList(
                (short) 0x01, /* seq no */
                (short) 0x02, /* status */
                (short) 0x00, /* reserved */
                (short) 0x03, /* new node id */
                (short) 0x9, /* node info length */
                (short) 0x04, /* listening + capability */
                (short) 0x05, /* opt + security */
                (short) 0x06, /* basic device class */
                (short) 0x07, /* generic device class */
                (short) 0x08, /* specific device class*/
                (short) 0x09, /* command class 1*/
                (short) 0x0A, /* command class 2*/
                (short) 0x0B, /* command class 3*/
                (short) 0x0C, /* granted keys*/
                (short) 0x0D /* kex fail type */
        ));
        NodeAddStatus nodeAddStatus = new NodeAddStatus();
        nodeAddStatus.setPayload(shortList);
        assertEquals((short) 0x0C, nodeAddStatus.getGrantedKeys());
        assertEquals((short) 0x0D, nodeAddStatus.getKexFailType());
        assertNotNull(nodeAddStatus.getCommandClass());
        assertEquals(3, nodeAddStatus.getCommandClass().size());


    }
}
