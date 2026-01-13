# Module 20: MCP with Hooks

Combining MCP servers with hooks for custom behavior.

## What You'll Learn

- Intercepting MCP tool calls with hooks
- Blocking MCP operations based on custom rules
- Logging MCP tool usage
- Building custom tool behavior with hooks + MCP

## Why Combine MCP with Hooks?

MCP servers provide external tools, but you may want to:
- Log all MCP tool usage
- Block certain operations (e.g., access to sensitive files)
- Track statistics
- Add custom validation

Hooks give you programmatic control over MCP tool execution.

## Detecting MCP Tools in Hooks

MCP tools follow the `mcp__{server}__{tool}` naming pattern:

```java
hooks.registerPreToolUse(input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;
    String toolName = preToolUse.toolName();

    if (toolName.startsWith("mcp__")) {
        // This is an MCP tool
        String[] parts = toolName.split("__");
        String serverName = parts.length > 1 ? parts[1] : "unknown";
        String mcpTool = parts.length > 2 ? parts[2] : "unknown";

        System.out.println("MCP call: server=" + serverName + ", tool=" + mcpTool);
    }

    return HookOutput.allow();
});
```

## Blocking MCP Operations

Block access to sensitive files through MCP filesystem server:

```java
import org.springaicommunity.claude.agent.sdk.hooks.HookRegistry;
import org.springaicommunity.claude.agent.sdk.types.control.HookInput;
import org.springaicommunity.claude.agent.sdk.types.control.HookOutput;

HookRegistry hooks = new HookRegistry();

hooks.registerPreToolUse(input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;
    String toolName = preToolUse.toolName();

    if (toolName.startsWith("mcp__")) {
        // Get file path argument (used by read_file, write_file, etc.)
        String filePath = preToolUse.getArgument("path", String.class).orElse("");

        // Block access to files with "secret" in the name
        if (filePath.toLowerCase().contains("secret")) {
            return HookOutput.block("Access denied: Cannot access files with 'secret' in the name.");
        }
    }

    return HookOutput.allow();
});
```

## MCP Statistics with PostToolUse

Track MCP tool usage:

```java
import java.util.concurrent.atomic.AtomicInteger;

AtomicInteger mcpToolCallCount = new AtomicInteger(0);
AtomicInteger mcpToolBlockCount = new AtomicInteger(0);

hooks.registerPreToolUse(input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;
    if (preToolUse.toolName().startsWith("mcp__")) {
        mcpToolCallCount.incrementAndGet();
        // ... check and possibly block
    }
    return HookOutput.allow();
});

hooks.registerPostToolUse(input -> {
    var postToolUse = (HookInput.PostToolUseInput) input;
    if (postToolUse.toolName().startsWith("mcp__")) {
        Object response = postToolUse.toolResponse();
        System.out.println("[MCP Result] " + postToolUse.toolName() + " → " +
            String.valueOf(response).substring(0, Math.min(80, String.valueOf(response).length())));
    }
    return HookOutput.allow();
});
```

## Complete Example

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.hooks.HookRegistry;
import org.springaicommunity.claude.agent.sdk.mcp.McpServerConfig;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import org.springaicommunity.claude.agent.sdk.types.control.HookInput;
import org.springaicommunity.claude.agent.sdk.types.control.HookOutput;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// Test files
Path testDir = Files.createTempDirectory("mcp-hooks-test");
Files.writeString(testDir.resolve("allowed.txt"), "This file can be read.");
Files.writeString(testDir.resolve("secret.txt"), "CONFIDENTIAL data.");

// Statistics
AtomicInteger mcpCalls = new AtomicInteger(0);
AtomicInteger mcpBlocks = new AtomicInteger(0);

// MCP server
McpServerConfig.McpStdioServerConfig filesystemServer = new McpServerConfig.McpStdioServerConfig(
    "npx", List.of("-y", "@modelcontextprotocol/server-filesystem", testDir.toString()), Map.of()
);

// Hooks for MCP interception
HookRegistry hooks = new HookRegistry();

hooks.registerPreToolUse(input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;
    String toolName = preToolUse.toolName();

    if (toolName.startsWith("mcp__")) {
        mcpCalls.incrementAndGet();
        String[] parts = toolName.split("__");
        System.out.printf("[Hook:PreMCP] Server=%s, Tool=%s%n",
            parts.length > 1 ? parts[1] : "?",
            parts.length > 2 ? parts[2] : "?");

        // Block access to secret files
        String filePath = preToolUse.getArgument("path", String.class).orElse("");
        if (filePath.toLowerCase().contains("secret")) {
            mcpBlocks.incrementAndGet();
            System.out.println("[Hook:PreMCP] BLOCKED: Secret file access");
            return HookOutput.block("Access denied: Cannot read secret files.");
        }
    }
    return HookOutput.allow();
});

hooks.registerPostToolUse(input -> {
    var postToolUse = (HookInput.PostToolUseInput) input;
    if (postToolUse.toolName().startsWith("mcp__")) {
        System.out.println("[Hook:PostMCP] Completed: " + postToolUse.toolName());
    }
    return HookOutput.allow();
});

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(testDir)
        .model(CLIOptions.MODEL_HAIKU)
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .mcpServer("fs", filesystemServer)
        .allowedTools(List.of("mcp__fs__read_file", "mcp__fs__list_directory"))
        .hookRegistry(hooks)
        .build()) {

    // This will work
    client.connect("Read allowed.txt");
    // ... process response

    // This will be blocked by our hook
    client.query("Read secret.txt");
    // ... Claude reports the file couldn't be accessed
}

System.out.println("MCP calls: " + mcpCalls.get());
System.out.println("MCP blocks: " + mcpBlocks.get());
```

## Pattern: Custom Tool Behavior

This hooks + MCP pattern provides "custom tool behavior":

| Capability | How |
|------------|-----|
| Interception | PreToolUse hook fires before MCP tool |
| Blocking | Return `HookOutput.block(message)` |
| Logging | Log in PreToolUse and PostToolUse |
| Statistics | AtomicInteger counters in hooks |
| Post-processing | Inspect results in PostToolUse |

## Key Points

- Hooks intercept MCP tools like any other tool
- Check for `mcp__` prefix to identify MCP tool calls
- Use PreToolUse to block operations before execution
- Use PostToolUse to log results and gather metrics
- This pattern provides programmatic control over external MCP servers

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-20-mcp-custom-tools)

## Running the Example

```bash
mvn compile exec:java -pl module-20-mcp-custom-tools
```

## Next Module

[Module 21: Subagents Introduction](/claude-agent-sdk/tutorial/21-subagents-intro) - Defining and spawning custom subagents.
