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
package com.parrotha.zwave.commands.networkmanagementinclusionv1;

import com.parrotha.zwave.Command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Note: This code is autogenerated, changes will be overwritten.
 */
public class NodeRemoveStatus extends Command {
    public static final Short REMOVE_NODE_STATUS_DONE = 6;
    public static final Short REMOVE_NODE_STATUS_FAILED = 7;

    public String getCMD() {
        return "3404";
    }

    private Short seqNo = 0;
    private Short status = 0;
    private Short nodeId = 0;

    public Short getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Short seqNo) {
        this.seqNo = seqNo;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Short getNodeId() {
        return nodeId;
    }

    public void setNodeId(Short nodeId) {
        this.nodeId = nodeId;
    }

    public List<Short> getPayload() {
        return Stream.of(seqNo, status, nodeId).collect(Collectors.toList());
    }

    public void setPayload(List<Short> payload) {
        if (payload == null) return;
        if (payload.size() > 0) {
            seqNo = payload.get(0);
        }
        if (payload.size() > 1) {
            status = payload.get(1);
        }
        if (payload.size() > 2) {
            nodeId = payload.get(2);
        }
    }

    @Override
    public String toString() {
        return "NodeRemoveStatus(" +
                "seqNo: " + seqNo +
                ", status: " + status +
                ", nodeId: " + nodeId +
                ')';
    }
}
