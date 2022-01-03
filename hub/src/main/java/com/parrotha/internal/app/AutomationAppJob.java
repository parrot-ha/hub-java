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
package com.parrotha.internal.app;

import com.parrotha.internal.ServiceFactory;
import com.parrotha.internal.hub.ScheduleService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Deprecated
public class AutomationAppJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String handlerMethod = "";
        String jobKeyName = context.getJobDetail().getKey().getName();
        if(jobKeyName.contains(":")) {
            handlerMethod = jobKeyName.split(":")[0];
        } else {
            handlerMethod = jobKeyName;
        }

        String jobKeyGroup = context.getJobDetail().getKey().getGroup();
        String automationAppId = "";
        if(jobKeyGroup.startsWith(ScheduleService.INSTALLED_AUTOMATION_APP_TYPE + ":")) {
            automationAppId = jobKeyGroup.substring(4);
        } else {
            automationAppId = jobKeyGroup;
        }

        ServiceFactory.getEntityService().runInstalledAutomationAppMethod(automationAppId, handlerMethod);
    }
}
