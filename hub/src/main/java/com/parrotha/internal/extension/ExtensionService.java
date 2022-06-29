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
package com.parrotha.internal.extension;

import com.parrotha.internal.common.FileSystemUtils;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtensionService {
    private static final String EXTENSION_PATH = "extensions/";
    private static final Logger logger = LoggerFactory.getLogger(ExtensionService.class);

    private Set<ExtensionStateListener> stateListeners = new HashSet<>();
    private ExtensionDataStore extensionDataStore;

    private Map<String, String> extensionStatus;
    private Map<String, Map> extensions;
    ReadWriteLock extensionsLock = new ReentrantReadWriteLock();

    public ExtensionService() {
        this.extensionDataStore = new ExtensionYamlDataStore();
    }

    public ExtensionService(ExtensionDataStore extensionDataStore) {
        this.extensionDataStore = extensionDataStore;
    }

    public void initialize() {
        // TODO: schedule refresh extension list to run overnight
        //this.refreshExtensionList();

        new Thread(this::processExtensionConfigurationDirectory).start();
    }

    public void clearExtensions() {
        Lock writeLock = extensionsLock.writeLock();
        writeLock.lock();
        try {
            if (this.extensions != null) {
                this.extensions.clear();
                this.extensions = null;
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void updateExtensionStatus(String id, String status) {
        if (this.extensionStatus == null) {
            this.extensionStatus = new HashMap<>();
        }
        this.extensionStatus.put(id, status);
    }

    public String getExtensionStatus(String extensionId) {
        if (this.extensionStatus != null && this.extensionStatus.size() > 0 && this.extensionStatus.containsKey(extensionId)) {
            return this.extensionStatus.get(extensionId);
        }
        return "IDLE";
    }


    public Map getExtension(String id) {
        return getExtensions().get(id);
    }

    public Collection<Map> getExtensionList() {
        return getExtensions().values();
    }

    private Map<String, Map> getExtensions() {
        if (this.extensions == null) {
            Lock writeLock = extensionsLock.writeLock();
            writeLock.lock();
            try {
                // check for null again so only the first thread to get here loads the extensions.
                if (this.extensions == null) {
                    this.extensions = loadExtensions();
                }
            } finally {
                writeLock.unlock();
            }
        }
        return this.extensions;
    }

    public List getExtensionLocationsList() {
        return new ArrayList(extensionDataStore.getExtensionLocations().values());
    }

    public String addLocation(String name, String type, String location) {
        String returnValue = extensionDataStore.addLocation(name, type, location);
        refreshExtensionList();
        return returnValue;
    }

    public boolean updateLocation(String id, String name, String type, String location) {
        boolean returnValue = extensionDataStore.updateLocation(id, name, type, location);
        refreshExtensionList();
        return returnValue;
    }

    public boolean deleteLocation(String id) {
        boolean returnValue = extensionDataStore.deleteLocation(id);
        refreshExtensionList();
        return returnValue;
    }

    public void registerStateListener(ExtensionStateListener stateListener) {
        synchronized (stateListeners) {
            stateListeners.add(stateListener);
        }
    }

    public void unregisterStateListener(ExtensionStateListener stateListener) {
        synchronized (stateListener) {
            stateListeners.remove(stateListener);
        }
    }

    private void notifyStateListeners(ExtensionState state) {
        if (stateListeners.size() > 0) {
            new Thread(() -> {
                for (ExtensionStateListener stateListener : stateListeners) {
                    stateListener.stateUpdated(state);
                }
            }).start();
        }
    }

    private Pair<Boolean, String> isExtensionInUse(String extensionId) {
        boolean inUse = false;
        StringBuilder message = new StringBuilder();
        if (stateListeners.size() > 0) {
            for (ExtensionStateListener stateListener : stateListeners) {
                Pair<Boolean, String> listenerResponse = stateListener.isExtensionInUse(extensionId);
                if (listenerResponse.getLeft()) {
                    inUse = true;
                    message.append(listenerResponse.getRight());
                }
            }
        }
        return new ImmutablePair<>(inUse, message.toString());
    }

    public boolean downloadAndInstallExtension(String id) {
        updateExtensionStatus(id, "INSTALLING");
        boolean success = false;
        Map extension = getExtension(id);
        if ("source".equals(extension.get("type"))) {
            success = downloadAndInstallSourceExtension(extension);
        } else {
            List<String> files;
            try {
                files = downloadBinaryExtension(extension, EXTENSION_PATH + "staging/" + id);
                if (files.size() > 0) {
                    success = installBinaryExtension(extension, files);
                }
            } catch (IOException e) {
                logger.warn("Exception when downloading and installing extension: " + id, e);
            }

            if (!success) {
                //TODO: wipe out downloads

            }
        }
        updateExtensionStatus(id, "IDLE");

        if (success) {
            extension.put("installed", true);
            notifyStateListeners(new ExtensionState(id, ExtensionState.StateType.INSTALLED));
        }

        processExtensionConfigurationDirectory();

        return success;
    }

    private boolean downloadAndInstallSourceExtension(Map extension) {
        String id = (String) extension.get("id");
        String destinationDirectory = EXTENSION_PATH + id;
        FileSystemUtils.cleanDirectory(destinationDirectory, true);

        Object filesObj = extension.get("files");
        if (filesObj != null && filesObj instanceof List) {
            String locationURL = (String) extension.get("locationURL");
            if (locationURL != null) {
                if (locationURL.endsWith("parrotExtension.yaml")) {
                    locationURL = locationURL.substring(0, locationURL.length() - "parrotExtension.yaml".length());
                }
                if (!locationURL.endsWith("/")) {
                    locationURL = locationURL + "/";
                }

                // download parrotExtension.yaml
                try {
                    FileUtils.copyURLToFile(new URL(locationURL + "parrotExtension.yaml"), new File(destinationDirectory + "/parrotExtension.yaml"));
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
            List files = (List) filesObj;
            for (Object fileObj : files) {
                if (fileObj instanceof Map) {
                    Map file = (Map) fileObj;
                    String remoteFileName = (String) file.get("file");
                    String type = null;
                    if (((Map) fileObj).containsKey("type")) {
                        type = (String) ((Map) fileObj).get("type");
                    }
                    if (type != null) {
                        String downloadURL = remoteFileName;
                        if (!remoteFileName.startsWith("http")) {
                            downloadURL = locationURL + remoteFileName;
                        }

                        String fileName = downloadURL.substring(downloadURL.lastIndexOf('/'));

                        // download file
                        String localFileName;
                        if ("AUTOMATION_APP".equals(type)) {
                            localFileName = destinationDirectory + "/automationApps/" + fileName;
                        } else if ("DEVICE_HANDLER".equals(type)) {
                            localFileName = destinationDirectory + "/deviceHandlers/" + fileName;
                        } else {
                            // we don't support other file types, move on.
                            continue;
                        }

                        try {
                            FileUtils.copyURLToFile(new URL(downloadURL), new File(localFileName));
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
            }
            extension.put("location", destinationDirectory);
            return true;
        }
        return false;
    }

    private List<String> downloadBinaryExtension(Map extension, String downloadDirectory) throws IOException {
        //String id = (String) extension.get("id");
        String locationType = (String) extension.get("locationType");
        String locationURL = (String) extension.get("locationURL");
        List<String> downloadedFiles = new ArrayList<>();

        if ("GithubRelease".equals(locationType)) {
            URL github = new URL(locationURL);
            String githubResponse = IOUtils.toString(github, "UTF8");
            Object githubInfoObject = new JsonSlurper().parseText(githubResponse);
            if (githubInfoObject instanceof Map) {
                Map githubInfo = (Map) githubInfoObject;
                List<Map> assetList = (List<Map>) githubInfo.get("assets");

                FileSystemUtils.createDirectory(downloadDirectory, true);

                for (Map asset : assetList) {
                    String assetName = (String) asset.get("name");
                    if (!"parrotRepository.yaml".equals(assetName) && !"parrotExtension.yaml".equals(assetName) &&
                            !"parrotIntegration.yaml".equals(assetName)) {
                        String extFileUrlStr = (String) asset.get("browser_download_url");
                        try {
                            FileUtils.copyURLToFile(new URL(extFileUrlStr), new File(downloadDirectory + "/" + assetName));
                            downloadedFiles.add(downloadDirectory + "/" + assetName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else if ("URL".equals(locationType)) {
            // TODO: download URL extensions
            logger.debug("extension info " + new JsonBuilder(extension));

        }
        return downloadedFiles;
    }

    private boolean installBinaryExtension(Map extensionInfo, List<String> files) throws IOException {
        String type = (String) extensionInfo.get("type");
        String id = (String) extensionInfo.get("id");
        String destinationDirectory = EXTENSION_PATH + id;
        if (!"source".equalsIgnoreCase(type)) {
            File destDirFile = new File(destinationDirectory);
            // clean out destination
            FileSystemUtils.cleanDirectory(destinationDirectory);
            for (String fileName : files) {
                // extract file if zip
                if (fileName.endsWith(".zip")) {
                    FileSystemUtils.unzipFile(fileName, destinationDirectory);
                } else {
                    // just copy file
                    FileUtils.copyFileToDirectory(new File(fileName), destDirFile);
                }
            }
            extensionInfo.put("location", destinationDirectory);
            return true;
        }
        return false;
    }

    public boolean updateExtension(String id) {
        boolean success = false;

        Map extension = getExtension(id);
        String type = (String) extension.get("type");
        synchronized (this) {
            if ((boolean) extension.get("updateAvailable") && extension.containsKey("updateInfo")) {
                Map updateInfo = (Map) extension.get("updateInfo");
                if ("source".equalsIgnoreCase(type)) {
                    success = downloadAndInstallSourceExtension(extension);
                } else {
                    try {
                        List<String> files = downloadBinaryExtension(updateInfo, EXTENSION_PATH + "staging/" + id);
                        // copy updated extensions
                        if (files != null || files.size() > 0) {
                            success = installBinaryExtension(updateInfo, files);
                        }
                    } catch (IOException ioe) {
                        logger.warn("Exception updating extension {}", id, ioe);
                    }

                    // clean up staging directory
                    FileSystemUtils.cleanDirectory(EXTENSION_PATH + "staging/" + id);
                }
                if (success) {
                    reprocessExtensionInfo(id);
                    // notify listeners
                    notifyStateListeners(new ExtensionState(id, ExtensionState.StateType.UPDATED));
                }
            }
        }

        return success;
    }

    public Map<String, Pair<Enumeration<URL>, ClassLoader>> getResourcesFromExtensions(String name) {
        Map<String, Pair<Enumeration<URL>, ClassLoader>> extensionResources = new HashMap<>();

        for (String extensionId : getExtensions().keySet()) {
            Pair<Enumeration<URL>, ClassLoader> resources = getResourcesFromExtension(extensionId, name);
            if (resources != null) {
                extensionResources.put(extensionId, resources);
            }
        }
        return extensionResources;
    }

    public Pair<Enumeration<URL>, ClassLoader> getResourcesFromExtension(String extensionId, String name) {
        ExtensionClassLoader myClassLoader = getCustomExtensionClassloader(extensionId);
        if (myClassLoader != null) {
            try {
                return new ImmutablePair<>(myClassLoader.getResources(name, false), myClassLoader);
            } catch (IOException e) {
                logger.warn("Exception loading resource(s) {} from extension {}", name, extensionId, e);
            }
        }

        return null;
    }

    private ExtensionClassLoader getCustomExtensionClassloader(String extensionId) {
        Path extensionDirectory = getExtensionDirectory(extensionId);
        if (extensionDirectory != null) {
            return getClassloaderForJarFiles(extensionDirectory, true);
        }
        return null;
    }

    private static ExtensionClassLoader getClassloaderForJarFiles(Path directory, boolean recurse) {
        URL[] urls = FileSystemUtils.listJarsForDirectory(directory, recurse);

        if (urls.length == 0) {
            return null;
        }
        return new ExtensionClassLoader(urls);
    }

    public Map<String, Map<String, InputStream>> getDeviceHandlerSources() {
        return getSources("/deviceHandlers");
    }

    public Map<String, Map<String, InputStream>> getAutomationAppSources() {
        return getSources("/automationApps");
    }

    private Map<String, Map<String, InputStream>> getSources(String sourceSubDir) {
        Map<String, Map<String, InputStream>> sourceList = new HashMap<>();

        // scan through extension directory for files
        Map<String, Path> extDirs = getInstalledExtensionsAndDirectories();
        for (String extId : extDirs.keySet()) {
            // get source code extensions
            File extSourceDir = new File(extDirs.get(extId) + sourceSubDir);
            sourceList.put(extId, loadSourcesFromDirectory(extSourceDir));
        }
        return sourceList;
    }

    public Map<String, InputStream> getAutomationAppSources(String extensionId) {
        return getSources(extensionId, "automationApps");
    }

    public Map<String, InputStream> getDeviceHandlerSources(String extensionId) {
        return getSources(extensionId, "deviceHandlers");
    }

    public Map<String, InputStream> getSources(String extensionId, String sourceSubDir) {
        Map<String, InputStream> sourceList = new HashMap<>();

        // get source code extensions
        Path extensionDirectory = getExtensionDirectory(extensionId);
        if (extensionDirectory != null) {
            File extDeviceHandlerDir = extensionDirectory.resolve(sourceSubDir).toFile();
            sourceList.putAll(loadSourcesFromDirectory(extDeviceHandlerDir));
        }

        return sourceList;
    }

    private Map<String, InputStream> loadSourcesFromDirectory(File sourceDir) {
        Map<String, InputStream> sourceList = new HashMap<>();
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            return sourceList;
        }

        // load automation apps from text files on local file system
        try (Stream<Path> pathStream = Files.find(sourceDir.toPath(),
                1,
                (p, basicFileAttributes) ->
                        p.getFileName().toString().endsWith(".groovy"))
        ) {
            sourceList = pathStream.collect(Collectors.toMap(Path::toString, path -> {
                try {
                    return Files.newInputStream(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sourceList;
    }

    public boolean deleteExtension(String id) {
        Map extension = getExtension(id);
        Pair<Boolean, String> extensionInUse = isExtensionInUse(id);

        if (!extensionInUse.getLeft()) {
            synchronized (this) {
                String type = (String) extension.get("type");

                // delete extension
                try {
                    FileUtils.deleteDirectory(new File(EXTENSION_PATH + id));
                    extension.put("installed", false);
                    // notify listeners
                    notifyStateListeners(new ExtensionState(id, ExtensionState.StateType.DELETED));
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        return false;
    }

    public void refreshExtensionList() {
        FileSystemUtils.cleanDirectory(EXTENSION_PATH + ".extensions/", true);
        Lock writeLock = extensionsLock.writeLock();
        writeLock.lock();
        try {
            // clear out extensions that are not installed from extensions list
            getExtensions().entrySet().removeIf(ext -> !((boolean) ext.getValue().get("installed")));
        } finally {
            writeLock.unlock();
        }

        List<Map> extLocs = getExtensionLocationsList();

        for (Map extLoc : extLocs) {
            try {
                loadExtensionLocation(extLoc, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        processExtensionConfigurationDirectory();
    }

    private void reprocessExtensionInfo(String extensionId) {
        Path extDir = getExtensionDirectory(extensionId);

        Map installedExtension = FileSystemUtils.loadYamlFile(extDir + "/parrotExtension.yaml");
        if (installedExtension != null) {
            installedExtension.put("installed", true);
            installedExtension.put("location", extDir.toString());
        } else {
            Map<String, Map> tmpExtensions = loadJarFiles(extDir);
            if (tmpExtensions != null) {
                installedExtension = tmpExtensions.get(extensionId);
            }
        }

        Map availableExtensionInfo = FileSystemUtils.loadYamlFile(EXTENSION_PATH + ".extensions/" + extensionId + "/parrotExtension.yaml");
        if (availableExtensionInfo != null) {
            Map updatedExtensionInfo = compareAndUpdateExtensionInfo(installedExtension, availableExtensionInfo);
            getExtensions().put(extensionId, updatedExtensionInfo);
        } else if (installedExtension != null) {
            getExtensions().put(extensionId, installedExtension);
        }
    }

    private void loadExtensionLocation(Map extLoc, int level) throws IOException {
        // this will stop us from entering an infinite loop should 2 repository files point to each other.
        if (level++ > 10) {
            // we've recursed too many times
            logger.warn("Possible loop in repository files, only 10 levels of repository files are supported");
            return;
        }

        String type = (String) extLoc.get("type");
        String location = (String) extLoc.get("location");

        if ("GithubRelease".equalsIgnoreCase(type)) {
            String url = "https://api.github.com/repos/" + location + "/releases/latest";
            URL github = new URL(url);
            String githubResponse = IOUtils.toString(github, "UTF8");

            Map parsedData = (Map) new JsonSlurper().parseText(githubResponse);
            List<Map> assetList = (List<Map>) parsedData.get("assets");

            for (Map asset : assetList) {
                String assetName = (String) asset.get("name");
                if ("parrotExtension.yaml".equals(assetName)) {
                    String extInfUrlStr = (String) asset.get("browser_download_url");
                    loadExtensionFile(extInfUrlStr, url, "GithubRelease");
                } else if ("parrotRepository.yaml".equals(assetName)) {
                    // we have a list of parrotExtension locations
                    loadRepositoryFile((String) asset.get("browser_download_url"), level);
                }
            }
        } else if ("URL".equalsIgnoreCase(type)) {
            if (location.endsWith("parrotExtension.yaml")) {
                loadExtensionFile(location, location, "URL");
            } else if (location.endsWith("parrotRepository.yaml")) {
                // we have a list of parrotExtension locations
                loadRepositoryFile(location, level);
            } else {
                logger.info("Unknown file: " + location);
            }
        } else {
            logger.info("Unknown extension type : " + type);
        }
    }

    private void loadExtensionFile(String fileURL, String locationURL, String locationType) throws IOException {
        String extInfStr = IOUtils.toString(new URL(fileURL), "UTF8");
        Yaml yaml = new Yaml();
        Map extensionInformation = yaml.load(extInfStr);
        extensionInformation.put("locationURL", locationURL);
        extensionInformation.put("locationType", locationType);
        String extensionId = (String) extensionInformation.get("id");

        FileSystemUtils.createDirectory(EXTENSION_PATH + ".extensions/" + extensionId, true);

        File file = new File(EXTENSION_PATH + ".extensions/" + extensionId + "/parrotExtension.yaml");
        FileWriter fileWriter = new FileWriter(file);
        yaml.dump(extensionInformation, fileWriter);
    }

    private void loadRepositoryFile(String fileURL, int level) throws IOException {
        String repoInfStr = IOUtils.toString(new URL(fileURL), "UTF8");
        Yaml yaml = new Yaml();
        Map repositoryInformation = yaml.load(repoInfStr);
        List<Map> repos = (List<Map>) repositoryInformation.get("repositories");
        for (Map repo : repos) {
            loadExtensionLocation(repo, level);
        }
        List<Map> extensions = (List<Map>) repositoryInformation.get("extensions");
        for (Map extension : extensions) {
            loadExtensionLocation(extension, level);
        }
    }

    private Map<String, Map> loadExtensions() {
        // load extensions from file system
        Set<Path> extDirs = getExtensionDirectories();

        Map<String, Map> tmpExtensions = new HashMap<>();
        for (Path extDir : extDirs) {
            Map extInf = FileSystemUtils.loadYamlFile(extDir + "/parrotExtension.yaml");
            if (extInf != null) {
                extInf.put("installed", true);
                extInf.put("location", extDir.toString());
                tmpExtensions.put((String) extInf.get("id"), extInf);
            } else {
                tmpExtensions.putAll(loadJarFiles(extDir));
            }
        }

        return tmpExtensions;
    }

    private Map compareAndUpdateExtensionInfo(Map installedExtension, Map updatedExtensionInfo) {
        if (installedExtension != null) {
            if ((boolean) installedExtension.get("installed")) {
                if (!StringUtils.equals((String) installedExtension.get("version"), (String) updatedExtensionInfo.get("version"))) {
                    installedExtension.put("updateAvailable", true);
                    installedExtension.put("updateInfo", updatedExtensionInfo);
                } else {
                    installedExtension.put("updateAvailable", false);
                    installedExtension.remove("updateInfo");
                }
                if (!installedExtension.containsKey("locationURL")) {
                    installedExtension.put("locationURL", updatedExtensionInfo.get("locationURL"));
                }
                if (!installedExtension.containsKey("locationType")) {
                    installedExtension.put("locationType", updatedExtensionInfo.get("locationType"));
                }
                return installedExtension;
            } else {
                // update existing information
                updatedExtensionInfo.put("installed", false);
                return updatedExtensionInfo;
            }
        } else {
            updatedExtensionInfo.put("installed", false);
            return updatedExtensionInfo;
        }
    }

    private void processExtensionConfigurationDirectory() {
        // load extensions from configuration directory
        File availableExtensionDirectory = new File(EXTENSION_PATH + ".extensions");
        if (availableExtensionDirectory.isDirectory()) {
            File avExtDirs[] = availableExtensionDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });

            for (File extDir : avExtDirs) {
                Map extInf = FileSystemUtils.loadYamlFile(extDir.getPath() + "/parrotExtension.yaml");
                if (extInf != null) {
                    String extensionId = (String) extInf.get("id");
                    Map installedExtension = getExtensions().get(extensionId);
                    Map updatedExtensionInfo = compareAndUpdateExtensionInfo(installedExtension, extInf);
                    getExtensions().put(extensionId, updatedExtensionInfo);
                }
            }
        }
    }

    private Map<String, Map> loadJarFiles(Path extDir) {
        Map<String, Map> extensions = new HashMap<>();

        ClassLoader myClassLoader = getClassloaderForJarFiles(extDir, true);
        if (myClassLoader != null) {
            extensions.putAll(getExtensionFromClassloader(myClassLoader, extDir.toString()));
        }

        return extensions;
    }

    private Map<String, Map> getExtensionFromClassloader(ClassLoader classLoader, String extensionDirectory) {
        Map<String, Map> extensions = new HashMap<>();
        try {
            Enumeration<URL> resources = classLoader.getResources("parrotExtension.yaml");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Yaml yaml = new Yaml();
                Map extensionInformation = yaml.load(url.openStream());
                String id = (String) extensionInformation.get("id");
                String name = (String) extensionInformation.get("name");
                String description = (String) extensionInformation.get("description");
                String version = (String) extensionInformation.get("version");
                // create mutable map
                extensions.put(id,
                        new HashMap<>(Map.of("id", id, "name", name, "description", description, "location", extensionDirectory, "version", version,
                                "installed", true)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extensions;
    }

    private static Set<Path> getExtensionDirectories() {
        File extensionDirectory = new File(EXTENSION_PATH);
        if (!extensionDirectory.exists()) {
            extensionDirectory.mkdir();
        }

        Set<Path> extDirs = new HashSet<>();
        try (Stream<Path> stream = Files.list(Paths.get(extensionDirectory.getPath()))) {
            extDirs = stream
                    .filter(Files::isDirectory)
                    .filter(file -> !file.endsWith(".extensions") && !file.endsWith("staging"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extDirs;
    }

    private Map<String, Path> getInstalledExtensionsAndDirectories() {
        Map<String, Path> extDirs = new HashMap<>();
        if (getExtensions().size() > 0) {
            extDirs = getExtensions().values().stream()
                    .filter(ext -> ext.get("location") != null && ext.get("installed") != null && (boolean) ext.get("installed"))
                    .collect(Collectors.toMap(ext -> (String) ext.get("id"), ext -> Paths.get((String) ext.get("location"))));
        }
        return extDirs;
    }

    private Path getExtensionDirectory(String extensionId) {
        Map extensionInfo = getExtensions().get(extensionId);
        if (extensionInfo != null && extensionInfo.get("location") != null) {
            return new File((String) extensionInfo.get("location")).toPath();
        }
        return null;
    }
}
