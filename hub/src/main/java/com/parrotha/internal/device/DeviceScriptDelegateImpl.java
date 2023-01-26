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
package com.parrotha.internal.device;

import com.parrotha.app.ChildDeviceWrapper;
import com.parrotha.app.ChildDeviceWrapperImpl;
import com.parrotha.app.DeviceWrapper;
import com.parrotha.app.DeviceWrapperImpl;
import com.parrotha.app.InstalledAutomationAppWrapperImpl;
import com.parrotha.app.LocationWrapper;
import com.parrotha.app.ParentDeviceWrapperImpl;
import com.parrotha.device.HubAction;
import com.parrotha.device.HubMultiAction;
import com.parrotha.device.Protocol;
import com.parrotha.internal.ChangeTrackingMap;
import com.parrotha.internal.app.AutomationAppService;
import com.parrotha.internal.app.InstalledAutomationApp;
import com.parrotha.internal.entity.EntityScriptDelegateCommon;
import com.parrotha.internal.entity.EntityService;
import com.parrotha.internal.entity.LiveLogger;
import com.parrotha.internal.hub.LocationService;
import com.parrotha.internal.hub.ScheduleService;
import com.parrotha.internal.script.device.DeviceScriptDelegate;
import com.parrotha.zigbee.ZigBee;
import com.parrotha.zwave.Zwave;
import groovy.lang.Closure;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.GStringImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

public class DeviceScriptDelegateImpl extends EntityScriptDelegateCommon implements DeviceScriptDelegate {
    private static final Logger logger = LoggerFactory.getLogger(DeviceScriptDelegateImpl.class);

    private DeviceWrapper device;

    private ChangeTrackingMap state;

    private EntityService entityService;
    private DeviceService deviceService;
    private LocationService locationService;
    private ScheduleService scheduleService;
    private AutomationAppService automationAppService;

    public DeviceWrapper getDevice() {
        return device;
    }

    public Map getSettings() {
        return device.getSettings();
    }

    private LiveLogger log = null;

    public LiveLogger getLog() {
        if (log == null) {
            log = new LiveLogger("parrothub.live.dev." + device.getId());
        }
        return log;
    }

    public DeviceScriptDelegateImpl() {
    }

    public DeviceScriptDelegateImpl(Device device) {
        this.device = new DeviceWrapperImpl(device, null, null, null);
        this.state = new ChangeTrackingMap(device.getState());
    }

    public DeviceScriptDelegateImpl(Device device, DeviceService deviceService, EntityService entityService,
                                    LocationService locationService, ScheduleService scheduleService,
                                    AutomationAppService automationAppService) {
        this.device = new DeviceWrapperImpl(device, deviceService, entityService, locationService);
        this.entityService = entityService;
        this.deviceService = deviceService;
        this.locationService = locationService;
        this.scheduleService = scheduleService;
        this.automationAppService = automationAppService;
        this.state = new ChangeTrackingMap(device.getState());
    }

    public void sendHubCommand(HubAction action) {
        new Thread(() -> {
            if (action.getDni() == null) {
                action.setDni(device.getDeviceNetworkId());
            }
            deviceService.processHubAction(device.getIntegrationId(), action);
        }).start();
    }

    public void sendHubCommand(HubMultiAction hubMultiAction) {
        for(HubAction hubAction : hubMultiAction.getActions()) {
            sendHubCommand(hubAction);
        }
    }

    public DeviceWrapper addChildDevice(String typeName, String deviceNetworkId) {
        return addChildDevice(null, typeName, deviceNetworkId, null, null);
    }

    public DeviceWrapper addChildDevice(String typeName, String deviceNetworkId, String hubId, Map properties) {
        return addChildDevice(null, typeName, deviceNetworkId, hubId, properties);
    }

    public DeviceWrapper addChildDevice(String namespace, String typeName, String deviceNetworkId) {
        return addChildDevice(namespace, typeName, deviceNetworkId, null, null);
    }

    public DeviceWrapper addChildDevice(String namespace, String typeName, String deviceNetworkId, Object hubId, Map properties) {
        Device childDevice = deviceService.addChildDevice(device.getId(), DeviceService.PARENT_TYPE_DEVICE, namespace, typeName, deviceNetworkId,
                properties);
        if (childDevice != null) {
            return new DeviceWrapperImpl(childDevice, deviceService, entityService, locationService);
        }
        return null;
    }

    List<ChildDeviceWrapper> getChildDevices() {
        List<ChildDeviceWrapper> childDeviceWrappers = new ArrayList<>();
        for (Device childDevice : deviceService.getChildDevicesForDevice(this.device.getId())) {
            childDeviceWrappers.add(new ChildDeviceWrapperImpl(childDevice, deviceService, entityService, locationService));
        }
        return childDeviceWrappers;
    }

    // returns parent installed automation app wrapper or parent device wrapper depending
    public Object getParent() {
        Object parent = entityService.getParentForDevice(this.device.getId());
        if (parent instanceof Device) {
            return new ParentDeviceWrapperImpl((Device) parent, deviceService, entityService, locationService);
        } else if (parent instanceof InstalledAutomationApp) {
            return new InstalledAutomationAppWrapperImpl((InstalledAutomationApp) parent, entityService, automationAppService);
        }
        return null;
    }

    public Map getState() {
        return state;
    }

    public void updateDataValue(String dataValueName, Object dataValueValue) {
        getDevice().updateDataValue(dataValueName, dataValueValue);
    }

    public Object getDataValue(String dataValueName) {
        return getDevice().getDataValue(dataValueName);
    }

    private ZigBee zigbee;

    public ZigBee getZigbee() {
        if (zigbee == null) {
            try {
                Class<?> clazz = Class.forName("com.parrotha.zigbee.ZigBeeImpl");
                zigbee = (ZigBee) clazz.getDeclaredConstructor(DeviceWrapper.class).newInstance(device);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                logger.warn("Exception while loading ZigBee class, may not be implemented.", e);
            }
        }

        return zigbee;
    }

    private Zwave zwave;

    public Zwave getZwave() {
        if (zwave == null) {
            zwave = new Zwave();
        }
        return zwave;
    }

    public Map getZwaveInfo() {
        return device.getZwaveInfo();
    }

    public Integer getZwaveHubNodeId() {
        return device.getZwaveHubNodeId();
    }

    public LocationWrapper getLocation() {
        return new LocationWrapper(locationService.getLocation());
    }

    public void unschedule() {
        scheduleService.unschedule(ScheduleService.DEVICE_TYPE, getDevice().getId());
    }

    public void unschedule(String method) {
        scheduleService.unschedule(ScheduleService.DEVICE_TYPE, getDevice().getId(), method);
    }

    public void unschedule(MetaMethod method) {
        unschedule(method.getName());
    }

    /**
     * Creates a recurring schedule that executes the specified handlerMethod every one minute.
     * Using this method will pick a random start time in the next one minute, and run every one minute after that.
     */
    public void runEvery1Minute(String handlerMethod) {
        // create a cron schedule that starts randomly in the next 1 minute
        Random rand = new Random();
        int seconds = rand.nextInt(60);
        String cronExpression = String.format("%d * * * * ?", seconds);
        scheduleService.schedule("DEV", getDevice().getId(), cronExpression, handlerMethod, null);
    }

    public void runEvery1Minute(MetaMethod handlerMethod) {
        if (handlerMethod == null) {
            return;
        }

        runEvery1Minute(handlerMethod.getName());
    }

    /**
     * Creates a recurring schedule that executes the specified handlerMethod every five minutes.
     * Using this method will pick a random start time in the next five minutes, and run every five minutes after that.
     */
    public void runEvery5Minutes(String handlerMethod) {
        scheduleEveryTimeOfMinutes(5, handlerMethod);
    }

    public void runEvery5Minutes(MetaMethod handlerMethod) {
        if (handlerMethod == null) {
            return;
        }
        runEvery5Minutes(handlerMethod.getName());
    }

    /**
     * Creates a recurring schedule that executes the specified handlerMethod every ten minutes.
     * Using this method will pick a random start time in the next ten minutes, and run every ten minutes after that.
     */
    public void runEvery10Minutes(String handlerMethod) {
        scheduleEveryTimeOfMinutes(10, handlerMethod);
    }

    public void runEvery10Minutes(MetaMethod handlerMethod) {
        if (handlerMethod == null) {
            return;
        }
        runEvery10Minutes(handlerMethod.getName());
    }

    /**
     * Creates a recurring schedule that executes the specified handlerMethod every fifteen minutes.
     * Using this method will pick a random start time in the next fifteen minutes, and run every fifteen minutes after that.
     */
    public void runEvery15Minutes(String handlerMethod) {
        scheduleEveryTimeOfMinutes(15, handlerMethod);
    }

    public void runEvery15Minutes(MetaMethod handlerMethod) {
        if (handlerMethod == null) {
            return;
        }
        runEvery15Minutes(handlerMethod.getName());
    }

    /**
     * Creates a recurring schedule that executes the specified handlerMethod every thirty minutes.
     * Using this method will pick a random start time in the next thirty minutes, and run every thirty minutes after that.
     */
    public void runEvery30Minutes(String handlerMethod) {
        scheduleEveryTimeOfMinutes(30, handlerMethod);
    }

    public void runEvery30Minutes(MetaMethod handlerMethod) {
        if (handlerMethod == null) {
            return;
        }
        runEvery30Minutes(handlerMethod.getName());
    }

    private void scheduleEveryTimeOfMinutes(int minutesParam, String handlerMethod) {
        // create a cron schedule that starts randomly in the next minutes
        Random rand = new Random();
        int seconds = rand.nextInt(60);
        // pick a random time to start
        int minutes = rand.nextInt(minutesParam);
        String cronExpression = String.format("%d %d/%d * * * ?", seconds, minutes, minutesParam);
        scheduleService.schedule("DEV", getDevice().getId(), cronExpression, handlerMethod, null);
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
        scheduleService.schedule(ScheduleService.DEVICE_TYPE, device.getId(), runTime, handlerMethod, options);
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
        Long runTime = System.currentTimeMillis() + (delayInSeconds * 1000);
        scheduleService.schedule(ScheduleService.DEVICE_TYPE, this.device.getId(), runTime, handlerMethod, options);
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
                .schedule(ScheduleService.DEVICE_TYPE, device.getId(), cronExpression, handlerMethod, options);
    }

    /**
     * https://docs.smartthings.com/en/latest/ref-docs/smartapp-ref.html#now
     *
     * @return current unix time in milliseconds
     */
    public Long now() {
        return System.currentTimeMillis();
    }

    /**
     * https://docs.smartthings.com/en/latest/device-type-developers-guide/building-z-wave-device-handlers.html?highlight=delaybetween
     *
     * @param commands
     * @param delay
     * @return
     */
    public List<String> delayBetween(List<Object> commands, int delay) {
        if (commands == null) {
            return null;
        }
        if (commands.size() == 0) {
            return new ArrayList<>();
        }

        List<Object> flatList = flattenList(commands);
        List<String> returnList = new ArrayList<>();

        boolean addDelay = false;
        for (Object listItem : flatList) {
            if (listItem instanceof String || listItem instanceof GStringImpl) {
                if (addDelay) {
                    returnList.add(String.format("delay %d", delay));
                    addDelay = false;
                }
                if (!listItem.toString().startsWith("delay")) {
                    returnList.add(listItem.toString());
                    addDelay = true;
                }
            }
        }
        return returnList;
    }

    private List<Object> flattenList(List<Object> list) {
        List<Object> returnList = new ArrayList<>();
        for (Object listItem : list) {
            if (listItem instanceof List) {
                returnList.addAll(flattenList((List<Object>) listItem));
            } else {
                returnList.add(listItem);
            }
        }
        return returnList;
    }

    public List<String> delayBetween(List<Object> commands) {
        return delayBetween(commands, 100);
    }

    public HubAction response(com.parrotha.zwave.Command zWaveCommand) {
        return new HubAction(zWaveCommand.format(), Protocol.ZWAVE);
    }

    public HubAction response(String action) {
        return new HubAction(action);
    }

    public HubMultiAction response(List<String> action) {
        return new HubMultiAction(action);
    }

    /**
     * name (required)	String - The name of the Event. Typically corresponds to an attribute name of a capability.
     * value (required)	The value of the Event. The value is stored as a string, but you can pass numbers or other objects.
     * descriptionText	String - The description of this Event. This appears in the mobile application activity for the device. If not specified, this will be created using the Event name and value.
     * displayed	Pass true to display this Event in the mobile application activity feed, false to not display. Defaults to true.
     * linkText	String - Name of the Event to show in the mobile application activity feed. (Deprecated: use displayName)
     * displayName String - The user-friendly name of the source of this Event. Typically the user-assigned device label.
     * isStateChange	true if this Event caused a device attribute to change state. Typically not used, since it will be set automatically.
     * unit	String - a unit string, if desired. This will be used to create the descriptionText if it (the descriptionText option) is not specified.
     * data	A map of additional information to store with the Event
     *
     * @param properties
     */
    public void sendEvent(Map properties) {
        entityService.sendEvent(properties, device);
    }

    public Map createEvent(Map properties) {
        Map event = new HashMap();
        if (properties != null && properties.containsKey("name") && properties.containsKey("value")) {
            event.put("linkText", device.getDisplayName());
            event.put("descriptionText", String.format("%s %s is %s", device.getDisplayName(), properties.get("name"), properties.get("value")));
            event.put("displayed", true);
            Object currentValue = device.currentValue((String) properties.get("name"));
            event.put("isStateChange", (currentValue == null || !currentValue.equals(properties.get("value"))));

            event.putAll(properties);
        }
        return event;
    }

    // from st, if you call log.debug metadata you get the following:
    // [tiles:[:], preferences:[sections:[], defaults:true]]  this is the base for the metadata object, the rest needs
    // to be populated

    public Map metadataValue;

    Map metadata(Closure closure) {
        metadataValue = new HashMap();
        closure.run();
        return metadataValue;
    }

    void attribute(String attributeName, String attributeType) {
        // do nothing, this is handled by DeviceDefinitionDelegate
    }

    void attribute(String attributeName, String attributeType, List possibleValues) {
        // do nothing, this is handled by DeviceDefinitionDelegate
    }

    // [name:Cree Bulb Custom, namespace:smartthings, author:SmartThings, ocfDeviceType:oic.d.light, runLocally:true, executeCommandsLocally:true, minHubCoreVersion:000.022.0004]
    void definition(Map map, Closure closure) {
        metadataValue.put("definition", map);
        closure.run();
    }

    void preferences(Closure closure) {
        metadataValue.put("preferences", new ArrayList<Map>());
        closure.run();
    }

    public void section(Map<String, Object> options, Closure closure) {
        // TODO: handle sections in ui, for now just ignore them and add all inputs to preferences by running closure
        if (closure != null) {
            closure.run();
        }
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

    void input(Map options, String name, String type) {
        options.put("name", name);
        options.put("type", type);
        input(options);
    }

    void input(Map options) {
        ((List<Map>) metadataValue.get("preferences")).add(options);
    }

    void capability(String capability) {
        Map definitionSection = (Map) metadataValue.get("definition");
        if (definitionSection != null) {
            List<String> capabilityList;
            if (definitionSection.get("capabilityList") == null) {
                capabilityList = new ArrayList<>();
                definitionSection.put("capabilityList", capabilityList);
            } else {
                capabilityList = (List) definitionSection.get("capabilityList");
            }
            capabilityList.add(capability);
        }
    }

    void command(String name) {
        command(name, null);
    }

    void command(String name, List<Object> arguments) {
        Map definitionSection = (Map) metadataValue.get("definition");
        DeviceDelegateHelper.command(definitionSection, name, arguments);
    }

    void simulator(Closure closure) {
        //TODO: handle this
    }

    void tiles(Closure closure) {
        tiles(null, closure);
    }
    // example fingerprint return:
    //[[manufacturer:CREE, model:Connected A-19 60W Equivalent, deviceJoinName:Cree Light], [manufacturer:CREE, model:Connected A-19 60W Equivalent, deviceJoinName:Cree Light]]
    // from:fingerprint manufacturer: "CREE", model: "Connected A-19 60W Equivalent" , deviceJoinName: "Cree Light"// 0A C05E 0100 02 07 0000 1000 0004 0003 0005 0006 0008 02 0000 0019

    //fingerprint manufacturer: "CREE", model: "Connected A-19 60W Equivalent" , deviceJoinName: "Cree Light"// 0A C05E 0100 02 07 0000 1000 0004 0003 0005 0006 0008 02 0000 0019
    //fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,1000", outClusters: "0019", manufacturer: "GE_Appliances", model: "ZLL Light", deviceJoinName: "GE Light" //GE Link Bulb
    //fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,1000", outClusters: "0019", manufacturer: "GE", model: "SoftWhite", deviceJoinName: "GE Light" //GE Link Soft White Bulb
    //fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,1000", outClusters: "0019", manufacturer: "GE", model: "Daylight", deviceJoinName: "GE Light" //GE Link Daylight Bulb

    //[[manufacturer:CREE, model:Connected A-19 60W Equivalent, deviceJoinName:Cree Light],
    // [profileId:0104, inClusters:0000,0003,0004,0005,0006,0008,1000, outClusters:0019, manufacturer:GE_Appliances, model:ZLL Light, deviceJoinName:GE Light],
    //[profileId:0104, inClusters:0000,0003,0004,0005,0006,0008,1000, outClusters:0019, manufacturer:GE, model:SoftWhite, deviceJoinName:GE Light],
    // [profileId:0104, inClusters:0000,0003,0004,0005,0006,0008,1000, outClusters:0019, manufacturer:GE, model:Daylight, deviceJoinName:GE Light],
    void fingerprint(Map map) {
        if (metadataValue != null) {
            List<Map> fingerprints = (List<Map>) metadataValue.get("fingerprints");
            if (fingerprints == null) {
                fingerprints = new ArrayList<Map>();
                metadataValue.put("fingerprints", fingerprints);
            }
            fingerprints.add(map);
        }
    }


    // tile output
    //[scale:2, definitions:[[width:6, height:4, canChangeIcon:true, inactiveLabel:true, canChangeBackground:false, states:[], name:switch, type:multi.lighting, attribute:multi, attributes:[[states:[[label:${name}, action:switch.off, icon:st.switches.light.on, backgroundColor:#00A0DC, nextState:turningOff, name:on], [label:${name}, action:switch.on, icon:st.switches.light.off, backgroundColor:#ffffff, nextState:turningOn, name:off], [label:${name}, action:switch.off, icon:st.switches.light.on, backgroundColor:#00A0DC, nextState:turningOff, name:turningOn], [label:${name}, action:switch.on, icon:st.switches.light.off, backgroundColor:#ffffff, nextState:turningOn, name:turningOff]], name:device.switch, key:PRIMARY_CONTROL, attribute:device.switch], [states:[[action:switch level.setLevel, name:level]], name:device.level, key:SLIDER_CONTROL, attribute:device.level]]], [width:2, height:2, canChangeIcon:false, inactiveLabel:false, canChangeBackground:false, states:[[label:, action:refresh.refresh, icon:st.secondary.refresh, name:default]], decoration:flat, type:standard, name:refresh, attribute:device.switch], [width:6, height:4, canChangeIcon:true, inactiveLabel:true, canChangeBackground:false, states:[], name:switch, type:multi.lighting, attribute:multi, attributes:[[states:[[label:${name}, action:switch.off, icon:st.switches.light.on, backgroundColor:#00A0DC, nextState:turningOff, name:on], [label:${name}, action:switch.on, icon:st.switches.light.off, backgroundColor:#ffffff, nextState:turningOn, name:off], [label:${name}, action:switch.off, icon:st.switches.light.on, backgroundColor:#00A0DC, nextState:turningOff, name:turningOn], [label:${name}, action:switch.on, icon:st.switches.light.off, backgroundColor:#ffffff, nextState:turningOn, name:turningOff]], name:device.switch, key:PRIMARY_CONTROL, attribute:device.switch], [states:[[action:switch level.setLevel, name:level]], name:device.level, key:SLIDER_CONTROL, attribute:device.level]]], [width:2, height:2, canChangeIcon:false, inactiveLabel:false, canChangeBackgro...[TRUNCATED]
    //from:
    /*
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }




    tiles(scale: 2) {
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("clearState", "device.switch") {
        	state "default", lable:"clear", action: "clearState"
        }
        main "refresh"
        details(["refresh", "clearState"])
    }

    log.debug tiles :
    [scale:2, definitions:[[width:2, height:2, canChangeIcon:false, inactiveLabel:false, canChangeBackground:false, states:[[label:, action:refresh.refresh, icon:st.secondary.refresh, name:default]], decoration:flat, type:standard, name:refresh, attribute:device.switch], [width:1, height:1, canChangeIcon:false, inactiveLabel:true, canChangeBackground:false, states:[[lable:clear, action:clearState, name:default]], type:standard, name:clearState, attribute:device.switch], [width:2, height:2, canChangeIcon:false, inactiveLabel:false, canChangeBackground:false, states:[[label:, action:refresh.refresh, icon:st.secondary.refresh, name:default]], decoration:flat, type:standard, name:refresh, attribute:device.switch], [width:1, height:1, canChangeIcon:false, inactiveLabel:true, canChangeBackground:false, states:[[lable:clear, action:clearState, name:default]], type:standard, name:clearState, attribute:device.switch]], main:[refresh], details:[refresh, clearState]]
    Tiles were repeated for some reason, could just be a logging issue, we will not repeat them.
     */

    public Map tiles;

    void tiles(Map options, Closure closure) {
        DeviceTilesDelegate dtd = new DeviceTilesDelegate(options);
        if (closure != null) {
            closure.setDelegate(dtd);
            closure.setResolveStrategy(Closure.DELEGATE_ONLY);
            closure.run();
        }
        this.tiles = dtd.getTiles();
    }
}
