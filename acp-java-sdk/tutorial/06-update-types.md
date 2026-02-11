# Module 06: Update Types

Comprehensive coverage of all `SessionUpdate` types in ACP.

## What You'll Learn

- All seven `SessionUpdate` types and when they appear
- Pattern matching with Java's `switch` on sealed interfaces
- Building rich UIs that show agent activity

## The Code

The client registers a `sessionUpdateConsumer` and uses pattern matching to handle each type:

```java
AcpSyncClient client = AcpClient.sync(transport)
    .sessionUpdateConsumer(notification -> {
        SessionUpdate update = notification.update();
        switch (update) {
            case AgentMessageChunk msg ->
                System.out.print(((TextContent) msg.content()).text());
            case AgentThoughtChunk thought ->
                System.out.println("[Thought] " +
                    ((TextContent) thought.content()).text());
            case ToolCall tc ->
                System.out.println("[Tool] " + tc.title() +
                    " | " + tc.kind() + " | " + tc.status());
            case ToolCallUpdateNotification tcUpdate ->
                System.out.println("[ToolUpdate] " +
                    tcUpdate.toolCallId() + " -> " + tcUpdate.status());
            case Plan plan -> {
                System.out.println("[Plan] " + plan.entries().size() + " entries:");
                plan.entries().forEach(entry ->
                    System.out.println("  - " + entry.content() +
                        " [" + entry.status() + "]"));
            }
            case AvailableCommandsUpdate commands ->
                System.out.println("[Commands] " +
                    commands.availableCommands().size() + " available");
            case CurrentModeUpdate mode ->
                System.out.println("[Mode] " + mode.currentModeId());
            default -> {}
        }
    })
    .build();
```

## Update Types Reference

| Type | Content | Typical Use |
|------|---------|-------------|
| `AgentMessageChunk` | Incremental response text | Main output, streamed word by word |
| `AgentThoughtChunk` | Agent's thinking process | Show reasoning in a collapsible panel |
| `ToolCall` | Tool execution start | Show tool name, kind, and status |
| `ToolCallUpdateNotification` | Tool progress | Update status of in-progress tool |
| `Plan` | Agent's planned steps | Show step list with priorities and completion |
| `AvailableCommandsUpdate` | Slash commands | Populate command palette |
| `CurrentModeUpdate` | Mode change | Update UI mode indicator |

This module extends Module 05 by handling every update type rather than just messages.

## Source Code

[View on GitHub](https://github.com/markpollack/acp-java-tutorial/tree/main/module-06-update-types)

## Running the Example

```bash
export GEMINI_API_KEY=your-key-here
./mvnw exec:java -pl module-06-update-types
```

## Next Module

[Module 07: Agent Requests](/acp-java-sdk/tutorial/07-agent-requests) — handle file read/write requests from agents.
