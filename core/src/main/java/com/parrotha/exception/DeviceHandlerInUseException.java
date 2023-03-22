package com.parrotha.exception;

import com.parrotha.internal.device.Device;

import java.util.Collection;

public class DeviceHandlerInUseException extends RuntimeException {
    private Collection<Device> devices;

    public DeviceHandlerInUseException(String message, Collection<Device> devices) {
        super(message);
        this.devices = devices;
    }

    public Collection<Device> getDevices() {
        return devices;
    }
}
