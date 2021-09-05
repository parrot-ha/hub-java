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
package com.parrotha.zwave.commands.associationv1;

import com.parrotha.zwave.Command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Note: This code is autogenerated, changes will be overwritten.
 */
public class AssociationGroupingsReport extends Command {
    public String getCMD() {
        return "8506";
    }

    private Short supportedGroupings = 0;

    public Short getSupportedGroupings() {
        return supportedGroupings;
    }

    public void setSupportedGroupings(Short supportedGroupings) {
        this.supportedGroupings = supportedGroupings;
    }

    public List<Short> getPayload() {
        return Stream.of(supportedGroupings).collect(Collectors.toList());
    }

    public void setPayload(List<Short> payload) {
        if (payload == null) return;
        if (payload.size() > 0) {
            supportedGroupings = payload.get(0);
        }
    }

    @Override
    public String toString() {
        return "AssociationGroupingsReport(" +
                "supportedGroupings: " + supportedGroupings +
                ')';
    }
}
