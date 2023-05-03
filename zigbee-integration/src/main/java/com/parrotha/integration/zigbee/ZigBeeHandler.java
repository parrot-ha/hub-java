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

import com.parrotha.integration.device.DeviceAddedEvent;
import com.parrotha.integration.device.DeviceAddingEvent;
import com.parrotha.internal.utils.HexUtils;
import com.zsmartsystems.zigbee.*;
import com.zsmartsystems.zigbee.app.basic.ZigBeeBasicServerExtension;
import com.zsmartsystems.zigbee.app.discovery.ZigBeeDiscoveryExtension;
import com.zsmartsystems.zigbee.app.iasclient.ZigBeeIasCieExtension;
import com.zsmartsystems.zigbee.app.otaserver.ZigBeeOtaUpgradeExtension;
import com.zsmartsystems.zigbee.database.ZigBeeNetworkDataStore;
import com.zsmartsystems.zigbee.dongle.ember.ZigBeeDongleEzsp;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.structure.EmberStatus;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.structure.EmberZdoConfigurationFlags;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.structure.EzspConfigId;
import com.zsmartsystems.zigbee.security.ZigBeeKey;
import com.zsmartsystems.zigbee.serial.ZigBeeSerialPort;
import com.zsmartsystems.zigbee.serialization.DefaultDeserializer;
import com.zsmartsystems.zigbee.serialization.DefaultSerializer;
import com.zsmartsystems.zigbee.transport.ConcentratorConfig;
import com.zsmartsystems.zigbee.transport.ConcentratorType;
import com.zsmartsystems.zigbee.transport.TransportConfig;
import com.zsmartsystems.zigbee.transport.TransportConfigOption;
import com.zsmartsystems.zigbee.transport.TrustCentreJoinMode;
import com.zsmartsystems.zigbee.transport.ZigBeePort;
import com.zsmartsystems.zigbee.transport.ZigBeeTransportTransmit;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.clusters.ZclBasicCluster;
import com.zsmartsystems.zigbee.zcl.clusters.ZclColorControlCluster;
import com.zsmartsystems.zigbee.zcl.clusters.ZclIasZoneCluster;
import com.zsmartsystems.zigbee.zcl.clusters.ZclLevelControlCluster;
import com.zsmartsystems.zigbee.zcl.clusters.ZclOnOffCluster;
import com.zsmartsystems.zigbee.zcl.clusters.ZclPressureMeasurementCluster;
import com.zsmartsystems.zigbee.zcl.clusters.ZclThermostatCluster;
import com.zsmartsystems.zigbee.zcl.clusters.ZclWindowCoveringCluster;
import com.zsmartsystems.zigbee.zdo.ZdoStatus;
import com.zsmartsystems.zigbee.zdo.command.SimpleDescriptorResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ZigBeeHandler implements ZigBeeNetworkStateListener, ZigBeeAnnounceListener, ZigBeeNetworkNodeListener, ZigBeeCommandListener {
    private static final Logger logger = LoggerFactory.getLogger(ZigBeeHandler.class);

    private String serialPortName = "/dev/ttyUSB1";
    private int serialBaud = 57600;
    private ZigBeePort.FlowControl flowControl = ZigBeePort.FlowControl.FLOWCONTROL_OUT_RTSCTS;
    // flag for keeping track of the state of the zigbee integration
    private boolean running = false;
    private int restartCount = 0;

    private ZigBeeNetworkManager networkManager;
    private ZigBeeIntegration zigBeeIntegration;

    public Map<IeeeAddress, Map<String, Object>> joinedDevices = new HashMap<>();

    public ZigBeeHandler(String serialPortName, int serialBaud, ZigBeePort.FlowControl flowControl, ZigBeeIntegration zigBeeIntegration) {
        if (serialPortName != null) {
            this.serialPortName = serialPortName;
        }
        if (flowControl != null) {
            this.flowControl = flowControl;
        }
        if (serialBaud > 0) {
            this.serialBaud = serialBaud;
        }
        this.zigBeeIntegration = zigBeeIntegration;
    }

    public ZigBeeNetworkManager getNetworkManager() {
        return networkManager;
    }

    public boolean joinMode = false;
    private long joinStart = 0;
    Timer timer;

    public ZigBeeStatus permitJoin(int duration) {
        if (duration <= 0) {
            joinMode = false;
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } else {
            joinedDevices = new HashMap<>();
            joinMode = true;
            joinStart = System.currentTimeMillis();
            TimerTask task = new TimerTask() {
                public void run() {
                    joinMode = false;
                    permitJoin(0);
                }
            };
            timer = new Timer("ZigbeeJoinTimer");
            timer.schedule(task, duration * 1000);
        }

        if (networkManager != null) {
            return networkManager.permitJoin(duration);
        } else {
            return ZigBeeStatus.FAILURE;
        }
    }

    private Map<String, SimpleDescriptorResponse> simpleDescriptorResponseMap;

    @Override
    public void commandReceived(ZigBeeCommand command) {
        if (command instanceof SimpleDescriptorResponse && ((SimpleDescriptorResponse) command).getStatus() == ZdoStatus.SUCCESS) {
            if (simpleDescriptorResponseMap == null) {
                simpleDescriptorResponseMap = new HashMap<>();
            }
            String simpleDescriptorSource = String.format("%04X/%d", command.getSourceAddress().getAddress(),
                    ((SimpleDescriptorResponse) command).getSimpleDescriptor().getEndpoint());
            if (logger.isDebugEnabled()) {
                logger.debug("Got simple descriptor response for " + simpleDescriptorSource);
            }
            simpleDescriptorResponseMap.put(simpleDescriptorSource, (SimpleDescriptorResponse) command);
        }
    }

    public void sendZigBeeCommand(ZigBeeCommand command) {
        getNetworkManager().sendTransaction(command);
    }


    public void processZigbeeCommand(String msg) {
        ZigBeeCommand command = ZigBeeMessageTransformer.createCommand(msg, networkManager);
        if (command != null) {
            sendZigBeeCommand(command);
        }
    }

    public ZigBeeNode getNode(final Integer networkAddress) {
        return networkManager.getNode(networkAddress);
    }

    private Set<String> deviceToRemoveList = new HashSet<>();

    public boolean removeDevice(String deviceNetworkId, boolean force) {
        ZigBeeNode node = getNode(HexUtils.hexStringToInt(deviceNetworkId));
        if (node != null) {
            networkManager.leave(node.getNetworkAddress(), node.getIeeeAddress());

            if (force) {
                networkManager.removeNode(node);
                logger.warn("ZigBee device " + deviceNetworkId + " was removed!");
            } else {
                deviceToRemoveList.add(node.getIeeeAddress().toString());

                // wait for node left
                boolean nodeLeft = false;
                int waitTime = 0;
                while (!nodeLeft && waitTime < 10000) {
                    waitTime += 500;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    nodeLeft = (networkManager.getNode(node.getIeeeAddress()) == null);
                }
                return nodeLeft;
            }
        }
        // if we don't have the node it's already removed so also return true.
        return true;
    }

    public void startWithReset(boolean resetNetwork) {
        running = true;
        this.networkManager = initializeZigbee(resetNetwork);
        if (this.networkManager != null) {
            this.networkManager.addNetworkStateListener(this);
            this.networkManager.addAnnounceListener(this);
            this.networkManager.addNetworkNodeListener(this);
            this.networkManager.addCommandListener(this);
        }
    }

    public void stop() {
        running = false;
        if (networkManager != null) {
            networkManager.shutdown();
        }
        networkManager = null;
    }

    public boolean changeChannel(int channel) {
        EmberStatus status = ((ParrotHubZigBeeNetworkManager) networkManager).changeChannel(channel);
        return EmberStatus.EMBER_SUCCESS == status;
    }

    @Override
    public void networkStateUpdated(ZigBeeNetworkState state) {
        logger.info("Network State Updated: " + state.toString());
        if (state == ZigBeeNetworkState.ONLINE) {
            this.restartCount = 0;
        } else if (state == ZigBeeNetworkState.OFFLINE && this.running && this.restartCount < 5) {
            // zigbee shutdown, but it should be running
            this.restartCount++;
            this.stop();
            this.startWithReset(false);
            //TODO: if this fails, we should have a watchdog to start the network again.
        }
    }

    @Override
    public void deviceStatusUpdate(ZigBeeNodeStatus deviceStatus, Integer networkAddress, IeeeAddress ieeeAddress) {
        logger.warn("Device status update " + deviceStatus.toString());

        if (deviceStatus == ZigBeeNodeStatus.UNSECURED_JOIN) {
            logger.warn("New Device Joined!");
            processDeviceJoin(networkAddress, ieeeAddress);
        } else if (deviceStatus == ZigBeeNodeStatus.DEVICE_LEFT) {
            // device left, remove it
            logger.warn("ZigBee Device Left {}", ieeeAddress.toString());
            //remove node only if we are removing the device from the ui
            if (deviceToRemoveList.contains(ieeeAddress.toString())) {
                networkManager.removeNode(networkManager.getNode(ieeeAddress));
            }
        }
    }

    private synchronized void processDeviceJoin(Integer networkAddress, IeeeAddress ieeeAddress) {
        logger.warn("New Device Joined!");

        // check for existing device.
        Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("zigbeeId", ieeeAddress.toString());
        zigBeeIntegration.sendEvent(new DeviceAddingEvent(HexUtils.integerToHexString(networkAddress, 2), additionalParams));
        // place device into list to be initialized once we get the node added message
        if (joinedDevices.containsKey(ieeeAddress) && !networkAddress.equals(joinedDevices.get(ieeeAddress).get("networkAddress"))) {
            // network address changed, update it
            joinedDevices.get(ieeeAddress).put("networkAddress", networkAddress);
        } else {
            joinedDevices.put(ieeeAddress, new HashMap<>(Map.of("networkAddress", networkAddress, "initializing", false)));
        }
    }

    @Override
    public void announceUnknownDevice(Integer networkAddress) {
        logger.warn("Unknown device announcement " + networkAddress);
    }

    @Override
    public void nodeAdded(ZigBeeNode node) {
        logger.debug("Node added: " + node.toString());
        if (checkAndUpdateNodeInitializing(node)) {
            addNode(node);
        }
    }

    private synchronized boolean checkAndUpdateNodeInitializing(ZigBeeNode node) {
        // wait for node added message where node has endpoints.
        if (node.getEndpoints() == null || node.getEndpoints().size() == 0) {
            return false;
        }
        if (joinedDevices == null || !joinedDevices.containsKey(node.getIeeeAddress())) {
            return false;
        }
        if ((boolean) joinedDevices.get(node.getIeeeAddress()).get("initializing")) {
            return false;
        }
        joinedDevices.get(node.getIeeeAddress()).put("initializing", true);
        return true;
    }

    private void addNode(ZigBeeNode node) {
        logger.debug("we have a node that was just joined to the network");

        for (ZigBeeEndpoint zigBeeEndpoint : node.getEndpoints()) {
            logger.debug("New Node Endpoint: " + zigBeeEndpoint.toString());

            Map<String, String> fingerprint = new HashMap<>();

            // check out simple descriptor map for a command:
            String simpleDescriptorSource = String.format("%04X/%d", node.getNetworkAddress(), zigBeeEndpoint.getEndpointId());
            if (simpleDescriptorResponseMap != null && simpleDescriptorResponseMap.containsKey(simpleDescriptorSource)) {
                SimpleDescriptorResponse simpleDescriptorResponse = simpleDescriptorResponseMap.get(simpleDescriptorSource);

                if (simpleDescriptorResponse.getSimpleDescriptor().getInputClusterList().size() > 0) {
                    fingerprint.put("inClusters",
                            HexUtils.integerArrayToHexStringCommaDelimited(simpleDescriptorResponse.getSimpleDescriptor().getInputClusterList(), 2));
                } else {
                    fingerprint.put("inClusters", "");
                }
                if (simpleDescriptorResponse.getSimpleDescriptor().getOutputClusterList().size() > 0) {
                    fingerprint.put("outClusters",
                            HexUtils.integerArrayToHexStringCommaDelimited(simpleDescriptorResponse.getSimpleDescriptor().getOutputClusterList(), 2));
                } else {
                    fingerprint.put("outClusters", "");
                }

            } else {
                if (zigBeeEndpoint.getInputClusterIds().size() > 0) {
                    fingerprint.put("inClusters", HexUtils.integerArrayToHexStringCommaDelimited(zigBeeEndpoint.getInputClusterIds(), 2));
                } else {
                    fingerprint.put("inClusters", "");
                }
                if (zigBeeEndpoint.getOutputClusterIds().size() > 0) {
                    fingerprint.put("outClusters", HexUtils.integerArrayToHexStringCommaDelimited(zigBeeEndpoint.getOutputClusterIds(), 2));
                } else {
                    fingerprint.put("outClusters", "");
                }
            }

            fingerprint.put("profileId", HexUtils.integerToHexString(zigBeeEndpoint.getProfileId(), 2));
            String manufacturer = getBasicAttributeValue(zigBeeEndpoint, 4);
            fingerprint.put("manufacturer", manufacturer);
            String model = getBasicAttributeValue(zigBeeEndpoint, 5);
            fingerprint.put("model", model);

            // add fingerprint to joinedDevices
            joinedDevices.get(node.getIeeeAddress()).put("fingerprint", fingerprint);

            Map<String, String> additionalParams = new HashMap<>();
            additionalParams.put("endpointId", HexUtils.integerToHexString(zigBeeEndpoint.getEndpointId(), 1));
            additionalParams.put("zigbeeId", node.getIeeeAddress().toString());
            Map<String, Object> deviceData = new HashMap<>();
            deviceData.put("endpointId", "01");
            if (StringUtils.isNotBlank(manufacturer)) {
                deviceData.put("manufacturer", manufacturer);
            }
            if (StringUtils.isNotBlank(model)) {
                deviceData.put("model", model);
            }
            // if in join mode or join mode was started in the past 5 minutes, consider it a user initiated add.
            boolean userInitiatedAdd = joinMode || ((System.currentTimeMillis() - joinStart) > 1000 * 60 * 5);
            zigBeeIntegration.sendEvent(
                    new DeviceAddedEvent(HexUtils.integerToHexString(node.getNetworkAddress(), 2), userInitiatedAdd, fingerprint, deviceData,
                            additionalParams));

            return;
        }
    }

    private String getBasicAttributeValue(ZigBeeEndpoint zigBeeEndpoint, int attributeId) {
        String value = "";
        ZclAttribute zclAttribute = zigBeeEndpoint.getInputCluster(0).getAttribute(attributeId);
        if (zclAttribute != null) {
            Object attributeValue = zclAttribute.readValue(5000);
            if (attributeValue != null) {
                value = attributeValue.toString();
            }
        }
        return value;
    }

    @Override
    public void nodeUpdated(ZigBeeNode node) {
        logger.warn("Node updated: " + node.toString());
        //addNode(node);
    }

    @Override
    public void nodeRemoved(ZigBeeNode node) {
        logger.warn("Node removed: " + node.toString());
    }

    private ZigBeeNetworkManager initializeZigbee(boolean resetNetwork) {
        //boolean resetNetwork = false;

        final int defaultProfileId = ZigBeeProfileType.ZIGBEE_HOME_AUTOMATION.getKey();
        final String dongleName = "EMBER";

        //ZigBeePort.FlowControl flowControl = ZigBeePort.FlowControl.FLOWCONTROL_OUT_RTSCTS;

        final ZigBeePort serialPort = new ZigBeeSerialPort(serialPortName, serialBaud, flowControl);

        final ZigBeeTransportTransmit dongle;
        final TransportConfig transportOptions = new TransportConfig();

        final Set<Integer> supportedClientClusters = new TreeSet<>();
        supportedClientClusters.addAll(Stream.of(ZclBasicCluster.CLUSTER_ID, ZclOnOffCluster.CLUSTER_ID, ZclLevelControlCluster.CLUSTER_ID,
                ZclColorControlCluster.CLUSTER_ID, ZclPressureMeasurementCluster.CLUSTER_ID, ZclThermostatCluster.CLUSTER_ID,
                ZclWindowCoveringCluster.CLUSTER_ID, ZclIasZoneCluster.CLUSTER_ID).collect(Collectors.toSet()));

        final Set<Integer> supportedServerClusters = new TreeSet<>();
        supportedServerClusters.addAll(Stream.of(ZclBasicCluster.CLUSTER_ID, ZclOnOffCluster.CLUSTER_ID, ZclLevelControlCluster.CLUSTER_ID,
                ZclColorControlCluster.CLUSTER_ID, ZclPressureMeasurementCluster.CLUSTER_ID, ZclWindowCoveringCluster.CLUSTER_ID,
                ZclIasZoneCluster.CLUSTER_ID).collect(Collectors.toSet()));


        ZigBeeDongleEzsp emberDongle = new ParrotHubZigBeeDongleEzsp(serialPort);
        dongle = emberDongle;

        emberDongle.updateDefaultConfiguration(EzspConfigId.EZSP_CONFIG_SOURCE_ROUTE_TABLE_SIZE, 32);
        emberDongle.updateDefaultConfiguration(EzspConfigId.EZSP_CONFIG_APS_UNICAST_MESSAGE_COUNT, 16);
        emberDongle.updateDefaultConfiguration(EzspConfigId.EZSP_CONFIG_NEIGHBOR_TABLE_SIZE, 24);
        // allow the framework to handle match descriptor request
        emberDongle.updateDefaultConfiguration(EzspConfigId.EZSP_CONFIG_APPLICATION_ZDO_FLAGS,
                EmberZdoConfigurationFlags.EMBER_APP_HANDLES_ZDO_ENDPOINT_REQUESTS.getKey());

        transportOptions.addOption(TransportConfigOption.RADIO_TX_POWER, 8);

        // Configure the concentrator
        // Max Hops defaults to system max
        ConcentratorConfig concentratorConfig = new ConcentratorConfig();
        concentratorConfig.setType(ConcentratorType.HIGH_RAM);
        concentratorConfig.setMaxFailures(8);
        concentratorConfig.setMaxHops(0);
        concentratorConfig.setRefreshMinimum(60);
        concentratorConfig.setRefreshMaximum(3600);
        transportOptions.addOption(TransportConfigOption.CONCENTRATOR_CONFIG, concentratorConfig);

        emberDongle.setEmberNcpResetProvider(new EmberNcpHardwareReset());


        //ZigBeeNetworkManager networkManager = new ZigBeeNetworkManager(dongle);
        ZigBeeNetworkManager networkManager = new ParrotHubZigBeeNetworkManager(dongle, zigBeeIntegration);

        ZigBeeNetworkDataStore dataStore = new ZigBeeYamlDataStore(dongleName);
        if (resetNetwork) {
            // networkStateSerializer.remove();
        }
        networkManager.setNetworkDataStore(dataStore);
        networkManager.setSerializer(DefaultSerializer.class, DefaultDeserializer.class);

        // Initialise the network
        ZigBeeStatus initResponse = networkManager.initialize();
        logger.debug("networkManager.initialize returned " + initResponse);
        if (initResponse != ZigBeeStatus.SUCCESS) {
            return null;
        }

        logger.debug("PAN ID          = " + networkManager.getZigBeePanId());
        logger.debug("Extended PAN ID = " + networkManager.getZigBeeExtendedPanId());
        logger.debug("Channel         = " + networkManager.getZigBeeChannel());

        if (resetNetwork == true) {
            ZigBeeKey nwkKey;
            ZigBeeKey linkKey;
            ExtendedPanId extendedPan;
            Integer channel;
            int pan;

            channel = 11;
            pan = 1;
            extendedPan = new ExtendedPanId();
            nwkKey = ZigBeeKey.createRandom();
            linkKey = new ZigBeeKey(new int[]{0x5A, 0x69, 0x67, 0x42, 0x65, 0x65, 0x41, 0x6C, 0x6C, 0x69, 0x61, 0x6E, 0x63, 0x65, 0x30, 0x39});

            logger.debug("*** Resetting network");
            logger.debug("  * Channel                = " + channel);
            logger.debug("  * PAN ID                 = " + pan);
            logger.debug("  * Extended PAN ID        = " + extendedPan);
            logger.debug("  * Link Key               = " + linkKey);
            if (nwkKey.hasOutgoingFrameCounter()) {
                logger.debug("  * Link Key Frame Cnt     = " + linkKey.getOutgoingFrameCounter());
            }
            logger.debug("  * Network Key            = " + nwkKey);
            if (nwkKey.hasOutgoingFrameCounter()) {
                logger.debug("  * Network Key Frame Cnt  = " + nwkKey.getOutgoingFrameCounter());
            }

            networkManager.setZigBeeChannel(ZigBeeChannel.UNKNOWN);
            networkManager.setZigBeePanId(pan);
            networkManager.setZigBeeExtendedPanId(extendedPan);
            networkManager.setZigBeeNetworkKey(nwkKey);
            networkManager.setZigBeeLinkKey(linkKey);
        }

        networkManager.setDefaultProfileId(defaultProfileId);

        transportOptions.addOption(TransportConfigOption.TRUST_CENTRE_JOIN_MODE, TrustCentreJoinMode.TC_JOIN_SECURE);

        // Add the default ZigBeeAlliance09 HA link key

        transportOptions.addOption(TransportConfigOption.TRUST_CENTRE_LINK_KEY,
                new ZigBeeKey(new int[]{0x5A, 0x69, 0x67, 0x42, 0x65, 0x65, 0x41, 0x6C, 0x6C, 0x69, 0x61, 0x6E, 0x63, 0x65, 0x30, 0x39}));
        // transportOptions.addOption(TransportConfigOption.TRUST_CENTRE_LINK_KEY, new ZigBeeKey(new int[] { 0x41, 0x61,
        // 0x8F, 0xC0, 0xC8, 0x3B, 0x0E, 0x14, 0xA5, 0x89, 0x95, 0x4B, 0x16, 0xE3, 0x14, 0x66 }));

        dongle.updateTransportConfig(transportOptions);

        // Add the extensions to the network
        //TODO: the framework seems to be attempting to read/set the enrolled status on every boot, should fix this.
        networkManager.addExtension(new ZigBeeIasCieExtension());
        networkManager.addExtension(new ZigBeeOtaUpgradeExtension());
        networkManager.addExtension(new ZigBeeBasicServerExtension());

        ZigBeeDiscoveryExtension discoveryExtension = new ZigBeeDiscoveryExtension();
        discoveryExtension.setUpdatePeriod(0);
        discoveryExtension.setUpdateOnChange(false);
        networkManager.addExtension(discoveryExtension);

        supportedClientClusters.stream().forEach(clusterId -> networkManager.addSupportedClientCluster(clusterId));
        supportedServerClusters.stream().forEach(clusterId -> networkManager.addSupportedServerCluster(clusterId));

        if (networkManager.startup(resetNetwork) != ZigBeeStatus.SUCCESS) {
            logger.debug("ZigBee starting up ... [FAIL]");
        } else {
            logger.debug("ZigBee starting up ... [OK]");
        }

        return networkManager;
    }
}
