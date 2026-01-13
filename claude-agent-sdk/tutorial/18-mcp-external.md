# Module 18: MCP External Servers

Connecting to external MCP servers.

## What You'll Learn

- Configuring stdio-based MCP servers
- MCP tool naming convention (`mcp__{server}__{tool}`)
- Pre-approving MCP tools with `allowedTools`
- Using external tools like filesystem operations

## What is MCP?

Model Context Protocol (MCP) allows Claude to use external tools provided by MCP servers. Servers can be:
- **Stdio**: Local processes communicating via stdin/stdout
- **SSE**: Remote servers using Server-Sent Events
- **HTTP**: Remote servers using HTTP

## McpStdioServerConfig

Configure an external MCP server that runs as a subprocess:

```java
import org.springaicommunity.claude.agent.sdk.mcp.McpServerConfig;
import java.util.List;
import java.util.Map;

// Configure the filesystem MCP server (npm package)
McpServerConfig.McpStdioServerConfig filesystemServer = new McpServerConfig.McpStdioServerConfig(
    "npx",                                              // Command to run
    List.of("-y", "@modelcontextprotocol/server-filesystem", "/path/to/dir"),  // Arguments
    Map.of()                                            // Environment variables (optional)
);
```

## MCP Tool Naming Convention

MCP tools follow this naming pattern:

```
mcp__{serverName}__{toolName}
```

Examples:
- `mcp__fs__read_file` - filesystem server's read_file tool
- `mcp__fs__list_directory` - filesystem server's list_directory tool
- `mcp__mem__store` - memory server's store tool

The `serverName` is what you provide when registering the server with `.mcpServer(name, config)`.

## Complete Example

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.mcp.McpServerConfig;
import org.springaicommunity.claude.agent.sdk.parsing.ParsedMessage;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import org.springaicommunity.claude.agent.sdk.types.AssistantMessage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Create test directory with sample files
Path testDir = Files.createTempDirectory("mcp-test");
Files.writeString(testDir.resolve("hello.txt"), "Hello from MCP!");
Files.writeString(testDir.resolve("data.json"), "{\"status\": \"ok\"}");

// Configure filesystem MCP server
McpServerConfig.McpStdioServerConfig filesystemServer = new McpServerConfig.McpStdioServerConfig(
    "npx",
    List.of("-y", "@modelcontextprotocol/server-filesystem", testDir.toString()),
    Map.of()
);

// Pre-approve MCP tools
List<String> allowedMcpTools = List.of(
    "mcp__fs__read_file",
    "mcp__fs__list_directory",
    "mcp__fs__get_file_info"
);

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(testDir)
        .model(CLIOptions.MODEL_HAIKU)
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .mcpServer("fs", filesystemServer)   // Register server with name "fs"
        .allowedTools(allowedMcpTools)
        .build()) {

    // Use MCP filesystem tools
    client.connect("List files in the current directory using the filesystem MCP tools.");
    Iterator<ParsedMessage> response = client.receiveResponse();
    while (response.hasNext()) {
        ParsedMessage msg = response.next();
        if (msg.isRegularMessage() && msg.asMessage() instanceof AssistantMessage am) {
            am.getTextContent().ifPresent(System.out::println);
        }
    }

    // Read a file
    client.query("Read hello.txt using the filesystem MCP tools.");
    // ... process response
}
```

## Available MCP Server Types

| Type | Class | Use Case |
|------|-------|----------|
| Stdio | `McpStdioServerConfig` | Local subprocess (npx, python, etc.) |
| SSE | `McpSseServerConfig` | Remote server with SSE transport |
| HTTP | `McpHttpServerConfig` | Remote server with HTTP transport |

### SSE Server Example

```java
McpServerConfig.McpSseServerConfig sseServer = new McpServerConfig.McpSseServerConfig(
    "http://localhost:3000/sse",    // SSE endpoint URL
    Map.of("Authorization", "Bearer token")  // Headers
);
```

## Common NPM MCP Servers

| Package | Description | Tools Provided |
|---------|-------------|----------------|
| `@modelcontextprotocol/server-filesystem` | File operations | read_file, write_file, list_directory |
| `@modelcontextprotocol/server-memory` | Key-value storage | store, retrieve |
| `@modelcontextprotocol/server-github` | GitHub operations | Various GitHub API tools |
| `@modelcontextprotocol/server-brave-search` | Web search | search |

## Prerequisites

For stdio servers using npx:
- Node.js and npm must be installed
- The MCP server package is downloaded automatically via npx

## Key Points

- Use `McpStdioServerConfig` for local MCP servers
- Register servers with `.mcpServer(name, config)` - the name becomes part of tool naming
- MCP tools need to be in `allowedTools` when using `BYPASS_PERMISSIONS`
- Tool names follow the pattern `mcp__{serverName}__{toolName}`

<Warning>
MCP tools require proper approval. When using `BYPASS_PERMISSIONS`, add the MCP tool names to `allowedTools`. Without this, Claude cannot use the tools.
</Warning>

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-18-mcp-external)

## Running the Example

```bash
mvn compile exec:java -pl module-18-mcp-external
```

## Next Module

[Module 19: Multiple MCP Servers](/claude-agent-sdk/tutorial/19-multiple-mcp-servers) - Using multiple MCP servers together.
