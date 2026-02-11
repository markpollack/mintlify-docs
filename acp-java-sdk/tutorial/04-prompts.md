# Module 04: Prompts

Deep dive into prompt requests and response handling.

## What You'll Learn

- `PromptRequest` structure — session ID and content list
- `PromptResponse` and `StopReason` values
- Content types for prompts

## The Code

```java
// Send a prompt with text content
var response = client.prompt(new PromptRequest(
    session.sessionId(),
    List.of(new TextContent("Explain ACP in one sentence."))
));

System.out.println("Stop reason: " + response.stopReason());
```

## Stop Reasons

The `PromptResponse` includes a `StopReason` that tells you why the agent finished:

| StopReason | Description |
|------------|-------------|
| `END_TURN` | Agent finished responding normally |
| `MAX_TOKENS` | Token limit reached |
| `REFUSAL` | Agent refused the request |
| `CANCELLED` | Prompt was cancelled by the client |

Understanding stop reasons helps handle different agent behaviors. `END_TURN` is the normal case. `MAX_TOKENS` means the response was truncated. `REFUSAL` may require rephrasing the prompt.

## Source Code

[View on GitHub](https://github.com/markpollack/acp-java-tutorial/tree/main/module-04-prompts)

## Running the Example

```bash
export GEMINI_API_KEY=your-key-here
./mvnw exec:java -pl module-04-prompts
```

## Next Module

[Module 05: Streaming Updates](/acp-java-sdk/tutorial/05-streaming-updates) — receive real-time updates during prompt processing.
