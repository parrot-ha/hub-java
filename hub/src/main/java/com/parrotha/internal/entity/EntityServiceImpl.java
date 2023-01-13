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
package com.parrotha.internal.entity;

import com.parrotha.api.Request;
import com.parrotha.api.Response;
import com.parrotha.app.DeviceWrapper;
import com.parrotha.app.DeviceWrapperImpl;
import com.parrotha.app.EventWrapper;
import com.parrotha.app.EventWrapperImpl;
import com.parrotha.device.Event;
import com.parrotha.device.HubAction;
import com.parrotha.exception.NotFoundException;
import com.parrotha.internal.ChangeTrackingMap;
import com.parrotha.internal.app.AutomationApp;
import com.parrotha.internal.app.AutomationAppScriptDelegateImpl;
import com.parrotha.internal.app.AutomationAppService;
import com.parrotha.internal.app.InstalledAutomationApp;
import com.parrotha.internal.app.Subscription;
import com.parrotha.internal.device.Device;
import com.parrotha.internal.device.DeviceHandler;
import com.parrotha.internal.device.DevicePreferencesDelegate;
import com.parrotha.internal.device.DeviceScriptDelegateImpl;
import com.parrotha.internal.device.DeviceService;
import com.parrotha.internal.device.DeviceTilesDelegate;
import com.parrotha.internal.device.Fingerprint;
import com.parrotha.internal.hub.EventService;
import com.parrotha.internal.hub.LocationService;
import com.parrotha.internal.hub.ScheduleService;
import com.parrotha.internal.integration.IntegrationRegistry;
import com.parrotha.internal.script.ParrotHubDelegatingScript;
import com.parrotha.internal.system.OAuthToken;
import groovy.json.JsonBuilder;
import groovy.lang.GString;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityServiceImpl implements EntityService {
    private static final Logger logger = LoggerFactory.getLogger(EntityServiceImpl.class);

    private DeviceService deviceService;
    private AutomationAppService automationAppService;
    private EventService eventService;
    private LocationService locationService;
    private ScheduleService scheduleService;
    private IntegrationRegistry integrationRegistry;

    private Set<EventListener> eventListeners = new HashSet<>();

    private Map<String, Class<Script>> deviceHandlerScripts = new HashMap<>();

    public EntityServiceImpl(DeviceService deviceService, AutomationAppService automationAppService,
                             EventService eventService, LocationService locationService,
                             ScheduleService scheduleService, IntegrationRegistry integrationRegistry) {
        this.deviceService = deviceService;
        this.automationAppService = automationAppService;
        this.eventService = eventService;
        this.locationService = locationService;
        this.scheduleService = scheduleService;
        this.integrationRegistry = integrationRegistry;
    }

    public boolean removeDevice(String id) {
        runDeviceMethod(id, "uninstalled");
        boolean deviceRemoved = deviceService.removeDevice(id);
        if (deviceRemoved) {
            // remove device from subscriptions
            eventService.removeSubscriptionsForDevice(id);
            // TODO: remove device from automation apps

            return true;
        }

        return false;
    }

    @Override
    public void clearDeviceHandlerScripts() {
        deviceHandlerScripts.clear();
    }

    private void clearDeviceHandlerScript(String id) {
        deviceHandlerScripts.remove(id);
    }

    public void sendEvent(Map properties, DeviceWrapper deviceWrapper) {
        if (properties == null) {
            return;
        }
        Event event = eventService.createEvent(properties, deviceWrapper);
        processEvent(event);
    }

    public void sendEvent(Map properties, InstalledAutomationApp installedAutomationApp) {
        if (properties == null) {
            return;
        }
        Event event = eventService.createEvent(properties, installedAutomationApp);
        processEvent(event);
    }

    public void sendLocationEvent(Map properties) {
        if (properties == null) {
            return;
        }
        Event event = eventService.createEvent(properties, locationService.getLocation());
        processEvent(event);
    }

    public void sendHubEvent(Map properties) {
        if (properties == null) {
            return;
        }
        Event event = eventService.createEvent(properties, locationService.getHub());
        processEvent(event);
    }

    private void processEvent(Event event) {
        // skip any events that have a null name
        if (event.getName() == null) {
            return;
        }

        List<Subscription> subscriptions = eventService.getAutomationAppList(event);

        if ((subscriptions != null && subscriptions.size() > 0) || event.isStateChange()) {
            // save event in database
            eventService.saveEvent(event);
            if ("DEVICE".equals(event.getSource())) {
                deviceService.updateDeviceState(event);
            }
        }

        notifyEventListeners(event);

        for (Subscription subscription : subscriptions) {
            //TODO: should this be a thread pool?
            //TODO: create a copy of the event so that this is thread safe.
            new Thread(() -> {
                runInstalledAutomationAppMethod(subscription.getSubscribedAppId(), subscription.getHandlerMethod(),
                        new EventWrapperImpl(event));
            }).start();
        }
    }

    public List<EventWrapper> eventsSince(String source, String sourceId, Date date, int maxEvents) {
        return eventService.eventsSince(source, sourceId, date, maxEvents);
    }

    @Override
    public List<EventWrapper> eventsBetween(String source, String sourceId, Date startDate, Date endDate,
                                            int maxEvents) {
        return eventService.eventsBetween(source, sourceId, startDate, endDate, maxEvents);
    }

    @Override
    public void registerEventListener(EventListener eventListener) {
        synchronized (eventListeners) {
            eventListeners.add(eventListener);
        }
    }

    @Override
    public void unregisterEventListener(EventListener eventListener) {
        synchronized (eventListeners) {
            eventListeners.remove(eventListener);
        }
    }

    private void notifyEventListeners(Event event) {
        if (eventListeners.size() > 0) {
            new Thread(() -> {
                for (EventListener eventListener : eventListeners) {
                    eventListener.eventReceived(event);
                }
            }).start();
        }
    }

    @Override
    public void runDeviceMethodByDNI(String integrationId, String deviceNetworkId, String methodName, Object... args) {
        Device device = deviceService.getDeviceByIntegrationAndDNI(integrationId, deviceNetworkId);
        if (device != null) {
            runDeviceMethod(device.getId(), methodName, args);
        }
    }

    public void runDeviceMethod(String id, String methodName, Object... args) {
        Class<Script> s = getScriptForDevice(id);
        if (s != null) {
            // get device settings
            Device device = deviceService.getDeviceById(id);

            try {
                ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) s.getConstructor()
                        .newInstance();
                parrotHubDelegatingScript.setDelegate(
                        new DeviceScriptDelegateImpl(device, deviceService, this, locationService, scheduleService,
                                automationAppService));

                Object returnObject;
                if (args != null && args instanceof Object[]) {
                    if (args.length == 1) {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, args[0]);
                    } else if (args.length == 0) {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, null);
                    } else {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, args);
                    }
                } else {
                    returnObject = parrotHubDelegatingScript.invokeMethod(methodName, null);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("State: " + new JsonBuilder(
                            ((DeviceScriptDelegateImpl) parrotHubDelegatingScript.getDelegate()).getState()).toString());
                }
                // save state
                deviceService.saveDeviceState(id,
                        (ChangeTrackingMap) ((DeviceScriptDelegateImpl) parrotHubDelegatingScript.getDelegate())
                                .getState());

                processReturnObject(returnObject, device);
            } catch (MissingMethodException missingMethodException) {
                if (missingMethodException.getMessage() != null &&
                        !missingMethodException.getMessage().replaceAll("\n", "")
                                .matches("No signature of method: DH_[0-9a-z]*\\." +
                                        methodName + "\\(\\).*")) {
                    logger.warn("Exception: ", missingMethodException);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                logger.warn("Exception: ", e);
            } catch (Throwable throwable) {
                logger.warn("Exception: ", throwable);
            }
        }
        //TODO: throw device or device handler not found exception?
    }

    private void processReturnObject(Object returnObject, Device device) {
        if (returnObject instanceof List) {
            List<Object> returnObjectActionList = new ArrayList<>();
            for (Object returnObjectItem : (List) returnObject) {
                if (returnObjectItem instanceof String || returnObjectItem instanceof GString || returnObjectItem instanceof HubAction) {
                    returnObjectActionList.add(returnObjectItem.toString());
                } else if (returnObjectItem instanceof List) {
                    processReturnObject(returnObjectItem, device);
                } else if (returnObjectItem instanceof Map) {
                    Map properties = (Map) returnObjectItem;
                    if (properties.get("name") != null && properties.get("value") != null) {
                        sendEvent(properties, new DeviceWrapperImpl(device, deviceService, this, locationService));
                    }
                }
            }
            if (returnObjectActionList.size() > 0) {
                deviceService.processReturnObj(device, returnObjectActionList);
            }
        } else if (returnObject instanceof Map) {
            Map properties = (Map) returnObject;
            if (properties.get("name") != null && properties.get("value") != null) {
                sendEvent(properties, new DeviceWrapperImpl(device, deviceService, this, locationService));
            }
        } else {
            deviceService.processReturnObj(device, returnObject);
        }
    }

    public Object runDeviceMethodAndReturn(String id, String methodName, Object... args) {
        Class<Script> s = getScriptForDevice(id);
        if (s != null) {
            // get device settings
            Device device = deviceService.getDeviceById(id);

            try {
                ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) s.getConstructor()
                        .newInstance();
                parrotHubDelegatingScript.setDelegate(
                        new DeviceScriptDelegateImpl(device, deviceService, this, locationService, scheduleService,
                                automationAppService));

                Object returnObject;
                if (args != null && args instanceof Object[]) {
                    if (args.length == 1) {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, args[0]);
                    } else if (args.length == 0) {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, null);
                    } else {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, args);
                    }
                } else {
                    returnObject = parrotHubDelegatingScript.invokeMethod(methodName, null);
                }

                // save state
                deviceService.saveDeviceState(id,
                        (ChangeTrackingMap) ((DeviceScriptDelegateImpl) parrotHubDelegatingScript.getDelegate())
                                .getState());

                return returnObject;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        //TODO: throw device or device handler not found exception?
        return null;
    }

    public void reprocessAutomationApps() {
        automationAppService.reprocessAutomationApps();
    }

    private void reprocessAutomationApp(String id) {
        automationAppService.reprocessAutomationApp(id);
    }

    public void reprocessDeviceHandlers() {
        deviceService.reprocessDeviceHandlers();
    }

    private void reprocessDeviceHandler(String id) {
        deviceService.reprocessDeviceHandler(id);
    }

    public void initialize() {
        deviceService.initialize();
        automationAppService.initialize();
    }

    public void shutdown() {
        automationAppService.shutdown();
        deviceService.shutdown();
    }

    private Map<Fingerprint, String> fingerprints;

    private Map<Fingerprint, String> getFingerprints() {
        if (fingerprints == null) {
            fingerprints = new HashMap<>();
            //List<Map<String, String>> fingerprints = new ArrayList<>();

            for (DeviceHandler dhInfo : deviceService.getAllDeviceHandlers()) {
                List<Fingerprint> dhInfoFPs = dhInfo.getFingerprints();
                if (dhInfoFPs != null) {
                    for (Fingerprint fp : dhInfoFPs) {
                        fingerprints.put(fp, dhInfo.getId());
                    }
                }
            }
        }
        return fingerprints;
    }

    public String[] getDeviceHandlerByFingerprint(Map<String, String> deviceInfo) {
        Map<Fingerprint, String> fingerprints = getFingerprints();
        if (logger.isDebugEnabled()) {
            logger.debug("Fingerprints! " + fingerprints);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("deviceInfo: " + deviceInfo);
        }
        int matchingScore = 0;
        Fingerprint matchingFingerprint = null;
        for (Fingerprint fingerprint : fingerprints.keySet()) {
            int score = fingerprintScore(fingerprint, deviceInfo);
            if (score > matchingScore) {
                matchingScore = score;
                matchingFingerprint = fingerprint;
            }
        }
        // TODO: what should be the minimum score?
        if (matchingFingerprint != null && matchingScore > 90) {
            if (logger.isDebugEnabled()) {
                logger.debug("We have a matching fingerprint! " + matchingFingerprint.getDeviceJoinName() + " id: " +
                        fingerprints.get(matchingFingerprint) + " score: " + matchingScore);
            }
            return new String[]{fingerprints.get(matchingFingerprint), matchingFingerprint.getDeviceJoinName()};
        }

        // if no match, return Thing
        DeviceHandler thingDeviceHandler = deviceService
                .getDeviceHandlerByNameAndNamespace("Thing", "com.parrotha.device.handler.thing");
        if (thingDeviceHandler != null) {
            return new String[]{thingDeviceHandler.getId(), "Unknown Device"};
        }

        return null;
    }

    @Override
    public Collection<Device> getDevicesByCapability(String capability) {
        return deviceService.getDevicesByCapability(capability);
    }

    private int fingerprintScore(Fingerprint fingerprint, Map<String, String> deviceInfo) {
        if (deviceInfo == null || deviceInfo.size() == 0) {
            return 0;
        }

        int fingerprintItemCount = 0;
        int deviceInfoItemCount = deviceInfo.size();
        int matchCount = 0;
        int weight = 0;

        boolean mfrMatch = false;
        boolean modelMatch = false;
        boolean prodMatch = false;
        boolean intgMatch = false;

        if (StringUtils.isNotBlank(fingerprint.getProfileId())) {
            fingerprintItemCount++;
            if (fingerprint.getProfileId().equals(deviceInfo.get("profileId"))) {
                matchCount++;
                weight += 1;
            }
        }

        if (StringUtils.isNotBlank(fingerprint.getEndpointId())) {
            fingerprintItemCount++;
            if (fingerprint.getEndpointId().equals(deviceInfo.get("endpointId"))) {
                matchCount++;
                weight += 1;
            }
        }

        if (StringUtils.isNotBlank(fingerprint.getInClusters())) {
            fingerprintItemCount++;
            if (fingerprint.getInClusters().equals(deviceInfo.get("inClusters"))) {
                matchCount++;
                weight += 2;
            } else if (fingerprint.getSortedInClusters().equals(deviceInfo.get("inClusters"))) {
                matchCount++;
                weight += 1;
            }
        }

        if (StringUtils.isNotBlank(fingerprint.getOutClusters())) {
            fingerprintItemCount++;
            if (fingerprint.getOutClusters().equals(deviceInfo.get("outClusters"))) {
                matchCount++;
                weight += 2;
            } else if (fingerprint.getSortedOutClusters().equals(deviceInfo.get("outClusters"))) {
                matchCount++;
                weight += 1;
            }
        }

        if (StringUtils.isNotBlank(fingerprint.getManufacturer())) {
            fingerprintItemCount++;
            if (fingerprint.getManufacturer().equals(deviceInfo.get("manufacturer"))) {
                matchCount++;
                weight += 2;
            }
        }

        if (StringUtils.isNotBlank(fingerprint.getModel())) {
            fingerprintItemCount++;
            if (fingerprint.getModel().equals(deviceInfo.get("model"))) {
                modelMatch = true;
                matchCount++;
                weight += 3;
            }
        }

        if (StringUtils.isNotBlank(fingerprint.getMfr())) {
            fingerprintItemCount++;
            if (fingerprint.getMfr().equals(deviceInfo.get("mfr"))) {
                mfrMatch = true;
                matchCount++;
                weight += 3;
            }
        }

        if (StringUtils.isNotBlank(fingerprint.getProd())) {
            fingerprintItemCount++;
            if (fingerprint.getProd().equals(deviceInfo.get("prod"))) {
                prodMatch = true;
                matchCount++;
                weight += 3;
            }
        }

        if (StringUtils.isNotBlank(fingerprint.getIntg())) {
            fingerprintItemCount++;
            if (fingerprint.getIntg().equals(deviceInfo.get("intg"))) {
                intgMatch = true;
                matchCount++;
                weight += 3;
            }
        }

        if (mfrMatch && modelMatch && prodMatch && intgMatch && (fingerprintItemCount == 4)) {
            // matched all four, best match
            return 100;
        }

        if (mfrMatch && modelMatch && prodMatch && (fingerprintItemCount == 3)) {
            // matched all three, best match
            return 99;
        }

        // similar match, all items, slightly less score
        if (fingerprintItemCount == matchCount && weight > 4) {
            return 98;
        }

        // similar match, all items, even less score
        if (fingerprintItemCount == matchCount && weight > 3) {
            return 97;
        }

        int score = ((matchCount / fingerprintItemCount) * 100) + weight;

        return score;
    }

    private Class<Script> getScriptForDevice(String id) {
        Device device = deviceService.getDeviceById(id);

        if (device != null) {
            String deviceHandlerId = device.getDeviceHandlerId();
            return getScriptForDeviceHandler(deviceHandlerId);
        }
        return null;
    }

    private Class<Script> getScriptForDeviceHandler(String deviceHandlerId) {
        if (deviceHandlerId == null) {
            return null;
        }
        DeviceHandler deviceHandler = deviceService.getDeviceHandler(deviceHandlerId);

        Class<Script> s = deviceHandlerScripts.get(deviceHandlerId);
        if (s == null) {
            try {
                if (deviceHandler.getType() == DeviceHandler.Type.USER || deviceHandler.getType() == DeviceHandler.Type.EXTENSION_SOURCE) {
                    InputStream is = new FileInputStream(deviceHandler.getFile());
                    if (is != null) {
                        String srcCode = IOUtils.toString(is, StandardCharsets.UTF_8);

                        CompilerConfiguration config = new CompilerConfiguration();
                        config.setScriptBaseClass("com.parrotha.internal.script.ParrotHubDelegatingScript");

                        GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader(), config);
                        Class<Script> scriptClass = (Class<Script>) gcl.parseClass(srcCode, "DH_" + deviceHandlerId);

                        deviceHandlerScripts.put(deviceHandlerId, scriptClass);
                        s = scriptClass;
                    }
                } else {
                    //process class in classpath
                    try {
                        ClassLoader myClassLoader = deviceService.getClassLoaderForDeviceHandler(deviceHandlerId);
                        Class<Script> scriptClass = (Class<Script>) Class
                                .forName(deviceHandler.getFile().substring("class:".length()), false, myClassLoader);
                        deviceHandlerScripts.put(deviceHandlerId, scriptClass);
                        s = scriptClass;
                    } catch (ClassNotFoundException classNotFoundException) {
                        classNotFoundException.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return s;
    }

    // Throws exceptions instead of just logging them.
    @Override
    public void runInstalledAutomationAppMethodWithException(String id, String methodName, Object... args)
            throws Exception {
        Class<Script> s = getScriptForInstalledAutomationApp(id);
        if (s != null) {
            InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);

            try {
                ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) s.getConstructor()
                        .newInstance();
                parrotHubDelegatingScript.setDelegate(
                        new AutomationAppScriptDelegateImpl(installedAutomationApp, locationService, eventService, this,
                                deviceService, scheduleService, automationAppService, integrationRegistry));

                Object returnObject;
                if (args != null && args instanceof Object[]) {
                    if (args.length == 1) {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, args[0]);
                    } else if (args.length == 0) {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, null);
                    } else {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, args);
                    }
                } else {
                    returnObject = parrotHubDelegatingScript.invokeMethod(methodName, null);
                }

                // save state
                automationAppService.saveState(id,
                        (ChangeTrackingMap) ((AutomationAppScriptDelegateImpl) parrotHubDelegatingScript.getDelegate())
                                .getState());

            } catch (MissingMethodException missingMethodException) {
                if (!missingMethodException.getMessage().contains("." + methodName + "()")) {
                    throw missingMethodException;
                }
            } catch (Exception e) {
                //TODO: get line number
                LoggerFactory.getLogger("parrothub.live.iaa." + id).error(e.getMessage() + " (" + methodName + ")");

                if (logger.isDebugEnabled()) {
                    logger.debug("Exception ", e);
                }
                throw e;
            }
        }
        //TODO: throw installed app not found exception?
    }

    private Object runInstalledAutomationAppMethodWithParamsAndReturn(String id, Map params, Request request, String methodName,
                                                                      Object... args) throws Exception {
        Class<Script> s = getScriptForInstalledAutomationApp(id);
        if (s != null) {
            InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);

            try {
                ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) s.getConstructor()
                        .newInstance();
                AutomationAppScriptDelegateImpl automationAppScriptDelegate = new AutomationAppScriptDelegateImpl(
                        installedAutomationApp, locationService, eventService, this, deviceService, scheduleService,
                        automationAppService, integrationRegistry, request);
                if (params != null) {
                    automationAppScriptDelegate.setBaseParams(params);
                }
                parrotHubDelegatingScript.setDelegate(automationAppScriptDelegate);

                Object returnObject;
                if (args != null && args instanceof Object[]) {
                    if (args.length == 1) {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, args[0]);
                    } else if (args.length == 0) {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, null);
                    } else {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, args);
                    }
                } else {
                    returnObject = parrotHubDelegatingScript.invokeMethod(methodName, null);
                }

                //Object retObj = parrotHubDelegatingScript.invokeMethod(methodName, args);

                // save state
                automationAppService.saveState(id,
                        (ChangeTrackingMap) ((AutomationAppScriptDelegateImpl) parrotHubDelegatingScript.getDelegate())
                                .getState());

                return returnObject;
//            } catch (MissingMethodException missingMethodException) {
//                if (!missingMethodException.getMessage().contains("." + methodName + "()")) {
//                    throw missingMethodException;
//                }
            } catch (Exception e) {
                //TODO: get line number
                LoggerFactory.getLogger("parrothub.live.iaa." + id).error(e.getMessage() + " (" + methodName + ")");

                if (logger.isDebugEnabled()) {
                    logger.debug("Exception ", e);
                }
                throw e;
            }
        } else {
            throw new NotFoundException("Installed Automation App Not found");
        }
    }

    @Override
    public Object runInstalledAutomationAppMethodAndReturn(String id, String methodName, Object... args)
            throws Exception {
        Class<Script> s = getScriptForInstalledAutomationApp(id);
        if (s != null) {
            InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);

            try {
                ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) s.getConstructor()
                        .newInstance();
                parrotHubDelegatingScript.setDelegate(
                        new AutomationAppScriptDelegateImpl(installedAutomationApp, locationService, eventService, this,
                                deviceService, scheduleService, automationAppService, integrationRegistry));

                Object returnObject;
                if (args != null && args instanceof Object[]) {
                    if (args.length == 1) {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, args[0]);
                    } else if (args.length == 0) {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, null);
                    } else {
                        returnObject = parrotHubDelegatingScript.invokeMethod(methodName, args);
                    }
                } else {
                    returnObject = parrotHubDelegatingScript.invokeMethod(methodName, null);
                }

                //Object retObj = parrotHubDelegatingScript.invokeMethod(methodName, args);

                // save state
                automationAppService.saveState(id,
                        (ChangeTrackingMap) ((AutomationAppScriptDelegateImpl) parrotHubDelegatingScript.getDelegate())
                                .getState());

                return returnObject;
//            } catch (MissingMethodException missingMethodException) {
//                if (!missingMethodException.getMessage().contains("." + methodName + "()")) {
//                    throw missingMethodException;
//                }
            } catch (Exception e) {
                //TODO: get line number
                LoggerFactory.getLogger("parrothub.live.iaa." + id).error(e.getMessage() + " (" + methodName + ")");

                if (logger.isDebugEnabled()) {
                    logger.debug("Exception ", e);
                }
                throw e;
            }
        } else {
            throw new NotFoundException("Installed Automation App Not found");
        }
    }

    @Override
    public void updateInstalledAutomationAppSettings(String id, Map<String, Object> settingsMap) {
        automationAppService.updateInstalledAutomationAppSettings(id, settingsMap);
    }

    @Override
    public void removeInstalledAutomationAppSetting(String id, String name) {
        automationAppService.removeInstalledAutomationAppSetting(id, name);
    }

    @Override
    public void updateOrInstallInstalledAutomationApp(String id) {
        InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);
        if (installedAutomationApp.isInstalled()) {
            runInstalledAutomationAppMethod(id, "updated");
        } else {
            installedAutomationApp.setInstalled(true);
            automationAppService.updateInstalledAutomationApp(installedAutomationApp);
            runInstalledAutomationAppMethod(id, "installed");
        }
    }

    @Override
    public InstalledAutomationApp getInstalledAutomationAppByClientId(String clientId, boolean createIfMissing)
            throws NotFoundException {
        return automationAppService.getInstalledAutomationAppByClientId(clientId, createIfMissing);
    }

    @Override
    public OAuthToken getOauthToken(String clientId, String clientSecret) {
        return automationAppService.createOAuthToken(clientId, clientSecret);
    }

    @Override
    public List<String> getInstalledAutomationAppsByToken(String token) {
        return automationAppService.getInstalledAutomationAppsByToken(token);
    }

    @Override
    public String getOAuthClientIdByToken(String token) {
        return automationAppService.getOAuthClientIdByToken(token);
    }

    @Override
    public Response processInstalledAutomationAppWebRequest(String id, String httpMethod, String path, String body, Map params, Map headers) {

        boolean authenticated = false;

        if (id == null) {
            // check if we can get the id from the bearer token
            String bearerToken = getBearerToken(headers);
            if (bearerToken != null) {
                List<String> installedAutomationAppIds = automationAppService.getInstalledAutomationAppsByToken(bearerToken);
                if (installedAutomationAppIds != null && installedAutomationAppIds.size() == 1) {
                    id = installedAutomationAppIds.get(0);
                    // set authenticated to true since we know we have a valid bearer token
                    authenticated = true;
                }
            }
        }
        InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);

        if (installedAutomationApp != null) {
            // check if we have an access token
            if (!authenticated && params != null) {
                Object paramAccessToken = params.get("access_token");
                if (paramAccessToken instanceof String) {
                    Map state = installedAutomationApp.getState();
                    if (state != null) {
                        Object accessToken = state.get("accessToken");
                        if (accessToken instanceof String) {
                            if (accessToken.equals(paramAccessToken)) {
                                authenticated = true;
                            }
                        }
                    }
                }
            }
            if (!authenticated) {
                // check for bearer token
                String bearerToken = getBearerToken(headers);
                if (bearerToken != null) {
                    // check bearer token is valid
                    List<String> installedAutomationAppIds = automationAppService.getInstalledAutomationAppsByToken(bearerToken);
                    if (installedAutomationAppIds != null && installedAutomationAppIds.contains(id)) {
                        authenticated = true;
                    }
                }
            }

            if (!authenticated) {
                return new Response(Map.of("status", 401,
                        "contentType", "application/xhtml+xml",
                        "data",
                        "<oauth><error_description>Invalid or missing access token</error_description><error>invalid_token</error></oauth>"));
            }

            Map<String, Map<String, String>> mappings = getInstalledAutomationAppMapping(id, params);
            if (mappings != null) {
                for (String key : mappings.keySet()) {
                    if (path.equalsIgnoreCase(key)) {
                        // we have a match
                        if (mappings.get(key) != null) {
                            String methodName = mappings.get(key).get(httpMethod);
                            try {
                                Object response = runInstalledAutomationAppMethodWithParamsAndReturn(id, params,
                                        new Request(httpMethod, headers, body),
                                        methodName, (Object) null);
                                if (response instanceof Response) {
                                    return (Response) response;
                                } else if (response instanceof Map) {
                                    return new Response(Map.of("data", new JsonBuilder(response).toString()));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }
                }

                // TODO: return 404 if we didn't find a mapping
            }
        }

        return null;
    }

    private String getBearerToken(Map headers) {
        String bearerToken = null;
        Object authorizationHeader = headers.get("Authorization");
        if (authorizationHeader != null && authorizationHeader instanceof String) {
            if (((String) authorizationHeader).startsWith("Bearer ")) {
                bearerToken = ((String) authorizationHeader).substring("Bearer ".length());
                if (bearerToken.contains(".")) {
                    String[] bearerTokenArray = bearerToken.split("\\.");
                    bearerToken = bearerTokenArray[bearerTokenArray.length - 1];
                }
            }
        }
        return bearerToken;
    }

    public void runInstalledAutomationAppMethod(String id, String methodName, Object... args) {
        try {
            runInstalledAutomationAppMethodWithException(id, methodName, args);
        } catch (Exception e) {
            // do nothing, its already been logged to "live" logger
        }
    }

    private Map<String, Class<Script>> automationAppScripts = new HashMap<>();

    public void clearAutomationAppScripts() {
        automationAppScripts.clear();
    }

    private void clearAutomationAppScript(String id) {
        automationAppScripts.remove(id);
    }

    public Class<Script> getScriptForInstalledAutomationApp(String id) {
        InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);

        if (installedAutomationApp != null) {
            String automationAppId = installedAutomationApp.getAutomationAppId();
            return getScriptForAutomationApp(automationAppId);
        }
        return null;
    }

    public Class<Script> getScriptForAutomationApp(String automationAppId) {
        if (automationAppId == null) {
            return null;
        }
        AutomationApp automationApp = automationAppService.getAutomationAppById(automationAppId);

        Class<Script> s = automationAppScripts.get(automationAppId);
        if (s == null) {
            try {
                if (automationApp.getType() == AutomationApp.Type.USER || automationApp.getType() == AutomationApp.Type.EXTENSION_SOURCE) {
                    InputStream is = new FileInputStream(automationApp.getFile());
                    if (is != null) {
                        String srcCode = IOUtils.toString(is, StandardCharsets.UTF_8);

                        CompilerConfiguration config = new CompilerConfiguration();
                        config.setScriptBaseClass("com.parrotha.internal.script.ParrotHubDelegatingScript");

                        GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader(), config);
                        Class<Script> scriptClass = (Class<Script>) gcl.parseClass(srcCode, "AA_" + automationAppId);

                        automationAppScripts.put(automationAppId, scriptClass);
                        s = scriptClass;
                    }
                } else {
                    //process class in classpath
                    try {
                        ClassLoader myClassLoader = automationAppService.getClassLoaderForAutomationApp(automationAppId);
                        Class<Script> scriptClass = (Class<Script>) Class
                                .forName(automationApp.getFile().substring("class:".length()), false, myClassLoader);
                        automationAppScripts.put(automationAppId, scriptClass);
                        s = scriptClass;
                    } catch (ClassNotFoundException classNotFoundException) {
                        classNotFoundException.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return s;
    }


    public Object getInstalledAutomationAppConfigurationPage(String id, String pageName) {
        Class<Script> s = getScriptForInstalledAutomationApp(id);
        if (s != null) {
            //get the installed automation app
            InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);

            try {
                ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) s.getConstructor()
                        .newInstance();
                parrotHubDelegatingScript.setDelegate(new AutomationAppScriptDelegateImpl(installedAutomationApp,
                        locationService, eventService, this, deviceService, scheduleService, automationAppService, integrationRegistry,
                        false, true));

                parrotHubDelegatingScript.invokeMethod("run", null);

                AutomationAppScriptDelegateImpl aasd = (AutomationAppScriptDelegateImpl) parrotHubDelegatingScript
                        .getDelegate();
                if (aasd.pageList != null) {
                    // multiple pages
                    if (pageName == null) {
                        // get first page
                        Map firstPage = aasd.pageList.get(0);
                        if (firstPage.get("content") != null) {
                            String content = (String) firstPage.get("content");
                            // this is a dynamic page, run method to get content
                            Object dynamicPageResponse = parrotHubDelegatingScript.invokeMethod(content, null);
                            // save state
                            automationAppService.saveState(id,
                                    (ChangeTrackingMap) ((AutomationAppScriptDelegateImpl) parrotHubDelegatingScript
                                            .getDelegate()).getState());

                            if (dynamicPageResponse instanceof Map) {
                                ((Map) dynamicPageResponse).put("content", content);
                                if (StringUtils.isEmpty((String) ((Map<?, ?>) dynamicPageResponse).get("title")) &&
                                        StringUtils.isNotEmpty((String) firstPage.get("title"))) {
                                    ((Map) dynamicPageResponse).put("title", (String) firstPage.get("title"));
                                }
                            }
                            return dynamicPageResponse;
                        } else {
                            return firstPage;
                        }
                    } else {
                        for (Map page : aasd.pageList) {
                            if (pageName.equals(page.get("name"))) {
                                if (page.get("content") != null) {
                                    String content = (String) page.get("content");
                                    // this is a dynamic page, run method to get content
                                    Object dynamicPageResponse = parrotHubDelegatingScript.invokeMethod(content, null);
                                    // save state
                                    automationAppService.saveState(id,
                                            (ChangeTrackingMap) ((AutomationAppScriptDelegateImpl) parrotHubDelegatingScript
                                                    .getDelegate()).getState());

                                    if (dynamicPageResponse instanceof Map) {
                                        ((Map) dynamicPageResponse).put("content", content);

                                        if ((((Map<?, ?>) dynamicPageResponse).get("title") == null || StringUtils
                                                .isEmpty(((Map<?, ?>) dynamicPageResponse).get("title").toString())) &&
                                                page.get("title") != null &&
                                                StringUtils.isNotEmpty(page.get("title").toString())) {
                                            ((Map) dynamicPageResponse).put("title", page.get("title").toString());
                                        }
                                    }
                                    return dynamicPageResponse;
                                } else {
                                    return page;
                                }
                            }
                        }
                    }
                } else {
                    return aasd.preferences;
                }

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        //TODO: throw installed automation app or automation app not found exception?
        return null;
    }

    private Map<String, Map<String, String>> getInstalledAutomationAppMapping(String id, Map params) {
        Class<Script> s = getScriptForInstalledAutomationApp(id);
        if (s != null) {
            //get the installed automation app
            InstalledAutomationApp installedAutomationApp = automationAppService.getInstalledAutomationApp(id);

            try {
                ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) s.getConstructor()
                        .newInstance();
                AutomationAppScriptDelegateImpl automationAppScriptDelegate = new AutomationAppScriptDelegateImpl(
                        installedAutomationApp,
                        locationService, eventService, this, deviceService, scheduleService, automationAppService, integrationRegistry, true,
                        false);
                if (params != null) {
                    automationAppScriptDelegate.setBaseParams(params);
                }
                parrotHubDelegatingScript.setDelegate(automationAppScriptDelegate);

                parrotHubDelegatingScript.invokeMethod("run", null);

                AutomationAppScriptDelegateImpl aasd = (AutomationAppScriptDelegateImpl) parrotHubDelegatingScript
                        .getDelegate();
                if (aasd.getPathMappings() != null) {
                    return aasd.getPathMappings();
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        //TODO: throw installed automation app or automation app not found exception?
        return null;
    }

    @Override
    public Map<String, Object> getDeviceTileLayout(String id) {
        Class<Script> deviceScript = getScriptForDevice(id);
        Device device = deviceService.getDeviceById(id);

        //TODO: throw device or device handler not found exception?
        return getDeviceTileLayout(deviceScript, device);
    }

    public Map<String, Object> getDevicePreferencesLayout(String id) {
        Class<Script> deviceScript = getScriptForDevice(id);
        Device device = deviceService.getDeviceById(id);

        //TODO: throw device or device handler not found exception?
        return getDevicePreferencesLayout(deviceScript, device);
    }

    @Override
    public Map<String, Object> getDeviceHandlerPreferencesLayout(String deviceHandlerId) {
        Class<Script> deviceScript = getScriptForDeviceHandler(deviceHandlerId);

        //TODO: throw device or device handler not found exception?
        return getDeviceScriptPreferencesLayout(deviceScript);
    }

    private Map<String, Object> getDeviceScriptPreferencesLayout(Class<Script> deviceScript) {
        if (deviceScript == null) {
            return null;
        }

        try {
            ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) deviceScript.getConstructor()
                    .newInstance();
            parrotHubDelegatingScript.setDelegate(new DevicePreferencesDelegate());

            parrotHubDelegatingScript.invokeMethod("run", null);

            DevicePreferencesDelegate dsd = (DevicePreferencesDelegate) parrotHubDelegatingScript.getDelegate();
            if (dsd.preferences != null) {
                return dsd.preferences;
            } else {
                return new HashMap<>();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Map<String, Object> getDevicePreferencesLayout(Class<Script> deviceScript, Device device) {
        if (deviceScript == null) {
            return null;
        }

        try {
            ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) deviceScript.getConstructor()
                    .newInstance();
            parrotHubDelegatingScript.setDelegate(new DeviceScriptDelegateImpl(device, deviceService, this, locationService, scheduleService,
                    automationAppService));

            parrotHubDelegatingScript.invokeMethod("run", null);

            DeviceScriptDelegateImpl dsd = (DeviceScriptDelegateImpl) parrotHubDelegatingScript.getDelegate();
            if (dsd.metadataValue != null) {
                ArrayList list = (ArrayList) dsd.metadataValue.get("preferences");
                Map<String, List> section = new HashMap<>();
                section.put("input", list);
                section.put("body", list);

                List<Map> sections = new ArrayList<>();
                sections.add(section);

                Map<String, Object> preferences = new HashMap<>();
                preferences.put("sections", sections);
                return preferences;
            } else {
                return new HashMap<>();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Map<String, Object> getDeviceTileLayout(Class<Script> deviceScript, Device device) {
        if (deviceScript == null) {
            return null;
        }

        try {
            ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) deviceScript.getConstructor()
                    .newInstance();
            parrotHubDelegatingScript.setDelegate(new DeviceTilesDelegate(null));

            parrotHubDelegatingScript.invokeMethod("run", null);

            DeviceTilesDelegate dtd = (DeviceTilesDelegate) parrotHubDelegatingScript.getDelegate();
            if (dtd.getTiles() != null) {
                ArrayList list = (ArrayList) dtd.getTiles();
                Map<String, List> section = new HashMap<>();
                section.put("input", list);
                section.put("body", list);

                List<Map> sections = new ArrayList<>();
                sections.add(section);

                Map<String, Object> preferences = new HashMap<>();
                preferences.put("sections", sections);
                return preferences;
            } else {
                return new HashMap<>();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object getParentForDevice(String deviceId) {
        Device device = deviceService.getDeviceById(deviceId);
        if (device == null) {
            return null;
        }
        if (StringUtils.isNotBlank(device.getParentDeviceId())) {
            return deviceService.getDeviceById(device.getParentDeviceId());
        }
        if (StringUtils.isNotBlank(device.getParentInstalledAutomationAppId())) {
            return automationAppService.getInstalledAutomationApp(device.getParentInstalledAutomationAppId());
        }
        return null;
    }

    @Override
    public void updateInstalledAutomationAppState(String id, Map state) {
        automationAppService.saveState(id, state);
    }

    @Override
    public Map getInstalledAutomationAppState(String id) {
        return automationAppService.getInstalledAutomationApp(id).getState();
    }

    public boolean updateAutomationAppSourceCode(String id, String sourceCode) {
        if (automationAppService.updateAutomationAppSourceCode(id, sourceCode)) {
            reprocessAutomationApp(id);
            clearAutomationAppScript(id);
            return true;
        }
        return false;
    }

    public boolean updateDeviceHandlerSourceCode(String id, String sourceCode) {
        if (deviceService.updateDeviceHandlerSourceCode(id, sourceCode)) {
            reprocessDeviceHandler(id);
            clearDeviceHandlerScript(id);
            return true;
        }
        return false;
    }
}
