# Module 16: Hooks PostToolUse

Monitoring and reacting to tool results.

## What You'll Learn

- Logging tool execution results
- Detecting errors in tool output
- Tracking tool usage statistics
- PostToolUse vs PreToolUse: observation vs control

## PostToolUse Hook Basics

PostToolUse hooks fire after a tool executes. Unlike PreToolUse, you cannot block execution (the tool already ran). Use PostToolUse for:
- Logging results
- Detecting errors
- Collecting metrics
- Auditing tool usage

## PostToolUseInput API

```java
import org.springaicommunity.claude.agent.sdk.types.control.HookInput;

var postToolUse = (HookInput.PostToolUseInput) input;

String toolName = postToolUse.toolName();           // Tool that executed
Object response = postToolUse.toolResponse();       // Tool's result
String toolUseId = postToolUse.toolUseId();         // Unique call ID
Map<String, Object> toolInput = postToolUse.toolInput();  // Original input
```

## Tool Response Formats

Different tools return different response structures:

```java
// Bash response
{stdout=Hello World, stderr=, interrupted=false, isImage=false}

// Read response
{type=text, file={filePath=/path/to/file.txt, content=...}}
```

## Error Detection Pattern

```java
import org.springaicommunity.claude.agent.sdk.hooks.HookRegistry;
import org.springaicommunity.claude.agent.sdk.types.control.HookInput;
import org.springaicommunity.claude.agent.sdk.types.control.HookOutput;

import java.util.concurrent.atomic.AtomicInteger;

AtomicInteger errorCount = new AtomicInteger(0);

HookRegistry hooks = new HookRegistry();

hooks.registerPostToolUse(input -> {
    var postToolUse = (HookInput.PostToolUseInput) input;
    String toolName = postToolUse.toolName();
    Object response = postToolUse.toolResponse();

    System.out.println("[PostToolUse] Tool: " + toolName);

    // Detect errors in response
    String responseStr = String.valueOf(response).toLowerCase();
    if (responseStr.contains("error") || responseStr.contains("failed") ||
        responseStr.contains("not found") || responseStr.contains("no such file")) {
        errorCount.incrementAndGet();
        System.out.println("[PostToolUse] WARNING: Possible error detected!");
    }

    return HookOutput.allow();  // PostToolUse can only allow
});
```

## Usage Statistics Pattern

```java
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

Map<String, AtomicInteger> toolUsageCount = new HashMap<>();

hooks.registerPostToolUse(input -> {
    var postToolUse = (HookInput.PostToolUseInput) input;
    String toolName = postToolUse.toolName();

    // Track usage
    toolUsageCount.computeIfAbsent(toolName, k -> new AtomicInteger(0))
                  .incrementAndGet();

    // Log response preview
    String preview = String.valueOf(postToolUse.toolResponse());
    if (preview.length() > 100) {
        preview = preview.substring(0, 100) + "...";
    }
    System.out.println("[PostToolUse] " + toolName + " → " + preview);

    return HookOutput.allow();
});

// After session completes:
System.out.println("Tool usage summary:");
toolUsageCount.forEach((tool, count) ->
    System.out.println("  " + tool + ": " + count.get() + " calls"));
```

## Complete Example

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.hooks.HookRegistry;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import org.springaicommunity.claude.agent.sdk.types.control.HookInput;
import org.springaicommunity.claude.agent.sdk.types.control.HookOutput;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

Map<String, AtomicInteger> toolUsage = new HashMap<>();
AtomicInteger errors = new AtomicInteger(0);

HookRegistry hooks = new HookRegistry();

hooks.registerPostToolUse(input -> {
    var postToolUse = (HookInput.PostToolUseInput) input;
    String toolName = postToolUse.toolName();
    Object response = postToolUse.toolResponse();

    // Track usage
    toolUsage.computeIfAbsent(toolName, k -> new AtomicInteger(0)).incrementAndGet();

    // Detect errors
    if (String.valueOf(response).toLowerCase().contains("error")) {
        errors.incrementAndGet();
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

    client.connect("Echo 'Success!' and then read pom.xml first line");
    // ... process response
}

// Print statistics
System.out.println("Tool usage: " + toolUsage);
System.out.println("Errors detected: " + errors.get());
```

## PreToolUse vs PostToolUse

| Aspect | PreToolUse | PostToolUse |
|--------|------------|-------------|
| When | Before execution | After execution |
| Can block | Yes | No |
| Has result | No | Yes |
| Use for | Permission, logging | Metrics, auditing |

## Key Points

- PostToolUse hooks fire after tool execution completes
- Cannot block execution (tool already ran) - always return `HookOutput.allow()`
- Use for logging, metrics, error detection, and auditing
- Response format varies by tool type
- Use `AtomicInteger` for thread-safe statistics

<Note>
If Claude determines a command will fail, it may skip execution entirely. In that case, the PostToolUse hook won't fire because the tool was never called.
</Note>

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-16-hooks-posttooluse)

## Running the Example

```bash
mvn compile exec:java -pl module-16-hooks-posttooluse
```

## Next Module

[Module 17: Interrupt Handling](/claude-agent-sdk/tutorial/17-interrupt-handling) - Graceful shutdown during Claude execution.
