package com.parrotha.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class HubResponseTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link HubResponse#HubResponse()}
     *   <li>{@link HubResponse#setBody(String)}
     *   <li>{@link HubResponse#setCallback(String)}
     *   <li>{@link HubResponse#getBody()}
     *   <li>{@link HubResponse#getCallback()}
     * </ul>
     */
    @Test
    void testConstructor() {
        HubResponse actualHubResponse = new HubResponse();
        actualHubResponse.setBody("Not all who wander are lost");
        actualHubResponse.setCallback("Callback");
        assertEquals("Not all who wander are lost", actualHubResponse.getBody());
        assertEquals("Callback", actualHubResponse.getCallback());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link HubResponse#HubResponse(String)}
     *   <li>{@link HubResponse#setBody(String)}
     *   <li>{@link HubResponse#setCallback(String)}
     *   <li>{@link HubResponse#getBody()}
     *   <li>{@link HubResponse#getCallback()}
     * </ul>
     */
    @Test
    void testConstructor2() {
        HubResponse actualHubResponse = new HubResponse("Not all who wander are lost");
        actualHubResponse.setBody("Not all who wander are lost");
        actualHubResponse.setCallback("Callback");
        assertEquals("Not all who wander are lost", actualHubResponse.getBody());
        assertEquals("Callback", actualHubResponse.getCallback());
    }

    /**
     * Method under test: {@link HubResponse#getXml()}
     */
    @Test
    void testGetXml() {
        assertNull((new HubResponse("Not all who wander are lost")).getXml());
    }

    /**
     * Method under test: {@link HubResponse#getXml()}
     */
    @Test
    void testGetXml2() {
        HubResponse hubResponse = new HubResponse("Not all who wander are lost");
        hubResponse.setBody("<msg>Not all who wander are lost</msg>");
        assertNotNull(hubResponse.getXml());
        assertEquals("Not all who wander are lost", hubResponse.getXml().text());
        assertEquals("msg", hubResponse.getXml().name());
    }
}

