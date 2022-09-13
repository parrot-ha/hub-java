/**
 * Copyright (c) 2021-2022 by the respective copyright holders.
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
package com.parrotha.internal.device;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Fingerprint {
    private String profileId;
    private String endpointId;
    private String inClusters;
    private String sortedInClusters;
    private String outClusters;
    private String sortedOutClusters;
    private String model;
    private String manufacturer;
    private String application;
    private String deviceJoinName;
    private String mfr;
    private String prod;
    private String intg;


    public Fingerprint() {
    }

    public Fingerprint(Map<String, String> values) {
        this.profileId = values.get("profileId");
        this.endpointId = values.get("endpointId");
        this.inClusters = values.get("inClusters");
        this.outClusters = values.get("outClusters");
        this.model = values.get("model");
        this.manufacturer = values.get("manufacturer");
        this.application = values.get("application");
        this.deviceJoinName = values.get("deviceJoinName");
        this.mfr = values.get("mfr");
        this.prod = values.get("prod");
        this.intg = values.get("intg");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Fingerprint) {
            if (!StringUtils.equals(this.profileId, ((Fingerprint) obj).getProfileId())) return false;
            if (!StringUtils.equals(this.endpointId, ((Fingerprint) obj).getEndpointId())) return false;
            if (!StringUtils.equals(this.inClusters, ((Fingerprint) obj).getInClusters())) return false;
            if (!StringUtils.equals(this.outClusters, ((Fingerprint) obj).getOutClusters())) return false;
            if (!StringUtils.equals(this.model, ((Fingerprint) obj).getModel())) return false;
            if (!StringUtils.equals(this.manufacturer, ((Fingerprint) obj).getManufacturer())) return false;
            if (!StringUtils.equals(this.application, ((Fingerprint) obj).getApplication())) return false;
            if (!StringUtils.equals(this.deviceJoinName, ((Fingerprint) obj).getDeviceJoinName())) return false;
            if (!StringUtils.equals(this.mfr, ((Fingerprint) obj).getMfr())) return false;
            if (!StringUtils.equals(this.prod, ((Fingerprint) obj).getProd())) return false;
            if (!StringUtils.equals(this.intg, ((Fingerprint) obj).getIntg())) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, endpointId, inClusters, outClusters, model, manufacturer, application, deviceJoinName);
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String getInClusters() {
        return inClusters;
    }

    public String getSortedInClusters() {
        if(inClusters != null && sortedInClusters == null) {
            sortedInClusters = Arrays.stream(inClusters.split(",")).sorted().collect(Collectors.joining(","));
        }
        return sortedInClusters;
    }

    public void setInClusters(String inClusters) {
        this.inClusters = inClusters;
    }

    public String getOutClusters() {
        return outClusters;
    }

    public String getSortedOutClusters() {
        if(outClusters != null && sortedOutClusters == null) {
            sortedOutClusters = Arrays.stream(outClusters.split(",")).sorted().collect(Collectors.joining(","));
        }
        return sortedOutClusters;
    }

    public void setOutClusters(String outClusters) {
        this.outClusters = outClusters;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getDeviceJoinName() {
        return deviceJoinName;
    }

    public void setDeviceJoinName(String deviceJoinName) {
        this.deviceJoinName = deviceJoinName;
    }

    public String getMfr() {
        return mfr;
    }

    public void setMfr(String mfr) {
        this.mfr = mfr;
    }

    public String getProd() {
        return prod;
    }

    public void setProd(String prod) {
        this.prod = prod;
    }

    public String getIntg() {
        return intg;
    }

    public void setIntg(String intg) {
        this.intg = intg;
    }

    @Override
    public String toString() {
        return "Fingerprint(" +
                "profileId: '" + profileId + '\'' +
                ", endpointId: '" + endpointId + '\'' +
                ", inClusters: '" + inClusters + '\'' +
                ", outClusters: '" + outClusters + '\'' +
                ", model: '" + model + '\'' +
                ", manufacturer: '" + manufacturer + '\'' +
                ", application: '" + application + '\'' +
                ", deviceJoinName: '" + deviceJoinName + '\'' +
                ", mfr: '" + mfr + '\'' +
                ", prod: '" + prod + '\'' +
                ", intg: '" + intg + '\'' +
                ')';
    }
}
