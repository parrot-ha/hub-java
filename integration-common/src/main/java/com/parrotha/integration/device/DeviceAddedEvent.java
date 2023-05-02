package com.parrotha.integration.device;

import java.util.HashMap;
import java.util.Map;

public class DeviceAddedEvent extends DeviceEvent {
    public DeviceAddedEvent(String deviceNetworkId, Map<String, String> fingerprint) {
        super(deviceNetworkId, DeviceStatusType.ADDED, Map.of("fingerprint", fingerprint, "parameters", new HashMap<>(), "data", new HashMap<>()));
    }

    public DeviceAddedEvent(String deviceNetworkId, Map<String, String> fingerprint, Map<String, Object> data, Map<String, String> parameters) {
        super(deviceNetworkId, DeviceStatusType.ADDED, Map.of("fingerprint", fingerprint, "data", data, "parameters", parameters));
    }

    public Map getFingerprint() {
        return (Map) ((Map) getEvent()).get("fingerprint");
    }

    public Map getAdditionalParameters() {
        return (Map) ((Map) getEvent()).get("parameters");
    }

    public Map getData() {
        return (Map) ((Map) getEvent()).get("data");
    }
}
