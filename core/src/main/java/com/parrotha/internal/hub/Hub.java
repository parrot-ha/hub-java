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
package com.parrotha.internal.hub;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Hub {
    private String id;
    private String name;
    private String type;
    private String hardwareID;

    private Map<String, Object> data;

    public Hub(String id, String name, String type, String hardwareID) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.hardwareID = hardwareID;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHardwareID() {
        return hardwareID;
    }

    public String getFirmwareVersionString() {
        //TODO: pull from version in build.gradle
        return "0.1";
    }

    public boolean isBatteryInUse() {
        return false;
    }

    public String getLocalIP() {
        //TODO: is there a better way to get the ip address?
        if (!getData().containsKey("localIP")) {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                String ip = socket.getLocalAddress().getHostAddress();
                getData().put("localIP", ip);
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return (String) getData().get("localIP");
    }

    public String getLocalSrvPortTCP() {
        //TODO: look up lan integration and get port
        return "39500";
    }

    public Map<String, Object> getData() {
        if (this.data == null)
            this.data = new HashMap<>();
        return this.data;
    }

    public Object getDataValue(String key) {
        if (getData().containsKey(key)) {
            return getData().get(key);
        } else if ("localIP".equals(key)) {
            return getLocalIP();
        } else if ("localSrvPortTCP".equals(key)) {
            return getLocalSrvPortTCP();
        }
        return null;
    }

    public String getType() {
        //TODO: could be virtual if running in the cloud?
        return type;
    }

    public String getZigbeeEui() {
        //TODO return value from zigbee integration
        return null;
    }

    public String getZigbeeId() {
        //TODO return value from zigbee integration
        return null;
    }

    public Map toMap() {
        Map<String, Object> hubMap = new HashMap();
        hubMap.put("id", id);
        hubMap.put("name", name);
        hubMap.put("type", type);
        hubMap.put("hardwareID", hardwareID);
        return hubMap;
    }

}
