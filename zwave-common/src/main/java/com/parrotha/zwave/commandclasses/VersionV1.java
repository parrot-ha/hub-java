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
package com.parrotha.zwave.commandclasses;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Note: This code is autogenerated, changes will be overwritten.
 */
public class VersionV1 {
    public com.parrotha.zwave.commands.versionv1.VersionGet versionGet() {
        return new com.parrotha.zwave.commands.versionv1.VersionGet();
    }

    public com.parrotha.zwave.commands.versionv1.VersionGet versionGet(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.versionv1.VersionGet cmd = new com.parrotha.zwave.commands.versionv1.VersionGet();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

    public com.parrotha.zwave.commands.versionv1.VersionReport versionReport() {
        return new com.parrotha.zwave.commands.versionv1.VersionReport();
    }

    public com.parrotha.zwave.commands.versionv1.VersionReport versionReport(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.versionv1.VersionReport cmd = new com.parrotha.zwave.commands.versionv1.VersionReport();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

    public com.parrotha.zwave.commands.versionv1.VersionCommandClassGet versionCommandClassGet() {
        return new com.parrotha.zwave.commands.versionv1.VersionCommandClassGet();
    }

    public com.parrotha.zwave.commands.versionv1.VersionCommandClassGet versionCommandClassGet(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.versionv1.VersionCommandClassGet cmd = new com.parrotha.zwave.commands.versionv1.VersionCommandClassGet();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

    public com.parrotha.zwave.commands.versionv1.VersionCommandClassReport versionCommandClassReport() {
        return new com.parrotha.zwave.commands.versionv1.VersionCommandClassReport();
    }

    public com.parrotha.zwave.commands.versionv1.VersionCommandClassReport versionCommandClassReport(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.versionv1.VersionCommandClassReport cmd = new com.parrotha.zwave.commands.versionv1.VersionCommandClassReport();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

}
