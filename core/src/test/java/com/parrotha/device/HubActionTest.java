package com.parrotha.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ClassInfo;

import org.codehaus.groovy.runtime.dgmimpl.NumberNumberDiv;
import org.junit.jupiter.api.Disabled;

import org.junit.jupiter.api.Test;

class HubActionTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link HubAction#HubAction()}
     *   <li>{@link HubAction#setAction(String)}
     *   <li>{@link HubAction#setDni(String)}
     *   <li>{@link HubAction#setOptions(Map)}
     *   <li>{@link HubAction#setProtocol(Protocol)}
     *   <li>{@link HubAction#setResponseType(String)}
     *   <li>{@link HubAction#getAction()}
     *   <li>{@link HubAction#getDni()}
     *   <li>{@link HubAction#getOptions()}
     *   <li>{@link HubAction#getProtocol()}
     *   <li>{@link HubAction#getResponseType()}
     *   <li>{@link HubAction#toString()}
     * </ul>
     */
    @Test
    void testConstructor() {
        HubAction actualHubAction = new HubAction();
        actualHubAction.setAction("Action");
        actualHubAction.setDni("Dni");
        HashMap<Object, Object> options = new HashMap<>();
        actualHubAction.setOptions(options);
        actualHubAction.setProtocol(Protocol.LAN);
        actualHubAction.setResponseType("Response Type");
        assertEquals("Action", actualHubAction.getAction());
        assertEquals("Dni", actualHubAction.getDni());
        assertSame(options, actualHubAction.getOptions());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertEquals("Response Type", actualHubAction.getResponseType());
        assertEquals("Action", actualHubAction.toString());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link HubAction#HubAction(String)}
     *   <li>{@link HubAction#setAction(String)}
     *   <li>{@link HubAction#setDni(String)}
     *   <li>{@link HubAction#setOptions(Map)}
     *   <li>{@link HubAction#setProtocol(Protocol)}
     *   <li>{@link HubAction#setResponseType(String)}
     *   <li>{@link HubAction#getAction()}
     *   <li>{@link HubAction#getDni()}
     *   <li>{@link HubAction#getOptions()}
     *   <li>{@link HubAction#getProtocol()}
     *   <li>{@link HubAction#getResponseType()}
     *   <li>{@link HubAction#toString()}
     * </ul>
     */
    @Test
    void testConstructor2() {
        HubAction actualHubAction = new HubAction("Action");
        actualHubAction.setAction("Action");
        actualHubAction.setDni("Dni");
        HashMap<Object, Object> options = new HashMap<>();
        actualHubAction.setOptions(options);
        actualHubAction.setProtocol(Protocol.LAN);
        actualHubAction.setResponseType("Response Type");
        assertEquals("Action", actualHubAction.getAction());
        assertEquals("Dni", actualHubAction.getDni());
        assertSame(options, actualHubAction.getOptions());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertEquals("Response Type", actualHubAction.getResponseType());
        assertEquals("Action", actualHubAction.toString());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link HubAction#HubAction(String, Protocol)}
     *   <li>{@link HubAction#setAction(String)}
     *   <li>{@link HubAction#setDni(String)}
     *   <li>{@link HubAction#setOptions(Map)}
     *   <li>{@link HubAction#setProtocol(Protocol)}
     *   <li>{@link HubAction#setResponseType(String)}
     *   <li>{@link HubAction#getAction()}
     *   <li>{@link HubAction#getDni()}
     *   <li>{@link HubAction#getOptions()}
     *   <li>{@link HubAction#getProtocol()}
     *   <li>{@link HubAction#getResponseType()}
     *   <li>{@link HubAction#toString()}
     * </ul>
     */
    @Test
    void testConstructor3() {
        HubAction actualHubAction = new HubAction("Action", Protocol.LAN);
        actualHubAction.setAction("Action");
        actualHubAction.setDni("Dni");
        HashMap<Object, Object> options = new HashMap<>();
        actualHubAction.setOptions(options);
        actualHubAction.setProtocol(Protocol.LAN);
        actualHubAction.setResponseType("Response Type");
        assertEquals("Action", actualHubAction.getAction());
        assertEquals("Dni", actualHubAction.getDni());
        assertSame(options, actualHubAction.getOptions());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertEquals("Response Type", actualHubAction.getResponseType());
        assertEquals("Action", actualHubAction.toString());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link HubAction#HubAction(String, Protocol, String)}
     *   <li>{@link HubAction#setAction(String)}
     *   <li>{@link HubAction#setDni(String)}
     *   <li>{@link HubAction#setOptions(Map)}
     *   <li>{@link HubAction#setProtocol(Protocol)}
     *   <li>{@link HubAction#setResponseType(String)}
     *   <li>{@link HubAction#getAction()}
     *   <li>{@link HubAction#getDni()}
     *   <li>{@link HubAction#getOptions()}
     *   <li>{@link HubAction#getProtocol()}
     *   <li>{@link HubAction#getResponseType()}
     *   <li>{@link HubAction#toString()}
     * </ul>
     */
    @Test
    void testConstructor4() {
        HubAction actualHubAction = new HubAction("Action", Protocol.LAN, "Dni");
        actualHubAction.setAction("Action");
        actualHubAction.setDni("Dni");
        HashMap<Object, Object> options = new HashMap<>();
        actualHubAction.setOptions(options);
        actualHubAction.setProtocol(Protocol.LAN);
        actualHubAction.setResponseType("Response Type");
        assertEquals("Action", actualHubAction.getAction());
        assertEquals("Dni", actualHubAction.getDni());
        assertSame(options, actualHubAction.getOptions());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertEquals("Response Type", actualHubAction.getResponseType());
        assertEquals("Action", actualHubAction.toString());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link HubAction#HubAction(String, Protocol, String, Map)}
     *   <li>{@link HubAction#setAction(String)}
     *   <li>{@link HubAction#setDni(String)}
     *   <li>{@link HubAction#setOptions(Map)}
     *   <li>{@link HubAction#setProtocol(Protocol)}
     *   <li>{@link HubAction#setResponseType(String)}
     *   <li>{@link HubAction#getAction()}
     *   <li>{@link HubAction#getDni()}
     *   <li>{@link HubAction#getOptions()}
     *   <li>{@link HubAction#getProtocol()}
     *   <li>{@link HubAction#getResponseType()}
     *   <li>{@link HubAction#toString()}
     * </ul>
     */
    @Test
    void testConstructor5() {
        HubAction actualHubAction = new HubAction("Action", Protocol.LAN, "Dni", new HashMap<>());
        actualHubAction.setAction("Action");
        actualHubAction.setDni("Dni");
        HashMap<Object, Object> options = new HashMap<>();
        actualHubAction.setOptions(options);
        actualHubAction.setProtocol(Protocol.LAN);
        actualHubAction.setResponseType("Response Type");
        assertEquals("Action", actualHubAction.getAction());
        assertEquals("Dni", actualHubAction.getDni());
        assertSame(options, actualHubAction.getOptions());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertEquals("Response Type", actualHubAction.getResponseType());
        assertEquals("Action", actualHubAction.toString());
    }

    /**
     * Method under test: {@link HubAction#HubAction(Map)}
     */
    @Test
    void testConstructor6() {
        HubAction actualHubAction = new HubAction(new HashMap<>());
        assertEquals("POST / HTTP/1.1\r\nAccept: */*\r\nUser-Agent: Linux UPnP/1.0 ParrotHub\r\n\r\n\r\n",
                actualHubAction.getAction());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertNull(actualHubAction.getOptions());
    }

    /**
     * Method under test: {@link HubAction#HubAction(Map)}
     */
    @Test
    void testConstructor7() {
        HashMap<Object, Object> objectObjectMap = new HashMap<>();
        objectObjectMap.put((Object) "body", null);
        objectObjectMap.put((Object) "Content-Length", "42");
        objectObjectMap.put((Object) "Accept", "42");
        objectObjectMap.put((Object) "User-Agent", "42");
        objectObjectMap.put((Object) "Content-Type", "42");

        HashMap<Object, Object> params = new HashMap<>();
        params.put((Object) "protocol", Protocol.LAN);
        params.put((Object) "headers", objectObjectMap);
        HubAction actualHubAction = new HubAction(params);
        assertEquals("POST / HTTP/1.1\r\nAccept: 42\r\nUser-Agent: 42\r\nbody: null\r\nContent-Type: 42\r\n\r\n\r\n",
                actualHubAction.getAction());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertNull(actualHubAction.getOptions());
    }

    /**
     * Method under test: {@link HubAction#HubAction(Map, String)}
     */
    @Test
    void testConstructor8() {
        HubAction actualHubAction = new HubAction(new HashMap<>(), "Dni");

        assertEquals("POST / HTTP/1.1\r\nAccept: */*\r\nUser-Agent: Linux UPnP/1.0 ParrotHub\r\n\r\n\r\n",
                actualHubAction.getAction());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertNull(actualHubAction.getOptions());
    }

    /**
     * Method under test: {@link HubAction#HubAction(Map)}
     */
    @Test
    void testConstructor9() {
        HashMap<Object, Object> params = new HashMap<>();
        params.put("protocol", Protocol.LAN);
        params.put("method", "GET");
        HubAction actualHubAction = new HubAction(params);
        assertEquals("GET / HTTP/1.1\r\nAccept: */*\r\nUser-Agent: Linux UPnP/1.0 ParrotHub\r\n\r\n\r\n",
                actualHubAction.getAction());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertNull(actualHubAction.getOptions());
    }

    /**
     * Method under test: {@link HubAction#HubAction(Map)}
     */
    @Test
    void testConstructor10() {
        HashMap<Object, Object> params = new HashMap<>();
        params.put("protocol", Protocol.LAN);
        params.put("method", "GET");
        params.put("body", Map.of("key", "value"));
        HubAction actualHubAction = new HubAction(params);
        assertEquals("GET / HTTP/1.1\r\nAccept: */*\r\nUser-Agent: Linux UPnP/1.0 ParrotHub\r\nContent-Type: application/json\r\nContent-Length: 15\r\n\r\n{\"key\":\"value\"}",
                actualHubAction.getAction());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertNull(actualHubAction.getOptions());
    }

    /**
     * Method under test: {@link HubAction#HubAction(Map)}
     */
    @Test
    void testConstructor11() {
        HashMap<Object, Object> params = new HashMap<>();
        params.put("protocol", Protocol.LAN);
        params.put("method", "GET");
        params.put("headers", new HashMap<>());
        params.put("body", Map.of("key", "value"));
        HubAction actualHubAction = new HubAction(params);
        assertEquals("GET / HTTP/1.1\r\nAccept: */*\r\nUser-Agent: Linux UPnP/1.0 ParrotHub\r\nContent-Type: application/json\r\nContent-Length: 15\r\n\r\n{\"key\":\"value\"}",
                actualHubAction.getAction());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertNull(actualHubAction.getOptions());
    }

    /**
     * Method under test: {@link HubAction#HubAction(Map)}
     */
    @Test
    void testConstructor12() {
        HashMap<Object, Object> params = new HashMap<>();
        params.put("protocol", Protocol.LAN);
        params.put("method", "GET");
        params.put("headers", Map.of("Content-Type", "text/html; charset=utf-8"));
        params.put("body", "test body");
        HubAction actualHubAction = new HubAction(params);
        assertEquals("GET / HTTP/1.1\r\nAccept: */*\r\nUser-Agent: Linux UPnP/1.0 ParrotHub\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: 9\r\n\r\ntest body",
                actualHubAction.getAction());
        assertEquals(Protocol.LAN, actualHubAction.getProtocol());
        assertNull(actualHubAction.getOptions());
    }

    /**
     * Method under test: {@link HubAction#getCallback()}
     */
    @Test
    void testGetCallback() {
        assertNull((new HubAction("Action")).getCallback());

        HubAction hubAction = new HubAction("Action");
        hubAction.setOptions(new HashMap<>());
        assertNull(hubAction.getCallback());

        HashMap<Object, Object> options = new HashMap<>();
        options.put((Object) "callback", "42");

        hubAction = new HubAction("Action");
        hubAction.setOptions(options);
        assertEquals("42", hubAction.getCallback());

        options = new HashMap<>();
        options.put((Object) "callback", new NumberNumberDiv());
        options.put((Object) "foo", "42");

        hubAction = new HubAction("Action");
        hubAction.setOptions(options);
        assertEquals("div", hubAction.getCallback());
    }
}

