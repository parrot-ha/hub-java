/**
 * Copyright (c) 2021 by the respective copyright holders.
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
package com.parrotha.internal.hub;

import com.parrotha.app.DeviceWrapper;
import com.parrotha.app.EventWrapper;
import com.parrotha.app.EventWrapperImpl;
import com.parrotha.device.Event;
import com.parrotha.internal.app.InstalledAutomationApp;
import com.parrotha.internal.app.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private Map<String, Subscription> subscriptionInfo;
    private Map<String, List<String>> deviceToSubscriptionMap;
    private Map<String, List<String>> locationToSubscriptionMap;
    private EventDataStore eventDataStore;
    private LocationService locationService;

    public EventService(LocationService locationService) {
        this.locationService = locationService;
    }

    public EventDataStore getEventDataStore() {
        if (eventDataStore == null)
            eventDataStore = new EventSQLDataStore();
        return eventDataStore;
    }

    private synchronized void loadSubscriptionInfo() {
        if (subscriptionInfo != null) return;
        Map<String, Subscription> tempSubscriptionInfo = new HashMap<>();
        Map<String, List<String>> tempDeviceToSubscriptionMap = new HashMap<>();
        Map<String, List<String>> tempLocationToSubscriptionMap = new HashMap<>();
        try {
            File subscriptionsConfigFile = new File("config/subscriptions.yaml");
            if (subscriptionsConfigFile.exists()) {
                Yaml yaml = new Yaml();
                List<Subscription> listObj = yaml.load(new FileInputStream(subscriptionsConfigFile));
                for (Subscription sub : listObj) {
                    tempSubscriptionInfo.put(sub.getId(), sub);
                    if (sub.getDeviceId() != null) {
                        if (tempDeviceToSubscriptionMap.get(sub.getDeviceId()) == null) {
                            tempDeviceToSubscriptionMap.put(sub.getDeviceId(), new ArrayList<>(Arrays.asList(sub.getId())));
                        } else {
                            tempDeviceToSubscriptionMap.get(sub.getDeviceId()).add(sub.getId());
                        }
                    } else if (sub.getLocationId() != null) {
                        if (tempLocationToSubscriptionMap.get(sub.getLocationId()) == null) {
                            tempLocationToSubscriptionMap.put(sub.getLocationId(), new ArrayList<>(Arrays.asList(sub.getId())));
                        } else {
                            tempLocationToSubscriptionMap.get(sub.getLocationId()).add(sub.getId());
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        subscriptionInfo = tempSubscriptionInfo;
        deviceToSubscriptionMap = tempDeviceToSubscriptionMap;
        locationToSubscriptionMap = tempLocationToSubscriptionMap;
    }

    private List<String> getSubscriptionListForDevice(String deviceId) {
        return getDeviceToSubscriptionMap().get(deviceId);
    }

    private List<String> getSubscriptionListForLocation(String locationId) {
        return getLocationToSubscriptionMap().get(locationId);
    }

    private Subscription getSubscriptionInfo(String subscriptionId) {
        return getSubscriptionInfo().get(subscriptionId);
    }

    private Map<String, Subscription> getSubscriptionInfo() {
        if (subscriptionInfo == null) {
            loadSubscriptionInfo();
        }
        return subscriptionInfo;
    }

    private Map<String, List<String>> getDeviceToSubscriptionMap() {
        if (deviceToSubscriptionMap == null) {
            loadSubscriptionInfo();
        }
        return deviceToSubscriptionMap;
    }

    private Map<String, List<String>> getLocationToSubscriptionMap() {
        if (locationToSubscriptionMap == null) {
            loadSubscriptionInfo();
        }
        return locationToSubscriptionMap;
    }

    public Event createEvent(Map properties, DeviceWrapper deviceWrapper) {
        // create event and send it
        properties.put("source", "DEVICE");
        Event event = new Event(properties, deviceWrapper, locationService);

        logger.info("Event! " + event.toString() + deviceWrapper.getId());
        return event;
    }

    public Event createEvent(Map properties, InstalledAutomationApp installedAutomationApp) {
        // create event and send it
        properties.put("source", "IAA");
        Event event = new Event(properties, installedAutomationApp, locationService);

        logger.info("Event! " + event.toString() + installedAutomationApp.getId());
        return event;
    }

    public Event createEvent(Map properties, Hub hub) {
        // create event and send it
        properties.put("source", "HUB");
        Event event = new Event(properties, hub, locationService);

        logger.info("Event! " + event.toString() + hub.getId());
        return event;
    }

    public Event createEvent(Map properties, Location location) {
        // create event and send it
        properties.put("source", "LOCATION");
        Event event = new Event(properties, location, locationService);

        logger.info("Event! " + event.toString() + location.getId());
        return event;
    }

    public void saveEvent(Event event) {
        getEventDataStore().saveEvent(event);
    }

    public List<EventWrapper> eventsSince(String source, String sourceId, Date date, int maxEvents) {
        List<Event> events = getEventDataStore().eventsSince(source, sourceId, date, maxEvents);
        return events.stream().map(event -> new EventWrapperImpl(event)).collect(Collectors.toList());
    }

    public List<EventWrapper> eventsBetween(String source, String sourceId, Date startDate, Date endDate, int maxEvents) {
        List<Event> events = getEventDataStore().eventsBetween(source, sourceId, startDate, endDate, maxEvents);
        return events.stream().map(event -> new EventWrapperImpl(event)).collect(Collectors.toList());
    }

    public List<Subscription> getAutomationAppList(Event event) {
        List<Subscription> subscriptionListReturnValue = new ArrayList<>();

        // look up subscription
        if ("DEVICE".equals(event.getSource())) {
            List<String> subscriptions = getSubscriptionListForDevice(event.getSourceId());
            if (subscriptions != null && subscriptions.size() > 0) {
                for (String subscriptionId : subscriptions) {
                    Subscription subscriptionInfo = getSubscriptionInfo(subscriptionId);
                    String attributeNameAndValue = subscriptionInfo.getAttributeNameAndValue();
                    if (event.getName() != null && (event.getName().equals(attributeNameAndValue) || (event.getValue() != null && (event.getName() + "." + event.getValue()).equals(attributeNameAndValue)))) {
                        String handlerMethod = subscriptionInfo.getHandlerMethod();
                        String installedAutomationAppId = subscriptionInfo.getSubscribedAppId();
                        if (handlerMethod != null && installedAutomationAppId != null) {
                            subscriptionListReturnValue.add(subscriptionInfo);
                        }
                    }
                }
            }
        } else if ("HUB".equals(event.getSource()) || "LOCATION".equals(event.getSource())) {
            // hub events are location events
            //TODO: handle multiple locations and hubs
            List<String> subscriptions = getLocationToSubscriptionMap().values().stream().flatMap(List::stream).collect(Collectors.toList());
            if (subscriptions != null && subscriptions.size() > 0) {
                for (String subscriptionId : subscriptions) {
                    Subscription subscriptionInfo = getSubscriptionInfo(subscriptionId);
                    if(subscriptionInfo != null) {
                        String attributeNameAndValue = subscriptionInfo.getAttributeNameAndValue();
                        if (event.getName() != null && (event.getName().equals(attributeNameAndValue) || (event.getValue() != null && (event.getName() + "." + event.getValue()).equals(attributeNameAndValue)))) {
                            String handlerMethod = subscriptionInfo.getHandlerMethod();
                            String installedAutomationAppId = subscriptionInfo.getSubscribedAppId();
                            if (handlerMethod != null && installedAutomationAppId != null) {
                                subscriptionListReturnValue.add(subscriptionInfo);
                            }
                        }
                    }
                }
            }

        }
        return subscriptionListReturnValue;
    }

    public void removeSubscriptionsForDevice(String deviceId) {
        List<String> subscriptions = getDeviceToSubscriptionMap().get(deviceId);
        for (String subscriptionId : subscriptions)
            getSubscriptionInfo().remove(subscriptionId);
        saveSubscriptionInfo();
    }

    public void removeSubscriptionsOfAutomationApp(String installedAutomationAppId) {
        if (installedAutomationAppId == null) return;
        getSubscriptionInfo().entrySet().removeIf(entry -> installedAutomationAppId.equals(entry.getValue().getSubscribedAppId()));
    }

    public void addLocationSubscription(String locationId, String subscribedAppId, String attributeNameAndValue, String handlerMethod) {
        Subscription subscription = new Subscription();
        subscription.setId(UUID.randomUUID().toString());
        subscription.setLocationId(locationId);
        subscription.setAttributeNameAndValue(attributeNameAndValue);
        subscription.setSubscribedAppId(subscribedAppId);
        subscription.setHandlerMethod(handlerMethod);

        // check for existing subscription
        if(!getSubscriptionInfo().values().stream().anyMatch(si -> si.equals(subscription))) {
            getSubscriptionInfo().put(subscription.getId(), subscription);
            if (subscription.getLocationId() != null) {
                if (getLocationToSubscriptionMap().get(locationId) == null) {
                    getLocationToSubscriptionMap().put(locationId, new ArrayList<>(Arrays.asList(subscription.getId())));
                } else {
                    getLocationToSubscriptionMap().get(locationId).add(subscription.getId());
                }
            }

            saveSubscriptionInfo();
        }
    }

    public void addDeviceSubscription(String deviceId, String subscribedAppId, String attributeNameAndValue, String handlerMethod) {
        Subscription subscription = new Subscription();
        subscription.setId(UUID.randomUUID().toString());
        subscription.setDeviceId(deviceId);
        subscription.setAttributeNameAndValue(attributeNameAndValue);
        subscription.setSubscribedAppId(subscribedAppId);
        subscription.setHandlerMethod(handlerMethod);

        getSubscriptionInfo().put(subscription.getId(), subscription);
        if (subscription.getDeviceId() != null) {
            if (getDeviceToSubscriptionMap().get(deviceId) == null) {
                getDeviceToSubscriptionMap().put(deviceId, new ArrayList<>(Arrays.asList(subscription.getId())));
            } else {
                getDeviceToSubscriptionMap().get(deviceId).add(subscription.getId());
            }
        }

        saveSubscriptionInfo();
    }

    private void saveSubscriptionInfo() {
        if (subscriptionInfo != null) {
            try {
                Yaml yaml = new Yaml();
                File subscriptionConfig = new File("config/subscriptions.yaml");
                FileWriter fileWriter = new FileWriter(subscriptionConfig);
                yaml.dump(new ArrayList<>(subscriptionInfo.values()), fileWriter);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
