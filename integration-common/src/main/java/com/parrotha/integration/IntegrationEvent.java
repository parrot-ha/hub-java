package com.parrotha.integration;

public abstract class IntegrationEvent {
    public enum EventType {
        HUB,
        DEVICE,
        INTEGRATION
    }

    public EventType getEventType() {
        return EventType.INTEGRATION;
    }

    private String integrationId;

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }
}
