package com.parrotha.internal.extension;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class ExtensionService {
    private List<Map<String, String>> extensionsList;

    public List getInstalledExtensions() {
        if (extensionsList == null) {
            loadExtensions();
        }
        return extensionsList;
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

                List<Map<String, String>> tmpIntegrations = getExtensionFromClassloader(myClassLoader, extensionDirectory.getAbsolutePath());
                extensions.addAll(tmpIntegrations);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        extensionsList = extensions;
    }

    private List<Map<String, String>> getExtensionFromClassloader(ClassLoader classLoader, String baseDirectory) {
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
                extensions.add(Map.of("id", id, "name", name, "description", description));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extensions;
    }
}
