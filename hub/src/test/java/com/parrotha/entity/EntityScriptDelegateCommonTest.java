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
package com.parrotha.entity;

import groovy.json.JsonBuilder;
import groovy.lang.Closure;
import groovyx.net.http.HttpResponseDecorator;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntityScriptDelegateCommonTest {
    Javalin app;

    @BeforeAll
    public void startServer() {
        app = Javalin.create();

        app.get("/", ctx -> ctx.result("Hello World"));

        app.get("/json", ctx -> {
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(Map.of("key1", "value1")).toString());
        });

        app.post("/json", ctx -> {
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(Map.of("key1", "value1")).toString());
        });

        app.start(7070);


    }

    @Test
    public void testAsyncHttpGetTextPlain() {
        ArgumentCaptor<AsyncResponse> asyncResponseArgumentCaptor = ArgumentCaptor.forClass(AsyncResponse.class);

        EntityScriptDelegateCommon mockEntityScriptDelegate = mock(EntityScriptDelegateCommon.class);

        doCallRealMethod().when(mockEntityScriptDelegate).asynchttpGet(ArgumentMatchers.eq("testMethod"), ArgumentMatchers.any(Map.class));
        doCallRealMethod().when(mockEntityScriptDelegate)
                .asynchttpGet(ArgumentMatchers.eq("testMethod"), ArgumentMatchers.any(Map.class), ArgumentMatchers.isNull());
        doCallRealMethod().when(mockEntityScriptDelegate)
                .asyncHttpRequest(ArgumentMatchers.eq("GET"), ArgumentMatchers.eq("testMethod"), ArgumentMatchers.any(Map.class),
                        ArgumentMatchers.isNull());

        mockEntityScriptDelegate.asynchttpGet("testMethod", Map.of("uri", "http://localhost:7070/"));

        verify(mockEntityScriptDelegate, timeout(5000).times(1)).runEntityMethod(ArgumentMatchers.eq("testMethod"),
                asyncResponseArgumentCaptor.capture());
        AsyncResponse asyncResponse = asyncResponseArgumentCaptor.getValue();
        assertEquals(200, asyncResponse.getStatus());
        assertEquals("Hello World", asyncResponse.getData());
    }

    @Test
    public void testAsyncHttpGetJson() {
        ArgumentCaptor<AsyncResponse> asyncResponseArgumentCaptor = ArgumentCaptor.forClass(AsyncResponse.class);

        EntityScriptDelegateCommon mockEntityScriptDelegate = mock(EntityScriptDelegateCommon.class);

        doCallRealMethod().when(mockEntityScriptDelegate).asynchttpGet(ArgumentMatchers.eq("testMethod"), ArgumentMatchers.any(Map.class));
        doCallRealMethod().when(mockEntityScriptDelegate)
                .asynchttpGet(ArgumentMatchers.eq("testMethod"), ArgumentMatchers.any(Map.class), ArgumentMatchers.isNull());
        doCallRealMethod().when(mockEntityScriptDelegate)
                .asyncHttpRequest(ArgumentMatchers.eq("GET"), ArgumentMatchers.eq("testMethod"), ArgumentMatchers.any(Map.class),
                        ArgumentMatchers.isNull());

        mockEntityScriptDelegate.asynchttpGet("testMethod", Map.of("uri", "http://localhost:7070/json"));

        verify(mockEntityScriptDelegate, timeout(5000).times(1)).runEntityMethod(ArgumentMatchers.eq("testMethod"),
                asyncResponseArgumentCaptor.capture());
        AsyncResponse asyncResponse = asyncResponseArgumentCaptor.getValue();
        assertEquals(200, asyncResponse.getStatus());
        assertEquals("{\"key1\":\"value1\"}", asyncResponse.getData());
        assertNotNull(asyncResponse.getJson());
        assertTrue(asyncResponse.getJson() instanceof Map);
        assertEquals(1, ((Map<?, ?>) asyncResponse.getJson()).size());
        assertEquals("value1", ((Map<?, ?>) asyncResponse.getJson()).get("key1"));
    }

    @Test
    public void testSTAsyncHttpGetTextPlain() {
        ArgumentCaptor<AsyncResponse> asyncResponseArgumentCaptor = ArgumentCaptor.forClass(AsyncResponse.class);

        EntityScriptDelegateCommon mockEntityScriptDelegate = mock(EntityScriptDelegateCommon.class);

        doCallRealMethod().when(mockEntityScriptDelegate).asynchttpGet(ArgumentMatchers.eq("testMethod"), ArgumentMatchers.any(Map.class));
        doCallRealMethod().when(mockEntityScriptDelegate)
                .asynchttpGet(ArgumentMatchers.eq("testMethod"), ArgumentMatchers.any(Map.class), ArgumentMatchers.isNull());
        doCallRealMethod().when(mockEntityScriptDelegate)
                .asyncHttpRequest(ArgumentMatchers.eq("GET"), ArgumentMatchers.eq("testMethod"), ArgumentMatchers.any(Map.class),
                        ArgumentMatchers.isNull());
        doCallRealMethod().when(mockEntityScriptDelegate).include(ArgumentMatchers.any());
        doCallRealMethod().when(mockEntityScriptDelegate).getAsynchttp_v1();

        mockEntityScriptDelegate.include("asynchttp_v1");
        mockEntityScriptDelegate.getAsynchttp_v1().get("testMethod", Map.of("uri", "http://localhost:7070/"));

        verify(mockEntityScriptDelegate, timeout(5000).times(1)).runEntityMethod(ArgumentMatchers.eq("testMethod"),
                asyncResponseArgumentCaptor.capture());
        AsyncResponse asyncResponse = asyncResponseArgumentCaptor.getValue();
        assertEquals(200, asyncResponse.getStatus());
        assertEquals("Hello World", asyncResponse.getData());
    }

    @Test
    public void testHttpGetTextPlain() {
        Closure closure = new Closure(this) {
            public void doCall(HttpResponseDecorator response) {
                assertEquals(200, response.getStatus());
                assertEquals("text/plain", response.getContentType());
            }
        };

        EntityScriptDelegateCommon mockEntityScriptDelegate = mock(EntityScriptDelegateCommon.class, Answers.CALLS_REAL_METHODS);

        try {
            mockEntityScriptDelegate.httpGet("http://localhost:7070/", closure);
        } catch (IOException | URISyntaxException e) {
            fail("Exception", e);
        }
    }

    @Test
    public void testHttpGetApplicationJson() {
        Closure closure = new Closure(this) {
            public void doCall(HttpResponseDecorator response) {
                assertEquals(200, response.getStatus());
                assertEquals("application/json", response.getContentType());
            }
        };

        EntityScriptDelegateCommon mockEntityScriptDelegate = mock(EntityScriptDelegateCommon.class, Answers.CALLS_REAL_METHODS);
        try {
            mockEntityScriptDelegate.httpGet("http://localhost:7070/json", closure);
        } catch (IOException | URISyntaxException e) {
            fail("Exception", e);
        }
    }

    @Test
    public void testHttpPostApplicationJson() {
        Closure closure = new Closure(this) {
            public void doCall(HttpResponseDecorator response) {
                assertEquals(200, response.getStatus());
                assertEquals("application/json", response.getContentType());
            }
        };

        EntityScriptDelegateCommon mockEntityScriptDelegate = mock(EntityScriptDelegateCommon.class, Answers.CALLS_REAL_METHODS);
        try {
            mockEntityScriptDelegate.httpPost(Map.of("uri", "http://localhost:7070/json"), closure);
        } catch (IOException | URISyntaxException e) {
            fail("Exception", e);
        }
    }

    @AfterAll
    public void stopServer() {
        app.stop();
    }
}
