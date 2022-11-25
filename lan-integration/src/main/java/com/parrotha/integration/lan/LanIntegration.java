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
package com.parrotha.integration.lan;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.http.util.TextUtils;
import org.eclipse.jetty.server.Server;
import com.parrotha.device.HubAction;
import com.parrotha.device.HubResponse;
import com.parrotha.device.Protocol;
import com.parrotha.integration.DeviceIntegration;
import com.parrotha.internal.utils.HexUtils;
import com.parrotha.ui.PreferencesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanIntegration extends DeviceIntegration {
    private static final Logger logger = LoggerFactory.getLogger(LanIntegration.class);

    private static final String validIpAddressRegex = "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";
    private static final String validHostnameRegex = "(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])";
    private static final String HOST_REGEX = "[hH][oO][sS][Tt]: (" + validIpAddressRegex + "|" + validHostnameRegex + ")[:0-9]*";

    private Server server;
    private Integer serverPort;

    @Override
    public void start() {
        serverPort = getSettingAsInteger("serverPort", 39500);

        server = new Server(serverPort);
        server.setHandler(new LanHandler(this));

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> getPreferencesLayout() {
        return new PreferencesBuilder()
                .withTextInput("serverPort",
                        "Server TCP Port",
                        "TCP port for server to listen on.",
                        true,
                        true)
                .build();
    }

    @Override
    public void settingValueChanged(List<String> keys) {
        if (logger.isDebugEnabled()) {
            logger.debug("values changed " + keys);
        }
        if (keys.contains("serverPort")) {
            // restart the integration
            this.stop();
            this.start();
        }
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.LAN;
    }

    @Override
    public String getName() {
        return "LAN";
    }

    @Override
    public String getDescription() {
        return "Allows integration of LAN based devices.";
    }

    @Override
    public Map<String, String> getDisplayInformation() {
        Map<String, String> model = new HashMap<>();
        if (server != null) {
            model.put("Port", serverPort != null ? serverPort.toString() : "Not Set");
            model.put("Status", server.getState());
        } else {
            model.put("Status", "STOPPED");
        }
        return model;
    }

    @Override
    public boolean removeIntegrationDevice(String deviceNetworkId) {
        return true;
    }

    private void processUpnpResponse(String response) {
        Map<String, String> values = Arrays.stream(response.trim().split("\r\n"))
                .collect(Collectors.toMap(k -> k.substring(0, k.indexOf(':') > -1 ? k.indexOf(':') : k.length()).trim(),
                        v -> v.indexOf(':') > -1 ? v.substring(v.indexOf(":") + 1).trim() : ""));
        CaseInsensitiveMap<String, String> insensitiveHeaders = new CaseInsensitiveMap<>(values);
        URI uri = URI.create(insensitiveHeaders.get("location"));

        String networkAddress = Stream.of(uri.getHost().split("\\."))
                .reduce("", (partialString, element) ->
                        partialString + String.format("%02X", Integer.parseInt(element)));
        String mac = LanUtils.getFormattedMacAddressForIpAddress(uri.getHost());
        String deviceAddress = HexUtils.integerToHexString(uri.getPort(), 2);

        String ssdpPath = uri.getPath();
        String ssdpUSN = insensitiveHeaders.getOrDefault("usn", "");
        String ssdpTerm = insensitiveHeaders.getOrDefault("st", "");
        String ssdpNTS = insensitiveHeaders.getOrDefault("nts", "");
        String description = String.format(
                "devicetype:04, mac:%s, networkAddress:%s, deviceAddress:%s, stringCount:04, ssdpPath:%s, ssdpUSN:%s, ssdpTerm:%s, ssdpNTS:%s", mac,
                networkAddress, deviceAddress, ssdpPath, ssdpUSN, ssdpTerm, ssdpNTS);
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "ssdpTerm");
        properties.put("value", ssdpTerm);
        properties.put("description", description);
        sendHubEvent(properties);
    }

    @Override
    public HubResponse processAction(HubAction hubAction) {
        if (hubAction == null) {
            return null;
        }
        if (hubAction.getAction().startsWith("lan discovery")) {
            if (logger.isDebugEnabled()) {
                logger.debug("lan action: {}", hubAction.getAction());
            }
            String searchValue = hubAction.getAction().substring("lan discovery ".length());

            try {
                if (searchValue.contains("/")) {
                    String[] searchValueArray = searchValue.split("/");
                    sendDiscoveryPacketAndListenForResponse(searchValueArray);
                } else {
                    sendDiscoveryPacketAndListenForResponse(new String[]{searchValue});
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } else {
            // send message out
            HubResponse hubResponse = new HubResponse();
            //TODO: check for options.get("protocol") : LAN_PROTOCOL_TCP, LAN_PROTOCOL_UDP
            // also: options.get("type") : LAN_TYPE_UDPCLIENT
            // https://community.smartthings.com/t/udp-not-possible-they-said-wait-whats-this/13466
            if (hubAction.getProtocol() == Protocol.LAN) {
                hubResponse = sendRawHubAction(hubAction);
            } else {
                logger.warn("Not implemented yet, Protocol: {} action: {}", hubAction.getProtocol(), hubAction.getAction());
            }
            return hubResponse;
        }
        return null;
    }

    private HubResponse sendRawHubAction(HubAction hubAction) {
        HubResponse hubResponse = new HubResponse();

        //https://stackoverflow.com/questions/106179/regular-expression-to-match-dns-hostname-or-ip-address
        Pattern pattern = Pattern.compile(HOST_REGEX);
        Matcher matcher = pattern.matcher(hubAction.getAction());
        String hostHeader = null;
        if (matcher.find()) {
            hostHeader = matcher.group().substring("host:".length()).trim();
        }

        String[] hostArray = hostHeader.split(":");
        int port = 80;
        if (hostArray.length > 1) {
            port = Integer.parseInt(hostArray[1]);
        }
        String hostname = hostArray[0];

        //https://www.codejava.net/java-se/networking/java-socket-client-examples-tcp-ip
        try (Socket socket = new Socket(hostname, port)) {

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            writer.print(hubAction.getAction());
            writer.flush();
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            StringBuilder head = new StringBuilder();
            String body = "";

            try {
                // read the headers
                while (true) {
                    String headerLine = reader.readLine();
                    if (headerLine.length() == 0) {
                        break;
                    }
                    head.append(headerLine).append("\n");
                }

                StringBuffer buf = new StringBuffer();
                InputStream in = new HttpInputStream(reader, head.toString().split("\n"));
                int c;
                while ((c = in.read()) != -1) {
                    buf.append((char) c);
                }
                body = buf.toString();
            } catch (IOException ioExcep) {
                logger.info("IOException", ioExcep);
            }
            if (hubAction.getCallback() != null) {
                hubResponse.setBody(body.toString());
            } else {
                // send message to device
                LanUtils.processLanMessage(this,
                        LanUtils.getFormattedMacAddressForIpAddress(hostname),
                        hostname,
                        port,
                        body.toString(),
                        head.toString());
            }
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }

        return hubResponse;
    }

    // thanks to https://commandlinefanatic.com/cgi-bin/showarticle.cgi?article=art077
    private class HttpInputStream extends InputStream  {
        private Reader source;
        private int bytesRemaining;
        private boolean chunked = false;

        public HttpInputStream(Reader source, String[] headers) throws IOException  {
            this.source = source;

            for(String header : headers) {
                if (header.toLowerCase().startsWith("transfer-encoding") && header.contains("chunked")) {
                    chunked = true;
                    bytesRemaining = parseChunkSize();
                } else if (header.toLowerCase().startsWith("content-length")) {
                    try  {
                        String[] contentLengthHeader = header.split(":");
                        Integer.parseInt(header.split(":")[1]);
                    } catch (Exception e)  {
                        throw new IOException("Malformed or missing Content-Length header");
                    }
                }
            }
        }

        private int parseChunkSize() throws IOException {
            int b;
            int chunkSize = 0;

            while ((b = source.read()) != '\r') {
                chunkSize = (chunkSize << 4) |
                        ((b > '9') ?
                                (b > 'F') ?
                                        (b - 'a' + 10) :
                                        (b - 'A' + 10) :
                                (b - '0'));
            }
            // Consume the trailing '\n'
            if (source.read() != '\n')  {
                throw new IOException("Malformed chunked encoding");
            }

            return chunkSize;
        }

        public int read() throws IOException  {
            if (bytesRemaining == 0)  {
                if (!chunked) {
                    return -1;
                } else  {
                    // Read next chunk size; return -1 if 0 indicating end of stream
                    // Read and discard extraneous \r\n
                    if (source.read() != '\r')  {
                        throw new IOException("Malformed chunked encoding");
                    }
                    if (source.read() != '\n')  {
                        throw new IOException("Malformed chunked encoding");
                    }
                    bytesRemaining = parseChunkSize();

                    if (bytesRemaining == 0)  {
                        return -1;
                    }
                }
            }

            bytesRemaining -= 1;
            return source.read();
        }
    }

    //https://objectpartners.com/2014/03/25/a-groovy-time-with-upnp-and-wemo/
    private void sendDiscoveryPacketAndListenForResponse(String[] searchValues) throws IOException {
        InetAddress multicastAddress = InetAddress.getByName("239.255.255.250");
        MulticastSocket socket = new MulticastSocket(null);
        socket.setReuseAddress(true);
        socket.setSoTimeout(1000);
        socket.joinGroup(multicastAddress);

        Runnable r = new Runnable() {
            boolean inService = true;

            @Override
            public void run() {

                // shutdown in 60 seconds
                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {
                        inService = false;
                    }
                };
                new Timer("Timer").schedule(tt, 60000);

                try {
                    while (inService) { //inService is a variable controlled by a thread to stop the listener
                        byte[] buf = new byte[2048];
                        DatagramPacket input = new DatagramPacket(buf, buf.length);
                        try {
                            socket.receive(input);
                            String originaldata = new String(input.getData());
                            processUpnpResponse(originaldata);
                        } catch (SocketTimeoutException e) {
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    if (socket != null) {
                        try {
                            socket.leaveGroup(multicastAddress);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        socket.disconnect();
                        socket.close();
                    }
                }
            }
        };

        Thread t = new Thread(r);
        t.start();

        try {
            for (String searchValue : searchValues) {
                StringBuilder packet = new StringBuilder();
                packet.append("M-SEARCH * HTTP/1.1\r\n");
                packet.append("HOST: 239.255.255.250:1900\r\n");
                packet.append("MAN: \"ssdp:discover\"\r\n");
                packet.append("MX: ").append("4").append("\r\n");
                packet.append("ST: ").append(searchValue).append("\r\n").append("\r\n");
                byte[] data = packet.toString().getBytes();
                socket.send(new DatagramPacket(data, data.length, multicastAddress, 1900));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }
}
