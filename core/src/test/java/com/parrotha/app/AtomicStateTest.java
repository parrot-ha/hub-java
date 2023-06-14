package com.parrotha.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parrotha.internal.entity.EntityService;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AtomicStateTest {
    /**
     * Method under test: {@link AtomicState#AtomicState(String, EntityService)}
     */
    @Test
    void testConstructor() {
        assertNotNull(new AtomicState("42", null));

    }

    /**
     * Method under test: {@link AtomicState#size()}
     */
    @Test
    void testSize() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        assertEquals(0, (new AtomicState("42", entityService)).size());
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
    }

    /**
     * Method under test: {@link AtomicState#isEmpty()}
     */
    @Test
    void testIsEmpty() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        assertTrue((new AtomicState("42", entityService)).isEmpty());
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
    }

    /**
     * Method under test: {@link AtomicState#isEmpty()}
     */
    @Test
    void testIsEmpty2() {
        HashMap<Object, Object> objectObjectMap = new HashMap<>();
        objectObjectMap.put("Key", "Value");
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(objectObjectMap);
        assertFalse((new AtomicState("42", entityService)).isEmpty());
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
    }

    /**
     * Method under test: {@link AtomicState#containsKey(Object)}
     */
    @Test
    void testContainsKey() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        assertFalse((new AtomicState("42", entityService)).containsKey("42"));
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
    }

    /**
     * Method under test: {@link AtomicState#containsKey(Object)}
     */
    @Test
    void testContainsKey2() {
        EntityService entityService = mock(EntityService.class);
        doNothing().when(entityService)
                .updateInstalledAutomationAppState(Mockito.<String>any(), Mockito.<Map<Object, Object>>any());
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());

        AtomicState atomicState = new AtomicState("42", entityService);
        atomicState.put("42", "O2");
        assertTrue(atomicState.containsKey("42"));
        verify(entityService, atLeast(1)).getInstalledAutomationAppState(Mockito.<String>any());
        verify(entityService).updateInstalledAutomationAppState(Mockito.<String>any(),
                Mockito.<Map<Object, Object>>any());
    }

    /**
     * Method under test: {@link AtomicState#containsValue(Object)}
     */
    @Test
    void testContainsValue() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        assertFalse((new AtomicState("42", entityService)).containsValue("42"));
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
    }

    /**
     * Method under test: {@link AtomicState#get(Object)}
     */
    @Test
    void testGet() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        assertNull((new AtomicState("42", entityService)).get("42"));
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
    }

    /**
     * Method under test: {@link AtomicState#put(Object, Object)}
     */
    @Test
    void testPut() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        doNothing().when(entityService)
                .updateInstalledAutomationAppState(Mockito.<String>any(), Mockito.<Map<Object, Object>>any());
        assertNull((new AtomicState("42", entityService)).put("42", "O2"));
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
        verify(entityService).updateInstalledAutomationAppState(Mockito.<String>any(),
                Mockito.<Map<Object, Object>>any());
    }

    /**
     * Method under test: {@link AtomicState#remove(Object)}
     */
    @Test
    void testRemove() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        doNothing().when(entityService)
                .updateInstalledAutomationAppState(Mockito.<String>any(), Mockito.<Map<Object, Object>>any());
        assertNull((new AtomicState("42", entityService)).remove("42"));
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
        verify(entityService).updateInstalledAutomationAppState(Mockito.<String>any(),
                Mockito.<Map<Object, Object>>any());
    }

    /**
     * Method under test: {@link AtomicState#putAll(Map)}
     */
    @Test
    void testPutAll() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        doNothing().when(entityService)
                .updateInstalledAutomationAppState(Mockito.<String>any(), Mockito.<Map<Object, Object>>any());
        AtomicState atomicState = new AtomicState("42", entityService);
        atomicState.putAll(new HashMap<>());
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
        verify(entityService).updateInstalledAutomationAppState(Mockito.<String>any(),
                Mockito.<Map<Object, Object>>any());
    }

    /**
     * Method under test: {@link AtomicState#clear()}
     */
    @Test
    void testClear() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        doNothing().when(entityService)
                .updateInstalledAutomationAppState(Mockito.<String>any(), Mockito.<Map<Object, Object>>any());
        (new AtomicState("42", entityService)).clear();
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
        verify(entityService).updateInstalledAutomationAppState(Mockito.<String>any(),
                Mockito.<Map<Object, Object>>any());
    }

    /**
     * Method under test: {@link AtomicState#keySet()}
     */
    @Test
    void testKeySet() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        assertTrue((new AtomicState("42", entityService)).keySet().isEmpty());
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
    }

    /**
     * Method under test: {@link AtomicState#values()}
     */
    @Test
    void testValues() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        assertTrue((new AtomicState("42", entityService)).values().isEmpty());
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
    }

    /**
     * Method under test: {@link AtomicState#entrySet()}
     */
    @Test
    void testEntrySet() {
        EntityService entityService = mock(EntityService.class);
        when(entityService.getInstalledAutomationAppState(Mockito.<String>any())).thenReturn(new HashMap<>());
        assertTrue((new AtomicState("42", entityService)).entrySet().isEmpty());
        verify(entityService).getInstalledAutomationAppState(Mockito.<String>any());
    }
}

