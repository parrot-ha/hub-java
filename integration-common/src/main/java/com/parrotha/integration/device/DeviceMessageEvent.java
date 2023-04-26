package com.parrotha.integration.device;

public class DeviceMessageEvent extends DeviceEvent {
    private String deviceNetworkId;
    private String message;

    public DeviceMessageEvent(String deviceNetworkId, String message) {
        this.deviceNetworkId = deviceNetworkId;
        this.message = message;
    }

    public String getDeviceNetworkId() {
        return deviceNetworkId;
    }

    public String getMessage() {
        return message;
    }
}
