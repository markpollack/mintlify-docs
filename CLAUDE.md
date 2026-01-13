# Mintlify Docs - Claude Code Instructions

## CRITICAL: Code-First Workflow (MANDATORY)

**Never write documentation without working tutorial code first.**

### The Golden Rule

```
Python SDK Example → Java Tutorial Code → Integration Test → Documentation
```

1. **Read Python SDK example** in `~/anthropic/claude-agent-sdk-python/examples/` (source material)
2. **Write/update Java tutorial code** in `~/community/claude-agent-sdk-java-tutorial/module-XX-*/`
3. **Verify it compiles**: `mvn compile -pl module-XX-* -q`
4. **Run integration test**: `cd integration-testing && jbang RunIntegrationTest.java module-XX-*`
5. **THEN write docs** based on the working code

Python SDK examples are the canonical reference. Java tutorial ports the behavior. Documentation extracts from tutorials.

---

## CRITICAL: Agent Workflow (MANDATORY)

**DO NOT write documentation without using the agents.** This is not optional.

### Pre-Flight Checklist

Before writing ANY documentation in this repository:

- [ ] Have I spawned the `module-code-writer` agent? (REQUIRED to write new tutorial code)
- [ ] Have I spawned the `tutorial-code-sync` agent? (REQUIRED to sync existing code with docs)
- [ ] Have I spawned the `technical-writer` agent? (REQUIRED before writing docs)
- [ ] Have I spawned the `code-sample-adapter` agent? (REQUIRED if converting code)
- [ ] Have I spawned the `doc-reviewer` agent? (REQUIRED before marking complete)

If you cannot check all applicable boxes, STOP and use the agents first.

### Agent Usage

**0. Before Writing New Module Code (Write Code First):**
```
Task tool → subagent_type=general-purpose
Prompt: "You are the module-code-writer agent. Read ~/.claude/agents/module-code-writer.md for your guidelines. Implement module-XX-name following the Python SDK example..."
```

**0b. Before Any Documentation Work (Sync Existing Code):**
```
Task tool → subagent_type=general-purpose
Prompt: "You are the tutorial-code-sync agent. Read ~/.claude/agents/tutorial-code-sync.md for your guidelines. Sync module-XX with its documentation..."
```

**1. Before Writing Documentation:**
```
Task tool → subagent_type=general-purpose
Prompt: "You are the technical-writer agent. Read ~/.claude/agents/technical-writer.md for your guidelines. Then write [specific content]..."
```

**2. Before Converting Code Samples:**
```
Task tool → subagent_type=general-purpose
Prompt: "You are the code-sample-adapter agent. Read ~/.claude/agents/code-sample-adapter.md for your guidelines. Then convert [specific code]..."
```

**3. Before Marking Step Complete:**
```
Task tool → subagent_type=general-purpose
Prompt: "You are the doc-reviewer agent. Read ~/.claude/agents/doc-reviewer.md for your guidelines. Review [specific files]..."
```

---

## Java SDK API Architecture

This is a **generic Java SDK**, not a Spring-specific library. Avoid over-emphasizing Spring.

### Three APIs with Feature Parity

| API | Class | Programming Style | Best For |
|-----|-------|-------------------|----------|
| **One-shot** | `Query` | Static methods | Scripts, CLI tools, simple queries |
| **Blocking** | `ClaudeSyncClient` | Iterator-based | Traditional applications |
| **Reactive** | `ClaudeAsyncClient` | Flux/Mono | Non-blocking, high concurrency |

**Both `ClaudeSyncClient` and `ClaudeAsyncClient` support the SAME features:**
- Multi-turn conversations
- Hooks (PreToolUse, PostToolUse)
- MCP server integration
- Permission callbacks

They differ ONLY in programming paradigm (blocking vs non-blocking).

### Tutorial Guidelines

1. **Prefer ClaudeSyncClient** - More accessible to most Java developers
2. **Show BOTH clients for features** - Both support multi-turn, hooks, MCP, etc.
3. **Avoid Spring-specific framing** - This SDK works with any Java application

### CRITICAL: Show Both Paradigms Equivalently

**Never imply one client has features the other lacks.** When documenting capabilities:

- Use `<Tabs>` with "Blocking" and "Reactive" tabs
- Show the SAME functionality in both paradigms
- Don't segregate features by client type

**Wrong presentation:**
```
### Multi-Turn Conversation (sync example only)
### Reactive Streaming (async example only)
```
This falsely implies async can't do multi-turn!

**Correct presentation:**
```
### Multi-Turn Conversation
<Tabs>
  <Tab title="Blocking">ClaudeSyncClient example</Tab>
  <Tab title="Reactive">ClaudeAsyncClient doing the SAME thing</Tab>
</Tabs>
```

### CRITICAL: Sync vs Async Client Positioning

- **Sync client**: blocking, simple sequential code
- **Async client**: reactive, composable, non-blocking chains

**Sync pattern:**
```java
try (ClaudeSyncClient client = ClaudeClient.sync()...) {
    String r1 = client.connectText("Hello");
    String r2 = client.queryText("Follow up");
}
```

**Async pattern (TurnSpec with flatMap):**
```java
client.connect("Hello").text()
    .flatMap(r1 -> client.query("Follow up").text())
    .doOnSuccess(System.out::println)
    .subscribe();  // Non-blocking
```

**TurnSpec methods:**
- `.text()` → `Mono<String>` - collected text, enables flatMap chaining
- `.textStream()` → `Flux<String>` - streaming text
- `.messages()` → `Flux<Message>` - all message types

### CRITICAL: Example Quality Standards

**DO NOT write incomplete examples.** Every code sample must be:

1. **Complete and runnable** - No `// process response here` stubs
2. **Self-contained** - Show helper methods inline, not as "exercise for the reader"
3. **Easy to grok** - Reader should understand what's happening without guessing

**Reactive Examples Must Be a "Wow Moment":**

The reactive/fluent API is a highlight. Examples should:
- Read almost like prose
- Show elegance via flatMap chaining
- Use `.subscribe()` (non-blocking), avoid `.block()`
- **NO `doFinally` cleanup noise** - client lifecycle is separate from request lifecycle

**Good reactive "wow":**
```java
// Multi-turn with elegant flatMap chaining
client.connect("My favorite color is blue.").text()
    .flatMap(r1 -> client.query("What is my favorite color?").text())
    .doOnSuccess(System.out::println)  // "blue"
    .subscribe();  // Non-blocking

// Text streaming
client.query("Write a haiku").textStream()
    .doOnNext(System.out::print)
    .subscribe();
```

**Avoid reactive "gobbldy-gook":**
```java
// DON'T: thenMany chaining, block(), doFinally cleanup
client.connectText("Hello")
    .then()
    .thenMany(client.queryText("Follow up"))
    .doFinally(signal -> client.close().subscribe())  // ← Noise!
    .blockLast();  // ← Defeats reactive purpose
```

### CRITICAL: Spring WebFlux Warning

**NEVER mention "Spring WebFlux"** unless the tutorial specifically covers web tier integration.

- This is a generic Java SDK, not Spring-specific
- The reactive API uses Project Reactor, which works anywhere

---

## Validation

Before committing:
```bash
mintlify dev --port 3000    # Check MDX validity and parsing errors
mintlify broken-links       # Check for broken links
```

Watch for parsing errors like:
```
parsing error ./path/to/file.md:130:22 - Could not parse expression with acorn
```

These indicate MDX parsing failures that must be fixed.

---

## MDX Parsing Gotchas

Mintlify uses MDX which parses markdown as JSX. This causes issues with certain patterns:

### CRITICAL: No Nested Code Blocks

**NEVER nest markdown code fences inside code blocks.** The MDX parser gets confused.

**WRONG - causes parsing error:**
````java
String prompt = """
    Review this code:

    ```java
    public class Example { }
    ```

    Provide feedback.
    """;
````

The inner triple backticks close the outer code block prematurely.

**CORRECT - remove inner fences:**
```java
String prompt = """
    Review this code:

    public class Example { }

    Provide feedback.
    """;
```

### Angle Brackets in Code

The `<` character in code blocks is usually fine, but can cause issues in edge cases. If you see parsing errors mentioning "acorn" or "expression", check for:
- Nested code blocks (most common)
- Unclosed angle brackets outside code blocks
- JSX-like syntax in prose text

## Key Files

| File | Purpose |
|------|---------|
| `plans/ROADMAP.md` | **Actionable implementation roadmap with agent commands** |
| `plans/java-sdk-docs-plan.md` | Master documentation plan |
| `plans/learnings/STEP-*.md` | Learning docs from each step |
| `~/.claude/agents/module-code-writer.md` | Agent for writing new tutorial modules |
| `~/.claude/agents/tutorial-code-sync.md` | Agent for syncing code with docs |
| `~/.claude/agents/technical-writer.md` | Agent for writing documentation |
| `~/.claude/agents/code-sample-adapter.md` | Agent for converting code samples |
| `~/.claude/agents/doc-reviewer.md` | Agent for reviewing documentation |
| `~/anthropic/claude-agent-sdk-python/examples/` | Python SDK examples (source material) |
| `~/community/claude-agent-sdk-java-tutorial/` | Java tutorial code repository |
| `~/community/claude-agent-sdk-java-tutorial/doc-fragments/` | Compilable code samples |
