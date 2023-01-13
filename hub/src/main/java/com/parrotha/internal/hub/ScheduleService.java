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
package com.parrotha.internal.hub;

import groovy.json.JsonBuilder;
import com.parrotha.internal.entity.EntityJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    public static final String DEVICE_TYPE = "DEV";
    public static final String INSTALLED_AUTOMATION_APP_TYPE = "IAA";

    private Scheduler scheduler;

    private Scheduler getScheduler() throws SchedulerException {
        if (scheduler == null) {
            Properties properties = new Properties();
            properties.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
            properties.setProperty("org.quartz.jobStore.dataSource", "parrotHubDS");
            properties.setProperty("org.quartz.dataSource.parrotHubDS.connectionProvider.class", "com.parrotha.internal.database.ConnectionProviderImpl");
            properties.setProperty("org.quartz.threadPool.threadCount", "3");

            scheduler = new StdSchedulerFactory(properties).getScheduler();
        }
        return scheduler;
    }

    public void start() {
        try {
            getScheduler().start();
        } catch (SchedulerException schedulerException) {
            schedulerException.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            getScheduler().shutdown();
            this.scheduler = null;
        } catch (SchedulerException schedulerException) {
            schedulerException.printStackTrace();
        }
    }

    public void unschedule(String type, String id) {
        try {
            Scheduler scheduler = getScheduler();
            Set<JobKey> jobKeySet = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(type + ":" + id));
            scheduler.deleteJobs(new ArrayList<>(jobKeySet));

            // TODO: remove this code: find old schedules and remove them
            jobKeySet = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(id));
            scheduler.deleteJobs(new ArrayList<>(jobKeySet));

        } catch (SchedulerException e) {
            logger.warn("SchedulerException " + e.getMessage(), e);
        }
    }

    public List<Map<String, String>> getSchedulesForInstalledAutomationApp(String id) {
        List<Map<String, String>> scheduleList = new ArrayList<>();

        try {
            Set<JobKey> jobKeySet = getScheduler()
                    .getJobKeys(GroupMatcher.jobGroupEquals(ScheduleService.INSTALLED_AUTOMATION_APP_TYPE + ":" + id));

            for (JobKey jobKey : jobKeySet) {
                Map<String, String> appSchedule = new HashMap<>();
                String jobKeyName = jobKey.getName();
                String[] jobKeyNameArray = jobKeyName.split(":");

                appSchedule.put("handlerMethod", jobKeyNameArray[0]);

                //TODO: extract job details (next run time, previous run time, status)
                if ("MULTIPLE".equals(jobKeyNameArray[1])) {
                    List<Trigger> triggers = (List<Trigger>) getScheduler().getTriggersOfJob(jobKey);
                    for (Trigger t : triggers) {
                        if (t instanceof CronTrigger) {
                            appSchedule.put("schedule", ((CronTrigger) t).getCronExpression());
                        }
                    }
                } else {
                    appSchedule.put("schedule", "Once");
                }

                scheduleList.add(appSchedule);
            }
        } catch (SchedulerException schedulerException) {
            schedulerException.printStackTrace();
        }

        return scheduleList;
    }

    public void unschedule(String type, String id, String handlerMethod) {
        try {
            Scheduler scheduler = getScheduler();
            Set<JobKey> jobKeySet = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(type + ":" + id));
            for (JobKey jobKeyGroupMatch : jobKeySet) {
                if (jobKeyGroupMatch.getName().startsWith(handlerMethod)) {
                    scheduler.deleteJob(jobKeyGroupMatch);
                }
            }

            scheduler.deleteJobs(new ArrayList<>(jobKeySet));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /*
     * When scheduling, there is an option to prevent overwrite (overwrite: false).  In ST this only applies to single
     * or multiple runs, they do not cross over, so runOnce() will not overwrite a runEvery5minutes().
     *
     * this method handles multiple runs
     */
    public void schedule(String type, String id, String cronExpression, String handlerMethod, Map<String, Object> options) {
        if (cronExpression == null || handlerMethod == null) return;

        if (type != INSTALLED_AUTOMATION_APP_TYPE && type != DEVICE_TYPE) {
            throw new IllegalArgumentException("Type " + type + " not supported");
        }

        // handle overwrite: false
        boolean overwrite = options == null || options.get("overwrite") == null || (Boolean) options.get("overwrite");

        try {
            Scheduler scheduler = getScheduler();
            if (overwrite) {
                Set<JobKey> jobKeySet = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(type + ":" + id));
                for (JobKey jobKeyGroupMatch : jobKeySet) {
                    if (jobKeyGroupMatch.getName().startsWith(handlerMethod)) {
                        scheduler.deleteJob(jobKeyGroupMatch);
                    }
                }
            }

            // jobKeyName should be unique
            String jobKeyName = handlerMethod + ":MULTIPLE:" + UUID.randomUUID().toString();
            String jobKeyGroup = type + ":" + id;
            JobKey jobKey = new JobKey(jobKeyName, jobKeyGroup);

            JobDetail jobDetail = buildJobDetail(jobKey, type, id, handlerMethod, options);

            Trigger trigger = newTrigger().withIdentity(jobKey.getName(), jobKey.getGroup())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .inTimeZone(TimeZone.getDefault())
                            .withMisfireHandlingInstructionDoNothing())
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /*
     * When scheduling, there is an option to prevent overwrite (overwrite: false).  In ST this only applies to single
     * or multiple runs, they do not cross over, so runOnce() will not overwrite a runEvery5minutes().
     *
     * this method handles single runs
     */
    public void schedule(String type, String id, Long runTime, String handlerMethod, Map<String, Object> options) {
        if (runTime == null || handlerMethod == null) return;

        //TODO: handle options data

        // handle overwrite: false
        boolean overwrite = options == null || options.get("overwrite") == null || (Boolean) options.get("overwrite");

        try {
            Scheduler scheduler = getScheduler();

            if (overwrite) {
                Set<JobKey> jobKeySet = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(type + ":" + id));
                for (JobKey jobKeyGroupMatch : jobKeySet) {
                    if (jobKeyGroupMatch.getName().startsWith(handlerMethod)) {
                        scheduler.deleteJob(jobKeyGroupMatch);
                    }
                }
            }

            String jobKeyName = handlerMethod + ":SINGLE:" + UUID.randomUUID().toString();
            String jobKeyGroup = type + ":" + id;
            JobKey jobKey = new JobKey(jobKeyName, jobKeyGroup);

            JobDetail jobDetail = buildJobDetail(jobKey, type, id, handlerMethod, options);

            Trigger trigger = newTrigger().withIdentity(jobKeyName, jobKeyGroup)
                    .startAt(new Date(runTime))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private JobDetail buildJobDetail(JobKey jobKey, String type, String id, String handlerMethod, Map<String, Object> options) {
        JobBuilder jobBuilder = newJob(EntityJob.class).withIdentity(jobKey)
                .usingJobData("handlerMethod", handlerMethod)
                .usingJobData("type", type)
                .usingJobData("id", id);

        if (options != null && options.get("data") != null) {
            jobBuilder.usingJobData("data", new JsonBuilder(options.get("data")).toString());
        }

        return jobBuilder.build();
    }
}
