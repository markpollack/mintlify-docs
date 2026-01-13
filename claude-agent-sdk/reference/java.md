---
title: "Java API Reference"
sidebarTitle: "Java"
description: "Complete API reference for the Claude Agent SDK Java."
---

Complete API reference for the Claude Agent SDK Java, including all classes, methods, and types.

<Note>
This documentation follows the structure of the official Python SDK documentation.
</Note>

---

## Installation

### Maven

```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>claude-code-sdk</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'org.springaicommunity:claude-code-sdk:1.0.0-SNAPSHOT'
```

## Three-API Architecture

The Java SDK provides three ways to interact with Claude Code:

### Quick Comparison

| Feature | `Query` | `ClaudeSyncClient` | `ClaudeAsyncClient` |
| :------ | :------ | :----------------- | :------------------ |
| **Style** | Static methods | Iterator-based | Flux/Mono (Reactor) |
| **Session** | New each call | Reuses same session | Reuses same session |
| **Conversation** | Single exchange | Multi-turn | Multi-turn |
| **Hooks** | Not supported | Supported | Supported |
| **MCP Servers** | Not supported | Supported | Supported |
| **Permission Callbacks** | Not supported | Supported | Supported |
| **Use Case** | CLI tools, scripts | Traditional apps | Non-blocking apps |

Both `ClaudeSyncClient` and `ClaudeAsyncClient` provide the same capabilities. They differ only in programming paradigm (blocking vs non-blocking).

### When to Use `Query`

**Best for:**
- One-off questions where you don't need conversation history
- Simple automation scripts
- CLI tools and batch processing

### When to Use `ClaudeSyncClient`

**Best for:**
- Multi-turn conversations with context
- Tool interception with hooks
- MCP server integration
- Standard Java iteration patterns

### When to Use `ClaudeAsyncClient`

**Best for:**
- Spring WebFlux applications
- Server-Sent Events (SSE) streaming
- Non-blocking I/O requirements
- Reactive pipelines

---

## `Query` - One-Shot API

Creates a new session for each interaction. Provides static methods for simple queries.

### Methods

| Method | Return Type | Description |
| :----- | :---------- | :---------- |
| `text(prompt)` | `String` | Simple one-liner, returns text response |
| `text(prompt, options)` | `String` | With configuration options |
| `execute(prompt)` | `QueryResult` | Full result with metadata |
| `execute(prompt, options)` | `QueryResult` | With configuration options |
| `query(prompt)` | `Iterable<Message>` | Iterate over messages |
| `stream(prompt)` | `Stream<Message>` | Java Stream over messages |

### Example - Simple query

```java
import org.springaicommunity.claude.agent.sdk.Query;

// Simplest usage - one line
String answer = Query.text("What is 2+2?");
System.out.println(answer);  // "4"
```

### Example - With options

```java
import org.springaicommunity.claude.agent.sdk.Query;
import org.springaicommunity.claude.agent.sdk.QueryOptions;
import java.time.Duration;

QueryOptions options = QueryOptions.builder()
    .model("claude-sonnet-4-20250514")
    .appendSystemPrompt("Be concise")
    .timeout(Duration.ofMinutes(5))
    .build();

String response = Query.text("Explain Java", options);
System.out.println(response);
```

### Example - Full result with metadata

```java
import org.springaicommunity.claude.agent.sdk.Query;
import org.springaicommunity.claude.agent.sdk.types.QueryResult;

QueryResult result = Query.execute("Write a haiku about Java");

System.out.println(result.text().orElse(""));
System.out.println("Cost: $" + result.metadata().cost().calculateTotal());
System.out.println("Duration: " + result.metadata().getDuration().toMillis() + "ms");
System.out.println("Model: " + result.metadata().model());
```

### Example - Streaming iteration

```java
for (Message msg : Query.query("Explain recursion")) {
    if (msg instanceof AssistantMessage am) {
        am.getTextContent().ifPresent(System.out::print);
    }
}

// Or with Stream API
Query.stream("Explain recursion")
    .filter(msg -> msg instanceof AssistantMessage)
    .forEach(msg -> System.out.println(msg));
```

---

## `ClaudeClient` - Factory

Factory class for creating sync and async clients.

### Methods

| Method | Return Type | Description |
| :----- | :---------- | :---------- |
| `sync()` | `ClaudeSyncClient.Builder` | Create blocking client builder |
| `sync(options)` | `ClaudeSyncClient.Builder` | Create builder with pre-configured CLIOptions |
| `async()` | `ClaudeAsyncClient.Builder` | Create reactive client builder |
| `async(options)` | `ClaudeAsyncClient.Builder` | Create builder with pre-configured CLIOptions |

### Example - Factory pattern

```java
// Sync client with fluent builder
try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .model("claude-sonnet-4-20250514")
        .systemPrompt("You are helpful")
        .timeout(Duration.ofMinutes(5))
        .build()) {
    // Use client
}

// Async client
ClaudeAsyncClient client = ClaudeClient.async()
    .workingDirectory(Path.of("."))
    .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
    .build();
```

---

## `ClaudeSyncClient` - Blocking Client

Maintains a conversation session across multiple exchanges. Uses iterator-based message handling.

### Methods

| Method | Description |
| :----- | :---------- |
| `connect(prompt)` | Start session with initial prompt |
| `query(prompt)` | Send follow-up message |
| `receiveResponse()` | Get iterator over response messages |
| `interrupt()` | Stop current operation |
| `close()` | End session and release resources |

### Example - Multi-turn conversation

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.parsing.ParsedMessage;
import org.springaicommunity.claude.agent.sdk.types.AssistantMessage;
import java.util.Iterator;

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .build()) {

    // First turn
    client.connect("My favorite color is blue. Remember this.");
    Iterator<ParsedMessage> response = client.receiveResponse();
    while (response.hasNext()) {
        ParsedMessage msg = response.next();
        if (msg.isRegularMessage() && msg.asMessage() instanceof AssistantMessage am) {
            am.getTextContent().ifPresent(System.out::println);
        }
    }

    // Second turn - Claude remembers context
    client.query("What is my favorite color?");
    response = client.receiveResponse();
    while (response.hasNext()) {
        ParsedMessage msg = response.next();
        if (msg.isRegularMessage() && msg.asMessage() instanceof AssistantMessage am) {
            am.getTextContent().ifPresent(System.out::println);  // "blue"
        }
    }
}
```

### Example - With hooks

```java
import org.springaicommunity.claude.agent.sdk.hooks.HookRegistry;
import org.springaicommunity.claude.agent.sdk.hooks.HookInput;
import org.springaicommunity.claude.agent.sdk.hooks.HookOutput;

HookRegistry hookRegistry = new HookRegistry();

// Block dangerous commands
hookRegistry.registerPreToolUse("Bash", input -> {
    if (input instanceof HookInput.PreToolUseInput preToolUse) {
        String cmd = preToolUse.getArgument("command", String.class).orElse("");
        if (cmd.contains("rm -rf")) {
            return HookOutput.block("Dangerous command blocked");
        }
    }
    return HookOutput.allow();
});

// Log all tool results
hookRegistry.registerPostToolUse(input -> {
    if (input instanceof HookInput.PostToolUseInput postToolUse) {
        System.out.println("Tool completed: " + postToolUse.toolName());
    }
    return HookOutput.allow();
});

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .permissionMode(PermissionMode.DEFAULT)
        .hookRegistry(hookRegistry)
        .build()) {
    // Hooks intercept tool calls
}
```

### Example - With MCP servers

```java
import org.springaicommunity.claude.agent.sdk.mcp.McpServerConfig;

// External MCP server (subprocess)
McpServerConfig npmServer = McpServerConfig.command("npx")
    .args("-y", "@anthropic/mcp-server-filesystem")
    .env("HOME", System.getProperty("user.home"))
    .build();

// In-process SDK MCP server
McpServerConfig sdkServer = McpServerConfig.sdk(myMcpServer);

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .mcpServer("filesystem", npmServer)
        .mcpServer("custom", sdkServer)
        .build()) {
    // MCP tools available to Claude
}
```

---

## `ClaudeAsyncClient` - Reactive Client

Non-blocking reactive API using Project Reactor. Use when you need composable, non-blocking chains.

### Methods

| Method | Return Type | Description |
| :----- | :---------- | :---------- |
| `connect(prompt)` | `TurnSpec` | Start session with prompt |
| `query(prompt)` | `TurnSpec` | Send follow-up message |
| `onMessage(handler)` | `ClaudeAsyncClient` | Register message handler |
| `onResult(handler)` | `ClaudeAsyncClient` | Register result handler |
| `close()` | `Mono<Void>` | End session |

### TurnSpec Methods

| Method | Return Type | Description |
| :----- | :---------- | :---------- |
| `.text()` | `Mono<String>` | Collected text, enables flatMap chaining |
| `.textStream()` | `Flux<String>` | Streaming text |
| `.messages()` | `Flux<Message>` | All message types |

### Example - Text response with TurnSpec

```java
ClaudeAsyncClient client = ClaudeClient.async()
    .workingDirectory(Path.of("."))
    .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
    .build();

// Simple text response
client.connect("Hello!").text()
    .doOnSuccess(System.out::println)
    .subscribe();  // Non-blocking
```

### Example - Multi-turn with flatMap chaining

```java
// Elegant multi-turn via flatMap
client.connect("My favorite color is blue.").text()
    .flatMap(r1 -> client.query("What is my favorite color?").text())
    .doOnSuccess(System.out::println)  // "blue"
    .subscribe();
```

### Example - Text streaming

```java
// Stream text as it arrives
client.query("Explain recursion").textStream()
    .doOnNext(System.out::print)
    .subscribe();
```

### Example - Full message access

```java
// Access all message types for metadata
client.query("List files").messages()
    .doOnNext(msg -> {
        if (msg instanceof AssistantMessage am) {
            am.text().ifPresent(System.out::println);
        } else if (msg instanceof ResultMessage rm) {
            System.out.printf("Cost: $%.6f%n", rm.totalCostUsd());
        }
    })
    .subscribe();
```

### Example - Spring WebFlux SSE endpoint

```java
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {
    private final ClaudeAsyncClient client;

    public ChatController() {
        this.client = ClaudeClient.async()
            .workingDirectory(Path.of("."))
            .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
            .build();
    }

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message) {
        return client.query(message).textStream();
    }
}
```

### Example - Cross-turn handlers

```java
ClaudeAsyncClient client = ClaudeClient.async()
    .workingDirectory(Path.of("."))
    .build();

// Register handlers (cross-turn concerns)
client.onMessage(msg -> logger.info("Message: {}", msg.getClass().getSimpleName()))
      .onResult(result -> logger.info("Turn complete, tokens: {}",
          result.usage().inputTokens() + result.usage().outputTokens()));

// Handlers are called for ALL turns
client.queryAndReceive("First question").subscribe();
client.queryAndReceive("Second question").subscribe();  // Handlers still fire
```

### Flux vs Handlers

| If you are... | Use |
|--------------|-----|
| Printing or streaming output to a user | **Flux** |
| Building a WebFlux/SSE endpoint | **Flux** |
| Doing per-turn filtering or transformation | **Flux** |
| Logging tool usage across all turns | **Handlers** |
| Collecting metrics or tracing | **Handlers** |
| Running a long-lived agent loop | **Handlers** |

> "If the logic belongs to a *single turn*, use `Flux`.
> If the logic applies to the *entire session*, use handlers."

---

## Types

### `QueryOptions`

Configuration for Query operations.

```java
QueryOptions.builder()
    .model("claude-sonnet-4-20250514")      // Model selection
    .systemPrompt("Custom system prompt")    // Replace default
    .appendSystemPrompt("Additional instructions")  // Append to default
    .timeout(Duration.ofMinutes(5))          // Query timeout
    .allowedTools(List.of("Read", "Write"))  // Restrict tools
    .disallowedTools(List.of("Bash"))        // Block tools
    .maxTurns(10)                            // Limit agentic turns
    .maxBudgetUsd(1.0)                       // Cost limit
    .workingDirectory(Path.of("/project"))   // Working directory
    .build();
```

### `CLIOptions`

Lower-level configuration for CLI transport. Use with `ClaudeClient.sync(options)` or `ClaudeClient.async(options)`.

```java
CLIOptions options = CLIOptions.builder()
    .model("claude-sonnet-4-20250514")
    .systemPrompt("You are helpful")
    .allowedTools(List.of("Read", "Write"))
    .maxTurns(10)
    .build();

// When using pre-built CLIOptions, only session config is available on builder
try (ClaudeSyncClient client = ClaudeClient.sync(options)
        .workingDirectory(Path.of("."))
        .timeout(Duration.ofMinutes(5))
        .build()) {
    // .model() NOT available here - already in options
}
```

### `PermissionMode`

```java
public enum PermissionMode {
    DEFAULT,           // Standard permission behavior
    ACCEPT_EDITS,      // Auto-accept file edits
    PLAN,              // Planning mode - read-only
    BYPASS_PERMISSIONS // Bypass all checks (use with caution)
}
```

### `HookRegistry`

Register callbacks to intercept tool execution.

```java
HookRegistry registry = new HookRegistry();

// Pre-tool hook - runs before tool executes
registry.registerPreToolUse("Bash", input -> {
    // Return HookOutput.allow() or HookOutput.block("reason")
    return HookOutput.allow();
});

// Post-tool hook - runs after tool completes
registry.registerPostToolUse(input -> {
    // Log, trace, or modify behavior
    return HookOutput.allow();
});
```

### `McpServerConfig`

Configure MCP servers for tool integration.

```java
// External process (npx, docker, etc.)
McpServerConfig external = McpServerConfig.command("npx")
    .args("-y", "@anthropic/mcp-server-filesystem")
    .env("HOME", System.getProperty("user.home"))
    .build();

// In-process SDK server
McpServerConfig inProcess = McpServerConfig.sdk(myMcpServer);
```

---

## Message Types

### `ParsedMessage`

Sealed interface wrapping messages from the CLI.

```java
public sealed interface ParsedMessage permits
    ParsedMessage.RegularMessage,
    ParsedMessage.Control,
    ParsedMessage.ControlResponseMessage,
    ParsedMessage.EndOfStream {

    boolean isRegularMessage();   // User, Assistant, System, Result
    boolean isControlRequest();   // Control protocol request
    boolean isControlResponse();  // Control protocol response

    Message asMessage();          // Get as Message, or null
    ControlRequest asControlRequest();
}
```

### Processing Messages

```java
Iterator<ParsedMessage> response = client.receiveResponse();
while (response.hasNext()) {
    ParsedMessage parsed = response.next();
    if (parsed.isRegularMessage()) {
        Message message = parsed.asMessage();
        if (message instanceof AssistantMessage am) {
            System.out.println(am.getTextContent().orElse(""));
        } else if (message instanceof ResultMessage result) {
            System.out.println("Done. Cost: $" + result.totalCostUsd());
        }
    }
}
```

### `Message`

Base interface for regular messages.

```java
public sealed interface Message permits UserMessage, AssistantMessage, ResultMessage
```

### `AssistantMessage`

```java
public record AssistantMessage(
    List<ContentBlock> content,
    String model
) implements Message {
    public Optional<String> getTextContent();
}
```

### `ResultMessage`

```java
public record ResultMessage(
    String subtype,
    long durationMs,
    long durationApiMs,
    boolean isError,
    int numTurns,
    String sessionId,
    Double totalCostUsd,
    Usage usage,
    String result
) implements Message
```

---

## Content Block Types

```java
public sealed interface ContentBlock permits TextBlock, ThinkingBlock, ToolUseBlock, ToolResultBlock
```

### `TextBlock`

```java
public record TextBlock(String text) implements ContentBlock
```

### `ToolUseBlock`

```java
public record ToolUseBlock(
    String id,
    String name,
    Map<String, Object> input
) implements ContentBlock
```

---

## Error Types

```java
public class ClaudeSDKException extends RuntimeException {
    // Base exception for all SDK errors
}

public class TransportException extends ClaudeSDKException {
    // Raised when CLI transport fails
}

public class SessionClosedException extends ClaudeSDKException {
    // Raised when operating on a closed session
}

public class MessageParseException extends ClaudeSDKException {
    // Raised when message parsing fails
}
```

---

## See also

- [SDK overview](https://docs.anthropic.com/en/docs/agents-and-tools/claude-code/overview) - General SDK concepts
- [Python SDK reference](https://docs.anthropic.com/en/docs/agents-and-tools/claude-code/sdk-reference) - Python SDK documentation
- [Claude Agent SDK Java GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java) - Source code and examples
