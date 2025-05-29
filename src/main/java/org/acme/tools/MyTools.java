package org.acme.tools;

import io.quarkiverse.mcp.server.ToolManager;
import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import io.quarkus.security.Authenticated;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.ProducerTemplate;
import jakarta.inject.Inject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MyTools {

    @Inject ToolManager toolManager;
    @Inject    ProducerTemplate producerTemplate;
    @Startup
    void addTool() {

        // Define a new tool for creating calendar events
        toolManager.newTool("createEvent")
                .setDescription("Creates a calendar event.")
                .addArgument("value", "Event details in the format: create event: summary, location, description, start, end", true, String.class)
                .setHandler(ta -> {
                    String input = ta.args().get("value").toString();
                    try {
                        String response = producerTemplate.requestBody("direct:createEvent", input, String.class);
                        return ToolResponse.success(response);
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        return ToolResponse.error("Error creating event: " + sw.toString());
                    }
                });
    }
}