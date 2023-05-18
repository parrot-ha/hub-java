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

import com.google.common.collect.Maps;
import com.parrotha.api.Request;
import com.parrotha.api.Response;
import com.parrotha.app.AtomicState;
import com.parrotha.app.ChildDeviceWrapper;
import com.parrotha.app.ChildDeviceWrapperImpl;
import com.parrotha.app.DeviceWrapper;
import com.parrotha.app.DeviceWrapperImpl;
import com.parrotha.app.DeviceWrapperList;
import com.parrotha.app.DeviceWrapperListImpl;
import com.parrotha.app.InstalledAutomationAppWrapper;
import com.parrotha.app.InstalledAutomationAppWrapperImpl;
import com.parrotha.app.LocationWrapper;
import com.parrotha.device.HubAction;
import com.parrotha.device.HubResponse;
import com.parrotha.entity.EntityScriptDelegateCommon;
import com.parrotha.integration.CloudIntegration;
import com.parrotha.internal.ChangeTrackingMap;
import com.parrotha.internal.device.Device;
import com.parrotha.internal.device.DeviceService;
import com.parrotha.internal.entity.EntityPreferencesHelper;
import com.parrotha.internal.entity.EntityService;
import com.parrotha.internal.entity.LiveLogger;
import com.parrotha.internal.hub.EventService;
import com.parrotha.internal.hub.LocationService;
import com.parrotha.internal.hub.ScheduleService;
import com.parrotha.internal.integration.AbstractIntegration;
import com.parrotha.internal.integration.IntegrationRegistry;
import com.parrotha.internal.script.app.AutomationAppScriptDelegate;
import groovy.json.JsonSlurperClassic;
import groovy.lang.Closure;
import groovy.lang.MetaMethod;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutomationAppScriptDelegateImpl extends EntityScriptDelegateCommon implements AutomationAppScriptDelegate {
    InstalledAutomationApp installedAutomationApp;
    private LocationService locationService;
    private EventService eventService;
    private EntityService entityService;
    private DeviceService deviceService;
    private ScheduleService scheduleService;
    private AutomationAppService automationAppService;
    private IntegrationRegistry integrationRegistry;
    private boolean ignorePreferences = true;
    private boolean ignoreMappings = true;

    private Map params = new HashMap();
    private final Request request;

    public AutomationAppScriptDelegateImpl(InstalledAutomationApp installedAutomationApp) {
        this.installedAutomationApp = installedAutomationApp;
        this.state = new ChangeTrackingMap(installedAutomationApp.getState());
        this.request = new Request();
    }

    public AutomationAppScriptDelegateImpl(InstalledAutomationApp installedAutomationApp,
                                           LocationService locationService,
                                           EventService eventService,
                                           EntityService entityService,
                                           DeviceService deviceService,
                                           ScheduleService scheduleService,
                                           AutomationAppService automationAppService,
                                           IntegrationRegistry integrationRegistry) {
        this.installedAutomationApp = installedAutomationApp;
        this.locationService = locationService;
        this.eventService = eventService;
        this.entityService = entityService;
        this.deviceService = deviceService;
        this.scheduleService = scheduleService;
        this.automationAppService = automationAppService;
        this.integrationRegistry = integrationRegistry;
        this.state = new ChangeTrackingMap(installedAutomationApp.getState());
        this.request = new Request();
    }

    public AutomationAppScriptDelegateImpl(InstalledAutomationApp installedAutomationApp,
                                           LocationService locationService,
                                           EventService eventService,
                                           EntityService entityService,
                                           DeviceService deviceService,
                                           ScheduleService scheduleService,
                                           AutomationAppService automationAppService,
                                           IntegrationRegistry integrationRegistry,
                                           Request request) {
        this.installedAutomationApp = installedAutomationApp;
        this.locationService = locationService;
        this.eventService = eventService;
        this.entityService = entityService;
        this.deviceService = deviceService;
        this.scheduleService = scheduleService;
        this.automationAppService = automationAppService;
        this.integrationRegistry = integrationRegistry;
        this.state = new ChangeTrackingMap(installedAutomationApp.getState());
        this.request = request;
    }

    public AutomationAppScriptDelegateImpl(InstalledAutomationApp installedAutomationApp,
                                           LocationService locationService,
                                           EventService eventService,
                                           EntityService entityService,
                                           DeviceService deviceService,
                                           ScheduleService scheduleService,
                                           AutomationAppService automationAppService,
                                           IntegrationRegistry integrationRegistry,
                                           boolean ignorePreferences,
                                           boolean ignoreMappings) {
        this.installedAutomationApp = installedAutomationApp;
        this.locationService = locationService;
        this.eventService = eventService;
        this.entityService = entityService;
        this.deviceService = deviceService;
        this.scheduleService = scheduleService;
        this.ignorePreferences = ignorePreferences;
        this.ignoreMappings = ignoreMappings;
        this.automationAppService = automationAppService;
        this.integrationRegistry = integrationRegistry;
        this.state = new ChangeTrackingMap(installedAutomationApp.getState());
        this.request = new Request();
    }

    @Override
    public InstalledAutomationAppWrapper getApp() {
        return new InstalledAutomationAppWrapperImpl(installedAutomationApp, entityService, automationAppService);
    }


    // TODO: ST has some other interesting methods:
    // getTarget() - appears to be the executing app, ie returns: script_app_b6...e5@2e9ccf7f, maybe for grails Converter interface
    // setTarget(Object) - maybe for grails Converter interface
    // setParent(Object)


    //TODO: ST has some additional values in params: action, controller,  what do they do?

    // log.debug params
    //[appId:123, param1:things, action:[GET:executeSmartAppGet, POST:executeSmartAppPost, PUT:executeSmartAppPut, DELETE:executeSmartAppDelete, OPTIONS:executeSmartAppOptions], controller:smartAppApi]

    // params should include:
    // param1 = first item in url
    // param2 = second item in url, etc
    // ie:  /things/test   - param1=things, param2=test
    // any defined params in the url
    // ie: if mappings is defined as /things/:id  and you call /things/1
    // then id=1 in params :  params.id == 1
    // if body is url form encoded, then values from the body end up in params
    // any query parameters from the url also end up in params

    public Map getParams() {
        return params;
    }

    // request is available in an Automation App when it is called via a web endpoint.
    // it is an object that includes a JSON representation of the body and an XML representation of the body.
    // below is some examples of calls to interrogate the request object:
    // log.debug request.JSON   : [:]
    // log.debug request.XML :  org.codehaus.groovy.grails.web.converters.exceptions.ConverterException: Error parsing XML @line 101 (listThings)
    // log.debug request.method : POST
    //request.headerNames physicalgraph.api.FilteredHttpServletRequestWrapper$CollectionWrapperEnumeration@74455b4a
    //log.debug "user-agent header: " + request.getHeader("user-agent"); :  user-agent header: PostmanRuntime/7.xx

    public Request getRequest() {
        return request;
    }

    public void setBaseParams(Map params) {
        this.params = params;
    }

    private ChangeTrackingMap state;

    public Map getState() {
        return state;
    }

    private Map atomicState;

    public Map getAtomicState() {
        if (atomicState == null) {
            atomicState = new AtomicState(this.installedAutomationApp.getId(), entityService);
        }
        return atomicState;
    }

    public LocationWrapper getLocation() {
        return new LocationWrapper(locationService.getLocation());
    }

    public String getHubUID() {
        return locationService.getHub().getId();
    }

    private Map<String, Object> settings;

    @Override
    public Map getSettings() {
        if (this.settings == null) {
            Map<String, Object> settingsMap = new HashMap<>();
            List<InstalledAutomationAppSetting> settingsList = installedAutomationApp.getSettings();
            if (settingsList != null) {
                for (InstalledAutomationAppSetting setting : settingsList) {
                    if (setting.getType() != null) {
                        if (setting.getType().startsWith("capability")) {
                            if (setting.getValue() != null) {
                                if (setting.isMultiple()) {
                                    List<DeviceWrapper> devices = new ArrayList<>();
                                    if (StringUtils.isNotBlank(setting.getValue())) {
                                        List<String> values = ((List) new JsonSlurperClassic().parseText(setting.getValue()));
                                        for (String value : values) {
                                            Device device = deviceService.getDeviceById(value.trim());
                                            if (device != null) {
                                                devices.add(new DeviceWrapperImpl(device, deviceService, entityService, locationService));
                                            }
                                        }
                                    }
                                    if (devices.size() > 0) {
                                        settingsMap.put(setting.getName(), new DeviceWrapperListImpl(devices));
                                    }
                                } else {
                                    // just one device
                                    Device device = deviceService.getDeviceById(setting.getValue());
                                    if (device != null) {
                                        settingsMap
                                                .put(setting.getName(), new DeviceWrapperImpl(device, deviceService, entityService, locationService));
                                    }
                                }
                            }
                        } else {
                            settingsMap.put(setting.getName(), setting.getValueAsType());
                        }
                    } else {
                        settingsMap.put(setting.getName(), setting.getValue());
                    }
                }
            }
            this.settings = settingsMap;
        }
        return this.settings;
    }

    private LiveLogger log = null;

    public LiveLogger getLog() {
        if (log == null) {
            log = new LiveLogger("parrothub.live.iaa." + installedAutomationApp.getId());
        }
        return log;
    }

    String getTemperatureScale() {
        return getLocation().getTemperatureScale();
    }

    public Response render(Map params) {
        return new Response(params);
    }

    /**
     * Get the cloud url for a web service app endpoint
     *
     * @return String the url for a web service app.
     */
    public String getApiServerUrl() {
        return apiServerUrl(null);
    }

    /**
     * Get the cloud url for a web service app endpoint
     *
     * @return String the url for a web service app.
     */
    public String apiServerUrl(Object url) {
        AbstractIntegration integration = integrationRegistry.getIntegration(AbstractIntegration.IntegrationType.CLOUD);
        if (integration instanceof CloudIntegration) {
            if (url != null) {
                return ((CloudIntegration) integration).getApiServerUrl() + url;
            } else {
                return ((CloudIntegration) integration).getApiServerUrl();
            }
        } else {
            // TODO: get local IP address and return that.
            if (url != null) {
                return "http://TODO" + url;
            } else {
                return "http://TODO";
            }
        }
    }

    List<ChildDeviceWrapper> getAllChildDevices() {
        return getChildDevices(true);
    }

    List<ChildDeviceWrapper> getChildDevices() {
        return getChildDevices(false);
    }

    List<ChildDeviceWrapper> getChildDevices(boolean includeVirtualDevices) {
        //TODO: how to handle includeVirtualDevices?
        List<ChildDeviceWrapper> childDeviceWrappers = new ArrayList<>();
        for (Device childDevice : deviceService.getInstalledAutomationAppChildDevices(this.installedAutomationApp.getId())) {
            childDeviceWrappers.add(new ChildDeviceWrapperImpl(childDevice, deviceService, entityService, locationService));
        }
        return childDeviceWrappers;
    }

    ChildDeviceWrapper getChildDevice(String deviceNetworkId) {
        List<ChildDeviceWrapper> childDeviceWrappers = new ArrayList<>();
        Device childDevice = deviceService.getInstalledAutomationAppChildDevice(this.installedAutomationApp.getId(), deviceNetworkId);
        if (childDevice != null) {
            return new ChildDeviceWrapperImpl(childDevice, deviceService, entityService, locationService);
        } else {
            return null;
        }
    }

    ChildDeviceWrapper addChildDevice(String typeName, String deviceNetworkId, Object hubId, Map properties) {
        return addChildDevice(null, typeName, deviceNetworkId, hubId, properties);
    }

    ChildDeviceWrapper addChildDevice(String namespace, String typeName, String deviceNetworkId, Object hubId, Map properties) {
        Device childDevice = deviceService
                .addChildDevice(this.installedAutomationApp.getId(), DeviceService.PARENT_TYPE_IA_APP, namespace, typeName, deviceNetworkId,
                        properties);
        if (childDevice != null) {
            return new ChildDeviceWrapperImpl(childDevice, deviceService, entityService, locationService);
        }
        return null;
    }

    List<InstalledAutomationAppWrapper> getAllChildApps() {
        return getChildApps();
    }

    List<InstalledAutomationAppWrapper> getChildApps() {
        return automationAppService.getChildInstalledAutomationApps(this.installedAutomationApp.getId(), null, null).stream()
                .map(ca -> new InstalledAutomationAppWrapperImpl(ca, entityService, automationAppService)).collect(Collectors.toList());
    }


    public void unsubscribe() {
        eventService.removeSubscriptionsOfAutomationApp(installedAutomationApp.getId());
    }

    public void subscribe(Object object, MetaMethod handlerMethod) {
        if (handlerMethod != null) {
            subscribe(object, null, handlerMethod.getName(), null);
        }
    }

    public void subscribe(Object object, String handlerMethod) {
        if (handlerMethod != null) {
            subscribe(object, null, handlerMethod, null);
        }
    }

    public void subscribe(Object object, String attributeName, MetaMethod handlerMethod, Map options) {
        if (handlerMethod != null) {
            subscribe(object, attributeName, handlerMethod.getName(), options);
        }
    }

    public void subscribe(Object object, String attributeName, MetaMethod handlerMethod) {
        if (handlerMethod != null) {
            subscribe(object, attributeName, handlerMethod.getName(), null);
        }
    }

    public void subscribe(Object object, String attributeName, String handlerMethod) {
        subscribe(object, attributeName, handlerMethod, null);
    }

    public void subscribe(Object object, String attributeName, String handlerMethod, Map options) {
        if (object != null && handlerMethod != null) {
            if (object instanceof DeviceWrapper) {
                eventService.addDeviceSubscription(((DeviceWrapper) object).getId(), installedAutomationApp.getId(), attributeName, handlerMethod,
                        options);
            } else if (object instanceof DeviceWrapperList) {
                for (DeviceWrapper deviceWrapper : (DeviceWrapperList) object) {
                    eventService.addDeviceSubscription(deviceWrapper.getId(), installedAutomationApp.getId(), attributeName, handlerMethod, options);
                }
            } else if (object instanceof LocationWrapper) {
                eventService
                        .addLocationSubscription(((LocationWrapper) object).getId(), installedAutomationApp.getId(), attributeName, handlerMethod,
                                options);
            }
        }
    }

    public void subscribe(LocationWrapper locationWrapper, MetaMethod handlerMethod) {
        subscribe(locationWrapper, null, handlerMethod.getName());
    }

    public void include(String includeType) {
        // TODO: why would we want to do this?, can't we just have a getter for asynchttp_v1, which appears to be the
        // only thing that is included
    }

    public String createAccessToken() {
        AutomationApp automationApp = automationAppService.getAutomationAppById(installedAutomationApp.getAutomationAppId());
        if (automationApp != null && automationApp.isOAuthEnabled()) {
            String accessToken = UUID.randomUUID().toString();
            getState().put("accessToken", accessToken);
            return accessToken;
        } else {
            throw new RuntimeException("OAuth is not enabled");
        }
    }

    public void pause(Long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Date> getSunriseAndSunset() {
        return getSunriseAndSunset(new HashMap<>());
    }

    public Map<String, Date> getSunriseAndSunset(Map<String, Object> options) {
        return locationService.getSunriseAndSunset(options);
    }

    public Date toDateTime(String dateTimeString) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return df.parse(dateTimeString);
    }

    /**
     * https://docs.smartthings.com/en/latest/ref-docs/smartapp-ref.html#timetoday
     *
     * @param timeString
     * @param timeZone
     * @return
     */
    Date timeToday(String timeString, TimeZone timeZone) {
        Pattern p = Pattern.compile("[0-9]{2}:[0-9]{2}");
        Matcher m = p.matcher(timeString);
        ZonedDateTime zonedDateTime = null;
        if (m.matches()) {
            DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':')
                    .appendValue(ChronoField.MINUTE_OF_HOUR, 2).toFormatter();
            TemporalAccessor ta = dtf.parse(timeString);
            LocalTime localTime = LocalTime.from(ta);
            LocalDateTime ldt = localTime.atDate(LocalDate.now());
            zonedDateTime = ldt.atZone(ZoneId.of(timeZone.getID()));
        } else {
            TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(timeString);
            zonedDateTime = ZonedDateTime.from(ta);
            zonedDateTime.withZoneSameInstant(ZoneId.of(timeZone.getID()));
        }
        if (zonedDateTime != null) {
            ZonedDateTime today = ZonedDateTime.now();
            zonedDateTime.withDayOfMonth(today.getDayOfMonth());
            zonedDateTime.withMonth(today.getMonthValue());
            zonedDateTime.withYear(today.getYear());
            Instant i = Instant.from(zonedDateTime.toInstant());
            Date d = Date.from(i);
            return d;
        }
        return null;
    }

    /**
     * https://docs.smartthings.com/en/latest/ref-docs/smartapp-ref.html#timetoday
     *
     * @param timeString
     * @return
     */
    Date timeToday(String timeString) {
        return timeToday(timeString, getLocation().getTimeZone());
    }

    /**
     * https://docs.smartthings.com/en/latest/ref-docs/smartapp-ref.html#now
     *
     * @return current unix time in milliseconds
     */
    public Long now() {
        return System.currentTimeMillis();
    }

    public void runEvery15Minutes(MetaMethod handlerMethod) {
        runEvery15Minutes(handlerMethod.getName());
    }

    /**
     * Creates a recurring schedule that executes the specified handlerMethod every fifteen minutes.
     * Using this method will pick a random start time in the next fifteen minutes, and run every fifteen minutes after that.
     */
    public void runEvery15Minutes(String handlerMethod) {
        scheduleEveryTimeOfMinutes(15, handlerMethod);
    }

    public void runEvery30Minutes(MetaMethod handlerMethod) {
        runEvery30Minutes(handlerMethod.getName());
    }

    public void runEvery3Hours(String handlerMethod) {
        scheduleEveryTimeOfHours(3, handlerMethod);
    }

    public void runEvery3Hours(MetaMethod handlerMethod) {
        runEvery3Hours(handlerMethod.getName());
    }

    /**
     * Creates a recurring schedule that executes the specified handlerMethod every thirty minutes.
     * Using this method will pick a random start time in the next thirty minutes, and run every thirty minutes after that.
     */
    public void runEvery30Minutes(String handlerMethod) {
        scheduleEveryTimeOfMinutes(30, handlerMethod);
    }

    private void scheduleEveryTimeOfMinutes(int minutesParam, String handlerMethod) {
        // create a cron schedule that starts randomly in the next minutes
        Random rand = new Random();
        int seconds = rand.nextInt(60);
        // pick a random time to start
        int minutes = LocalTime.now().getMinute() + rand.nextInt(minutesParam);
        if (minutes > 59) {
            minutes = minutes - 60;
        }
        String cronExpression = String.format("%d %d/%d * * * ?", seconds, minutes, minutesParam);
        scheduleService.schedule(ScheduleService.INSTALLED_AUTOMATION_APP_TYPE, getApp().getId(), cronExpression, handlerMethod, null);
    }

    private void scheduleEveryTimeOfHours(int hourParam, String handlerMethod) {
        // create a cron schedule that starts randomly in the next minutes
        Random rand = new Random();
        int seconds = rand.nextInt(60);
        // pick a random time to start
        int minutes = LocalTime.now().getMinute() + rand.nextInt(60);
        if (minutes > 59) {
            minutes = minutes - 60;
        }
        String cronExpression = String.format("%d %d */%d * * ?", seconds, minutes, hourParam);
        scheduleService.schedule(ScheduleService.INSTALLED_AUTOMATION_APP_TYPE, getApp().getId(), cronExpression, handlerMethod, null);
    }

    public void runOnce(String dateTime, String handlerMethod) {
        runOnce(Date.from(OffsetDateTime.parse(dateTime).toInstant()), handlerMethod, null);
    }

    public void runOnce(String dateTime, String handlerMethod, Map<String, Object> options) {
        runOnce(Date.from(OffsetDateTime.parse(dateTime).toInstant()), handlerMethod, options);
    }

    public void runOnce(String dateTime, MetaMethod handlerMethod) {
        runOnce(Date.from(OffsetDateTime.parse(dateTime).toInstant()), handlerMethod.getName(), null);
    }

    public void runOnce(String dateTime, MetaMethod handlerMethod, Map<String, Object> options) {
        runOnce(Date.from(OffsetDateTime.parse(dateTime).toInstant()), handlerMethod.getName(), options);
    }

    public void runOnce(Date dateTime, String handlerMethod) {
        runOnce(dateTime, handlerMethod, null);
    }

    public void runOnce(Date dateTime, MetaMethod handlerMethod) {
        runOnce(dateTime, handlerMethod.getName(), null);
    }

    public void runOnce(Date dateTime, MetaMethod handlerMethod, Map<String, Object> options) {
        runOnce(dateTime, handlerMethod.getName(), options);
    }

    public void runOnce(Date dateTime, String handlerMethod, Map<String, Object> options) {
        Long runTime = dateTime.getTime();
        scheduleService.schedule(ScheduleService.INSTALLED_AUTOMATION_APP_TYPE, installedAutomationApp.getId(), runTime, handlerMethod, options);
    }

    public void runIn(Integer delayInSeconds, MetaMethod handlerMethod) {
        runIn(delayInSeconds, handlerMethod.getName(), null);
    }

    public void runIn(Integer delayInSeconds, String handlerMethod) {
        runIn(delayInSeconds, handlerMethod, null);
    }

    public void runIn(Integer delayInSeconds, MetaMethod handlerMethod, Map<String, Object> options) {
        runIn(delayInSeconds, handlerMethod.getName(), options);
    }

    public void runIn(Integer delayInSeconds, String handlerMethod, Map<String, Object> options) {
        Long runTime = now() + (delayInSeconds * 1000);
        scheduleService.schedule(ScheduleService.INSTALLED_AUTOMATION_APP_TYPE, installedAutomationApp.getId(), runTime, handlerMethod, options);
    }

    public void unschedule() {
        scheduleService.unschedule(ScheduleService.INSTALLED_AUTOMATION_APP_TYPE, installedAutomationApp.getId());
    }

    public void unschedule(String method) {
        scheduleService.unschedule(ScheduleService.INSTALLED_AUTOMATION_APP_TYPE, installedAutomationApp.getId(), method);
    }

    public void schedule(Date dateTime, MetaMethod handlerMethod) {
        schedule(dateTime, handlerMethod.getName(), new HashMap<>());
    }

    public void schedule(Date dateTime, String handlerMethod) {
        schedule(dateTime, handlerMethod, new HashMap<>());
    }

    public void schedule(Date dateTime, MetaMethod handlerMethod, Map<String, Object> options) {
        schedule(dateTime, handlerMethod.getName(), options);
    }

    public void schedule(Date dateTime, String handlerMethod, Map<String, Object> options) {
        if (dateTime == null || handlerMethod == null) {
            return;
        }
        ZonedDateTime zdt = ZonedDateTime.ofInstant(dateTime.toInstant(), TimeZone.getDefault().toZoneId());
        String cronExpression = String.format("%d %d %d * * ?", zdt.getSecond(), zdt.getMinute(), zdt.getHour());
        schedule(cronExpression, handlerMethod, options);
    }

    public void schedule(String cronExpression, MetaMethod handlerMethod) {
        schedule(cronExpression, handlerMethod.getName(), new HashMap<>());
    }

    public void schedule(String cronExpression, String handlerMethod) {
        schedule(cronExpression, handlerMethod, new HashMap<>());
    }

    public void schedule(String cronExpression, MetaMethod handlerMethod, Map<String, Object> options) {
        schedule(cronExpression, handlerMethod.getName(), options);
    }

    public void schedule(String cronExpression, String handlerMethod, Map<String, Object> options) {
        scheduleService
                .schedule(ScheduleService.INSTALLED_AUTOMATION_APP_TYPE, installedAutomationApp.getId(), cronExpression, handlerMethod, options);
    }

    public void sendLocationEvent(Map properties) {
        entityService.sendLocationEvent(properties);
    }

    public void sendEvent(Map properties) {
        entityService.sendEvent(properties, this.installedAutomationApp);
    }

    //https://docs.smartthings.com/en/latest/ref-docs/smartapp-ref.html#sendhubcommand
    void sendHubCommand(HubAction action) {
        new Thread(() -> {
            HubResponse retVal = deviceService.processHubAction(null, action);
            if (action.getCallback() != null) {
                retVal.setCallback(action.getCallback());
                entityService.runInstalledAutomationAppMethod(this.installedAutomationApp.getId(), action.getCallback(), retVal);
            }
        }).start();
    }

    void sendHubCommand(List<HubAction> actions) {
        sendHubCommand(actions, 1000);
    }

    void sendHubCommand(List<HubAction> actions, long delay) {
        new Thread(() -> {
            for (HubAction action : actions) {
                HubResponse retVal = deviceService.processHubAction(null, action);
                if (action.getCallback() != null) {
                    retVal.setCallback(action.getCallback());
                    entityService.runInstalledAutomationAppMethod(this.installedAutomationApp.getId(), action.getCallback(), retVal);
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //log.debug definition(name: "App Tester",namespace: "st",author: "ST",description: "App Test",category: "My Apps",iconUrl: "test",iconX2Url: "test",iconX3Url: "test")
    // [capabilities:[], commands:[], attributes:[], appSettings:[], name:App Tester, namespace:st, author:ST, description:App Test, category:My Apps, iconUrl:test, iconX2Url:test, iconX3Url:test]
    public Map definitionInfo;

    public void definition(Map properties) {
        definitionInfo = properties;
    }

    public Map<String, Object> preferences;
    public List<Map> pageList;

    public void preferences(Closure closure) {
        if (ignorePreferences) {
            return;
        }

        preferences = Stream.of(new Object[][]{
                {"sections", new ArrayList()},
                {"defaults", true},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1]));

        if (closure != null) {
            closure.run();
        }
    }

    // this was the preferences section at the top:
    //preferences {
    //	section {
    //    	input "mySwitch", title: "Switch", "capability.sensor", required: false
    //    }
    //}
    // and then from ST logging:
    //    log.debug preferences { page(name: "hello") };
    //    log.debug preferences
    //[[name:hello, title:null, nextPage:null, previousPage:null, content:hello, install:false, refreshInterval:-1, sections:[], popToAncestor:null, onUpdate:null]]
    // [sections:[[input:[[description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor]], body:[[element:input, description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor]], hideable:false, hidden:false]], defaults:true]
    // log.debug preferences { section(name: "hello") {} };
    // [[input:[[description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor]], body:[[element:input, description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor]], hideable:false, hidden:false], [input:[], body:[], hideable:false, hidden:false, name:hello]]
    //log.debug preferences { page(name: "pageone") { section("sectionOne") {} } }
    // [[name:pageone, title:null, nextPage:null, previousPage:null, content:null, install:false, refreshInterval:-1, sections:[[input:[], body:[], hideable:false, hidden:false, title:sectionOne]], popToAncestor:null, onUpdate:null]]
    // add log.debug preferences inside preferences section at top of app:
    // [sections:[[input:[[description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor]], body:[[element:input, description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor]], hideable:false, hidden:false]], defaults:true]
    // if we have pages defined, we get back a default value from preferences [sections:[], defaults:true], otherwise we get back a Map from preferences if it is a one page app.

    //Example preferences section:
    //preferences {
    //
    //	section("section1") {
    //    	input "mySwitch", title: "Switch", "capability.sensor", required: false
    //        input "switch2", title: "Switch2", "capability.sensor", required: false
    //        paragraph "Hello! this is paragraph"
    //    }
    //}
    // Example ST output:
    //[
    //  sections:[
    //    [
    //      input:[
    //        [description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor],
    //        [description:Tap to set, multiple:false, title:Switch2, required:false, name:switch2, type:capability.sensor]
    //      ],
    //      body:[
    //        [element:input, description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor],
    //        [element:input, description:Tap to set, multiple:false, title:Switch2, required:false, name:switch2, type:capability.sensor],
    //        [title:, description:Hello! this is paragraph, element:paragraph, type:paragraph, required:false, multiple:false]
    //      ],
    //      hideable:false,
    //      hidden:false,
    //      title:section1
    //    ]
    //  ],
    //  defaults:true
    //]

    private Map<String, Object> temporaryPage;
    private Map<String, Object> temporarySection;
    private boolean inDynamicPage = false;

    public Map getPreferences() {
        return preferences;
    }

    public List<Map> getPageList() {
        return pageList;
    }

    public void section(Map<String, Object> options, Closure closure) {
        temporarySection = Stream.of(new Object[][]{
                {"input", new ArrayList()},
                {"body", new ArrayList()},
                {"hideable", false},
                {"hidden", false},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1]));

        if (options != null) {
            temporarySection.putAll(options);
        }

        if (closure != null) {
            closure.run();
        }

        if (temporaryPage != null) {
            // we are inside a page setting
            ((List) temporaryPage.get("sections")).add(temporarySection);
        } else {
            // we are creating a single page app
            ((List) preferences.get("sections")).add(temporarySection);
        }

        temporarySection = null;
    }

    public void section(Map<String, Object> options, String sectionTitle, Closure closure) {
        if (options == null) {
            options = new HashMap<>();
        }
        if (sectionTitle != null) {
            options.put("title", sectionTitle);
        }
        section(options, closure);
    }

    public void section(String sectionTitle, Closure closure) {
        section(null, sectionTitle, closure);
    }

    public void section(Closure closure) {
        section(null, null, closure);
    }

    public void app(Map<String, Object> params) {
        //page(name: "mainPage", title: "Child Apps", install: true, uninstall: true) {
        //        section {
        //            app(name: "childApps", appName: "Child App", namespace: "mynamespace", title: "New Child App", multiple: true)
        //        }
        //    }
        //[element:app, type:app, multiple:true, name:childApps, appName:Child App, namespace:mynamespace, title:New Child App]], hideable:false, hidden:false]
        // create a child app input with default values
        LinkedHashMap<String, Object> tempApp = new LinkedHashMap<>();
        tempApp.put("element", "app");
        tempApp.put("type", "app");
        tempApp.put("multiple", false);
        tempApp.put("title", "");
        tempApp.put("name", "");

        if (params != null) {
            tempApp.putAll(params);
        }

        ((List) temporarySection.get("body")).add(tempApp);
    }

    //[title:, description:Hello! this is paragraph, element:paragraph, type:paragraph, required:false, multiple:false]
    public void paragraph(String description) {
        paragraph(null, description);
    }

    public void paragraph(Map<String, Object> params, String description) {
        // create a standard paragraph with default values
        LinkedHashMap<String, Object> tempParagraph = new LinkedHashMap<>();
        tempParagraph.put("title", "");
        tempParagraph.put("description", description);
        tempParagraph.put("element", "paragraph");
        tempParagraph.put("type", "paragraph");
        // defaults to false
        tempParagraph.put("required", false);
        tempParagraph.put("multiple", false);
        if (params != null) {
            tempParagraph.putAll(params);
        }

        ((List) temporarySection.get("body")).add(tempParagraph);
    }

    //[description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor]
    //[element:input, description:Tap to set, multiple:false, title:Switch, required:false, name:mySwitch, type:capability.sensor]
    //input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    //input:               [[description:, multiple:false, title:Enable debug logging, required:true, name:logEnable, type:bool, defaultValue:true]],
    //body: [[element:input, description:, multiple:false, title:Enable debug logging, required:true, name:logEnable, type:bool, defaultValue:true]]
    public void input(Map<String, Object> params, String name, String type) {
        Map<String, Object> paramsCopy = Maps.newHashMap(params);
        paramsCopy.put("name", name);
        paramsCopy.put("type", type);
        input(paramsCopy);
    }

    public void input(Map<String, Object> params) {
        // create a standard input with default values
        LinkedHashMap<String, Object> tempInput = EntityPreferencesHelper.input(params);

        ((List) temporarySection.get("input")).add(tempInput);

        HashMap tempBody = SerializationUtils.clone(tempInput);
        tempBody.put("element", "input");
        ((List) temporarySection.get("body")).add(tempBody);
    }

    public void label(Map<String, Object> params) {
        LinkedHashMap<String, Object> tempLabel = EntityPreferencesHelper.createLabel(params);
        ((List) temporarySection.get("body")).add(tempLabel);
    }

    public Map<String, Object> dynamicPage(Map<String, Object> params, Closure closure) {
        this.inDynamicPage = true;

        temporaryPage = EntityPreferencesHelper.createStandardPage();
        temporaryPage.putAll(params);

        if (closure != null) {
            closure.run();
        }

        Map<String, Object> newDynamicPage = new HashMap<>(temporaryPage);

        this.inDynamicPage = false;
        temporaryPage = null;
        return newDynamicPage;
    }

    public void page(Map<String, Object> params, Closure closure) {
        if (pageList == null) {
            pageList = new ArrayList<>();
        }

        temporaryPage = EntityPreferencesHelper.createStandardPage();
        temporaryPage.putAll(params);

        if (closure != null) {
            closure.setDelegate(this);
            closure.setResolveStrategy(Closure.DELEGATE_ONLY);
            closure.run();
        }

        pageList.add(temporaryPage);
        temporaryPage = null;
    }

    public void page(Map<String, Object> params) {
        if (pageList == null) {
            pageList = new ArrayList<>();
        }
        if (!params.containsKey("content")) {
            params.put("content", params.get("name"));
        }
        // this is a dynamic page definition, the contents are in a method.
        temporaryPage = EntityPreferencesHelper.createStandardPage();
        temporaryPage.putAll(params);

        pageList.add(temporaryPage);
        temporaryPage = null;
    }

    public void page(String name, String title) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("title", title);
        params.put("content", name);
        page(params);
    }

    public void page(String name) {
        page(name, null);
    }

    //href "prefLogIn2"
    //[title:Next Page, description:Tap to show, element:href, external:false, required:false, page:prefLogIn2]
    public void href(String page) {
        href(null, page);
    }

    //href "prefLogIn", title: "", description: "Tap to modify account", params: [nextPageName: "mainPage"]
    //title:, description:Tap to modify account, element:href, external:false, required:false, params:[nextPageName:mainPage], page:prefLogIn
    //href "prefLogIn2", title: ""
    //[title:, description:Tap to show, element:href, external:false, required:false, page:prefLogIn2]
    //href "prefLogIn2", description: ""
    //title:Next Page, description:, element:href, external:false, required:false, page:prefLogIn2
    // href(name: "Release notes", title: "Release notes", required: false, url: "https://github.com/${gitBranch()}/SmartThings_MyQ/blob/master/CHANGELOG.md")
    //title:Release notes, description:Tap to show, element:href, external:false, required:false, name:Release notes, url:https://github.com/brbeaird/SmartThings_MyQ/blob/master/CHANGELOG.md]
    public void href(Map<String, Object> params, String page) {
        params.put("page", page);
        href(params);
    }

    public void href(Map<String, Object> params) {
        LinkedHashMap<String, Object> tempHref = new LinkedHashMap<>();
        tempHref.put("title", "Next Page");
        tempHref.put("description", "Tap to show");
        tempHref.put("element", "href");
        tempHref.put("external", false);
        tempHref.put("required", false);

        if (params != null) {
            tempHref.putAll(params);
        }

        ((List) temporarySection.get("body")).add(tempHref);
    }

    private Map<String, Map<String, String>> pathMappings;

    public Map<String, Map<String, String>> getPathMappings() {
        return pathMappings;
    }

    public void mappings(Closure closure) {
        if (ignoreMappings) {
            return;
        }
        this.pathMappings = new HashMap<>();
        closure.run();
    }

    public void path(String path, Closure closure) {
        Object o = closure.call();
        if (o instanceof Map) {
            pathMappings.put(path, (Map<String, String>) o);
        }
    }

    @Override
    protected void runEntityMethod(String methodName, Object... args) {
        entityService.runInstalledAutomationAppMethod(this.installedAutomationApp.getId(), methodName, args);
    }
}
