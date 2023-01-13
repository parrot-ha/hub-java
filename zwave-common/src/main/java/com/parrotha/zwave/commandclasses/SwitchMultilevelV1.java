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
package com.parrotha.zwave.commandclasses;

import com.parrotha.zwave.internal.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Note: This code is autogenerated, changes will be overwritten.
 */
public class SwitchMultilevelV1 {
    public com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelSet switchMultilevelSet() {
        return new com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelSet();
    }

    public com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelSet switchMultilevelSet(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd = new com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelSet();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

    public com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelGet switchMultilevelGet() {
        return new com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelGet();
    }

    public com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelGet switchMultilevelGet(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelGet cmd = new com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelGet();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

    public com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelReport switchMultilevelReport() {
        return new com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelReport();
    }

    public com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelReport switchMultilevelReport(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd = new com.parrotha.zwave.commands.switchmultilevelv1.SwitchMultilevelReport();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

}
