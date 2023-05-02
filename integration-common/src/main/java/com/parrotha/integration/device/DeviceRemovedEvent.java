package com.parrotha.integration.device;

public class DeviceRemovedEvent extends DeviceEvent {
    public DeviceRemovedEvent(String deviceNetworkId) {
        super(deviceNetworkId, DeviceStatusType.REMOVED);
    }
}
