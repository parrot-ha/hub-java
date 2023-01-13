/**
 * Copyright (c) 2021-2023 by the respective copyright holders.
 * All rights reserved.
 * <p>
 * This file is part of Parrot Home Automation Hub.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.parrotha.app;

import com.parrotha.internal.entity.EntityService;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of AtomicState to match ST: every update to the map results in writing to the data store and
 * every read from the map is read from the data store.
 */
public class AtomicState implements Map {

    private String installedAutomationAppId;
    private EntityService entityService;

    public AtomicState(String installedAutomationAppId, EntityService entityService) {
        this.installedAutomationAppId = installedAutomationAppId;
        this.entityService = entityService;
    }

    private Map getState() {
        return entityService.getInstalledAutomationAppState(installedAutomationAppId);
    }

    private void saveState(Map state) {
        entityService.updateInstalledAutomationAppState(installedAutomationAppId, state);
    }

    @Override
    public int size() {
        return getState().size();
    }

    @Override
    public boolean isEmpty() {
        return getState().isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return getState().containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return getState().containsValue(o);
    }

    @Override
    public Object get(Object o) {
        return getState().get(o);
    }

    @Override
    public Object put(Object o, Object o2) {
        Map state = getState();
        Object returnVal = state.put(o, o2);
        saveState(state);
        return returnVal;
    }

    @Override
    public Object remove(Object o) {
        Map state = getState();
        Object returnVal = state.remove(o);
        saveState(state);
        return returnVal;
    }

    @Override
    public void putAll(Map map) {
        Map state = getState();
        state.putAll(map);
        saveState(state);
    }

    @Override
    public void clear() {
        Map state = getState();
        state.clear();
        saveState(state);
    }

    @Override
    public Set keySet() {
        return getState().keySet();
    }

    @Override
    public Collection values() {
        return getState().values();
    }

    @Override
    public Set<Entry> entrySet() {
        return getState().entrySet();
    }
}
