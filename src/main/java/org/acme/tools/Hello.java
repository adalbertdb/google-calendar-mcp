package org.acme.tools;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

public class Hello {
    @Tool(description = "hello")

    String hello(@ToolArg(description = "name") String name) {
        return "Hello from tool!"+name;
    }
}
