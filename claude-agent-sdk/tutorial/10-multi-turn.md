# Module 10: Multi-Turn Conversations

Building conversational applications with context preservation.

## What You'll Learn

- Maintaining context across multiple exchanges
- The query/response pattern for conversations
- How Claude remembers previous messages in a session

## Single-Turn vs Multi-Turn

| Pattern | API | Context |
|---------|-----|---------|
| Single-turn | `Query.text()` | No memory between calls |
| Multi-turn | `ClaudeSyncClient` or `ClaudeAsyncClient` | Remembers all previous messages |

Both clients support multi-turn conversations with full context preservation. Choose based on your programming paradigm.

## The Conversation Loop Pattern

<Tabs>
  <Tab title="Blocking">
```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import java.nio.file.Path;

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .model(CLIOptions.MODEL_HAIKU)
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .build()) {

    // Turn 1: Establish context
    String response1 = client.connectText("My favorite programming language is Java.");
    System.out.println(response1);

    // Turn 2: Claude remembers the context from Turn 1
    String response2 = client.queryText("What is my favorite programming language?");
    System.out.println(response2);  // "Java"

    // Turn 3: Continue building on context
    String response3 = client.queryText("Spell it backwards.");
    System.out.println(response3);  // "avaJ"
}
```
  </Tab>
  <Tab title="Reactive">
```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeAsyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import java.nio.file.Path;

ClaudeAsyncClient client = ClaudeClient.async()
    .workingDirectory(Path.of("."))
    .model(CLIOptions.MODEL_HAIKU)
    .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
    .build();

// Multi-turn with elegant flatMap chaining
client.connect("My favorite programming language is Java.").text()
    .doOnSuccess(System.out::println)
    .flatMap(r1 -> client.query("What is my favorite programming language?").text())
    .doOnSuccess(System.out::println)  // "Java"
    .flatMap(r2 -> client.query("Spell it backwards.").text())
    .doOnSuccess(System.out::println)  // "avaJ"
    .subscribe();  // Non-blocking
```
  </Tab>
</Tabs>

The pattern differs by paradigm: blocking uses `connectText()`/`queryText()`, reactive uses `connect().text()`/`query().text()` with `flatMap` chaining. Context is preserved across all turns in both.

## How It Works

```
┌──────────────────────────────────────────────────────────┐
│                    Claude CLI Session                     │
├──────────────────────────────────────────────────────────┤
│  Turn 1: "My favorite language is Java"                  │
│    → Claude: "Great choice!"                             │
│                                                          │
│  Turn 2: "What is my favorite language?"                 │
│    → Claude: "Your favorite is Java"                     │
│         (remembers from Turn 1)                          │
│                                                          │
│  Turn 3: "Spell it backwards"                            │
│    → Claude: "avaJ"                                      │
│         (remembers Java from context)                    │
└──────────────────────────────────────────────────────────┘
```

## Full Message Access

When you need metadata, tool use details, or cost information:

```java
import org.springaicommunity.claude.agent.sdk.types.Message;
import org.springaicommunity.claude.agent.sdk.types.AssistantMessage;
import org.springaicommunity.claude.agent.sdk.types.ResultMessage;

for (Message msg : client.connectAndReceive("Create a test file")) {
    System.out.println(msg);  // All types have useful toString()

    if (msg instanceof AssistantMessage am) {
        am.getToolUses().forEach(tool ->
            System.out.println("Tool used: " + tool.name()));
    } else if (msg instanceof ResultMessage rm) {
        System.out.printf("Cost: $%.6f%n", rm.totalCostUsd());
    }
}
```

Use `connectAndReceive()` and `queryAndReceive()` when you need access to all message types.

## Key Points

- Both `ClaudeSyncClient` and `ClaudeAsyncClient` support multi-turn conversations
- `connectText(prompt)` starts the session and returns the text response
- `queryText(prompt)` sends follow-ups and returns the text response
- `connectAndReceive()`/`queryAndReceive()` for full message access
- Context persists until the client is closed

## When to Use Multi-Turn

| Use Case | Why Multi-Turn |
|----------|----------------|
| Chatbots | User expects continuous conversation |
| Iterative tasks | "Now do X with that" references prior output |
| Guided workflows | Step-by-step with context from previous steps |
| Debugging | "Why did you do that?" requires memory |

## Tradeoffs

- **Memory growth**: Context accumulates with each turn. Long conversations consume more tokens and increase latency as Claude re-processes the full history.
- **No partial context**: You cannot selectively forget messages. The entire conversation history persists until the session closes.
- **Session coupling**: The client must remain open for the conversation to continue. Network interruptions or process crashes lose the session.
- **Cost implications**: Each turn includes all prior messages in the API call. A 20-turn conversation sends the full history 20 times.

<Warning>
Avoid multi-turn for simple, independent queries. Use `Query.text()` when context is unnecessary.
</Warning>

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-10-multi-turn)

## Running the Example

```bash
mvn compile exec:java -pl module-10-multi-turn
```

## Next Module

[Module 11: Session Resume](/claude-agent-sdk/tutorial/11-session-resume) - Continuing conversations across restarts.
