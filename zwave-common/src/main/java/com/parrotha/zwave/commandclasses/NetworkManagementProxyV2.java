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
public class NetworkManagementProxyV2 {
    public com.parrotha.zwave.commands.networkmanagementproxyv2.NodeListGet nodeListGet() {
        return new com.parrotha.zwave.commands.networkmanagementproxyv2.NodeListGet();
    }

    public com.parrotha.zwave.commands.networkmanagementproxyv2.NodeListGet nodeListGet(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.networkmanagementproxyv2.NodeListGet cmd = new com.parrotha.zwave.commands.networkmanagementproxyv2.NodeListGet();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

    public com.parrotha.zwave.commands.networkmanagementproxyv2.NodeListReport nodeListReport() {
        return new com.parrotha.zwave.commands.networkmanagementproxyv2.NodeListReport();
    }

    public com.parrotha.zwave.commands.networkmanagementproxyv2.NodeListReport nodeListReport(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.networkmanagementproxyv2.NodeListReport cmd = new com.parrotha.zwave.commands.networkmanagementproxyv2.NodeListReport();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

    public com.parrotha.zwave.commands.networkmanagementproxyv2.NodeInfoCachedGet nodeInfoCachedGet() {
        return new com.parrotha.zwave.commands.networkmanagementproxyv2.NodeInfoCachedGet();
    }

    public com.parrotha.zwave.commands.networkmanagementproxyv2.NodeInfoCachedGet nodeInfoCachedGet(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.networkmanagementproxyv2.NodeInfoCachedGet cmd = new com.parrotha.zwave.commands.networkmanagementproxyv2.NodeInfoCachedGet();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

    public com.parrotha.zwave.commands.networkmanagementproxyv2.NodeInfoCachedReport nodeInfoCachedReport() {
        return new com.parrotha.zwave.commands.networkmanagementproxyv2.NodeInfoCachedReport();
    }

    public com.parrotha.zwave.commands.networkmanagementproxyv2.NodeInfoCachedReport nodeInfoCachedReport(Map values) throws InvocationTargetException, IllegalAccessException {
        com.parrotha.zwave.commands.networkmanagementproxyv2.NodeInfoCachedReport cmd = new com.parrotha.zwave.commands.networkmanagementproxyv2.NodeInfoCachedReport();
        BeanUtils.populate(cmd, values);
        return cmd;
    }

}
