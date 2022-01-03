/**
 * Copyright (c) 2021-2022 by the respective copyright holders.
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
package com.parrotha.internal.script.device;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import com.parrotha.internal.device.Device;
import com.parrotha.internal.device.DeviceScriptDelegateImpl;
import com.parrotha.internal.hub.ScheduleService;
import com.parrotha.internal.script.ParrotHubDelegatingScript;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeviceScriptDelegateTest {
    @Test
    public void testStandardTiles() {
        String script =
                "tiles(scale: 2) {\n" +
                        "    standardTile(\"refresh\", \"device.switch\", inactiveLabel: false, decoration: \"flat\", width: 2, height: 2) {\n" +
                        "        state \"default\", label:\"\", action:\"refresh.refresh\", icon:\"st.secondary.refresh\"\n" +
                        "    }\n" +
                        "    standardTile(\"clearState\", \"device.switch\") {\n" +
                        "    \tstate \"default\", lable:\"clear\", action: \"clearState\"\n" +
                        "    }\n" +
                        "    main \"refresh\"\n" +
                        "    details([\"refresh\", \"clearState\"])\n" +
                        "}";
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass("com.parrotha.internal.script.ParrotHubDelegatingScript");
        GroovyShell shell = new GroovyShell(compilerConfiguration);
        ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) shell.parse(script);
        parrotHubDelegatingScript.setDelegate(new DeviceScriptDelegateImpl(new Device()));

        parrotHubDelegatingScript.invokeMethod("run", null);

        DeviceScriptDelegateImpl dsd = (DeviceScriptDelegateImpl) parrotHubDelegatingScript.getDelegate();
        //[scale:2, definitions:[[width:2, height:2, canChangeIcon:false, inactiveLabel:false, canChangeBackground:false, states:[[label:, action:refresh.refresh, icon:st.secondary.refresh, name:default]], decoration:flat, type:standard, name:refresh, attribute:device.switch], [width:1, height:1, canChangeIcon:false, inactiveLabel:true, canChangeBackground:false, states:[[lable:clear, action:clearState, name:default]], type:standard, name:clearState, attribute:device.switch], [width:2, height:2, canChangeIcon:false, inactiveLabel:false, canChangeBackground:false, states:[[label:, action:refresh.refresh, icon:st.secondary.refresh, name:default]], decoration:flat, type:standard, name:refresh, attribute:device.switch], [width:1, height:1, canChangeIcon:false, inactiveLabel:true, canChangeBackground:false, states:[[lable:clear, action:clearState, name:default]], type:standard, name:clearState, attribute:device.switch]], main:[refresh], details:[refresh, clearState]]
        assertEquals(2, dsd.tiles.get("scale"));
        System.out.println(dsd.tiles);
        assertNotNull(dsd.tiles.get("definitions"));

    }

    @Test
    public void testRunOnce() {
        Device device = new Device();
        device.setId("1");
        ScheduleService mockScheduleService = mock(ScheduleService.class);
        DeviceScriptDelegateImpl deviceScriptDelegate = new DeviceScriptDelegateImpl(device, null, null,
                null, mockScheduleService,
                null);

        deviceScriptDelegate.runOnce(Date.from(Instant.parse("2021-03-07T11:12:13.456Z")), "myMethod1");
        deviceScriptDelegate.runOnce("2021-03-07T11:12:13.457Z", "myMethod2");

        deviceScriptDelegate.runOnce(Date.from(Instant.parse("2021-03-07T11:12:13.458Z")), "myMethod3", new HashMap<>());
        deviceScriptDelegate.runOnce("2021-03-07T11:12:13.459Z", "myMethod4", new HashMap<>());

        verify(mockScheduleService, atMostOnce()).schedule(eq(ScheduleService.DEVICE_TYPE), eq("1"), eq(1615115533456l), eq("myMethod1"), isNull());
        verify(mockScheduleService, atMostOnce()).schedule(eq(ScheduleService.DEVICE_TYPE), eq("1"), eq(1615115533457l), eq("myMethod2"), isNull());
        verify(mockScheduleService, atMostOnce()).schedule(eq(ScheduleService.DEVICE_TYPE), eq("1"), eq(1615115533458l), eq("myMethod3"), isNull());
        verify(mockScheduleService, atMostOnce()).schedule(eq(ScheduleService.DEVICE_TYPE), eq("1"), eq(1615115533459l), eq("myMethod4"), isNull());
    }

    @Test
    public void testParseLanMessage() {
        Map parsedLanMessage = DeviceScriptDelegateImpl.parseLanMessage("index:11, mac:D4D252A89864, headers:R0VUIC8gSFRUUC8xLjENCkNvbm5lY3Rpb246IGNsb3NlDQpVc2VyLUFnZW50OiBQb3N0bWFuUnVudGltZS83LjI2LjINCkFjY2VwdDogKi8qDQpDYWNoZS1Db250cm9sOiBuby1jYWNoZQ0KUG9zdG1hbi1Ub2tlbjogOGU3YjI2MmYtNTg4OC00Mjg1LWE3YWQtM2YzZWRmNTYxOWRmDQpIb3N0OiAxOTIuMTY4LjEuMTE6Mzk1MDANCkFjY2VwdC1FbmNvZGluZzogZ3ppcCwgZGVmbGF0ZSwgYnI=, body:");

        assertNotNull(parsedLanMessage);
        assertEquals(5, parsedLanMessage.size());
        assertNull(parsedLanMessage.get("body"));
        assertNotNull(parsedLanMessage.get("index"));
        assertNotNull(parsedLanMessage.get("header"));
        assertTrue(parsedLanMessage.get("header") instanceof String);
        assertNotNull(parsedLanMessage.get("mac"));
        assertEquals("D4D252A89864", parsedLanMessage.get("mac"));

        assertNotNull(parsedLanMessage.get("headers"));
        assertTrue(parsedLanMessage.get("headers") instanceof Map);
        assertEquals(8, ((Map<?, ?>) parsedLanMessage.get("headers")).size());

        parsedLanMessage = DeviceScriptDelegateImpl.parseLanMessage("mac: 2C3AE806B09D, headers: R0VUIC9kYXRhL1NpZGVEb29yL1N0YXRlLzAgSFRUUC8xLjEKQWNjZXB0OiAqLyo7cT0wLjEKVXNlci1BZ2VudDogRVNQIEVhc3kvMjAxMDkvSnVsIDIxIDIwMjAgMDk6NDk6NTkKQ29ubmVjdGlvbjogY2xvc2UKSG9zdDogMTkyLjE2OC4xLjE2OjM5NTAwCg==, body: ");
        assertNotNull(parsedLanMessage);
    }

    @Test
    public void testMultiAttributeTiles() {
        String script =
                "tiles(scale: 2) {\n" +
                        "    multiAttributeTile(name:\"switch\", type: \"lighting\", width: 6, height: 4, canChangeIcon: true){\n" +
                        "            tileAttribute (\"device.switch\", key: \"PRIMARY_CONTROL\") {\n" +
                        "                attributeState \"on\", label:'${name}', action:\"switch.off\", icon:\"st.Home.home30\", backgroundColor:\"#00A0DC\", nextState:\"turningOff\"\n" +
                        "                attributeState \"off\", label:'${name}', action:\"switch.on\", icon:\"st.Home.home30\", backgroundColor:\"#FFFFFF\", nextState:\"turningOn\", defaultState: true\n" +
                        "                attributeState \"turningOn\", label:'Turning On', action:\"switch.off\", icon:\"st.Home.home30\", backgroundColor:\"#00A0DC\", nextState:\"turningOn\"\n" +
                        "                attributeState \"turningOff\", label:'Turning Off', action:\"switch.on\", icon:\"st.Home.home30\", backgroundColor:\"#FFFFFF\", nextState:\"turningOff\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    main \"switch\"\n" +
                        "    details([\"switch\"])\n" +
                        "}";
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass("com.parrotha.internal.script.ParrotHubDelegatingScript");
        GroovyShell shell = new GroovyShell(compilerConfiguration);
        ParrotHubDelegatingScript dhScript = (ParrotHubDelegatingScript) shell.parse(script);
        dhScript.setDelegate(new DeviceScriptDelegateImpl(new Device()));

        dhScript.invokeMethod("run", null);

        DeviceScriptDelegateImpl dsd = (DeviceScriptDelegateImpl) dhScript.getDelegate();
        assertEquals(2, dsd.tiles.get("scale"));
        System.out.println(dsd.tiles);
        System.out.println(dsd.tiles.get("definitions"));
        System.out.println("[{width=6, height=4, canChangeIcon=true, inactiveLabel=true, canChangeBackground=false, states=[], name=switch, type=multi.lighting, attribute=multi, attributes=[{states=[{label=${name}, action=switch.off, icon=st.Home.home30, backgroundColor=#00A0DC, nextState=turningOff, name=on}, {label=${name}, action=switch.on, icon=st.Home.home30, backgroundColor=#FFFFFF, nextState=turningOn, defaultState=true, name=off}, {label=Turning On, action=switch.off, icon=st.Home.home30, backgroundColor=#00A0DC, nextState=turningOn, name=turningOn}, {label=Turning Off, action=switch.on, icon=st.Home.home30, backgroundColor=#FFFFFF, nextState=turningOff, name=turningOff}], name=device.switch, key=PRIMARY_CONTROL, attribute=device.switch}]}]");

        // should be: {scale=2, definitions=[{width=6, height=4, canChangeIcon=true, inactiveLabel=true, canChangeBackground=false, states=[], name=switch, type=multi.lighting, attribute=multi, attributes=[{states=[{label=${name}, action=switch.off, icon=st.Home.home30, backgroundColor=#00A0DC, nextState=turningOff, name=on}, {label=${name}, action=switch.on, icon=st.Home.home30, backgroundColor=#FFFFFF, nextState=turningOn, defaultState=true, name=off}, {label=Turning On, action=switch.off, icon=st.Home.home30, backgroundColor=#00A0DC, nextState=turningOn, name=turningOn}, {label=Turning Off, action=switch.on, icon=st.Home.home30, backgroundColor=#FFFFFF, nextState=turningOff, name=turningOff}], name=device.switch, key=PRIMARY_CONTROL, attribute=device.switch}]}], main=[switch], details=[switch]}
        /*
        [
  {
    width=6,
    height=4,
    canChangeIcon=true,
    inactiveLabel=true,
    canChangeBackground=false,
    states=[],
    name=switch,
    type=multi.lighting,
    attribute=multi,
    attributes=[
      {
        states=[
          {label=${name}, action=switch.off, icon=st.Home.home30, backgroundColor=#00A0DC, nextState=turningOff, name=on},
          {label=${name}, action=switch.on, icon=st.Home.home30, backgroundColor=#FFFFFF, nextState=turningOn, defaultState=true, name=off},
          {label=Turning On, action=switch.off, icon=st.Home.home30, backgroundColor=#00A0DC, nextState=turningOn, name=turningOn},
          {label=Turning Off, action=switch.on, icon=st.Home.home30, backgroundColor=#FFFFFF, nextState=turningOff, name=turningOff}
        ],
        name=device.switch,
        key=PRIMARY_CONTROL,
        attribute=device.switch
      }
    ]
  }
]
         */
        assertNotNull(dsd.tiles.get("definitions"));
        assertEquals(1, ((List) (dsd.tiles.get("definitions"))).size());

    }
}
