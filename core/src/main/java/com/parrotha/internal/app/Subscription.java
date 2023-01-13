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
package com.parrotha.internal.app;

import java.util.Objects;

public class Subscription {
    private String id;
    private String deviceId;
    private String locationId;
    private String attributeNameAndValue;
    private String handlerMethod;
    private String subscribedAppId;
    private boolean filterEvents = true;

    public Subscription() {
    }

    public Subscription(String handlerMethod, String subscribedAppId) {
        this.handlerMethod = handlerMethod;
        this.subscribedAppId = subscribedAppId;
    }

    public String getHandlerMethod() {
        return handlerMethod;
    }

    public void setHandlerMethod(String handlerMethod) {
        this.handlerMethod = handlerMethod;
    }

    public String getSubscribedAppId() {
        return subscribedAppId;
    }

    public void setSubscribedAppId(String subscribedAppId) {
        this.subscribedAppId = subscribedAppId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getAttributeNameAndValue() {
        return attributeNameAndValue;
    }

    public void setAttributeNameAndValue(String attributeNameAndValue) {
        this.attributeNameAndValue = attributeNameAndValue;
    }

    public boolean isFilterEvents() {
        return filterEvents;
    }

    public void setFilterEvents(boolean filterEvents) {
        this.filterEvents = filterEvents;
    }

    /**
     * Equals method ignores id so that duplicates can be eliminated.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Subscription that = (Subscription) o;
        return Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(locationId, that.locationId) && Objects.equals(attributeNameAndValue, that.attributeNameAndValue) &&
                Objects.equals(handlerMethod, that.handlerMethod) && Objects.equals(subscribedAppId, that.subscribedAppId) &&
                (filterEvents == that.filterEvents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, locationId, attributeNameAndValue, handlerMethod, subscribedAppId);
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id='" + id + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", locationId='" + locationId + '\'' +
                ", attributeNameAndValue='" + attributeNameAndValue + '\'' +
                ", handlerMethod='" + handlerMethod + '\'' +
                ", subscribedAppId='" + subscribedAppId + '\'' +
                ", filterEvents='" + filterEvents + '\'' +
                '}';
    }
}
