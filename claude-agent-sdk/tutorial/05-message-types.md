# Module 05: Message Types

Understanding the message and content block type hierarchy.

## What You'll Learn

- The four message types: User, Assistant, System, Result
- Content block types inside Assistant messages
- Pattern matching with sealed interfaces
- Extracting text and tool use information

## Message Type Hierarchy

```
ParsedMessage (sealed)
├── RegularMessage → contains Message
│   ├── UserMessage      - Your input
│   ├── AssistantMessage - Claude's response (has content blocks)
│   ├── SystemMessage    - System context
│   └── ResultMessage    - Session completion metadata
├── Control              - Hook requests
├── ControlResponseMessage
└── EndOfStream          - Internal marker
```

## Content Block Types

Inside `AssistantMessage.content()`:

| Block Type | Description | Key Methods |
|------------|-------------|-------------|
| `TextBlock` | Regular text | `text()` |
| `ToolUseBlock` | Tool invocation | `id()`, `name()`, `input()` |
| `ToolResultBlock` | Tool result | `toolUseId()`, `content()` |
| `ThinkingBlock` | Reasoning (when enabled) | `thinking()` |

## Pattern Matching with Switch

```java
Message message = parsed.asMessage();

switch (message) {
    case AssistantMessage assistant -> {
        for (ContentBlock block : assistant.content()) {
            switch (block) {
                case TextBlock text ->
                    System.out.println(text.text());
                case ToolUseBlock tool ->
                    System.out.println("Tool: " + tool.name());
                case ToolResultBlock result ->
                    System.out.println("Result: " + result.toolUseId());
                case ThinkingBlock thinking ->
                    System.out.println("Thinking: " + thinking.thinking());
                default -> { }
            }
        }
    }
    case ResultMessage result -> {
        System.out.printf("Cost: $%.6f%n", result.totalCostUsd());
        System.out.printf("Duration: %d ms%n", result.durationMs());
    }
    case UserMessage user ->
        System.out.println("User: " + user.content());
    case SystemMessage system ->
        System.out.println("System: " + system.subtype());
    default -> { }
}
```

## ResultMessage Fields

| Field | Description |
|-------|-------------|
| `durationMs()` | Total time |
| `durationApiMs()` | API time only |
| `numTurns()` | Conversation turns |
| `sessionId()` | Session identifier |
| `totalCostUsd()` | Total cost |
| `usage()` | Token usage |
| `isError()` | Error flag |
| `result()` | Error message if isError |

## AssistantMessage Convenience Methods

```java
AssistantMessage assistant = ...;

// Get all text concatenated (empty string if none)
String text = assistant.text();

// Print directly - toString() returns the text
System.out.println(assistant);  // Same as assistant.text()

// Get first text block content (Optional)
Optional<String> firstText = assistant.getTextContent();

// Get all text blocks
List<TextBlock> textBlocks = assistant.getTextBlocks();

// Get all tool use blocks
List<ToolUseBlock> tools = assistant.getToolUses();

// Check if message has tool use
boolean hasTool = assistant.hasToolUse();
```

## Message toString() Methods

All message types have useful `toString()` implementations:

```java
for (Message msg : client.connectAndReceive("List files")) {
    System.out.println(msg);  // Works for all message types
}

// AssistantMessage: prints the text content
// ResultMessage: "[Result: cost=$0.001234, turns=3, session=abc123]"
// UserMessage: prints the user input
// SystemMessage: prints system info
```

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-05-message-types)

## Running the Example

```bash
mvn compile exec:java -pl module-05-message-types
```

## Next Module

[Module 06: CLI Options](/claude-agent-sdk/tutorial/06-cli-options) - Learn about fine-grained configuration of Claude's behavior.
