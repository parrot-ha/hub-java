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

import com.parrotha.internal.ServiceFactory;
import com.parrotha.internal.common.FileSystemUtils;
import groovy.json.JsonSlurper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionService {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionService.class);
    private ExtensionDataStore extensionDataStore;

    public ExtensionService() {
        this.extensionDataStore = new ExtensionYamlDataStore();
    }

    public ExtensionService(ExtensionDataStore extensionDataStore) {
        this.extensionDataStore = extensionDataStore;
    }

    public void clearExtensions() {
        this.extensions.clear();
        this.extensions = null;
    }

    private Map<String, Map> extensions;

    public Map getExtension(String id) {
        return getExtensions().get(id);
    }

    public Collection<Map> getExtensionList() {
        return getExtensions().values();
    }

    public synchronized Map<String, Map> getExtensions() {
        if (extensions == null) {
            extensions = loadExtensions();
        }
        return extensions;
    }

    public List getExtensionSettings() {
        return extensionDataStore.getExtensionSettings();
    }

    public String addSetting(String name, String type, String location) {
        return extensionDataStore.addSetting(name, type, location);
    }

    public boolean updateSetting(String id, String name, String type, String location) {
        return extensionDataStore.updateSetting(id, name, type, location);
    }

    public boolean downloadAndInstallExtension(String id) {
        boolean success = true;
        Map extension = getExtension(id);
        List<String> files = null;
        try {
            files = downloadExtension(extension, "./extensions/tmp/" + id);
            if (files.size() == 0) {
                success = false;
            }
            installExtension(files, "./extensions/" + id);
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }

        if (!success) {
            //TODO: wipe out downloads

        }
        return success;
    }

    private List<String> downloadExtension(Map extension, String downloadDirectory) throws IOException {
        String id = (String) extension.get("id");
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

                FileSystemUtils.createDirectory(downloadDirectory);

                for (Map asset : assetList) {
                    String assetName = (String) asset.get("name");
                    if (!"parrotExtension.yaml".equals(assetName) && !"parrotIntegration.yaml".equals(assetName)) {
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
            // extension should include a list of files.
        }
        return downloadedFiles;
    }

    private void installExtension(List<String> files, String destinationDirectory) throws IOException {
        File destDirFile = new File(destinationDirectory);
        for (String fileName : files) {
            // extract file if zip
            if (fileName.endsWith(".zip")) {
                FileSystemUtils.unzipFile(fileName, destinationDirectory);
            } else {
                // just copy file
                FileUtils.copyFileToDirectory(new File(fileName), destDirFile);
            }
        }
    }

    public boolean updateExtension(String id) {
        try {
            Map extension = getExtension(id);

            synchronized (this) {
                List<String> files = downloadExtension(extension, "./extensions/tmp/" + id);

                // stop services after download is complete
                stopServices();

                // copy updated extensions
                installExtension(files, "./extensions/" + id);

                startServices();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteExtension(String id) {
        synchronized (this) {
            stopServices();

            // delete extension
            try {
                FileUtils.deleteDirectory(new File("./extensions/" + id));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                startServices();
            }
        }

        return false;
    }

    private void stopServices() {
        ServiceFactory.getScheduleService().shutdown();
        ServiceFactory.getIntegrationService().stop();
    }

    private void startServices() {
        ServiceFactory.getScheduleService().start();
        ServiceFactory.getIntegrationService().start();
    }

    public void refreshExtensionList() {
        FileSystemUtils.createDirectory("./extensions");
        FileSystemUtils.createDirectory("./extensions/.extensions");

        List<Map> extLocs = getExtensionSettings();

        for (Map extLoc : extLocs) {
            try {
                loadExtensionLocation(extLoc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadExtensionLocation(Map extLoc) throws IOException {
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
                    loadRepositoryFile((String) asset.get("browser_download_url"));
                }
            }
        } else if ("URL".equalsIgnoreCase(type)) {
            if (location.endsWith("parrotExtension.yaml")) {
                loadExtensionFile(location, location, "URL");
            } else if (location.endsWith("parrotRepository.yaml")) {
                // we have a list of parrotExtension locations
                loadRepositoryFile(location);
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

        FileSystemUtils.createDirectory("./extensions/.extensions/" + extensionId);

        File file = new File("./extensions/.extensions/" + extensionId + "/parrotExtension.yaml");
        FileWriter fileWriter = new FileWriter(file);
        yaml.dump(extensionInformation, fileWriter);
    }

    private void loadRepositoryFile(String fileURL) throws IOException {
        String repoInfStr = IOUtils.toString(new URL(fileURL), "UTF8");
        Yaml yaml = new Yaml();
        Map repositoryInformation = yaml.load(repoInfStr);
        List<Map> repos = (List<Map>) repositoryInformation.get("repositories");
        for (Map repo : repos) {
            loadExtensionLocation(repo);
        }
        List<Map> extensions = (List<Map>) repositoryInformation.get("extensions");
        for (Map extension : extensions) {
            loadExtensionLocation(extension);
        }
    }

    private synchronized Map<String, Map> loadExtensions() {
        // load extensions from file system
        File extensionDirectory = new File("./extensions");
        if (!extensionDirectory.exists()) {
            extensionDirectory.mkdir();
        }
        File extDirs[] = extensionDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        Map<String, Map> tmpExtensions = new HashMap<>();
        for (File extDir : extDirs) {
            tmpExtensions.putAll(loadJarFiles(extDir));
        }

        // load extensions from configuration directory
        File availableExtensionDirectory = new File("./extensions/.extensions");
        if (availableExtensionDirectory.isDirectory()) {
            File avExtDirs[] = availableExtensionDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });

            for (File extDir : avExtDirs) {
                File[] extInfFiles = extDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        return "parrotExtension.yaml".equals(s);
                    }
                });

                if (extInfFiles.length > 0) {
                    File extInfFile = extInfFiles[0];
                    Yaml yaml = new Yaml();
                    try {
                        Map extInf = yaml.load(new FileInputStream(extInfFile));
                        String extInfId = (String) extInf.get("id");
                        if (tmpExtensions.containsKey(extInfId)) {
                            Map extension = tmpExtensions.get(extInfId);
                            if (!StringUtils.equals((String) extension.get("version"), (String) extInf.get("version"))) {
                                extension.put("updateAvailable", true);
                                extension.put("updateInfo", extInf);
                            } else {
                                extension.put("updateAvailable", false);
                            }
                        } else {
                            extInf.put("installed", false);
                            tmpExtensions.put((String) extInf.get("id"), extInf);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return tmpExtensions;
    }

    private Map<String, Map> loadJarFiles(File extDir) {
        Map<String, Map> extensions = new HashMap<>();

        File additionalDirectories[] = extDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        // recurse through directories
        for (File addDir : additionalDirectories) {
            extensions.putAll(loadJarFiles(addDir));
        }

        ClassLoader myClassLoader = FileSystemUtils.getClassloaderForJarFiles(extDir);
        if (myClassLoader != null) {
            extensions.putAll(getExtensionFromClassloader(myClassLoader, extDir.getPath()));
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
}
