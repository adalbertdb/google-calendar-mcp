package org.acme.tools;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

public class Hello {
    @Tool(description = "Hello tool to test communications, say hello to the user!")

    String hello(@ToolArg(description = "Name of the user") String name) {
        return "Hello from tool! "+name;
    }
}
