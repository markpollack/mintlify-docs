# Module 06: CLI Options

Configuring Claude clients with the fluent builder and CLIOptions.

## What You'll Learn

- Two approaches to configuration: fluent builder vs CLIOptions
- Common configuration options (model, system prompt, timeout, limits)
- Model ID constants for convenience
- When to use each approach

## Two Configuration Approaches

| Approach | Entry Point | Best For |
|----------|-------------|----------|
| **Fluent Builder** | `ClaudeClient.sync()` | One-off configurations |
| **CLIOptions** | `ClaudeClient.sync(options)` | Shared/reusable config |

## Approach 1: Fluent Builder

Configure everything inline with method chaining:

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import java.nio.file.Path;
import java.time.Duration;

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .model(CLIOptions.MODEL_HAIKU)                      // Fast model
        .appendSystemPrompt("Be concise. Answer in one sentence.")  // Add to defaults
        .timeout(Duration.ofMinutes(2))                     // Operation timeout
        .maxTurns(5)                                        // Limit turns
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)  // Skip prompts
        .build()) {

    String answer = client.connectText("What is Java?");
    System.out.println(answer);
}
```

## Approach 2: Pre-built CLIOptions

Build options separately, then pass to client:

```java
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;

// Build options separately (could load from config file)
CLIOptions options = CLIOptions.builder()
        .model(CLIOptions.MODEL_HAIKU)
        .appendSystemPrompt("Answer like a pirate.")  // Add to defaults
        .maxTokens(500)                               // Limit response length
        .maxBudgetUsd(0.10)                           // Limit cost to 10 cents
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .build();

// Pass pre-built options - only session config available now
try (ClaudeSyncClient client = ClaudeClient.sync(options)
        .workingDirectory(Path.of("."))
        .timeout(Duration.ofMinutes(1))
        // Note: .model() is NOT available here - already in options!
        .build()) {

    String answer = client.connectText("What is the best programming language?");
    System.out.println(answer);
}
```

## Model ID Constants

The SDK provides constants for model IDs:

| Constant | Model | Description |
|----------|-------|-------------|
| `CLIOptions.MODEL_HAIKU` | claude-haiku-4-5-* | Fast and cost-effective |
| `CLIOptions.MODEL_SONNET` | claude-sonnet-4-5-* | Balanced performance |
| `CLIOptions.MODEL_OPUS` | claude-opus-4-5-* | Most capable |

## Configuration Options Reference

### Model Configuration

| Option | Type | Description |
|--------|------|-------------|
| `model` | `String` | Claude model to use |
| `appendSystemPrompt` | `String` | Add to default system prompt (recommended) |
| `systemPrompt` | `String` | Replace default system prompt (use with caution) |
| `maxTokens` | `Integer` | Maximum response tokens |
| `maxThinkingTokens` | `Integer` | Extended thinking tokens |

### Limits and Budget

| Option | Type | Description |
|--------|------|-------------|
| `maxTurns` | `Integer` | Maximum conversation turns |
| `maxBudgetUsd` | `Double` | Maximum cost in USD |
| `timeout` | `Duration` | Operation timeout |

### Session Configuration

| Option | Type | Description |
|--------|------|-------------|
| `workingDirectory` | `Path` | Where Claude operates (required) |
| `claudePath` | `String` | Custom Claude CLI path |
| `hookRegistry` | `HookRegistry` | Tool interception callbacks |

## Tradeoffs

- **Fluent builder vs CLIOptions**: Fluent builder is more discoverable but options cannot be reused. CLIOptions can be shared across clients or loaded from configuration.
- **maxTokens**: Setting too low truncates responses mid-sentence. Setting too high increases latency and cost. Start without limits, then tune based on actual usage.
- **timeout**: Claude may need several minutes for complex multi-tool tasks. Short timeouts cause failures that appear as connection errors.
- **Model selection**: Haiku is 10-20x cheaper than Opus but may require more specific prompts for complex tasks.

<Warning>
**systemPrompt vs appendSystemPrompt**: Using `systemPrompt()` replaces Claude Code's default instructions, which may affect tool usage behavior. Always prefer `appendSystemPrompt()` to add constraints without losing defaults. Only use `systemPrompt()` when you explicitly want to replace all default instructions.
</Warning>

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-06-cli-options)

## Running the Example

```bash
mvn compile exec:java -pl module-06-cli-options
```

## Next Module

[Module 07: Tool Permissions](/claude-agent-sdk/tutorial/07-tool-permissions) - Control which tools Claude can use.
