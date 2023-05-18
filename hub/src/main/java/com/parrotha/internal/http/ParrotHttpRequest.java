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
package com.parrotha.internal.http;

import com.google.common.collect.Maps;
import groovy.json.JsonBuilder;
import groovy.lang.GString;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class ParrotHttpRequest {

    public static HttpResponse<String> doHttpRequest(String method, Map params) throws URISyntaxException, IOException, InterruptedException {
        Map paramsCopy = Maps.newHashMap(params);

        Object uriObj = paramsCopy.remove("uri");
        if (uriObj == null) {
            throw new IllegalStateException("No 'uri' parameter was given");
        }
        URI uri = new URI(uriObj.toString());

        String queryStr = null;
        Object queryObj = paramsCopy.remove("query");
        if (queryObj instanceof Map) {
            Map<?, ?> queryMap = ((Map<?, ?>) queryObj);
            if (queryMap.size() > 0) {
                StringBuilder queryStrBuilder = new StringBuilder();
                for (Object queryKey : queryMap.keySet()) {
                    queryStrBuilder.append("&").append(queryKey).append("=").append(queryMap.get(queryKey));
                }
                queryStrBuilder.replace(0, 1, "?");
                queryStr = queryStrBuilder.toString();
            }
        }

        Object path = paramsCopy.remove("path");
        StringBuilder pathStrBuilder = new StringBuilder();
        if (path != null) {
            pathStrBuilder.append(path);
        }
        if (pathStrBuilder.charAt(0) != '/') {
            pathStrBuilder.insert(0, '/');
        }
        if (queryStr != null) {
            pathStrBuilder.append(queryStr);
        }

        uri = uri.resolve(pathStrBuilder.toString());

        String requestContentType = "application/json";
        Object contentTypeObj = paramsCopy.remove("requestContentType");
        if ((contentTypeObj instanceof String || contentTypeObj instanceof GString)) {
            requestContentType = contentTypeObj.toString();
        }

        String contentType = requestContentType;
        contentTypeObj = paramsCopy.remove("contentType");
        if ((contentTypeObj instanceof String || contentTypeObj instanceof GString)) {
            contentType = contentTypeObj.toString();
        }

        int timeout = 30;
        Object timeoutObj = paramsCopy.remove("timeout");
        if (timeoutObj instanceof Number) {
            timeout = ((Number) timeoutObj).intValue();
        }

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder().uri(uri)
                .timeout(Duration.of(timeout, ChronoUnit.SECONDS));

        Object body = paramsCopy.remove("body");
        if (body != null) {
            if (body instanceof String || body instanceof GString) {
                httpRequestBuilder.method(method, HttpRequest.BodyPublishers.ofString(body.toString()));
            } else if ((body instanceof Map || body instanceof List) && requestContentType.contains("json")) {
                httpRequestBuilder.method(method, HttpRequest.BodyPublishers.ofString(new JsonBuilder(body).toString()));
            } else {
                throw new IllegalStateException("Unable to handle body of type " + body.getClass().getName());
            }
        }

        Object headersObj = paramsCopy.remove("headers");
        if (headersObj instanceof Map) {
            Map<?, ?> headersMap = (Map<?, ?>) headersObj;
            if (headersMap.size() > 0) {
                for (Object headerKey : headersMap.keySet()) {
                    httpRequestBuilder.header(headerKey.toString(), headersMap.get(headerKey) != null ? headersMap.get(headerKey).toString() : null);
                }
            }
        }
        httpRequestBuilder.header("Accept", contentType);
        httpRequestBuilder.header("Content-Type", requestContentType);


        HttpRequest httpRequest = httpRequestBuilder.build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return httpResponse;
    }

    public static CloseableHttpResponse doApacheHttpRequest(String method, Map params) throws URISyntaxException, IOException {
        Map paramsCopy = Maps.newHashMap(params);

        Object uriObj = paramsCopy.remove("uri");
        if (uriObj == null) {
            throw new IllegalStateException("No 'uri' parameter was given");
        }
        URI uri = new URI(uriObj.toString());

        String queryStr = null;
        Object queryObj = paramsCopy.remove("query");
        if (queryObj instanceof Map) {
            Map<?, ?> queryMap = ((Map<?, ?>) queryObj);
            if (queryMap.size() > 0) {
                StringBuilder queryStrBuilder = new StringBuilder();
                for (Object queryKey : queryMap.keySet()) {
                    queryStrBuilder.append("&").append(queryKey).append("=").append(queryMap.get(queryKey));
                }
                queryStrBuilder.replace(0, 1, "?");
                queryStr = queryStrBuilder.toString();
            }
        }

        Object path = paramsCopy.remove("path");
        StringBuilder pathStrBuilder = new StringBuilder();
        if (path != null) {
            pathStrBuilder.append(path);
        }
        if (pathStrBuilder.charAt(0) != '/') {
            pathStrBuilder.insert(0, '/');
        }
        if (queryStr != null) {
            pathStrBuilder.append(queryStr);
        }

        uri = uri.resolve(pathStrBuilder.toString());

        String requestContentType = "application/json";
        Object contentTypeObj = paramsCopy.remove("requestContentType");
        if ((contentTypeObj instanceof String || contentTypeObj instanceof GString)) {
            requestContentType = contentTypeObj.toString();
        }

        String contentType = requestContentType;
        contentTypeObj = paramsCopy.remove("contentType");
        if ((contentTypeObj instanceof String || contentTypeObj instanceof GString)) {
            contentType = contentTypeObj.toString();
        }

        int timeout = 30;
        Object timeoutObj = paramsCopy.remove("timeout");
        if (timeoutObj instanceof Number) {
            timeout = ((Number) timeoutObj).intValue();
        }

        RequestBuilder requestBuilder = RequestBuilder.create(method).setUri(uri);

        Object body = paramsCopy.remove("body");
        if (body != null) {
            if (body instanceof String || body instanceof GString) {
                requestBuilder.setEntity(EntityBuilder.create().setText(body.toString()).setContentType(ContentType.create(contentType)).build());
            } else if ((body instanceof Map || body instanceof List) && requestContentType.contains("json")) {
                requestBuilder.setEntity(
                        EntityBuilder.create().setText(new JsonBuilder(body).toString()).setContentType(ContentType.create(contentType)).build());
            } else {
                throw new IllegalStateException("Unable to handle body of type " + body.getClass().getName());
            }
        }

        Object headersObj = paramsCopy.remove("headers");
        if (headersObj instanceof Map) {
            Map<?, ?> headersMap = (Map<?, ?>) headersObj;
            if (headersMap.size() > 0) {
                for (Object headerKey : headersMap.keySet()) {
                    requestBuilder.addHeader(headerKey.toString(), headersMap.get(headerKey) != null ? headersMap.get(headerKey).toString() : null);
                }
            }
        }
        requestBuilder.addHeader("Accept", contentType);
        requestBuilder.addHeader("Content-Type", requestContentType);

        HttpUriRequest httpUriRequest = requestBuilder.build();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse reponse = httpclient.execute(httpUriRequest);

        return reponse;
    }
}
