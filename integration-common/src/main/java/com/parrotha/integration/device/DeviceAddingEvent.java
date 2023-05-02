package com.parrotha.integration.device;

import java.util.Map;

public class DeviceAddingEvent extends DeviceEvent {
    public DeviceAddingEvent(String deviceNetworkId, Map<String, String> parameters) {
        super(deviceNetworkId, DeviceStatusType.ADDING, Map.of("parameters", parameters));
    }

    public Map getAdditionalParameters() {
        return (Map) ((Map) getEvent()).get("parameters");
    }
}
