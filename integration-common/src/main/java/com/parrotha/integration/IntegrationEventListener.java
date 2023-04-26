package com.parrotha.integration;

public interface IntegrationEventListener {
    public void messageReceived(IntegrationEvent integrationEvent);
}
