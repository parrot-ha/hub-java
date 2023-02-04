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
package com.parrotha.device;

import java.util.ArrayList;
import java.util.List;

public class HubMultiAction {
    private List<HubAction> actions = new ArrayList<>();

    public HubMultiAction() {
    }

    public HubMultiAction(List<String> stringActions) {
        for(String action : stringActions) {
            actions.add(new HubAction(action));
        }
    }

    public void add(String action) {
        actions.add(new HubAction(action));
    }

    public void add(HubAction action) {
        actions.add(action);
    }

    public List<HubAction> getActions() {
        return actions;
    }
}
