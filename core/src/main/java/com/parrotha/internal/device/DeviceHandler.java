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
package com.parrotha.internal.device;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceHandler {
    private String id;
    private String file;
    private String name;
    private String namespace;
    private String author;
    private List<String> capabilityList;
    private List<Command> commandList;
    private List<Attribute> attributeList;
    private List<Fingerprint> fingerprints;

    public DeviceHandler() {
    }

    public DeviceHandler(String id, String file, Map metadata) {
        this.id = id;
        this.file = file;

        //Map dhi = extractDeviceHandlerInformation(scriptCode);
        Map<String, Object> definition = (Map<String, Object>) metadata.get("definition");
        this.name = (String) definition.get("name");
        this.namespace = (String) definition.get("namespace");
        this.author = (String) definition.get("author");
        this.capabilityList = (List<String>) definition.get("capabilityList");
        this.commandList = (List<Command>) definition.get("commandList");
        this.attributeList = (List<Attribute>) definition.get("attributeList");
        List<Map> fingerprintMapList = (List<Map>) metadata.get("fingerprints");
        if (fingerprintMapList != null && fingerprintMapList.size() > 0) {
            this.fingerprints = new ArrayList<>();
            for (Map fingerprintMap : fingerprintMapList) {
                fingerprints.add(new Fingerprint(fingerprintMap));
            }
        }
    }

    /**
     * Check if the 2 DeviceHandlers are equal but ignore the id field
     */
    public boolean equalsIgnoreId(DeviceHandler dh) {
        if (dh == null) return false;
        if (dh == this) return true;
        if (!StringUtils.equals(file, dh.getFile())) return false;
        if (!StringUtils.equals(name, dh.getName())) return false;
        if (!StringUtils.equals(namespace, dh.getNamespace())) return false;
        if (!StringUtils.equals(author, dh.getAuthor())) return false;

        if (capabilityList != null) {
            if (!capabilityList.equals(dh.getCapabilityList())) return false;
        } else if (dh.getCapabilityList() != null) {
            // dh.capabilityList is not null but capabilityList is null, they are not equal
            return false;
        }

        if (commandList != null) {
            if (!commandList.equals(dh.getCommandList())) return false;
        } else if (dh.getCommandList() != null) {
            // dh.commandlist is not null but commandlist is null, they are not equal
            return false;
        }

        if (attributeList != null) {
            if (!attributeList.equals(dh.getAttributeList())) return false;
        } else if (dh.getAttributeList() != null) {
            // dh.attributeList is not null but attributeList is null, they are not equal
            return false;
        }

        if (fingerprints != null) {
            if (!fingerprints.equals(dh.getFingerprints())) return false;
        } else if (dh.getFingerprints() != null) {
            // dh.fingerprints is not null but fingerprints is null, they are not equal
            return false;
        }
        return true;
    }

    private static boolean compareList(List l1, List l2) {
        if ((l1 != null && l2 == null) || (l1 == null && l2 != null))
            return false;
        if (l1 != null && l2 != null) {
            if (l1.size() != l2.size()) {
                return false;
            }
            if (!l1.containsAll(l2) || !l2.containsAll(l1)) {
                return false;
            }
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getCapabilityList() {
        return capabilityList;
    }

    public void setCapabilityList(List<String> capabilityList) {
        this.capabilityList = capabilityList;
    }

    public List<Command> getCommandList() {
        return commandList;
    }

    public void setCommandList(List<Command> commandList) {
        this.commandList = commandList;
    }

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    public List<Fingerprint> getFingerprints() {
        return fingerprints;
    }

    public void setFingerprints(List<Fingerprint> fingerprints) {
        this.fingerprints = fingerprints;
    }

    //- metadata:
    //    definition:
    //      name: Sonoff Temperature and Humidity Sensor
    //      namespace: com.parrotha.drivers
    //      author: Parrot HA
    //      capabilityList: [Sensor, PresenceSensor, Initialize, Refresh, Battery, TemperatureMeasurement,
    //        RelativeHumidityMeasurement, PressureMeasurement]
    //    fingerprints:
    //    - {profileId: '0104', endpointId: '01', inClusters: '0000,0003,0402,0405,0001',
    //      outClusters: '0003', model: TH01, manufacturer: eWeLink, application: '04'}
    //    - {profileId: '0104', endpointId: '01', inClusters: '0000,0001,0402,0405', outClusters: '0019',
    //      model: TS0201, manufacturer: _TZ2000_hjsgdkfl, application: '43'}
    //  file: deviceHandlers/SonoffTemperatureHumiditySensor.groovy
    //  id: 90a36c79-7904-405b-9bf5-4cdd9eb11ad9
}
