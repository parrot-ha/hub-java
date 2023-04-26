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
import com.parrotha.integration.device.LanDeviceMessageEvent;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        integration.sendEvent(new LanDeviceMessageEvent(macAddress, remoteAddress, remotePort, deviceDescription));
    }
}
