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
package com.parrotha.zwave.commands.networkmanagementproxyv3;

import com.parrotha.internal.utils.HexUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeInfoCachedReportTest {
    @Test
    public void testSetPayload() {
        NodeInfoCachedReport nodeInfoCachedReport = new NodeInfoCachedReport();
        nodeInfoCachedReport.setPayload(HexUtils.hexStringToShortList("9300D39C000410015E5686725A855C59732527702C2B7A6823"));
        assertEquals((short) 0, nodeInfoCachedReport.getGrantedKeys());
    }
}
