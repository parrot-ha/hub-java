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
package com.parrotha.internal.entity;

import groovy.json.JsonSlurperClassic;
import com.parrotha.internal.ServiceFactory;
import com.parrotha.internal.hub.ScheduleService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Map;

public class EntityJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String handlerMethod = (String) context.getJobDetail().getJobDataMap().get("handlerMethod");
        String type = (String) context.getJobDetail().getJobDataMap().get("type");
        String id = (String) context.getJobDetail().getJobDataMap().get("id");
        Object dataObj = context.getJobDetail().getJobDataMap().get("data");
        Map data = null;
        if (dataObj instanceof String) {
            data = (Map) new JsonSlurperClassic().parseText((String) dataObj);
        }

        if (ScheduleService.INSTALLED_AUTOMATION_APP_TYPE.equals(type)) {
            ServiceFactory.getEntityService().runInstalledAutomationAppMethod(id, handlerMethod, data);
        } else if (ScheduleService.DEVICE_TYPE.equals(type)) {
            ServiceFactory.getEntityService().runDeviceMethod(id, handlerMethod, data);
        }
    }
}
