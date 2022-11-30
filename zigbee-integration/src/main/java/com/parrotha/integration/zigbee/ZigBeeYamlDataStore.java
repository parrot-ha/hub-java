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
package com.parrotha.integration.zigbee;

import com.zsmartsystems.zigbee.IeeeAddress;
import com.zsmartsystems.zigbee.database.ZigBeeNetworkDataStore;
import com.zsmartsystems.zigbee.database.ZigBeeNodeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Serializes and deserializes the ZigBee network state to yaml files.
 */
public class ZigBeeYamlDataStore implements ZigBeeNetworkDataStore {
    private final static Logger logger = LoggerFactory.getLogger(ZigBeeYamlDataStore.class);

    private final String networkId;

    public ZigBeeYamlDataStore(String networkId) {
        this.networkId = "config/zigbee/" + networkId + "/";
        File file = new File(this.networkId);
        if (file.exists()) {
            return;
        }
        if (!file.mkdirs()) {
            logger.error("Error creating network database folder {}", file);
        }
    }

    private File getFile(IeeeAddress address, boolean tmpFile) {
        if (tmpFile) {
            return new File(networkId + ".tmp_" + address + ".yaml");
        } else {
            return new File(networkId + address + ".yaml");
        }
    }

    @Override
    public Set<IeeeAddress> readNetworkNodes() {
        Set<IeeeAddress> nodes = new HashSet<>();
        File dir = new File(networkId);
        File[] files = dir.listFiles();

        if (files == null) {
            return nodes;
        }

        for (File file : files) {
            if (file.getName().toLowerCase().startsWith(".tmp_") || !file.getName().toLowerCase().endsWith(".yaml")) {
                continue;
            }

            try {
                IeeeAddress address = new IeeeAddress(file.getName().substring(0, 16));
                nodes.add(address);
            } catch (IllegalArgumentException e) {
                logger.error("Error parsing database filename: {}", file.getName());
            }
        }

        return nodes;
    }

    @Override
    public ZigBeeNodeDao readNode(IeeeAddress address) {

        ZigBeeNodeDao node = null;
        try {
            File file = getFile(address, false);
            Yaml yaml = new Yaml(new CalendarConstructor());

            yaml.setBeanAccess(BeanAccess.FIELD);
            node = yaml.load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ScannerException se) {
            // there was a problem loading the file, try the tmp file
            File file = getFile(address, true);
            Yaml yaml = new Yaml(new CalendarConstructor());
            yaml.setBeanAccess(BeanAccess.FIELD);
            try {
                node = yaml.load(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw se;
            }
        }

        return node;
    }


    @Override
    public void writeNode(ZigBeeNodeDao node) {
        synchronized (networkId) {
            try {
                Yaml yaml = new Yaml(new CalendarConstructor());
                yaml.setBeanAccess(BeanAccess.FIELD);
                File file = getFile(node.getIeeeAddress(), true);
                FileWriter fileWriter = new FileWriter(file);

                yaml.dump(node, fileWriter);
                fileWriter.close();

                ZigBeeNodeDao tmpNode = yaml.load(new FileInputStream(file));
                if (tmpNode != null) {
                    // the node was loaded successfully, copy it to the existing file.
                    Files.copy(file.toPath(), getFile(node.getIeeeAddress(), false).toPath(), REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeNode(IeeeAddress address) {
        File file = getFile(address, false);
        if (!file.delete()) {
            logger.error("{}: Error removing network state", address);
        }
        file = getFile(address, true);
        if (!file.delete()) {
            logger.error("{}: Error removing network state temp file", address);
        }
    }

    // override snake yaml timestamp constructor to create Calendar instead of Date
    public static class CalendarConstructor extends Constructor {
        public CalendarConstructor() {
            this.yamlConstructors.put(new Tag("tag:yaml.org,2002:timestamp"), new ConstructCalendar());
        }

        private class ConstructCalendar extends AbstractConstruct {
            @Override
            public Object construct(Node node) {
                if (node instanceof ScalarNode) {
                    try {
                        String str = constructScalar((ScalarNode) node);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(ZonedDateTime.parse(str).toInstant().toEpochMilli());
                        return calendar;
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        }
    }
}
