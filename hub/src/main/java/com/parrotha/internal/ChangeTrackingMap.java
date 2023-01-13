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
package com.parrotha.internal;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurperClassic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ChangeTrackingMap implements Map {

    // https://docs.smartthings.com/en/latest/smartapp-developers-guide/state.html#how-state-works
    private Map map;
    private Map mapCopy;

    public ChangeTrackingMap(Map map) {
        if (map != null) {
            this.map = (Map) new JsonSlurperClassic().parseText(new JsonBuilder(map).toString());
        } else {
            this.map = new HashMap();
        }
        //serialize state to json and back to filter out any bad values
        //https://docs.smartthings.com/en/latest/smartapp-developers-guide/state.html#persistence-model
        this.mapCopy = (Map) new JsonSlurperClassic().parseText(new JsonBuilder(this.map).toString());
        // old way which resulted in serialization exception
        //this.mapCopy = SerializationUtils.clone((HashMap) this.map);
    }

    // https://stackoverflow.com/questions/38015282/how-to-get-the-difference-between-two-maps-java/42950187#42950187
    // ST returns a ChangeSet Object where the first item is removed keys, the second item
    public ChangeSet changes() {
        MapDifference diff = Maps.difference(map, mapCopy);
        Map added = diff.entriesOnlyOnLeft();
        Set removed = diff.entriesOnlyOnRight().keySet();
        Map updated = new HashMap();
        for (Object key : diff.entriesDiffering().keySet()) {
            updated.put(key, map.get(key));
        }

        return new ChangeSet(removed, updated, added);
    }

    public class ChangeSet {
        private Set removed;
        private Map updated;
        private Map added;

        public ChangeSet(Set removed, Map updated, Map added) {
            this.removed = removed;
            this.updated = updated;
            this.added = added;
        }

        public Set getRemoved() {
            return removed;
        }

        public void setRemoved(Set removed) {
            this.removed = removed;
        }

        public Map getUpdated() {
            return updated;
        }

        public void setUpdated(Map updated) {
            this.updated = updated;
        }

        public Map getAdded() {
            return added;
        }

        public void setAdded(Map added) {
            this.added = added;
        }

        @Override
        public String toString() {
            return "ChangeSet(" +
                    removed +
                    ", " + updated +
                    ", " + added +
                    ')';
        }
    }


    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Nullable
    @Override
    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(@NotNull Map m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NotNull
    @Override
    public Set keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer action) {
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction function) {
        map.replaceAll(function);
    }

    @Nullable
    @Override
    public Object putIfAbsent(Object key, Object value) {
        return map.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    @Override
    public boolean replace(Object key, Object oldValue, Object newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @Nullable
    @Override
    public Object replace(Object key, Object value) {
        return map.replace(key, value);
    }

    @Override
    public Object computeIfAbsent(Object key, @NotNull Function mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object computeIfPresent(Object key, @NotNull BiFunction remappingFunction) {
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public Object compute(Object key, @NotNull BiFunction remappingFunction) {
        return map.compute(key, remappingFunction);
    }

    @Override
    public Object merge(Object key, @NotNull Object value, @NotNull BiFunction remappingFunction) {
        return map.merge(key, value, remappingFunction);
    }
}
