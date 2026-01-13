# Module 07: Tool Permissions

Controlling which tools Claude can use.

## What You'll Learn

- `allowedTools`: Explicitly allow specific tools (only these are available)
- `disallowedTools`: Block specific tools (all others remain available)
- Common Claude Code tools
- When to use each approach

## Claude Code Tools

Claude Code has access to these built-in tools:

| Tool | Description | Risk Level |
|------|-------------|------------|
| `Read` | Read files | Low |
| `Glob` | Find files by pattern | Low |
| `Grep` | Search file contents | Low |
| `Write` | Create/overwrite files | Medium |
| `Edit` | Modify existing files | Medium |
| `Bash` | Execute shell commands | High |
| `WebSearch` | Search the web | Low |
| `WebFetch` | Fetch web page content | Low |

## Approach 1: Allowed Tools

Claude can ONLY use tools in this list. Use for maximum control:

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import org.springaicommunity.claude.agent.sdk.transport.CLIOptions;
import java.nio.file.Path;
import java.util.List;

try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .model(CLIOptions.MODEL_HAIKU)
        .allowedTools(List.of("Read", "Grep"))  // ONLY these tools
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .build()) {

    // Claude can read files and search, but cannot write or execute
    String answer = client.connectText("What files are in the current directory?");
    System.out.println(answer);
}
```

## Approach 2: Disallowed Tools

All tools are available EXCEPT these. Use to block specific dangerous operations:

```java
try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .model(CLIOptions.MODEL_HAIKU)
        .disallowedTools(List.of("Bash", "Write", "Edit"))  // Block these
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .build()) {

    // Claude can read files but cannot execute commands or modify files
    String answer = client.connectText("Read the pom.xml and tell me the project name.");
    System.out.println(answer);
}
```

## Which Approach to Use?

| Scenario | Approach | Example |
|----------|----------|---------|
| Read-only agent | `allowedTools` | `List.of("Read", "Glob", "Grep")` |
| General agent, no shell | `disallowedTools` | `List.of("Bash")` |
| File editing only | `allowedTools` | `List.of("Read", "Write", "Edit", "Glob")` |
| Research agent | `allowedTools` | `List.of("WebSearch", "WebFetch", "Read")` |

## Common Tool Combinations

### Read-Only Agent

```java
.allowedTools(List.of("Read", "Glob", "Grep"))
```

### Safe Editing Agent

```java
.allowedTools(List.of("Read", "Write", "Edit", "Glob", "Grep"))
```

### Research Agent

```java
.allowedTools(List.of("WebSearch", "WebFetch", "Read", "Write"))
```

## Key Points

- `allowedTools` is more restrictive - use for sensitive environments
- `disallowedTools` is more permissive - use when you just want to block specific tools
- Tool permissions are enforced at the CLI level, not just in your code

<Warning>
**Do not combine allowedTools and disallowedTools.** When `allowedTools` is set, only those tools are available. Adding `disallowedTools` has no effect since unlisted tools are already blocked. Choose one approach.
</Warning>

## Tradeoffs

- **allowedTools**: More secure but requires knowing all tools needed upfront. New Claude Code tools added in future updates will be blocked by default.
- **disallowedTools**: More flexible but less secure. New tools added in future Claude Code versions will be allowed by default.
- **Neither**: All tools available. Only appropriate when running in a sandboxed environment.
- Tool names are case-sensitive and must match exactly (e.g., `"Bash"` not `"bash"`).
- Invalid tool names fail silently - no error if you typo a tool name.

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-07-tool-permissions)

## Running the Example

```bash
mvn compile exec:java -pl module-07-tool-permissions
```

## Next Module

[Module 08: Permission Modes](/claude-agent-sdk/tutorial/08-permission-modes) - Control how Claude handles tool execution permissions.
