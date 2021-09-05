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
package com.parrotha.integration.lan;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Thanks to eclipse for this tutorial:
 * https://www.eclipse.org/jetty/documentation/current/embedding-jetty.html
 */
public class LanHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(LanHandler.class);

    LanIntegration lanIntegration;

    public LanHandler(LanIntegration lanIntegration) {
        this.lanIntegration = lanIntegration;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException,
            ServletException {

        logger.debug("Processing request from LAN");

        // examples from ST:
        //index:10, mac:D4D252A89864, headers:R0VUIC8gSFRUUC8xLjENClVzZXItQWdlbnQ6IFdnZXQvMS4xOS40IChsaW51eC1nbnUpDQpBY2NlcHQ6ICovKg0KQWNjZXB0LUVuY29kaW5nOiBpZGVudGl0eQ0KSG9zdDogMTkyLjE2OC4xLjExOjM5NTAwDQpDb25uZWN0aW9uOiBLZWVwLUFsaXZl, body:

        // received from device:
        //index:11, mac:D4D252A89864, headers:R0VUIC8gSFRUUC8xLjENCkNvbm5lY3Rpb246IGNsb3NlDQpVc2VyLUFnZW50OiBQb3N0bWFuUnVudGltZS83LjI2LjINCkFjY2VwdDogKi8qDQpDYWNoZS1Db250cm9sOiBuby1jYWNoZQ0KUG9zdG1hbi1Ub2tlbjogOGU3YjI2MmYtNTg4OC00Mjg1LWE3YWQtM2YzZWRmNTYxOWRmDQpIb3N0OiAxOTIuMTY4LjEuMTE6Mzk1MDANCkFjY2VwdC1FbmNvZGluZzogZ3ppcCwgZGVmbGF0ZSwgYnI=, body:
        // parsed lan message:
        //[index:11, mac:D4D252A89864, headers:[accept-encoding:gzip, deflate, br, cache-control:no-cache, connection:close, host:192.168.1.11:39500, user-agent:PostmanRuntime/7.26.2, get / http/1.1:null, postman-token:8e7b262f-5888-4285-a7ad-3f3edf5619df, accept:*/*], body:null, header:GET / HTTP/1.1
        //Connection: close
        //User-Agent: PostmanRuntime/7.26.2
        //Accept: */*
        //Cache-Control: no-cache
        //Postman-Token: 8e7b262f-5888-4285-a7ad-3f3edf5619df
        //Host: 192.168.1.11:39500
        //Accept-Encoding: gzip, deflate, br]

        //index:12, mac:D4D252A89864, headers:UE9TVCAvIEhUVFAvMS4xDQpDb25uZWN0aW9uOiBjbG9zZQ0KVXNlci1BZ2VudDogUG9zdG1hblJ1bnRpbWUvNy4yNi4yDQpBY2NlcHQ6ICovKg0KQ2FjaGUtQ29udHJvbDogbm8tY2FjaGUNClBvc3RtYW4tVG9rZW46IDc5ZGEzMDQ3LTg0MGMtNDA3My1iMmM5LTE0OTllYWNiZWU5Mg0KSG9zdDogMTkyLjE2OC4xLjExOjM5NTAwDQpBY2NlcHQtRW5jb2Rpbmc6IGd6aXAsIGRlZmxhdGUsIGJyDQpDb250ZW50LVR5cGU6IG11bHRpcGFydC9mb3JtLWRhdGE7IGJvdW5kYXJ5PS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tMzE5NzAwMzA1Nzc5MzMzMTA1NDA4NDgwDQpDb250ZW50LUxlbmd0aDogMTY3, body:LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLTMxOTcwMDMwNTc3OTMzMzEwNTQwODQ4MA0KQ29udGVudC1EaXNwb3NpdGlvbjogZm9ybS1kYXRhOyBuYW1lPSJteWtleSINCg0KbXl2YWx1ZQ0KLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLTMxOTcwMDMwNTc3OTMzMzEwNTQwODQ4MC0tDQo=
        //[index:12, mac:D4D252A89864, headers:[content-length:167, accept-encoding:gzip, deflate, br, cache-control:no-cache, connection:close, host:192.168.1.11:39500, user-agent:PostmanRuntime/7.26.2, postman-token:79da3047-840c-4073-b2c9-1499eacbee92, content-type:multipart/form-data; boundary=--------------------------319700305779333105408480, accept:*/*, post / http/1.1:null], body:----------------------------319700305779333105408480
        //Content-Disposition: form-data; name="mykey"
        //
        //myvalue
        //----------------------------319700305779333105408480--
        //, header:POST / HTTP/1.1
        //Connection: close
        //User-Agent: PostmanRuntime/7.26.2
        //Accept: */*
        //Cache-Control: no-cache
        //Postman-Token: 79da3047-840c-4073-b2c9-1499eacbee92
        //Host: 192.168.1.11:39500
        //Accept-Encoding: gzip, deflate, br
        //Content-Type: multipart/form-data; boundary=--------------------------319700305779333105408480
        //Content-Length: 167]

        //index:14, mac:D4D252A89864, headers:UE9TVCAvIEhUVFAvMS4xDQpDb250ZW50LVR5cGU6IGFwcGxpY2F0aW9uL2pzb24NClVzZXItQWdlbnQ6IFBvc3RtYW5SdW50aW1lLzcuMjYuMg0KQWNjZXB0OiAqLyoNCkNhY2hlLUNvbnRyb2w6IG5vLWNhY2hlDQpQb3N0bWFuLVRva2VuOiAzOTkzNjMwYS05OGNlLTQyYjctYWE1MC0zZmU2MDRjMDRlZjgNCkhvc3Q6IDE5Mi4xNjguMS4xMTozOTUwMA0KQWNjZXB0LUVuY29kaW5nOiBnemlwLCBkZWZsYXRlLCBicg0KQ29udGVudC1MZW5ndGg6IDIx, body:eyJzdGF0dXMiOiJjb21wbGV0ZSJ9
        //[index:14, mac:D4D252A89864, headers:[content-length:21, accept-encoding:gzip, deflate, br, cache-control:no-cache, host:192.168.1.11:39500, user-agent:PostmanRuntime/7.26.2, postman-token:3993630a-98ce-42b7-aa50-3fe604c04ef8, content-type:application/json, accept:*/*, post / http/1.1:null], body:{"status":"complete"}, header:POST / HTTP/1.1
        //Content-Type: application/json
        //User-Agent: PostmanRuntime/7.26.2
        //Accept: */*
        //Cache-Control: no-cache
        //Postman-Token: 3993630a-98ce-42b7-aa50-3fe604c04ef8
        //Host: 192.168.1.11:39500
        //Accept-Encoding: gzip, deflate, br
        //Content-Length: 21, data:[status:complete], json:[status:complete]]

        //'index:05, mac:D4D252A89864, headers:UE9TVCAvdGVzdD9wYXJhbT0xIEhUVFAvMS4xDQpDb250ZW50LVR5cGU6IGFwcGxpY2F0aW9uL2pzb24NClVzZXItQWdlbnQ6IFBvc3RtYW5SdW50aW1lLzcuMjYuOA0KQWNjZXB0OiAqLyoNCkNhY2hlLUNvbnRyb2w6IG5vLWNhY2hlDQpQb3N0bWFuLVRva2VuOiA0NjA3MTFmZS02NzMyLTRjODItYWZhMy01MDBkZmRjZDRmMzcNCkhvc3Q6IDE5Mi4xNjguMS4xMTozOTUwMA0KQWNjZXB0LUVuY29kaW5nOiBnemlwLCBkZWZsYXRlLCBicg0KQ29ubmVjdGlvbjoga2VlcC1hbGl2ZQ0KQ29udGVudC1MZW5ndGg6IDQ3, body:ewogICAgInN0YXR1cyI6ImNvbXBsZXRlIiwKICAgICJ2YWx1ZSI6Im1pbmUiCn0='
        //Parsed: '[index:05, mac:D4D252A89864, headers:[post /test?param=1 http/1.1:null, content-length:47, accept-encoding:gzip, deflate, br, connection:keep-alive, cache-control:no-cache, host:192.168.1.11:39500, user-agent:PostmanRuntime/7.26.8, postman-token:460711fe-6732-4c82-afa3-500dfdcd4f37, content-type:application/json, accept:*/*], body:{
        //"status":"complete",
        //"value":"mine"
        //}, header:POST /test?param=1 HTTP/1.1
        //Content-Type: application/json
        //User-Agent: PostmanRuntime/7.26.8
        //Accept: */*
        //Cache-Control: no-cache
        //Postman-Token: 460711fe-6732-4c82-afa3-500dfdcd4f37
        //Host: 192.168.1.11:39500
        //Accept-Encoding: gzip, deflate, br
        //Connection: keep-alive
        //Content-Length: 47, data:[status:complete, value:mine], json:[status:complete, value:mine]]'


        String macAddress = LanUtils.getFormattedMacAddressForIpAddress(baseRequest.getRemoteAddr());

        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        response.setHeader("Connection", "close");
        response.setHeader("Content-Length", "0");
        response.setHeader("Server", "ParrotHub");

        String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

        response.setStatus(202);
        baseRequest.setHandled(true);

        // must parse headers into base64
        StringBuilder headerSB = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();

        headerSB.append(request.getMethod())
                .append(' ')
                .append(((Request) request).getOriginalURI())
                .append(' ')
                .append(((Request) request).getHttpVersion().toString())
                .append("\n");

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headers = request.getHeaders(headerName);
            Collections.list(headers).forEach((s) -> headerSB.append(headerName).append(": ").append(s).append("\n"));
        }

        processLanMessage(macAddress, request.getRemoteAddr(), request.getRemotePort(), body, headerSB.toString());
    }

    private void processLanMessage(String macAddress, String remoteAddress, int remotePort, String body, String headers) {
        Runnable lanMessageProcessor =
                () -> {
                    LanUtils.processLanMessage(lanIntegration, macAddress, remoteAddress, remotePort, body, headers);
                };

        Thread lanMessageProcessorThead = new Thread(lanMessageProcessor, "LanMessageProcessor");
        lanMessageProcessorThead.start();
    }
}
