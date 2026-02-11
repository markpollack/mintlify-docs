# Module 03: Sessions

Understanding session creation and lifecycle in ACP.

## What You'll Learn

- Creating sessions with `NewSessionRequest`
- Working directory context and context documents
- Multiple independent sessions

## The Code

```java
// Create a session with working directory context
var session = client.newSession(
    new NewSessionRequest("/workspace", List.of()));

System.out.println("Session ID: " + session.sessionId());
```

Sessions are workspaces for conversations. Each session:
- Has a unique ID assigned by the agent
- Has a working directory context (the `cwd` parameter)
- Can have context documents attached
- Is independent of other sessions

## Multiple Sessions

```java
// Each session is independent
var session1 = client.newSession(new NewSessionRequest("/project-a", List.of()));
var session2 = client.newSession(new NewSessionRequest("/project-b", List.of()));

// Prompts in session1 don't affect session2
client.prompt(new PromptRequest(session1.sessionId(),
    List.of(new TextContent("Analyze this project"))));
```

## Source Code

[View on GitHub](https://github.com/markpollack/acp-java-tutorial/tree/main/module-03-sessions)

## Running the Example

```bash
export GEMINI_API_KEY=your-key-here
./mvnw exec:java -pl module-03-sessions
```

## Next Module

[Module 04: Prompts](/acp-java-sdk/tutorial/04-prompts) — prompt requests and response handling in depth.
