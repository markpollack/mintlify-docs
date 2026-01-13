# Module 08: Permission Modes

Controlling how Claude handles tool execution permissions.

## What You'll Learn

- The four permission modes and when to use each
- How permission modes affect tool execution
- Best practices for production vs development

## Permission Modes

| Mode | CLI Flag | Use Case |
|------|----------|----------|
| `DEFAULT` | `--permission-mode default` | Interactive CLI, human approval |
| `ACCEPT_EDITS` | `--permission-mode acceptEdits` | Auto-approve edits, prompt for Bash |
| `BYPASS_PERMISSIONS` | `--permission-mode bypassPermissions` | Automated scripts, CI/CD |
| `DANGEROUSLY_SKIP_PERMISSIONS` | `--dangerously-skip-permissions` | Sandboxed environments only |

## Using Permission Modes

```java
import org.springaicommunity.claude.agent.sdk.ClaudeClient;
import org.springaicommunity.claude.agent.sdk.ClaudeSyncClient;
import org.springaicommunity.claude.agent.sdk.config.PermissionMode;
import java.nio.file.Path;

// BYPASS_PERMISSIONS - most common for automated scripts
try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .build()) {

    // Claude executes tools without prompting
    String answer = client.connectText("List files in the current directory");
    System.out.println(answer);
}
```

## Mode Details

### DEFAULT

```java
.permissionMode(PermissionMode.DEFAULT)
```

- Prompts for every tool use
- Best for interactive CLI sessions
- User must approve each operation

### ACCEPT_EDITS

```java
.permissionMode(PermissionMode.ACCEPT_EDITS)
```

- Auto-approves file editing tools (Read, Write, Edit)
- Still prompts for shell commands (Bash)
- Good balance of automation and safety

### BYPASS_PERMISSIONS

```java
.permissionMode(PermissionMode.BYPASS_PERMISSIONS)
```

- Skips all permission prompts
- Most common for SDK usage
- Use with `allowedTools`/`disallowedTools` for safety

### DANGEROUSLY_SKIP_PERMISSIONS

```java
.permissionMode(PermissionMode.DANGEROUSLY_SKIP_PERMISSIONS)
```

- Uses a different CLI flag (`--dangerously-skip-permissions`) than `BYPASS_PERMISSIONS` (`--permission-mode bypassPermissions`)
- Both skip permission prompts; the behavioral difference is minimal
- The explicit "dangerous" naming serves as a code-level warning to reviewers
- Intended for fully sandboxed environments (containers, VMs) without network access
- Use `BYPASS_PERMISSIONS` for most automated use cases; reserve this for true sandboxes

## Best Practices

| Environment | Recommended Mode | Additional Safety |
|-------------|-----------------|-------------------|
| Development | `BYPASS_PERMISSIONS` | None needed |
| CI/CD | `BYPASS_PERMISSIONS` | `disallowedTools(["Bash"])` |
| Production | `BYPASS_PERMISSIONS` | `allowedTools` to limit scope |
| Sandbox | `DANGEROUSLY_SKIP_PERMISSIONS` | Network isolation |

## Combining with Tool Permissions

For maximum safety, combine permission modes with tool restrictions:

```java
try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
        .allowedTools(List.of("Read", "Grep", "Glob"))  // Only these tools
        .build()) {

    // Claude can only read, not modify
    String answer = client.connectText("What's in the README?");
    System.out.println(answer);
}
```

## Tradeoffs

- **DEFAULT mode** provides safety but blocks automation - Claude waits for approval that never comes in non-interactive contexts.
- **BYPASS_PERMISSIONS** is required for automation but shifts responsibility to your code for safety (use tool restrictions).
- Permission modes are session-wide. You cannot require approval for some tools while bypassing others (use permission callbacks for fine-grained control - see Module 13).
- No audit log is generated when permissions are bypassed. Implement your own logging if needed.
- Claude may still refuse unsafe operations even with permissions bypassed, based on its training.

## Source Code

[View on GitHub](https://github.com/spring-ai-community/claude-agent-sdk-java-tutorial/tree/main/module-08-permission-modes)

## Running the Example

```bash
mvn compile exec:java -pl module-08-permission-modes
```

## Next Module

[Module 09: Structured Outputs](/claude-agent-sdk/tutorial/09-structured-outputs) - Get Claude to return structured JSON responses.
