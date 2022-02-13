package com.parrotha.internal.extension;

import com.parrotha.internal.BaseApiHandler;
import groovy.json.JsonBuilder;
import io.javalin.Javalin;

import java.util.List;

public class ExtensionApiHandler extends BaseApiHandler {
    private ExtensionService extensionService;

    public ExtensionApiHandler(ExtensionService extensionService) {
        this.extensionService = extensionService;
    }

    @Override
    public void setupApi(Javalin app) {
        app.get("/api/extensions", ctx -> {
            List extensionList = extensionService.getInstalledExtensions();

            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(new JsonBuilder(extensionList).toString());
        });
    }
}
