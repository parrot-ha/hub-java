package com.parrotha.internal.entity;

import com.parrotha.entity.EntityScriptDelegateCommon;
import groovy.lang.Closure;
import groovyx.net.http.HttpResponseDecorator;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntityScriptDelegateCommonTest {
    Javalin app;


    @BeforeAll
    public void startServer() {
        app = Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"))
                .start(7070);
    }

    @Test
    public void testReprocessAutomationAppWithChange() {
        Closure closure = new Closure(this) {
            public void doCall(HttpResponseDecorator response) {
                assertEquals(200, response.getStatus());
                assertTrue("text/plain".equals(response.getContentType()));
            }
        };

        try {
            new EntityScriptDelegateCommonImpl().httpGet("http://localhost:7070/", closure);
        } catch (IOException | URISyntaxException e) {
            fail("Exception", e);
        }
    }

    @AfterAll
    public void stopServer() {
        app.stop();
    }

    private class EntityScriptDelegateCommonImpl extends EntityScriptDelegateCommon {

        @Override
        protected void runEntityMethod(String methodName, Object... args) {

        }
    }
}
