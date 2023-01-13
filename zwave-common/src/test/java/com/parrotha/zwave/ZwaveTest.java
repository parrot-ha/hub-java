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
package com.parrotha.zwave;

import com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ZwaveTest {
    @Test
    public void testParse() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0x26, 1);
        Command cmd = new Zwave().parse("zw device: 03, command: 2603, payload: 10", map);
        Assertions.assertNotNull(cmd);
        Assertions.assertTrue(cmd instanceof SwitchMultilevelReport);

        map.clear();
        cmd = new Zwave().parse("zw device: 03, command: 2603, payload: 10", map);
        Assertions.assertNotNull(cmd);
        Assertions.assertTrue(cmd instanceof com.parrotha.zwave.commands.switchmultilevelv3.SwitchMultilevelReport);

        map.clear();
        map.put(0x71, 2);
        cmd = new Zwave().parse("zw device: 0A, command: 7105, payload: 15 01 ", map);
        Assertions.assertNotNull(cmd);
        Assertions.assertTrue(cmd instanceof com.parrotha.zwave.commands.alarmv2.AlarmReport);

        map.clear();
        map.put(0x63, 1);
        cmd = new Zwave().parse("zw device: 0D, command: 6303, payload: 31 FF ", map);
        Assertions.assertNotNull(cmd);
        Assertions.assertTrue(cmd instanceof com.parrotha.zwave.commands.usercodev1.UserCodeReport);
    }
}
