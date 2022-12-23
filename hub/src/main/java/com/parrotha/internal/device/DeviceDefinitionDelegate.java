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

import com.parrotha.internal.entity.LiveLogger;
import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceDefinitionDelegate {
    private LiveLogger log = null;

    public LiveLogger getLog() {
        if (log == null) {
            log = new LiveLogger("parrothub.live.dev.temporary");
        }
        return log;
    }

    public DeviceDefinitionDelegate() {
    }

    // from st, if you call log.debug metadata you get the following:
    // [tiles:[:], preferences:[sections:[], defaults:true]]  this is the base for the metadata object, the rest needs
    // to be populated

    public Map metadataValue;

    Map metadata(Closure closure) {
        metadataValue = new HashMap();
        closure.run();
        return metadataValue;
    }

    // [name:Cree Bulb Custom, namespace:smartthings, author:SmartThings, ocfDeviceType:oic.d.light, runLocally:true, executeCommandsLocally:true, minHubCoreVersion:000.022.0004]
    void definition(Map map, Closure closure) {
        metadataValue.put("definition", map);
        closure.run();
    }

    void preferences(Closure closure) {
        // we don't need preferences right now, just getting definition
    }

    void capability(String capability) {
        Map definitionSection = (Map) metadataValue.get("definition");
        if (definitionSection != null) {
            List<String> capabilityList;
            if (definitionSection.get("capabilityList") == null) {
                capabilityList = new ArrayList<>();
                definitionSection.put("capabilityList", capabilityList);
            } else {
                capabilityList = (List) definitionSection.get("capabilityList");
            }
            capabilityList.add(capability);
        }
    }

    void command(String name) {
        command(name, null);
    }

    void command(String name, List<Object> arguments) {
        Map definitionSection = (Map) metadataValue.get("definition");
        DeviceDelegateHelper.command(definitionSection, name, arguments);
    }

    void attribute(String attributeName, String attributeType) {
        attribute(attributeName, attributeType, null);
    }

    void attribute(String attributeName, String attributeType, List possibleValues) {
        Map definitionSection = (Map) metadataValue.get("definition");
        if (definitionSection != null) {
            List<Attribute> attributeList;
            if (definitionSection.get("attributeList") == null) {
                attributeList = new ArrayList<>();
                definitionSection.put("attributeList", attributeList);
            } else {
                attributeList = (List<Attribute>) definitionSection.get("attributeList");
            }
            attributeList.add(new Attribute("", attributeType, attributeName, possibleValues));
        }
    }

    void simulator(Closure closure) {
    }

    void tiles(Closure closure) {
    }
    // example fingerprint return:
    //[[manufacturer:CREE, model:Connected A-19 60W Equivalent, deviceJoinName:Cree Light], [manufacturer:CREE, model:Connected A-19 60W Equivalent, deviceJoinName:Cree Light]]
    // from:fingerprint manufacturer: "CREE", model: "Connected A-19 60W Equivalent" , deviceJoinName: "Cree Light"// 0A C05E 0100 02 07 0000 1000 0004 0003 0005 0006 0008 02 0000 0019

    //fingerprint manufacturer: "CREE", model: "Connected A-19 60W Equivalent" , deviceJoinName: "Cree Light"// 0A C05E 0100 02 07 0000 1000 0004 0003 0005 0006 0008 02 0000 0019
    //fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,1000", outClusters: "0019", manufacturer: "GE_Appliances", model: "ZLL Light", deviceJoinName: "GE Light" //GE Link Bulb
    //fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,1000", outClusters: "0019", manufacturer: "GE", model: "SoftWhite", deviceJoinName: "GE Light" //GE Link Soft White Bulb
    //fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,1000", outClusters: "0019", manufacturer: "GE", model: "Daylight", deviceJoinName: "GE Light" //GE Link Daylight Bulb

    //[[manufacturer:CREE, model:Connected A-19 60W Equivalent, deviceJoinName:Cree Light],
    // [profileId:0104, inClusters:0000,0003,0004,0005,0006,0008,1000, outClusters:0019, manufacturer:GE_Appliances, model:ZLL Light, deviceJoinName:GE Light],
    //[profileId:0104, inClusters:0000,0003,0004,0005,0006,0008,1000, outClusters:0019, manufacturer:GE, model:SoftWhite, deviceJoinName:GE Light],
    // [profileId:0104, inClusters:0000,0003,0004,0005,0006,0008,1000, outClusters:0019, manufacturer:GE, model:Daylight, deviceJoinName:GE Light],
    void fingerprint(Map map) {
        if (metadataValue != null) {
            List<Map> fingerprints = (List<Map>) metadataValue.get("fingerprints");
            if (fingerprints == null) {
                fingerprints = new ArrayList<Map>();
                metadataValue.put("fingerprints", fingerprints);
            }
            fingerprints.add(map);
        }
    }

    void tiles(Map options, Closure closure) {
    }
}
