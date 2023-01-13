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

import groovy.lang.GroovyObjectSupport;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class DeviceWrapperListImpl extends GroovyObjectSupport implements DeviceWrapperList {
    public DeviceWrapperListImpl() {
    }

    public DeviceWrapperListImpl(List<DeviceWrapper> devices) {
        this.devices = devices;
    }

    public Object methodMissing(String methodName, Object arguments) {
        for (DeviceWrapper device : devices) {
            device.methodMissing(methodName, arguments);
        }

        return null;
    }

    public List getId() {
        return devices.stream().map(DeviceWrapper::getId).collect(Collectors.toList());
    }

    public List getDisplayName() {
        return devices.stream().map(DeviceWrapper::getDisplayName).collect(Collectors.toList());
    }

    @Override
    public List getSupportedAttributes() {
        return devices.stream().map(DeviceWrapper::getSupportedAttributes).collect(Collectors.toList());
    }

    public List<DeviceWrapper> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceWrapper> devices) {
        this.devices = devices;
    }

    @Override
    public int size() {
        return devices.size();
    }

    @Override
    public boolean isEmpty() {
        return devices.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return devices.contains(o);
    }

    @NotNull
    @Override
    public Iterator<DeviceWrapper> iterator() {
        return devices.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return devices.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] ts) {
        return devices.toArray(ts);
    }

    @Override
    public boolean add(DeviceWrapper deviceWrapper) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return devices.containsAll(collection);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends DeviceWrapper> collection) {
        return false;
    }

    @Override
    public boolean addAll(int i, @NotNull Collection<? extends DeviceWrapper> collection) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public DeviceWrapper get(int i) {
        return devices.get(i);
    }

    @Override
    public DeviceWrapper set(int i, DeviceWrapper deviceWrapper) {
        return null;
    }

    @Override
    public void add(int i, DeviceWrapper deviceWrapper) {
    }

    @Override
    public DeviceWrapper remove(int i) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return devices.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return devices.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<DeviceWrapper> listIterator() {
        return devices.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<DeviceWrapper> listIterator(int i) {
        return devices.listIterator(i);
    }

    @NotNull
    @Override
    public List<DeviceWrapper> subList(int i, int i1) {
        return subList(i, i1);
    }

    private List<DeviceWrapper> devices;

    @Override
    public String toString() {
        if (devices == null || devices.size() == 0) {
            return "[]";
        } else {
            return devices.stream().map(DeviceWrapper::toString).collect(Collectors.toList()).toString();
        }
    }
}
