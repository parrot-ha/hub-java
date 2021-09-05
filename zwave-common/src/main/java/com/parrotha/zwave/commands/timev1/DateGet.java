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
package com.parrotha.zwave.commands.timev1;

import com.parrotha.zwave.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Note: This code is autogenerated, changes will be overwritten.
 */
public class DateGet extends Command {
    public String getCMD() {
        return "8A03";
    }

    public List<Short> getPayload() {
        return new ArrayList<>();
    }

    public void setPayload(List<Short> payload) {
    }

    @Override
    public String toString() {
        return "DateGet(" +
                ')';
    }
}
