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
package com.parrotha.integration.zigbee;

import com.parrotha.device.HubAction;
import com.parrotha.device.HubResponse;
import com.parrotha.device.Protocol;
import com.parrotha.integration.DeviceIntegration;
import com.parrotha.integration.extension.DeviceScanIntegrationExtension;
import com.parrotha.integration.extension.ResetIntegrationExtension;
import com.parrotha.internal.utils.HexUtils;
import com.parrotha.ui.PreferencesBuilder;
import com.zsmartsystems.zigbee.IeeeAddress;
import com.zsmartsystems.zigbee.ZigBeeNode;
import com.zsmartsystems.zigbee.ZigBeeStatus;
import com.zsmartsystems.zigbee.transport.ZigBeePort;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ZigBeeIntegration extends DeviceIntegration implements DeviceScanIntegrationExtension, ResetIntegrationExtension {
    private static final Logger logger = LoggerFactory.getLogger(ZigBeeIntegration.class);

    private ZigBeeHandler zigBeeHandler;

    private static final List<String> tags = List.of("PROTOCOL_ZIGBEE");

    public ZigBeeIntegration() {
    }

    @Override
    public void settingValueChanged(List<String> keys) {
        logger.debug("values changed " + keys);
        if (keys.contains("serialPortName") || keys.contains("serialPortBaud") || keys.contains("serialPortFlowControl")) {
            // restart the integration
            this.stop();
            this.start();
        }

        if (keys.contains("zigbeeChannel")) {
            // change the channel of the zigbee radio
            Object channelObj = getSettingAsString("zigbeeChannel");
            if (channelObj instanceof String && NumberUtils.isCreatable((String) channelObj)) {
                int channel = NumberUtils.createInteger((String) channelObj);
                if (channel > 10 && channel < 27) {
                    zigBeeHandler.changeChannel(channel);
                }
            }
        }
    }

    @Override
    public Map<String, Object> getPreferencesLayout() {
        return new PreferencesBuilder()
                .withTextInput("serialPortName",
                        "Serial Port Name",
                        "Serial Port Name",
                        true,
                        true)
                .withEnumInput("serialPortBaud",
                        "Serial Port Baud",
                        "Serial Port Baud",
                        Arrays.asList("57600", "115200"),
                        false,
                        true,
                        true)
                .withEnumInput("serialPortFlowControl",
                        "Serial Port Flow Control",
                        "Serial Port Flow Control",
                        Arrays.asList("Software (XOn / XOff)", "Hardware (RTS / CTS)"),
                        false,
                        true,
                        true)
                .withEnumInput("zigbeeChannel",
                        "ZigBee Channel",
                        "ZigBee Channel",
                        Arrays.asList("Use Existing Value", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25",
                                "26"),
                        false,
                        false,
                        true)
                .build();
    }

    @Override
    public void start() {
        String serialPortName = getSettingAsString("serialPortName");
        Object serialPortFlowControl = getSettingAsString("serialPortFlowControl");
        ZigBeePort.FlowControl flowControl = null;
        if (serialPortFlowControl instanceof String) {
            if (((String) serialPortFlowControl).startsWith("Hardware")) {
                flowControl = ZigBeePort.FlowControl.FLOWCONTROL_OUT_RTSCTS;
            } else if (((String) serialPortFlowControl).startsWith("Software")) {
                flowControl = ZigBeePort.FlowControl.FLOWCONTROL_OUT_XONOFF;
            }
        }

        Integer serialBaud = getSettingAsInteger("serialPortBaud", 0);
        zigBeeHandler = new ZigBeeHandler(serialPortName, serialBaud, flowControl, this);
        zigBeeHandler.startWithReset(false);
    }

    @Override
    public void stop() {
        zigBeeHandler.stop();
    }

    @Override
    public Map<String, String> getDisplayInformation() {
        Map<String, String> model = new HashMap<>();
        if (zigBeeHandler != null && zigBeeHandler.getNetworkManager() != null) {
            model.put("Channel", String.valueOf(zigBeeHandler.getNetworkManager().getZigBeeChannel().getChannel()));
            model.put("Extended Pan ID", zigBeeHandler.getNetworkManager().getZigBeeExtendedPanId().toString());
            model.put("Pan ID", HexUtils.integerToHexString(zigBeeHandler.getNetworkManager().getZigBeePanId(), 2));
            model.put("EUI", zigBeeHandler.getNetworkManager().getLocalIeeeAddress().toString());
            model.put("Node ID", HexUtils.integerToHexString(zigBeeHandler.getNetworkManager().getLocalNwkAddress(), 2));
            model.put("Status", "Online");
        } else {
            model.put("Status", "Offline");
        }
        return model;
    }

    @Override
    public Future<Boolean> removeIntegrationDeviceAsync(String deviceNetworkId, boolean force) {
        //TODO: update zigbee handler to return future
        return CompletableFuture.completedFuture(zigBeeHandler.removeDevice(deviceNetworkId, force));
    }

    @Override
    public boolean reset(Map options) {
        stop();
        zigBeeHandler.startWithReset(true);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public String getResetWarning() {
        ResourceBundle messages = ResourceBundle.getBundle("com.parrotha.zigbee.MessageBundle");
        return messages.getString("reset.warning");
    }

    @Override
    public boolean startScan(Map options) {
        if (!zigBeeHandler.joinMode) {
            ZigBeeStatus status = zigBeeHandler.permitJoin(90);
            if (status == ZigBeeStatus.SUCCESS) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean stopScan(Map options) {
        if (zigBeeHandler.joinMode) {
            ZigBeeStatus status = zigBeeHandler.permitJoin(0);
            if (status == ZigBeeStatus.SUCCESS) {
                return true;
            }
        }
        return false;
    }

    //TODO, make this an object instead of a map so that we enforce the response contents
    @Override
    public Map getScanStatus(Map options) {
        Map<String, Object> scanStatus = new HashMap<>();
        scanStatus.put("running", zigBeeHandler.joinMode);
        if (zigBeeHandler.joinedDevices != null && zigBeeHandler.joinedDevices.size() > 0) {
            List<Map<String, String>> joinedDevicesList = new ArrayList<>();
            for (Map.Entry<IeeeAddress, Map<String, Object>> entry : zigBeeHandler.joinedDevices.entrySet()) {
                Map<String, String> joinedDeviceMap = new HashMap<>();
                joinedDeviceMap.put("Network Address", HexUtils.integerToHexString((Integer) entry.getValue().get("networkAddress"), 2));
                joinedDeviceMap.put("IEEE Address", entry.getKey().toString());

                Object fingerprint = entry.getValue().get("fingerprint");
                if (fingerprint != null) {
                    joinedDeviceMap.put("Fingerprint", fingerprint.toString());
                    joinedDeviceMap.put("Join Status", "Done");
                } else {
                    joinedDeviceMap.put("Join Status", "Initializing");
                }
                joinedDevicesList.add(joinedDeviceMap);
            }
            scanStatus.put("foundDevices", joinedDevicesList);
        }
        return scanStatus;
    }

    @Override
    public String getName() {
        return "ZigBee";
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.ZIGBEE;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public String getDescription() {
        ResourceBundle messages = ResourceBundle.getBundle("com.parrotha.zigbee.MessageBundle");
        return messages.getString("integration.description");
    }

    public ZigBeeNode getNode(final Integer networkAddress) {
        return zigBeeHandler.getNetworkManager().getNode(networkAddress);
    }

    @Override
    public HubResponse processAction(HubAction hubAction) {
        zigBeeHandler.processZigbeeCommand(hubAction.getAction());
        return null;
    }
}
