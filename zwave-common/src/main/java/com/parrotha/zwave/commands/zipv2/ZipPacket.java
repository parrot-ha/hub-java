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
package com.parrotha.zwave.commands.zipv2;

import com.parrotha.zwave.Command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZipPacket extends Command {
    public final static short ZIP_OPTION_EXPECTED_DELAY = 1;
    public final static short INSTALLATION_MAINTENANCE_GET = 2;
    public final static short INSTALLATION_MAINTENANCE_REPORT = 3;

    public String getCMD() {
        return "2302";
    }

    private Boolean ackRequest = false;
    private Boolean ackResponse = false;
    private Boolean nackResponse = false;
    private Boolean nackWaiting = false;
    private Boolean nackQueueFull = false;
    private Boolean nackOptionError = false;
    private Boolean headerExtIncluded = false;
    private Boolean zWaveCmdIncluded = false;
    private Boolean moreInformation = false;
    private Boolean secureOrigin = false;
    private Short seqNo = 0;
    private Short sourceEndPoint = 0;
    private Boolean bitAddress = false;
    private Short destinationEndPoint = 0;
    List<Short> headerExtension = null;
    List<Short> zWaveCommand = null;

    public Boolean getAckRequest() {
        return ackRequest;
    }

    public void setAckRequest(Boolean ackRequest) {
        this.ackRequest = ackRequest;
    }

    public Boolean getAckResponse() {
        return ackResponse;
    }

    public void setAckResponse(Boolean ackResponse) {
        this.ackResponse = ackResponse;
    }

    public Boolean getNackResponse() {
        return nackResponse;
    }

    public void setNackResponse(Boolean nackResponse) {
        this.nackResponse = nackResponse;
    }

    public Boolean getNackWaiting() {
        return nackWaiting;
    }

    public void setNackWaiting(Boolean nackWaiting) {
        this.nackWaiting = nackWaiting;
    }

    public Boolean getNackQueueFull() {
        return nackQueueFull;
    }

    public void setNackQueueFull(Boolean nackQueueFull) {
        this.nackQueueFull = nackQueueFull;
    }

    public Boolean getNackOptionError() {
        return nackOptionError;
    }

    public void setNackOptionError(Boolean nackOptionError) {
        this.nackOptionError = nackOptionError;
    }

    public Boolean getHeaderExtIncluded() {
        return headerExtIncluded;
    }

    public void setHeaderExtIncluded(Boolean headerExtIncluded) {
        this.headerExtIncluded = headerExtIncluded;
    }

    public Boolean getzWaveCmdIncluded() {
        return zWaveCmdIncluded;
    }

    public void setzWaveCmdIncluded(Boolean zWaveCmdIncluded) {
        this.zWaveCmdIncluded = zWaveCmdIncluded;
    }

    public Boolean getMoreInformation() {
        return moreInformation;
    }

    public void setMoreInformation(Boolean moreInformation) {
        this.moreInformation = moreInformation;
    }

    public Boolean getSecureOrigin() {
        return secureOrigin;
    }

    public void setSecureOrigin(Boolean secureOrigin) {
        this.secureOrigin = secureOrigin;
    }

    public Short getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Short seqNo) {
        this.seqNo = seqNo;
    }

    public Short getSourceEndPoint() {
        return sourceEndPoint;
    }

    public void setSourceEndPoint(Short sourceEndPoint) {
        this.sourceEndPoint = sourceEndPoint;
    }

    public Boolean getBitAddress() {
        return bitAddress;
    }

    public void setBitAddress(Boolean bitAddress) {
        this.bitAddress = bitAddress;
    }

    public Short getDestinationEndPoint() {
        return destinationEndPoint;
    }

    public void setDestinationEndPoint(Short destinationEndPoint) {
        this.destinationEndPoint = destinationEndPoint;
    }

    public List<Short> getHeaderExtension() {
        return headerExtension;
    }

    public void setHeaderExtension(List<Short> headerExtension) {
        this.headerExtension = headerExtension;
    }

    public List<Short> getzWaveCommand() {
        return zWaveCommand;
    }

    public void setzWaveCommand(List<Short> zWaveCommand) {
        this.zWaveCommand = zWaveCommand;
    }

    public List<Short> getPayload() {
        Short data0 = (short) ((ackRequest ? 0x80 : 0) | (ackResponse ? 0x40 : 0) | (nackResponse ? 0x20 : 0) |
                (nackWaiting ? 0x10 : 0) | (nackQueueFull ? 0x8 : 0) | (nackOptionError ? 0x4 : 0));
        Short data1 = (short) ((headerExtIncluded ? 0x80 : 0) | (zWaveCmdIncluded ? 0x40 : 0) |
                (moreInformation ? 0x20 : 0) | (secureOrigin ? 0x10 : 0));
        Short data3 = (short) (sourceEndPoint & 0x7F);
        Short data4 = (short) ((bitAddress ? 0x80 : 0) | (destinationEndPoint & 0x7F));
        List<Short> retList = Stream.of(data0, data1, seqNo, data3, data4).collect(Collectors.toList());
        if (headerExtIncluded && headerExtension != null) {
            retList.addAll(headerExtension);
        }
        if (zWaveCmdIncluded && zWaveCommand != null) {
            retList.addAll(zWaveCommand);
        }

        return retList;
    }

    public void setPayload(List<Short> payload) {
        if (payload == null) return;
        if (payload.size() > 0) {
            ackRequest = ((payload.get(0)) & 0x80) == 0x80;
            ackResponse = ((payload.get(0)) & 0x40) == 0x40;
            nackResponse = ((payload.get(0)) & 0x20) == 0x20;
            nackWaiting = ((payload.get(0)) & 0x10) == 0x10;
            nackQueueFull = ((payload.get(0)) & 0x8) == 0x8;
            nackOptionError = ((payload.get(0)) & 0x4) == 0x4;
        }
        if (payload.size() > 1) {
            headerExtIncluded = ((payload.get(1)) & 0x80) == 0x80;
            zWaveCmdIncluded = ((payload.get(1)) & 0x40) == 0x40;
            moreInformation = ((payload.get(1)) & 0x20) == 0x20;
            secureOrigin = ((payload.get(1)) & 0x10) == 0x10;
        }
        if (payload.size() > 2) {
            seqNo = payload.get(2);
        }
        if (payload.size() > 3) {
            sourceEndPoint = (short) (payload.get(3) & 0x7F);
        }
        if (payload.size() > 4) {
            bitAddress = ((payload.get(4)) & 0x80) == 0x80;
            destinationEndPoint = (short) (payload.get(4) & 0x7F);
        }
        if (payload.size() > 5) {
            if (headerExtIncluded) {
                Short headerExtensionLength = payload.get(5);
                if (payload.size() > 5 + headerExtensionLength) {
                    headerExtension = payload.subList(5, 5 + headerExtensionLength);
                }
                if (zWaveCmdIncluded && payload.size() > 6 + headerExtensionLength) {
                    zWaveCommand = payload.subList(5 + headerExtensionLength, payload.size());
                }
            } else if (zWaveCmdIncluded) {
                this.zWaveCommand = payload.subList(5, payload.size());
            }
        }
    }

    @Override
    public String toString() {
        return "ZipPacket(" +
                "ackRequest: " + ackRequest +
                ", ackResponse: " + ackResponse +
                ", nackResponse: " + nackResponse +
                ", nackWaiting: " + nackWaiting +
                ", nackQueueFull: " + nackQueueFull +
                ", nackOptionError: " + nackOptionError +
                ", headerExtIncluded: " + headerExtIncluded +
                ", zWaveCmdIncluded: " + zWaveCmdIncluded +
                ", moreInformation: " + moreInformation +
                ", secureOrigin: " + secureOrigin +
                ", seqNo: " + seqNo +
                ", sourceEndPoint: " + sourceEndPoint +
                ", bitAddress: " + bitAddress +
                ", destinationEndPoint: " + destinationEndPoint +
                ", headerExtension: " + headerExtension +
                ", zWaveCommand: " + zWaveCommand +
                ')';
    }
}
