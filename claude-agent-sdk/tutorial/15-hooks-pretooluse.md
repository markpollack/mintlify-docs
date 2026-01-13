# Module 15: Hooks PreToolUse

Intercepting tool execution before it happens.

## What You'll Learn

- Tool-specific hook registration with pattern matching
- Logging tool invocations before execution
- Blocking specific commands with custom rules
- Hook execution order when multiple hooks match

## PreToolUse Hook Basics

PreToolUse hooks fire before a tool executes. You can:
- Log the tool name and arguments
- Allow the execution to proceed
- Block the execution with a message

## Tool-Specific vs Global Hooks

```java
import org.springaicommunity.claude.agent.sdk.hooks.HookRegistry;
import org.springaicommunity.claude.agent.sdk.types.control.HookInput;
import org.springaicommunity.claude.agent.sdk.types.control.HookOutput;

HookRegistry hooks = new HookRegistry();

// Hook for specific tool (matches only "Bash")
hooks.registerPreToolUse("Bash", input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;
    String command = preToolUse.getArgument("command", String.class).orElse("");
    System.out.println("[PreToolUse:Bash] Command: " + command);

    if (command.contains("foo.sh")) {
        return HookOutput.block("Script foo.sh is not allowed");
    }
    return HookOutput.allow();
});

// Hook for specific tool (matches only "Read")
hooks.registerPreToolUse("Read", input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;
    String filePath = preToolUse.getArgument("file_path", String.class).orElse("");
    System.out.println("[PreToolUse:Read] File: " + filePath);
    return HookOutput.allow();
});

// Global hook (matches ALL tools)
hooks.registerPreToolUse(input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;
    System.out.println("[PreToolUse:*] Tool invoked: " + preToolUse.toolName());
    return HookOutput.allow();
});
```

## Hook Execution Order

When multiple hooks match a tool:

1. **Tool-specific hooks** execute first (in registration order)
2. **Global hooks** execute after
3. If any hook returns `block()`, the tool is blocked

## PreToolUseInput API

```java
var preToolUse = (HookInput.PreToolUseInput) input;

// Get tool name
String toolName = preToolUse.toolName();

// Get specific argument by name and type
String command = preToolUse.getArgument("command", String.class).orElse("");
String filePath = preToolUse.getArgument("file_path", String.class).orElse("");

// Get full input map
Map<String, Object> toolInput = preToolUse.toolInput();
```

## Complete Example

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.hooks.HookRegistry;
import org.springaicommunity.claude.agent.sdk.parsing.ParsedMessage;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import org.springaicommunity.claude.agent.sdk.types.AssistantMessage;
import org.springaicommunity.claude.agent.sdk.types.control.HookInput;
import org.springaicommunity.claude.agent.sdk.types.control.HookOutput;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

HookRegistry hooks = new HookRegistry();

// Log all Bash commands, block dangerous ones
hooks.registerPreToolUse("Bash", input -> {
    var preToolUse = (HookInput.PreToolUseInput) input;
    String command = preToolUse.getArgument("command", String.class).orElse("");

    System.out.println("[Hook] Bash command: " + command);

    if (command.contains("rm -rf") || command.contains("sudo")) {
        System.out.println("[Hook] BLOCKED: Dangerous command");
        return HookOutput.block("Command blocked for safety");
    }

    return HookOutput.allow();
});

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .model(CLIOptions.MODEL_HAIKU)
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .allowedTools(List.of("Bash", "Read"))
        .hookRegistry(hooks)
        .build()) {

    // This will be logged and allowed
    client.connect("Run: echo 'Hello from hook test'");
    Iterator<ParsedMessage> response = client.receiveResponse();
    while (response.hasNext()) {
        ParsedMessage msg = response.next();
        if (msg.isRegularMessage() && msg.asMessage() instanceof AssistantMessage am) {
            am.getTextContent().ifPresent(System.out::println);
        }
    }
}
```

## Common Hook Arguments by Tool

| Tool | Argument | Type | Description |
|------|----------|------|-------------|
| `Bash` | `command` | String | Shell command to execute |
| `Read` | `file_path` | String | Path to file |
| `Write` | `file_path` | String | Path to file |
| `Write` | `content` | String | Content to write |
| `Glob` | `pattern` | String | Glob pattern |
| `Grep` | `pattern` | String | Search pattern |

## Key Points

- Use tool-specific hooks (`registerPreToolUse("Bash", ...)`) for targeted interception
- Use global hooks (`registerPreToolUse(...)`) for logging all tool usage
- Tool-specific hooks fire before global hooks
- Return `HookOutput.block(message)` to prevent tool execution

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-15-hooks-pretooluse)

## Running the Example

```bash
mvn compile exec:java -pl module-15-hooks-pretooluse
```

## Next Module

[Module 16: Hooks PostToolUse](/claude-agent-sdk/tutorial/16-hooks-posttooluse) - Monitoring tool results after execution.
