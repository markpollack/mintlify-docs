# Module 19: Multiple MCP Servers

Using multiple MCP servers together.

## What You'll Learn

- Registering multiple MCP servers with different capabilities
- Using tools from different servers in the same conversation
- Cross-server workflows
- MCP tool naming with multiple servers

## Registering Multiple Servers

Chain `.mcpServer()` calls to register multiple servers:

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.mcp.McpServerConfig;

import java.util.List;
import java.util.Map;

// Server 1: Filesystem operations
McpServerConfig.McpStdioServerConfig filesystemServer = new McpServerConfig.McpStdioServerConfig(
    "npx",
    List.of("-y", "@modelcontextprotocol/server-filesystem", "/path/to/dir"),
    Map.of()
);

// Server 2: Memory (key-value storage)
McpServerConfig.McpStdioServerConfig memoryServer = new McpServerConfig.McpStdioServerConfig(
    "npx",
    List.of("-y", "@modelcontextprotocol/server-memory"),
    Map.of()
);

// Register both servers
ClaudeSyncClient client = ClaudeClient.sync()
    .mcpServer("fs", filesystemServer)    // Tools: mcp__fs__*
    .mcpServer("mem", memoryServer)       // Tools: mcp__mem__*
    .allowedTools(List.of(
        "mcp__fs__read_file",
        "mcp__fs__list_directory",
        "mcp__mem__store",
        "mcp__mem__retrieve"
    ))
    .build();
```

## Tool Naming with Multiple Servers

Each server's tools are namespaced:

| Server Name | Tool Name | Full MCP Tool Name |
|-------------|-----------|-------------------|
| `fs` | `read_file` | `mcp__fs__read_file` |
| `fs` | `list_directory` | `mcp__fs__list_directory` |
| `mem` | `store` | `mcp__mem__store` |
| `mem` | `retrieve` | `mcp__mem__retrieve` |

## Cross-Server Workflows

Claude can chain tools from different servers in a single request:

```java
client.query("Read notes.txt using filesystem tools, then store its content in memory with key 'notes_backup'.");
```

Claude will call:
1. `mcp__fs__read_file` to read the file
2. `mcp__mem__store` to save to memory

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

// Create test files
Path testDir = Files.createTempDirectory("mcp-multi-test");
Files.writeString(testDir.resolve("notes.txt"), "Important: MCP tools use namespaced naming.");

// Configure servers
McpServerConfig.McpStdioServerConfig filesystemServer = new McpServerConfig.McpStdioServerConfig(
    "npx", List.of("-y", "@modelcontextprotocol/server-filesystem", testDir.toString()), Map.of()
);

McpServerConfig.McpStdioServerConfig memoryServer = new McpServerConfig.McpStdioServerConfig(
    "npx", List.of("-y", "@modelcontextprotocol/server-memory"), Map.of()
);

// Pre-approve tools from both servers
List<String> allowedTools = List.of(
    "mcp__fs__read_file",
    "mcp__fs__list_directory",
    "mcp__mem__store",
    "mcp__mem__retrieve"
);

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(testDir)
        .model(CLIOptions.MODEL_HAIKU)
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .mcpServer("fs", filesystemServer)
        .mcpServer("mem", memoryServer)
        .allowedTools(allowedTools)
        .build()) {

    // Task 1: List files (filesystem server)
    client.connect("List files using the filesystem MCP tools.");
    printResponse(client);

    // Task 2: Store data (memory server)
    client.query("Store 'Hello from MCP' with key 'greeting' using memory MCP tools.");
    printResponse(client);

    // Task 3: Cross-server workflow
    client.query("Read notes.txt and store its content in memory with key 'backup'.");
    printResponse(client);

    // Task 4: Retrieve from memory
    client.query("Retrieve the value with key 'greeting' from memory.");
    printResponse(client);
}

private static void printResponse(ClaudeSyncClient client) {
    Iterator<ParsedMessage> response = client.receiveResponse();
    while (response.hasNext()) {
        ParsedMessage msg = response.next();
        if (msg.isRegularMessage() && msg.asMessage() instanceof AssistantMessage am) {
            am.getTextContent().ifPresent(System.out::println);
        }
    }
}
```

## Server Registration Order

The order of `.mcpServer()` calls doesn't affect functionality. All registered servers are available simultaneously to Claude.

## Key Points

- Chain `.mcpServer(name, config)` calls to register multiple servers
- Each server's tools are namespaced: `mcp__{serverName}__{toolName}`
- Claude can use tools from different servers in a single conversation
- All servers are available simultaneously regardless of registration order
- Add all needed tools to `allowedTools` for `BYPASS_PERMISSIONS` mode

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-19-mcp-spring-ai)

## Running the Example

```bash
mvn compile exec:java -pl module-19-mcp-spring-ai
```

## Next Module

[Module 20: MCP with Hooks](/claude-agent-sdk/tutorial/20-mcp-with-hooks) - Combining MCP servers with hooks for custom behavior.
