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

import com.parrotha.internal.system.OAuthToken;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurperClassic;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AutomationAppYamlDataStore implements AutomationAppDataStore {
    private static final Logger logger = LoggerFactory.getLogger(AutomationAppYamlDataStore.class);

    private Map<String, InstalledAutomationApp> installedAutomationApps;
    private Map<String, List<String>> childAppMap;

    public Collection<InstalledAutomationApp> getAllInstalledAutomationApps() {
        return getInstalledAutomationAppMap().values();
    }

    @Override
    public Collection<InstalledAutomationApp> getInstalledAutomationAppsByExtension(String extensionId) {
        Collection<InstalledAutomationApp> installedAutomationApps = new HashSet<>();
        for (AutomationApp automationApp : getAllAutomationApps(true)) {
            for (InstalledAutomationApp installedAutomationApp : getAllInstalledAutomationApps()) {
                if (automationApp.getId() != null && automationApp.getId().equals(installedAutomationApp.getAutomationAppId())) {
                    installedAutomationApps.add(installedAutomationApp);
                }
            }
        }
        return installedAutomationApps;
    }

    public InstalledAutomationApp getInstalledAutomationAppById(String id) {
        InstalledAutomationApp installedAutomationApp = getInstalledAutomationAppMap().get(id);
        if (installedAutomationApp != null) {
            try {
                return (InstalledAutomationApp) installedAutomationApp.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Map<String, InstalledAutomationApp> getInstalledAutomationAppMap() {
        if (installedAutomationApps == null) {
            loadInstalledAutomationAppMap();
        }
        return installedAutomationApps;
    }

    private Map<String, List<String>> getChildAppMap() {
        if (childAppMap == null) {
            loadInstalledAutomationAppMap();
        }
        return childAppMap;
    }

    public synchronized String addInstalledAutomationApp(InstalledAutomationApp installedAutomationApp) {
        String iaaId = UUID.randomUUID().toString();
        installedAutomationApp.setId(iaaId);

        getInstalledAutomationAppMap().put(iaaId, installedAutomationApp);

        // add to child app map if this is a child app
        if (installedAutomationApp.getParentInstalledAutomationAppId() != null) {
            if (getChildAppMap().get(installedAutomationApp.getParentInstalledAutomationAppId()) == null) {
                getChildAppMap().put(installedAutomationApp.getParentInstalledAutomationAppId(),
                        new ArrayList<>(List.of(installedAutomationApp.getId())));
            } else {
                getChildAppMap().get(installedAutomationApp.getParentInstalledAutomationAppId()).add(installedAutomationApp.getId());
            }
        }
        saveInstalledAutomationApp(iaaId);

        return iaaId;
    }

    @Override
    public List<InstalledAutomationApp> getChildInstalledAutomationApps(String parentId) {
        List<InstalledAutomationApp> childApps = new ArrayList<>();
        List<String> childAppIds = getChildAppMap().get(parentId);
        if (childAppIds == null || childAppIds.size() == 0) {
            return childApps;
        }
        for (String childAppId : childAppIds) {
            childApps.add(getInstalledAutomationAppById(childAppId));
        }
        return childApps;
    }

    @Override
    public boolean deleteInstalledAutomationApp(String id) {
        //delete file in installedAutomationApps
        File iaaConfig = new File("config/installedAutomationApps/" + id + ".yaml");
        boolean deleted = iaaConfig.delete();
        if (!deleted) {
            logger.warn("Unable to remove installed automation app config file for " + id);
            return false;
        }

        getInstalledAutomationAppMap().remove(id);

        return true;
    }

    @Override
    public boolean updateInstalledAutomationApp(InstalledAutomationApp installedAutomationApp) {
        InstalledAutomationApp existingInstalledAutomationApp = getInstalledAutomationAppMap()
                .get(installedAutomationApp.getId());
        //synchronize this on each installed automation app, don't need to synchronize for all
        synchronized (existingInstalledAutomationApp) {
            existingInstalledAutomationApp.setInstalled(installedAutomationApp.isInstalled());
            existingInstalledAutomationApp.setLabel(installedAutomationApp.getLabel());
            existingInstalledAutomationApp.setSettings(installedAutomationApp.getSettings());
            return saveInstalledAutomationApp(installedAutomationApp.getId());
        }
    }

    private boolean saveInstalledAutomationApp(String iaaId) {
        InstalledAutomationApp existingIaa = getInstalledAutomationAppMap().get(iaaId);
        try {
            Yaml yaml = new Yaml();
            File iaaConfig = new File("config/installedAutomationApps/" + iaaId + ".yaml");
            FileWriter fileWriter = new FileWriter(iaaConfig);
            yaml.dump(convertInstalledAutomationAppToMap(existingIaa), fileWriter);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateInstalledAutomationAppState(String installedAutomationAppId, Map state) {
        InstalledAutomationApp installedAutomationApp = getInstalledAutomationAppMap().get(installedAutomationAppId);
        if (installedAutomationApp != null) {
            //serialize state to json and back to filter out any bad values
            //https://docs.smartthings.com/en/latest/smartapp-developers-guide/state.html#persistence-model
            if (state != null) {
                installedAutomationApp
                        .setState((Map) new JsonSlurperClassic().parseText(new JsonBuilder(state).toString()));
            }
            saveInstalledAutomationApp(installedAutomationApp.getId());
        } else {
            throw new IllegalArgumentException("Installed Automation App does not exist");
        }

        return true;
    }

    private synchronized void loadInstalledAutomationAppMap() {
        if (installedAutomationApps != null) {
            return;
        }

        Map<String, InstalledAutomationApp> newInstalledAutomationApps = new HashMap<>();
        Map<String, List<String>> newChildAppMap = new HashMap<>();

        File installedAutomationAppsConfigDir = new File("config/installedAutomationApps/");
        if (installedAutomationAppsConfigDir.exists() && installedAutomationAppsConfigDir.isDirectory()) {
            File[] installedAutomationAppConfigFiles = installedAutomationAppsConfigDir
                    .listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".yaml"));

            if (installedAutomationAppConfigFiles != null && installedAutomationAppConfigFiles.length > 0) {
                Yaml yaml = new Yaml();
                for (File f : installedAutomationAppConfigFiles) {
                    try {
                        Map<String, Object> iaaMap = yaml.load(new FileInputStream(f));
                        InstalledAutomationApp iaa = createInstalledAutomationAppFromMap(iaaMap);
                        AutomationApp automationApp = getAutomationAppById(iaa.getAutomationAppId());
                        iaa.setName(automationApp.getName());
                        iaa.setNamespace(automationApp.getNamespace());
                        if (iaa.getParentInstalledAutomationAppId() != null) {
                            if (newChildAppMap.get(iaa.getParentInstalledAutomationAppId()) == null) {
                                newChildAppMap.put(iaa.getParentInstalledAutomationAppId(), new ArrayList<>(List.of(iaa.getId())));
                            } else {
                                newChildAppMap.get(iaa.getParentInstalledAutomationAppId()).add(iaa.getId());
                            }
                        }
                        newInstalledAutomationApps.put(iaa.getId(), iaa);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        installedAutomationApps = newInstalledAutomationApps;
        childAppMap = newChildAppMap;
    }

    private InstalledAutomationApp createInstalledAutomationAppFromMap(Map<String, Object> map) {
        InstalledAutomationApp iaa = new InstalledAutomationApp();
        iaa.setId((String) map.get("id"));
        iaa.setLabel((String) map.get("label"));
        iaa.setAutomationAppId((String) map.get("automationAppId"));
        iaa.setParentInstalledAutomationAppId((String) map.get("parentInstalledAutomationAppId"));
        if (map.get("installed") != null) {
            iaa.setInstalled((Boolean) map.get("installed"));
        } else {
            iaa.setInstalled(false);
        }
        if (map.get("state") != null && map.get("state") instanceof String &&
                StringUtils.isNotEmpty((String) map.get("state"))) {
            // need to use classic json slurper so we don't end up with LazyMap that can't be serialized.
            iaa.setState((Map) new JsonSlurperClassic().parseText((String) map.get("state")));
        }
        Object settings = map.get("settings");
        if (settings instanceof List) {
            List<InstalledAutomationAppSetting> iaaSettings = new ArrayList<>();
            for (Object setting : (List) settings) {
                iaaSettings.add(new InstalledAutomationAppSetting((Map) setting));
            }
            iaa.setSettings(iaaSettings);
        }

        return iaa;
    }

    private Map convertInstalledAutomationAppToMap(InstalledAutomationApp iaa) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", iaa.getId());
        map.put("label", iaa.getLabel());
        map.put("automationAppId", iaa.getAutomationAppId());
        map.put("installed", iaa.isInstalled());
        map.put("parentInstalledAutomationAppId", iaa.getParentInstalledAutomationAppId());
        //serialize state to json and back to filter out any bad values
        //https://docs.smartthings.com/en/latest/smartapp-developers-guide/state.html#persistence-model
        if (iaa.getState() != null) {
            map.put("state", new JsonBuilder(iaa.getState()).toString());
            iaa.setState((Map) new JsonSlurperClassic().parseText((String) map.get("state")));
        }
        if (iaa.getSettings() != null) {
            ArrayList<Map> mapSettings = new ArrayList<>();
            for (InstalledAutomationAppSetting setting : iaa.getSettings()) {
                mapSettings.add(setting.toMap(false));
            }
            map.put("settings", mapSettings);
        }

        return map;
    }

    /*
     * Automation App code
     */

    @Override
    public Collection<AutomationApp> getAllAutomationApps(boolean includeChildren) {
        if (includeChildren) {
            return getAutomationAppMap().values();
        } else {
            return getAutomationAppMap().values().stream().filter(aa -> aa.getParent() == null).collect(Collectors.toList());
        }
    }

    public AutomationApp getAutomationAppById(String id) {
        return getAutomationAppMap().get(id);
    }

    @Override
    public String getAutomationAppIdByClientId(String clientId) {
        return getAllAutomationApps(true).stream().filter(aa -> clientId.equals(aa.getoAuthClientId())).findFirst()
                .map(AutomationApp::getId).orElse(null);
    }

    @Override
    public boolean updateAutomationApp(AutomationApp automationApp) {
        getAutomationAppMap().put(automationApp.getId(), automationApp);
        saveAutomationApps();
        // clear out map so it can be reloaded
        this.tokenToAutomationAppMap = null;
        return true;
    }

    @Override
    public void addAutomationApp(AutomationApp automationApp) {
        getAutomationAppMap().put(automationApp.getId(), automationApp);
        saveAutomationApps();
    }

    @Override
    public boolean deleteAutomationApp(String id) {
        AutomationApp aa = getAutomationAppById(id);
        if (AutomationApp.Type.USER.equals(aa.getType())) {
            //delete source file
            boolean fileDeleted = new File(aa.getFile()).delete();
            if (!fileDeleted) {
                logger.warn("Unable to remove automation app file for " + id);
                return false;
            }
        }

        getAutomationAppMap().remove(id);
        saveAutomationApps();
        return true;
    }

    private void saveAutomationApps() {
        if (automationAppInfo != null && automationAppInfo.size() > 0) {
            try {
                Yaml yaml = new Yaml();
                File automationAppConfig = new File("config/automationApps.yaml");
                FileWriter fileWriter = new FileWriter(automationAppConfig);
                yaml.dump(new ArrayList<>(automationAppInfo.values()), fileWriter);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, AutomationApp> automationAppInfo;

    private Map<String, AutomationApp> getAutomationAppMap() {
        if (automationAppInfo == null) {
            automationAppInfo = loadAutomationAppInfo();
        }
        return automationAppInfo;
    }

    private Map<String, String> tokenToAutomationAppMap;

    private Map<String, String> getTokenToAutomationAppMap() {
        if (tokenToAutomationAppMap == null) {
            tokenToAutomationAppMap = loadTokenToAutomationAppMap();
        }
        return tokenToAutomationAppMap;
    }

    private synchronized Map<String, String> loadTokenToAutomationAppMap() {
        Map<String, String> tokenToAutomationAppMap = new HashMap<>();
        for (AutomationApp automationApp : getAllAutomationApps(true)) {
            if (automationApp.getoAuthTokens() != null) {
                for (OAuthToken authToken : automationApp.getoAuthTokens()) {
                    tokenToAutomationAppMap.put(authToken.getAccessToken(), automationApp.getId());
                }
            }
        }
        return tokenToAutomationAppMap;
    }

    private synchronized Map<String, AutomationApp> loadAutomationAppInfo() {
        Map<String, AutomationApp> automationAppInfo = new HashMap<>();
        try {
            File automationAppsConfigFile = new File("config/automationApps.yaml");
            if (automationAppsConfigFile.exists()) {
                Yaml yaml = new Yaml();
                List<AutomationApp> listObj = yaml.load(new FileInputStream(automationAppsConfigFile));
                if (listObj != null) {
                    for (AutomationApp aa : listObj) {
                        automationAppInfo.put(aa.getId(), aa);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return automationAppInfo;
    }

    @Override
    public String getAutomationAppSourceCode(String id) {
        AutomationApp automationApp = getAutomationAppMap().get(id);
        if (automationApp != null && !automationApp.getFile().startsWith("class:")) {
            File f = new File(automationApp.getFile());
            try {
                String scriptCode = IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8);
                return scriptCode;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public Map<String, InputStream> getAutomationAppSources() {
        Map<String, InputStream> automationAppSourceList = new HashMap<>();

        // load automation apps from text files on local file system
        try {
            final String aaFilePath = "automationApps/";
            File automationAppDir = new File(aaFilePath);
            if (!automationAppDir.exists()) {
                automationAppDir.mkdir();
            }
            if (automationAppDir.exists() && automationAppDir.isDirectory()) {
                File[] automationAppFiles = automationAppDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && pathname.getName().endsWith(".groovy");
                    }
                });

                if (automationAppFiles != null && automationAppFiles.length > 0) {
                    for (File f : automationAppFiles) {
                        automationAppSourceList.put(aaFilePath + f.getName(), new FileInputStream(f));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return automationAppSourceList;
    }

    @Override
    public boolean updateAutomationAppSourceCode(String id, String sourceCode) {
        AutomationApp automationApp = getAutomationAppMap().get(id);
        if (automationApp != null && !automationApp.getFile().startsWith("class:")) {
            File f = new File(automationApp.getFile());
            try {
                IOUtils.write(sourceCode, new FileOutputStream(f), StandardCharsets.UTF_8);
                return true;
            } catch (IOException e) {
                logger.warn("Exception saving automation app source code", e);
            }
        }

        return false;
    }

    @Override
    public String addAutomationAppSourceCode(String sourceCode, AutomationApp automationApp) {
        String aaId = UUID.randomUUID().toString();
        String fileName = "automationApps/" + aaId + ".groovy";

        automationApp.setId(aaId);
        automationApp.setFile(fileName);

        File f = new File(fileName);
        try {
            IOUtils.write(sourceCode, new FileOutputStream(f), StandardCharsets.UTF_8);
            addAutomationApp(automationApp);
            return aaId;
        } catch (IOException e) {
            logger.warn("Exception saving automation app source code", e);
        }
        return null;
    }

    @Override
    public List<String> getInstalledAutomationAppsByToken(String token) {
        String automationAppId = getTokenToAutomationAppMap().get(token);
        if (automationAppId != null) {
            return getAllInstalledAutomationApps().stream()
                    .filter(iaa -> automationAppId.equals(iaa.getAutomationAppId()))
                    .map(InstalledAutomationApp::getId).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public String getOAuthClientIdByToken(String token) {
        String automationAppId = getTokenToAutomationAppMap().get(token);
        if (automationAppId != null) {
            return getAutomationAppById(automationAppId).getoAuthClientId();
        }
        return null;
    }
}
