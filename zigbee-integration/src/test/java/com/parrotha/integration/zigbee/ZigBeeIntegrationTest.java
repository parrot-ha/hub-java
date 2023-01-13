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
package com.parrotha.integration.zigbee;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ZigBeeIntegrationTest {
    @Test
    public void testGetPreferenceInputs() {
        ZigBeeIntegration zigBeeIntegration = new ZigBeeIntegration();
        Map preferenceInputs = zigBeeIntegration.getPreferencesLayout();
        assertNotNull(preferenceInputs);
        assertEquals(2, preferenceInputs.keySet().size());
        assertNotNull(preferenceInputs.get("sections"));
    }
}
