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
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class ExtensionService {
    private List<Map<String, String>> extensionsList;
    private List<Map> availableExtensionsList;

    private ExtensionDataStore extensionDataStore;

    public ExtensionService() {
        this.extensionDataStore = new ExtensionYamlDataStore();
    }

    public ExtensionService(ExtensionDataStore extensionDataStore) {
        this.extensionDataStore = extensionDataStore;
    }


    public List getInstalledExtensions() {
        if (extensionsList == null) {
            loadExtensions();
        }
        return extensionsList;
    }


    synchronized public List getAvailableExtensions() {
        if (availableExtensionsList == null) {
            availableExtensionsList = loadAvailableExtensions();
        }
        return availableExtensionsList;
    }

    private List loadAvailableExtensions() {
        List<Map> tmpAvailableExtensionsList = new ArrayList<>();
        File extensionDirectory = new File("./extensions/.extensions");
        if (extensionDirectory.isDirectory()) {
            File extDirs[] = extensionDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });

            for (File extDir : extDirs) {

                File[] extInfFiles = extDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        return "extensionInformation.yaml".equals(s);
                    }
                });

                if (extInfFiles.length > 0) {
                    File extInfFile = extInfFiles[0];
                    Yaml yaml = new Yaml();
                    try {
                        Map extInf = yaml.load(new FileInputStream(extInfFile));
                        tmpAvailableExtensionsList.add(extInf);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return tmpAvailableExtensionsList;
    }

    private Map<String, Map> extensionSettings;

    public List getExtensionSettings() {
        return extensionDataStore.getExtensionSettings();
    }

    public String addSetting(String name, String type, String location) {
        return extensionDataStore.addSetting(name, type, location);
    }

    public boolean updateSetting(String id, String name, String type, String location) {
        return extensionDataStore.updateSetting(id, name, type, location);
    }

    public List refreshExtensionList() {
        FileSystemUtils.createDirectory("./extensions");
        FileSystemUtils.createDirectory("./extensions/.extensions");

        List<Map> extLocs = getExtensionSettings();

        for (Map extLoc : extLocs) {
            String type = (String) extLoc.get("type");
            String location = (String) extLoc.get("location");

            if (type.equalsIgnoreCase("GithubRelease")) {
                try {
                    String url = "https://api.github.com/repos/" + location + "/releases/latest";
                    URL github = new URL(url);
                    String githubResponse = IOUtils.toString(github, "UTF8");

                    Map parsedData = (Map) new JsonSlurper().parseText(githubResponse);
                    List<Map> assetList = (List<Map>) parsedData.get("assets");

                    for (Map asset : assetList) {
                        String assetName = (String) asset.get("name");
                        if ("extensionInformation.yaml".equals(assetName)) {
                            String extInfUrlStr = (String) asset.get("browser_download_url");
                            String extInfStr = IOUtils.toString(new URL(extInfUrlStr), "UTF8");
                            System.out.println(extInfStr);
                            Yaml yaml = new Yaml();
                            Map extensionInformation = yaml.load(extInfStr);
                            String extensionId = (String) extensionInformation.get("id");

                            FileSystemUtils.createDirectory("./extensions/.extensions/" + extensionId);

                            File file = new File("./extensions/.extensions/" + extensionId + "/extensionInformation.yaml");
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(extInfStr.getBytes(StandardCharsets.UTF_8));
                            fos.close();

                            file = new File("./extensions/.extensions/" + extensionId + "/githubReleaseInformation.json");
                            fos = new FileOutputStream(file);
                            fos.write(new JsonBuilder(parsedData).toPrettyString().getBytes(StandardCharsets.UTF_8));
                            fos.close();
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }


    private void loadExtensions() {
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

        List<Map<String, String>> extensions = new ArrayList<>();
        for (File extDir : extDirs) {
            try {
                File jarFiles[] = extDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".jar");
                    }
                });
                ArrayList<URL> urls = new ArrayList<>();
                urls.add(extDir.toURI().toURL());
                for (File jarFile : jarFiles) {
                    urls.add(jarFile.toURI().toURL());
                }

                ClassLoader myClassLoader = new URLClassLoader(urls.toArray(new URL[0]));

                List<Map<String, String>> tmpIntegrations = getExtensionFromClassloader(myClassLoader, extDir.getPath());
                extensions.addAll(tmpIntegrations);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        extensionsList = extensions;
    }

    private List<Map<String, String>> getExtensionFromClassloader(ClassLoader classLoader, String extensionDirectory) {
        List<Map<String, String>> extensions = new ArrayList<>();
        try {
            Enumeration<URL> resources = classLoader.getResources("extensionInformation.yaml");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Yaml yaml = new Yaml();
                Map extensionInformation = yaml.load(url.openStream());
                String id = (String) extensionInformation.get("id");
                String name = (String) extensionInformation.get("name");
                String description = (String) extensionInformation.get("description");
                extensions.add(Map.of("id", id, "name", name, "description", description, "location", extensionDirectory));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extensions;
    }
}
