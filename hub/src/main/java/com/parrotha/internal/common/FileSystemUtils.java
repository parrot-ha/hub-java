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
package com.parrotha.internal.common;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileSystemUtils {

    public static void createDirectory(String directory) {
        File directoryFile = new File(directory);
        if (!directoryFile.exists()) {
            directoryFile.mkdir();
        }
    }

    public static void cleanDirectory(String directory) {
        File directoryFile = new File(directory);
        if (directoryFile.exists() && directoryFile.isDirectory()) {
            try {
                FileUtils.cleanDirectory(directoryFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void unzipFile(String fileZip, String destDir) throws IOException {
        File destDirFile = new File(destDir);
        unzipFile(fileZip, destDirFile);
    }

    /**
     * Unzip a file on the file system. Thanks to https://www.baeldung.com/java-compress-and-uncompress
     *
     * @param fileZip The location and name of the zip file on the filesystem.
     * @param destDir The directory to unzip the file to.
     * @throws IOException
     */
    public static void unzipFile(String fileZip, File destDir) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static ClassLoader getClassloaderForJarFiles(File directory) {
        List<URL> urls = listJarsForDirectory(directory, false);

        if (urls.isEmpty()) {
            return null;
        }

        ClassLoader myClassLoader = new URLClassLoader(urls.toArray(new URL[0]));
        return myClassLoader;
    }

    public static List<URL> listJarsForDirectory(File directory, boolean recurse) {
        List<URL> urls = new ArrayList<>();

        if (recurse) {
            File additionalDirectories[] = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });
            // recurse through directories
            for (File addDir : additionalDirectories) {
                urls.addAll(listJarsForDirectory(addDir, recurse));
            }
        }

        File jarFiles[] = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".jar");
            }
        });

        for (File jarFile : jarFiles) {
            try {
                urls.add(jarFile.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return urls;
    }
}
