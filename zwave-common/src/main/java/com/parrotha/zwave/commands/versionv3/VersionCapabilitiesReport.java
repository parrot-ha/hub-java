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
package com.parrotha.zwave.commands.versionv3;

import com.parrotha.zwave.Command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Note: This code is autogenerated, changes will be overwritten.
 */
public class VersionCapabilitiesReport extends Command {
    public String getCMD() {
        return "8616";
    }

    private Boolean version = false;
    private Boolean commandClass = false;
    private Boolean zWaveSoftware = false;

    public Boolean getVersion() {
        return version;
    }

    public void setVersion(Boolean version) {
        this.version = version;
    }

    public Boolean getCommandClass() {
        return commandClass;
    }

    public void setCommandClass(Boolean commandClass) {
        this.commandClass = commandClass;
    }

    public Boolean getZWaveSoftware() {
        return zWaveSoftware;
    }

    public void setZWaveSoftware(Boolean zWaveSoftware) {
        this.zWaveSoftware = zWaveSoftware;
    }

    public List<Short> getPayload() {
        Short data0 = (short) ((version == true ? 1 : 0) | 
                (commandClass == true ? (1 << 1) : 0) | 
                (zWaveSoftware == true ? (1 << 2) : 0));

        return Stream.of(data0).collect(Collectors.toList());
    }

    public void setPayload(List<Short> payload) {
        if (payload == null) return;
        if (payload.size() > 0) {
            version = ((payload.get(0)) & 1) == 1;
            commandClass = ((payload.get(0) >> 1) & 1) == 1;
            zWaveSoftware = ((payload.get(0) >> 2) & 1) == 1;
        }
    }

    @Override
    public String toString() {
        return "VersionCapabilitiesReport(" +
                "version: " + version +
                ", commandClass: " + commandClass +
                ", zWaveSoftware: " + zWaveSoftware +
                ')';
    }
}
