# Module 14: Permission Callbacks

Implementing dynamic tool permission decisions with hooks.

## What You'll Learn

- Using `HookRegistry.registerPreToolUse()` for permission decisions
- Allowing or blocking tools based on runtime context
- Pattern matching on tool names and inputs
- Building a permission callback system

## Permission Callbacks via Hooks

The SDK provides permission callbacks through the PreToolUse hook mechanism. When Claude attempts to use a tool, your hook is invoked with the tool name and input, allowing you to make runtime decisions.

| Hook Method | Result | Purpose |
|-------------|--------|---------|
| `HookOutput.allow()` | Tool executes | Permit the operation |
| `HookOutput.block(message)` | Tool blocked | Deny with reason shown to Claude |

## Implementing Permission Callbacks

Register a PreToolUse hook to intercept all tool calls:

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.hooks.HookRegistry;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import org.springaicommunity.claude.agent.sdk.types.control.HookInput;
import org.springaicommunity.claude.agent.sdk.types.control.HookOutput;

import java.nio.file.Path;
import java.util.List;

// Define dangerous command patterns
List<String> DANGEROUS_PATTERNS = List.of("rm -rf", "sudo", "chmod 777", "mkfs");

// Create hook registry with permission logic
HookRegistry hooks = new HookRegistry();

hooks.registerPreToolUse(input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;
    String toolName = preToolUse.toolName();

    System.out.println("[Permission] Tool: " + toolName);

    // Always allow read-only operations
    if (toolName.equals("Read") || toolName.equals("Glob") || toolName.equals("Grep")) {
        return HookOutput.allow();
    }

    // Check Bash commands for dangerous patterns
    if (toolName.equals("Bash")) {
        String command = preToolUse.getArgument("command", String.class).orElse("");

        for (String pattern : DANGEROUS_PATTERNS) {
            if (command.contains(pattern)) {
                return HookOutput.block("Blocked dangerous command: " + pattern);
            }
        }
    }

    // Default: allow other tools
    return HookOutput.allow();
});

// Use the hooks with a client
try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .model(CLIOptions.MODEL_HAIKU)
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .allowedTools(List.of("Read", "Bash", "Glob"))
        .hookRegistry(hooks)
        .build()) {

    // Safe command - will be allowed
    client.connect("Run: echo 'Hello'");
    // ... process response

    // Dangerous command - will be blocked by hook
    client.query("Run: rm -rf /tmp/test");
    // Claude receives the block message and reports it couldn't execute
}
```

## Permission Decision Flow

```
Claude requests tool → PreToolUse hook fires → Your code decides
                                                    │
                                         ┌─────────┴─────────┐
                                         ▼                   ▼
                                  HookOutput.allow()  HookOutput.block(msg)
                                         │                   │
                                         ▼                   ▼
                                   Tool executes       Tool blocked
                                                     (msg sent to Claude)
```

## Common Permission Patterns

### Read-Only Mode

```java
hooks.registerPreToolUse(input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;
    String toolName = preToolUse.toolName();

    return switch (toolName) {
        case "Read", "Glob", "Grep" -> HookOutput.allow();
        default -> HookOutput.block("Only read operations are permitted");
    };
});
```

### Path-Based Restrictions

```java
hooks.registerPreToolUse(input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;

    if (preToolUse.toolName().equals("Read")) {
        String path = preToolUse.getArgument("file_path", String.class).orElse("");
        if (path.contains("/etc/") || path.contains("/secrets/")) {
            return HookOutput.block("Access to sensitive paths denied");
        }
    }
    return HookOutput.allow();
});
```

## Key Points

- Use `HookRegistry.registerPreToolUse()` for permission callbacks
- Return `HookOutput.allow()` to permit, `HookOutput.block(message)` to deny
- The block message is shown to Claude, who reports it to the user
- Hooks fire for all tool types including MCP tools

<Note>
Claude has built-in safety checks and may refuse dangerous commands even before your hook fires. Your permission callbacks provide an additional layer of control.
</Note>

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-14-permission-callbacks)

## Running the Example

```bash
mvn compile exec:java -pl module-14-permission-callbacks
```

## Next Module

[Module 15: Hooks PreToolUse](/claude-agent-sdk/tutorial/15-hooks-pretooluse) - Tool-specific hook registration and logging.
