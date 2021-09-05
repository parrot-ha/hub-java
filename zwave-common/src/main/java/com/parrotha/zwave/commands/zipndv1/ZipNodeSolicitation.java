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
package com.parrotha.zwave.commands.zipndv1;

import com.parrotha.zwave.Command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Note: This code is autogenerated, changes will be overwritten.
 */
public class ZipNodeSolicitation extends Command {
    public String getCMD() {
        return "5803";
    }

    private Short nodeId = 0;
    private List<Short> ipv6Address;

    public Short getNodeId() {
        return nodeId;
    }

    public void setNodeId(Short nodeId) {
        this.nodeId = nodeId;
    }

    public List<Short> getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(List<Short> ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    public List<Short> getPayload() {
        List<Short> retList = Stream.of((short) 0, nodeId).collect(Collectors.toList());
        retList.addAll(ipv6Address);
        return retList;
    }

    public void setPayload(List<Short> payload) {
        if (payload == null) return;
        if (payload.size() > 1) {
            nodeId = payload.get(1);
        }
        if (payload.size() > 18) {
            ipv6Address = payload.subList(2, 18);
        }
    }

    @Override
    public String toString() {
        return "ZipNodeSolicitation(" +
                ", nodeId: " + nodeId +
                ", ipv6Address: " + ipv6Address +
                ')';
    }
}
