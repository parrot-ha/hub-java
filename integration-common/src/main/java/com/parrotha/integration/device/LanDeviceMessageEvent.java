package com.parrotha.integration.device;

public class LanDeviceMessageEvent extends DeviceMessageEvent {
    public LanDeviceMessageEvent(String deviceNetworkId, String message) {
        super(deviceNetworkId, message);
    }

    public LanDeviceMessageEvent(String macAddress, String remoteAddress, int remotePort, String message) {
        super(macAddress, message);
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    private String remoteAddress;
    private int remotePort;


    public String getRemoteAddress() {
        return remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getMacAddress() {
        return getDeviceNetworkId();
    }
}
