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
package com.parrotha.internal.device;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Capabilities {
    ACCELERATION_SENSOR("AccelerationSensor", new Capability("", "AccelerationSensor", "capability.accelerationSensor",
            Arrays.asList(new Attribute("", "ENUM", "acceleration", Arrays.asList(new String[]{"active", "inactive"}))), null)),
    ACTUATOR("Actuator", new Capability("", "Actuator", "capability.actuator", null, null)),
    AUDIO_VOLUME("AudioVolume", new Capability("", "AudioVolume", "capability.audioVolume",
            Arrays.asList(new Attribute("", "NUMBER", "volume", null)),
            Arrays.asList(new Command("setVolume", "volume", "duration"),
                    new Command("volumeUp"),
                    new Command("volumeDown")))),
    BATTERY("Battery", new Capability("", "Battery", "capability.battery",
            Arrays.asList(new Attribute("", "NUMBER", "battery", null)), null)),
    CONFIGURATION("Configuration", new Capability("", "Configuration", "capability.configuration", null,
            Arrays.asList(new Command("configure")))),
    CONTACT_SENSOR("ContactSensor", new Capability("", "ContactSensor", "capability.contactSensor",
            Arrays.asList(new Attribute("", "ENUM", "contact", Arrays.asList(new String[]{"open", "closed"}))), null)),
    ILLUMINANCE_MEASUREMENT("IlluminanceMeasurement", new Capability("", "IlluminanceMeasurement", "capability.illuminanceMeasurement",
            Arrays.asList(new Attribute("", "NUMBER", "illuminance", null)), null)),
    LOCK("Lock", new Capability("", "Lock", "capability.lock",
            Arrays.asList(new Attribute("", "ENUM", "lock", Arrays.asList(new String[]{"locked", "unlocked", "unknown", "unlocked with timeout"}))),
            Arrays.asList(new Command("lock"), new Command("unlock")))),
    MOTION_SENSOR("MotionSensor", new Capability("", "MotionSensor", "capability.motionSensor",
            Arrays.asList(new Attribute("", "ENUM", "motion", Arrays.asList(new String[]{"action", "inactive"}))), null)),
    POLLING("Polling", new Capability("", "Polling", "capability.polling", null,
            Arrays.asList(new Command("poll")))),
    POWER_METER("PowerMeter", new Capability("", "PowerMeter", "capability.powerMeter",
            Arrays.asList(new Attribute("", "NUMBER", "power", null)), null)),
    REFRESH("Refresh", new Capability("", "Refresh", "capability.refresh", null,
            Arrays.asList(new Command("refresh")))),
    RELATIVE_HUMIDITY_MEASURMENT("RelativeHumidityMeasurement", new Capability("", "RelativeHumidityMeasurement", "capability.relativeHumidityMeasurement",
            Arrays.asList(new Attribute("", "NUMBER", "humidity", null)), null)),
    SENSOR("Sensor", new Capability("", "Sensor", "capability.sensor", null, null)),
    SMOKE_DETECTOR("SmokeDetector", new Capability("", "SmokeDetector", "capability.smokeDetector",
            Arrays.asList(new Attribute("", "ENUM", "smoke", Arrays.asList(new String[]{"clear", "detected", "tested"}))), null)),
    SPEECH_SYNTHESIS("SpeechSynthesis", new Capability("", "SpeechSynthesis", "capability.speechSynthesis", null,
            Arrays.asList(new Command("speak", "phrase")))),
    SWITCH("Switch", new Capability("", "Switch", "capability.switch",
            Arrays.asList(new Attribute("", "ENUM", "switch", Arrays.asList(new String[]{"on", "off"}))),
            Arrays.asList(new Command("on"), new Command("off")))),
    SWITCH_LEVEL("SwitchLevel", new Capability("", "SwitchLevel", "capability.switchLevel",
            Arrays.asList(new Attribute("", "NUMBER", "level", null)),
            Arrays.asList(new Command("setLevel", "level", "duration")))),
    TEMPERATURE_MEASUREMENT("TemperatureMeasurement", new Capability("", "TemperatureMeasurement", "capability.temperatureMeasurement",
            Arrays.asList(new Attribute("", "NUMBER", "temperature", null)), null)),
    OUTLET("Outlet", new Capability("", "Outlet", "capability.outlet",
            Arrays.asList(new Attribute("", "ENUM", "switch", Arrays.asList(new String[]{"on", "off"}))),
            Arrays.asList(new Command("on"), new Command("off")))),
    WATER_SENSOR("WaterSensor", new Capability("", "WaterSensor", "capability.waterSensor",
            Arrays.asList(new Attribute("", "ENUM", "water", Arrays.asList(new String[]{"dry", "wet"}))), null));

    private String capabilityName;
    private Capability capability;

    Capabilities(String capabilityName, Capability capability) {
        this.capabilityName = capabilityName;
        this.capability = capability;
    }

    private static Map<String, Capability> capabilityMap;

    private synchronized static void createType() {
        if (capabilityMap != null) return;

        Map<String, Capability> newCapabilityMap = new HashMap<>();
        for (Capabilities c : values()) {
            newCapabilityMap.put(c.capabilityName, c.capability);
        }
        capabilityMap = newCapabilityMap;
    }

    public static Capability getCapability(String name) {
        if (capabilityMap == null) {
            createType();
        }
        return capabilityMap.get(StringUtils.deleteWhitespace(name));
    }
}
