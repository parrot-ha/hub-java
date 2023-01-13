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
package com.parrotha.integration.lan;

import com.parrotha.integration.DeviceIntegration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LanUtils {
    private static final Logger logger = LoggerFactory.getLogger(LanUtils.class);

    public static String getFormattedMacAddressForIpAddress(String ipAddress) {
        String macAddress = getMacAddressForIPAddress(ipAddress);
        if (macAddress != null) {
            return macAddress.toUpperCase().replace(":", "");
        }
        return null;
    }

    // https://www.baeldung.com/run-shell-command-in-java
    public static String getMacAddressForIPAddress(String ipAddress) {

        ProcessBuilder builder = new ProcessBuilder();
        //TODO: handle other types of OSes, this is for linux
        builder.command("sh", "-c", "arp -a " + ipAddress);
        builder.directory(new File(System.getProperty("user.home")));
        try {
            Process process = builder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                List<String> output = IOUtils.readLines(process.getInputStream(), StandardCharsets.UTF_8);

                Pattern pattern = Pattern.compile("[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}");
                for (String outStr : output) {
                    Matcher matcher = pattern.matcher(outStr);
                    if (matcher.find()) {
                        return matcher.group(0);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Exception while getting mac address for ip address", e);
        }
        return null;
    }

    public static void processLanMessage(DeviceIntegration integration, String macAddress, String remoteAddress, int remotePort, String body,
                                         String headers) {
        String base64Headers = Base64.getEncoder().encodeToString(headers.getBytes(StandardCharsets.UTF_8));

        String base64Body = "";
        if (body != null) {
            base64Body = Base64.getEncoder().encodeToString(body.getBytes(StandardCharsets.UTF_8));
        }

        String deviceDescription = "mac: " + macAddress + ", headers: " + base64Headers + ", body: " + base64Body;

        if (logger.isDebugEnabled()) {
            logger.debug("Message received: " + deviceDescription);
        }

        // look for device based on mac address first
        if (integration.deviceExists(macAddress)) {
            integration.sendDeviceMessage(macAddress, deviceDescription);
            return;
        }

        String portHexString = String.format("%04x", remotePort);
        String ipAddressHexString = Stream.of(remoteAddress.split("\\."))
                .reduce("", (partialString, element) ->
                        partialString + String.format("%02x", Integer.parseInt(element)));

        // next look for device based on ip address : port
        String ipAddressAndPortHexString = ipAddressHexString + ":" + portHexString;
        if (integration.deviceExists(ipAddressAndPortHexString)) {
            integration.sendDeviceMessage(ipAddressAndPortHexString, deviceDescription);
            return;
        }

        // look for device based on ip address
        if (integration.deviceExists(ipAddressHexString)) {
            integration.sendDeviceMessage(ipAddressHexString, deviceDescription);
            return;
        }

        // look for device without integration id

        // look for device based on mac address first
        if (integration.deviceExists(macAddress, true)) {
            integration.sendDeviceMessage(macAddress, deviceDescription, true);
            return;
        }

        // next look for device based on ip address : port
        if (integration.deviceExists(ipAddressAndPortHexString, true)) {
            integration.sendDeviceMessage(ipAddressAndPortHexString, deviceDescription, true);
            return;
        }

        // look for device based on ip address
        if (integration.deviceExists(ipAddressHexString, true)) {
            integration.sendDeviceMessage(ipAddressHexString, deviceDescription, true);
            return;
        }

        // Finally, send message as hub event if no match above, it appears that Smartthings used to do this.
        // TODO: is lanMessage the right name of the event?  Can't find documentation about it.
        integration.sendHubEvent(new HashMap<>(Map.of("name", "lanMessage", "value", macAddress, "description", deviceDescription)));
    }
}
