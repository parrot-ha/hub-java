package com.parrotha.integration.device;

import com.parrotha.integration.IntegrationEvent;

public class DeviceEvent extends IntegrationEvent {
    private DeviceEventType eventType;
    private DeviceStatusType deviceStatus;
    private Object event;

    public DeviceEvent() {
    }

    public DeviceEvent(DeviceEventType eventType, DeviceStatusType deviceStatus) {
        this.eventType = eventType;
        this.deviceStatus = deviceStatus;
    }

    @Override
    public EventType getEventType() {
        return EventType.DEVICE;
    }
}
