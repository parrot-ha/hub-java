package com.parrotha.integration.device;

import com.parrotha.integration.IntegrationEvent;

public class DeviceEvent extends IntegrationEvent {
    private String deviceNetworkId;
    private DeviceStatusType deviceStatus = DeviceStatusType.UNKNOWN;
    private Object event;

    public DeviceEvent() {
    }

    public DeviceEvent(String deviceNetworkId) {
        this.deviceNetworkId = deviceNetworkId;
    }

    public DeviceEvent(String deviceNetworkId, DeviceStatusType deviceStatus, Object event) {
        this.deviceNetworkId = deviceNetworkId;
        this.deviceStatus = deviceStatus;
        this.event = event;
    }

    public DeviceEvent(String deviceNetworkId, Object event) {
        this.deviceNetworkId = deviceNetworkId;
        this.event = event;
    }

    @Override
    public EventType getEventType() {
        return EventType.DEVICE;
    }

    public DeviceStatusType getDeviceStatus() {
        return deviceStatus;
    }

    public Object getEvent() {
        return event;
    }

    public String getDeviceNetworkId() {
        return deviceNetworkId;
    }
}
