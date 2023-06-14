package com.parrotha.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class HubMultiActionTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link HubMultiAction#HubMultiAction()}
     *   <li>{@link HubMultiAction#getActions()}
     * </ul>
     */
    @Test
    void testConstructor() {
        assertTrue((new HubMultiAction()).getActions().isEmpty());
    }

    /**
     * Method under test: {@link HubMultiAction#HubMultiAction(List)}
     */
    @Test
    void testConstructor2() {
        ArrayList<String> stringActions = new ArrayList<>();
        assertEquals(stringActions, (new HubMultiAction(stringActions)).getActions());
    }

    /**
     * Method under test: {@link HubMultiAction#HubMultiAction(List)}
     */
    @Test
    void testConstructor3() {
        ArrayList<String> stringActions = new ArrayList<>();
        stringActions.add("foo");
        assertEquals(1, (new HubMultiAction(stringActions)).getActions().size());
    }

    /**
     * Method under test: {@link HubMultiAction#add(HubAction)}
     */
    @Test
    void testAdd() {
        HubMultiAction hubMultiAction = new HubMultiAction();
        hubMultiAction.add(new HubAction("Action"));
        assertEquals(1, hubMultiAction.getActions().size());
    }

    /**
     * Method under test: {@link HubMultiAction#add(String)}
     */
    @Test
    void testAdd2() {
        HubMultiAction hubMultiAction = new HubMultiAction();
        hubMultiAction.add("Action");
        assertEquals(1, hubMultiAction.getActions().size());
    }
}

