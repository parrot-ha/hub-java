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
package com.parrotha.internal.app;

import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import com.parrotha.exception.NotFoundException;
import com.parrotha.internal.ChangeTrackingMap;
import com.parrotha.internal.Main;
import com.parrotha.internal.script.ParrotHubDelegatingScript;
import com.parrotha.internal.system.OAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AutomationAppService {
    private static final Logger logger = LoggerFactory.getLogger(AutomationAppService.class);

    AutomationAppDataStore automationAppDataStore;

    public AutomationAppService(AutomationAppDataStore automationAppDataStore) {
        this.automationAppDataStore = automationAppDataStore;
    }

    public AutomationAppService() {
        this.automationAppDataStore = new AutomationAppYamlDataStore();
    }

    public void setAutomationAppDataStore(AutomationAppDataStore automationAppDataStore) {
        this.automationAppDataStore = automationAppDataStore;
    }

    public Collection<InstalledAutomationApp> getAllInstalledAutomationApps() {
        return automationAppDataStore.getAllInstalledAutomationApps();
    }

    public InstalledAutomationApp getInstalledAutomationApp(String id) {
        return automationAppDataStore.getInstalledAutomationAppById(id);
    }

    public boolean updateInstalledAutomationApp(InstalledAutomationApp installedAutomationApp) {
        return automationAppDataStore.updateInstalledAutomationApp(installedAutomationApp);
    }

    public String addInstalledAutomationApp(String automationAppId) {
        AutomationApp aa = getAutomationAppById(automationAppId);

        InstalledAutomationApp iaa = new InstalledAutomationApp(null, aa.getName());
        iaa.setAutomationAppId(aa.getId());
        return automationAppDataStore.addInstalledAutomationApp(iaa);
    }

    public String addChildInstalledAutomationApp(String parentAppId, String appName, String namespace) throws NotFoundException {
        AutomationApp childAutomationApp = getAutomationAppByNameAndNamespace(appName, namespace);
        InstalledAutomationApp parentInstalledAutomationApp = getInstalledAutomationApp(parentAppId);
        if(parentInstalledAutomationApp == null) {
            throw new NotFoundException("Parent App Id not found: " + parentAppId);
        }
        AutomationApp parentAutomationApp = getAutomationAppById(parentInstalledAutomationApp.getAutomationAppId());
        if(childAutomationApp.getParent() != null && childAutomationApp.getParent().equals(parentAutomationApp.getNamespace() + ":" + parentAutomationApp.getName())) {
            InstalledAutomationApp iaa = new InstalledAutomationApp(null, childAutomationApp.getName());
            iaa.setAutomationAppId(childAutomationApp.getId());
            iaa.setParentInstalledAutomationAppId(parentAppId);
            return automationAppDataStore.addInstalledAutomationApp(iaa);
        } else {
            throw new IllegalArgumentException("Specified app is not a child of the parent app.");
        }
    }

    public AutomationApp getAutomationAppByNameAndNamespace(String name, String namespace) {
        for (AutomationApp automationApp : getAllAutomationApps(true)) {
            if (automationApp.getName() != null && automationApp.getName().equals(name) &&
                    automationApp.getNamespace() != null && automationApp.getNamespace().equals(namespace)) {
                return automationApp;
            }
        }
        return null;
    }

    public List<InstalledAutomationApp> getChildInstalledAutomationApps(String parentId, String name, String namespace) {
        List<InstalledAutomationApp> childApps = automationAppDataStore.getChildInstalledAutomationApps(parentId);
        if(name != null && namespace != null && childApps.size() > 0) {
            // filter out child apps
            AutomationApp automationApp = getAutomationAppByNameAndNamespace(name, namespace);
            if(automationApp != null) {
                return childApps.stream().filter(ca -> ca.getAutomationAppId().equals(automationApp.getId())).collect(Collectors.toList());
            }
        }
        return childApps;
    }

    public boolean removeInstalledAutomationApp(String installedAutomationAppId) {
        return automationAppDataStore.deleteInstalledAutomationApp(installedAutomationAppId);
    }

    public boolean saveState(String installedAutomationAppId, ChangeTrackingMap state) {
        InstalledAutomationApp installedAutomationApp = automationAppDataStore
                .getInstalledAutomationAppById(installedAutomationAppId);
        if (installedAutomationApp != null) {
            Map existingState = installedAutomationApp.getState();
            if (existingState != null) {
                ChangeTrackingMap.ChangeSet stateChanges = state.changes();
                for (Object key : stateChanges.getRemoved()) {
                    existingState.remove(key);
                }
                existingState.putAll(stateChanges.getUpdated());
                existingState.putAll(stateChanges.getAdded());
                automationAppDataStore.updateInstalledAutomationAppState(installedAutomationAppId, existingState);
            } else {
                automationAppDataStore.updateInstalledAutomationAppState(installedAutomationAppId, state);
            }
        }
        return true;
    }

    public boolean saveState(String installedAutomationAppId, Map state) {
        InstalledAutomationApp installedAutomationApp = automationAppDataStore
                .getInstalledAutomationAppById(installedAutomationAppId);
        if (installedAutomationApp != null) {
            automationAppDataStore.updateInstalledAutomationAppState(installedAutomationAppId, state);
        }
        return true;
    }


    public void updateInstalledAutomationAppSettings(String id, Map<String, Object> settingsMap) {
        InstalledAutomationApp iaa = getInstalledAutomationApp(id);
        for (String key : settingsMap.keySet()) {
            Map setting = (Map) settingsMap.get(key);
            InstalledAutomationAppSetting iaaSetting = iaa.getSettingByName(key);
            if (iaaSetting != null) {
                // update existing setting
                iaaSetting.processValueTypeAndMultiple(setting.get("value"), (String) setting.get("type"),
                        (Boolean) setting.get("multiple"));
            } else {
                // create new setting
                iaaSetting = new InstalledAutomationAppSetting();
                iaaSetting.setId(UUID.randomUUID().toString());
                iaaSetting.setName(key);
                iaaSetting.processValueTypeAndMultiple(setting.get("value"), (String) setting.get("type"),
                        (Boolean) setting.get("multiple"));
                iaa.addSetting(iaaSetting);
            }
        }
        automationAppDataStore.updateInstalledAutomationApp(iaa);
    }

    public void updateInstalledAutomationAppSetting(String id, String name, Object value) {
        updateInstalledAutomationAppSetting(id, name, null, value);
    }

    public void updateInstalledAutomationAppSetting(String id, String name, String type, Object value) {
        InstalledAutomationApp iaa = getInstalledAutomationApp(id);
        InstalledAutomationAppSetting iaas = iaa.getSettingByName(name);
        if (iaas != null) {
            iaas.processValueTypeAndMultiple(value, type != null ? type : iaas.getType(), iaas.isMultiple());
            automationAppDataStore.updateInstalledAutomationApp(iaa);
        }
    }

    public void addOrUpdateInstalledAutomationAppSetting(String id, String name, String type, Object value,
                                                         boolean multiple) {
        InstalledAutomationApp iaa = getInstalledAutomationApp(id);
        InstalledAutomationAppSetting iaaSetting = iaa.getSettingByName(name);
        if (iaaSetting != null) {
            iaaSetting.processValueTypeAndMultiple(value, type != null ? type : iaaSetting.getType(), multiple);
            automationAppDataStore.updateInstalledAutomationApp(iaa);
        } else {
            // create new setting
            iaaSetting = new InstalledAutomationAppSetting();
            iaaSetting.setId(UUID.randomUUID().toString());
            iaaSetting.setName(name);
            iaaSetting.processValueTypeAndMultiple(value, type,
                    multiple);
            iaa.addSetting(iaaSetting);
        }
    }

    public void removeInstalledAutomationAppSetting(String id, String name) {
        InstalledAutomationApp iaa = getInstalledAutomationApp(id);
        if (iaa != null) {
            InstalledAutomationAppSetting iaaSetting = iaa.getSettingByName(name);
            if (iaaSetting != null) {
                iaa.getSettings().remove(iaaSetting);
                updateInstalledAutomationApp(iaa);
            }
        }
    }

    public void initialize() {
        reprocessAutomationApps();
    }

    public Collection<AutomationApp> getAllAutomationApps(boolean includeChildren) {
        return automationAppDataStore.getAllAutomationApps(includeChildren);
    }

    public AutomationApp getAutomationAppById(String id) {
        return automationAppDataStore.getAutomationAppById(id);
    }

    /**
     * Get an install automation app by client id.
     *
     * @param clientId
     * @param createIfMissing Set to true to create an instance of the automation app if not already existing.
     * @return
     */
    public InstalledAutomationApp getInstalledAutomationAppByClientId(String clientId, boolean createIfMissing)
            throws NotFoundException {
        if (clientId == null) {
            throw new NotFoundException("Automation App not found for client id " + clientId);
        }

        Optional<AutomationApp> optionalAutomationApp = getAllAutomationApps(true).stream()
                .filter(aa -> clientId.equals(aa.getoAuthClientId())).findFirst();
        if (optionalAutomationApp.isPresent()) {
            String automationAppId = optionalAutomationApp.get().getId();
            Optional<InstalledAutomationApp> optionalInstalledAutomationApp = automationAppDataStore
                    .getAllInstalledAutomationApps().stream()
                    .filter(iaa -> automationAppId.equals(iaa.getAutomationAppId())).findFirst();
            if (optionalInstalledAutomationApp.isPresent()) {
                return optionalInstalledAutomationApp.get();
            } else if (createIfMissing) {
                return getInstalledAutomationApp(addInstalledAutomationApp(automationAppId));
            } else {
                throw new NotFoundException("Installed Automation App not found for client id " + clientId);
            }
        } else {
            throw new NotFoundException("Automation App not found for client id " + clientId);
        }
    }

    public OAuthToken createOAuthToken(String clientId, String clientSecret) {
        Optional<AutomationApp> optionalAutomationApp = getAllAutomationApps(true).stream()
                .filter(aa -> clientId.equals(aa.getoAuthClientId()) && clientSecret.equals(aa.getoAuthClientSecret()))
                .findFirst();
        if (optionalAutomationApp.isPresent()) {
            AutomationApp automationApp = optionalAutomationApp.get();
            OAuthToken oAuthToken = new OAuthToken(true, "bearer");
            automationApp.addoAuthToken(oAuthToken);
            automationAppDataStore.updateAutomationApp(automationApp);
            return oAuthToken;
        } else {
            return null;
        }
    }

    public List<String> getInstalledAutomationAppsByToken(String token) {
        return automationAppDataStore.getInstalledAutomationAppsByToken(token);
    }

    public String getOAuthClientIdByToken(String token) {
        return automationAppDataStore.getOAuthClientIdByToken(token);
    }


    public void reprocessAutomationApps() {
        Collection<AutomationApp> automationApps = automationAppDataStore.getAllAutomationApps(true);

        // run this process in the background, allows quicker start up of system at the
        // expense of system starting up with possibly old automation app definition, however
        // this should be quickly rectified once system is fully running
        new Thread(() -> {

            Map<String, AutomationApp> newAutomationAppInfoMap = processAutomationApps();

            // check each automation app info against what is in the config file.
            if (newAutomationAppInfoMap != null) {

                Iterator<AutomationApp> newAAInfoIter = newAutomationAppInfoMap.values().iterator();
                while (newAAInfoIter.hasNext()) {
                    AutomationApp newAAInfo = newAAInfoIter.next();
                    String fileName = newAAInfo.getFile();
                    Iterator<AutomationApp> oldAAInfoIter = automationApps.iterator();
                    boolean foundExistingAA = false;
                    while (oldAAInfoIter.hasNext()) {
                        AutomationApp oldAAInfo = oldAAInfoIter.next();
                        if (fileName.equals(oldAAInfo.getFile())) {
                            foundExistingAA = true;
                            // the file name matches, let see if any of the values have changed.
                            //TODO: this check is only if the file name stays the same, add another check in case all the contents stay the same, but the file name changed.

                            updateAutomationAppIfChanged(oldAAInfo, newAAInfo);
                        }
                    }
                    if (!foundExistingAA) {
                        // we have a new automation app, load it.
                        // we have a new device handler.
                        automationAppDataStore.addAutomationApp(newAAInfo);
                    }
                }
            }
        }).start();
    }

    private void updateAutomationAppIfChanged(AutomationApp oldAutomationApp, AutomationApp newAutomationApp) {
        // if any changes are made to the new app excluding client id and client secret, then update.
        // or if there are changes to the client id and client secret and the new app does not have it set to null
        // this is so that it will not clear out client id and client secret that have been set by the user at runtime instead of
        // being defined in the automation app definition.
        if (!newAutomationApp.equalsIgnoreId(oldAutomationApp, false) ||
                (!newAutomationApp.equalsIgnoreId(oldAutomationApp, true) &&
                        (newAutomationApp.getoAuthClientId() != null && newAutomationApp.getoAuthClientSecret() != null))) {
            logger.debug("Changes for file " + newAutomationApp.getFile());
            newAutomationApp.setId(oldAutomationApp.getId());
            newAutomationApp.setoAuthTokens(oldAutomationApp.getoAuthTokens());
            automationAppDataStore.updateAutomationApp(newAutomationApp);
        } else {
            // only difference is the id,, so no changes
            logger.debug("No changes for file " + newAutomationApp.getFile());
        }
    }

    public void reprocessAutomationApp(String id) {
        AutomationApp existingAutomationApp = getAutomationAppById(id);
        String fileName = existingAutomationApp.getFile();
        if (!fileName.startsWith("class")) {
            File f = new File(fileName);
            try {
                String scriptCode = IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8);
                Map definition = extractAutomationAppDefinition(scriptCode);
                AutomationApp newAutomationApp = new AutomationApp(id, fileName, definition);
                updateAutomationAppIfChanged(existingAutomationApp, newAutomationApp);
            } catch (IOException e) {
                logger.warn("IOException while attempting to load file " + fileName, e);
            }
        }
    }

    // load automation apps from local file system.
    private Map<String, AutomationApp> processAutomationApps() {
        // we need to process automation apps
        Map<String, AutomationApp> automationAppInfo = new HashMap<>();

        // load automation apps from jar files (pre-compiled)
        try {
            Enumeration<URL> resources = Main.class.getClassLoader().getResources("automationAppClasses.yaml");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                List<Map> list = yaml.load(url.openStream());
                for (Map m : list) {
                    String automationAppId = (String) m.get("id");
                    String className = (String) m.get("className");

                    Class<ParrotHubDelegatingScript> automationAppScriptClass = (Class<ParrotHubDelegatingScript>) Class
                            .forName(className);
                    ParrotHubDelegatingScript automationAppScript = automationAppScriptClass.getDeclaredConstructor()
                            .newInstance();
                    Map definition = extractAutomationAppDefinition(automationAppScript);
                    AutomationApp automationApp = new AutomationApp(UUID.randomUUID().toString(), "class:" + className,
                            definition);
                    automationAppInfo.put(automationApp.getId(), automationApp);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        } catch (InstantiationException instantiationException) {
            instantiationException.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // load automation apps from data store
        Map<String, InputStream> aaSources = automationAppDataStore.getAutomationAppSources();
        if (aaSources != null && aaSources.size() > 0) {
            for (String aaSourceKey : aaSources.keySet()) {
                try {
                    String scriptCode = IOUtils.toString(aaSources.get(aaSourceKey), StandardCharsets.UTF_8);
                    Map definition = extractAutomationAppDefinition(scriptCode);
                    AutomationApp automationApp = new AutomationApp(UUID.randomUUID().toString(), aaSourceKey,
                            definition);
                    automationAppInfo.put(automationApp.getId(), automationApp);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return automationAppInfo;
    }

    private static Map extractAutomationAppDefinition(String automationAppScript) {
        AutomationAppScriptDelegateImpl aasd = extractAutomationAppInformation(automationAppScript);
        return aasd.definitionInfo;
    }

    private static Map extractAutomationAppPreferences(String automationAppScript) {
        AutomationAppScriptDelegateImpl aasd = extractAutomationAppInformation(automationAppScript);
        return aasd.preferences;
    }

    private static Map extractAutomationAppDefinition(DelegatingScript automationAppScript) {
        automationAppScript.setDelegate(new AutomationAppScriptDelegateImpl(new InstalledAutomationApp()));

        automationAppScript.invokeMethod("run", null);
        AutomationAppScriptDelegateImpl automationAppScriptDelegate =
                (AutomationAppScriptDelegateImpl) automationAppScript.getDelegate();

        return automationAppScriptDelegate.definitionInfo;
    }

    private static AutomationAppScriptDelegateImpl extractAutomationAppInformation(String automationAppScript) {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass("com.parrotha.internal.script.ParrotHubDelegatingScript");
        GroovyShell shell = new GroovyShell(compilerConfiguration);
        ParrotHubDelegatingScript parrotHubDelegatingScript = (ParrotHubDelegatingScript) shell.parse(automationAppScript);
        parrotHubDelegatingScript.setDelegate(new AutomationAppScriptDelegateImpl(new InstalledAutomationApp()));

        parrotHubDelegatingScript.invokeMethod("run", null);
        AutomationAppScriptDelegateImpl aasd = (AutomationAppScriptDelegateImpl) parrotHubDelegatingScript.getDelegate();

        return aasd;
    }

    public String getAutomationAppSourceCode(String id) {
        return automationAppDataStore.getAutomationAppSourceCode(id);
    }

    public boolean updateAutomationAppSourceCode(String id, String sourceCode) {
        extractAutomationAppInformation(sourceCode);
        return automationAppDataStore.updateAutomationAppSourceCode(id, sourceCode);
    }

    public String addAutomationAppSourceCode(String sourceCode) {
        Map definition = extractAutomationAppDefinition(sourceCode);
        if(definition == null) {
            throw new IllegalArgumentException("No definition found.");
        }
        String aaId = automationAppDataStore
                .addAutomationAppSourceCode(sourceCode, new AutomationApp(null, null, definition));
        return aaId;
    }

    public boolean updateAutomationApp(AutomationApp automationApp) {
        return automationAppDataStore.updateAutomationApp(automationApp);
    }

    public Collection<AutomationApp> getUserAutomationApps() {
        return automationAppDataStore.getAllAutomationApps(true).stream()
                .filter(aa -> !aa.getFile().startsWith("class")).collect(Collectors.toList());
    }

}
