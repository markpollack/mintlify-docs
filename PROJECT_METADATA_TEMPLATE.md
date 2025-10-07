# Project Metadata Template

This document defines the standard metadata and resource links for all Spring AI Community projects.

## Minimalist Design Pattern

### Top of Page (After Title/Description)

Use badge-style links right after the status badge for quick access:

```mdx
<img src="https://img.shields.io/badge/Status-Production-green" />

[GitHub](https://github.com/spring-ai-community/{project}) •
[Docs](https://spring-ai-community.github.io/{project}/) •
[Maven](https://central.sonatype.com/artifact/...)
```

**For projects without published docs:**
```mdx
<img src="https://img.shields.io/badge/Status-Incubating-blue" />

[GitHub](https://github.com/spring-ai-community/{project}) •
[Maven](https://central.sonatype.com/artifact/...)
```

### Bottom Resources Section (Optional - Only if 3+ links)

Keep minimal - only add if there are meaningful additional resources:

```mdx
## Resources

<CardGroup cols={2}>
  <Card
    title="GitHub Repository"
    icon="github"
    href="https://github.com/spring-ai-community/{project-name}"
  >
    Source code and issues
  </Card>
  <Card
    title="Documentation"
    icon="book"
    href="https://spring-ai-community.github.io/{project-name}/"
  >
    Complete reference docs
  </Card>
</CardGroup>
```

**Rules:**
- Skip Resources section if all links are already at the top
- Only include Resources if there are examples, tutorials, or additional materials
- Keep it to 2-4 cards maximum

## Project Metadata by Project

### Production Projects

#### Moonshot
- **GitHub**: `https://github.com/spring-ai-community/moonshot`
- **Docs**: README only (no GitHub Pages yet)
- **Maven**: `org.springframework.ai.moonshot:moonshot-core`
- **Issue**: #3

#### QianFan
- **GitHub**: `https://github.com/spring-ai-community/qianfan`
- **Docs**: README only (no GitHub Pages yet)
- **Maven**: `org.springframework.ai.qianfan:qianfan-core`
- **Issue**: #2

### Incubating Projects

#### Spring AI Agents
- **GitHub**: `https://github.com/spring-ai-community/spring-ai-agents`
- **Docs**: `https://spring-ai-community.github.io/spring-ai-agents/`
- **Maven**: `org.springaicommunity.agents:spring-ai-starter-agent`
- **Issue**: TBD

#### Spring AI Bench
- **GitHub**: `https://github.com/spring-ai-community/spring-ai-bench`
- **Docs**: `https://spring-ai-community.github.io/spring-ai-bench/`
- **Maven**: `org.springaicommunity.bench:bench-core`
- **Issue**: TBD

#### MCP Annotations
- **GitHub**: `https://github.com/spring-ai-community/mcp-annotations`
- **Docs**: README only
- **Maven**: `org.springaicommunity:mcp-annotations`
- **Issue**: #6

#### MCP Security
- **GitHub**: `https://github.com/spring-ai-community/mcp-security`
- **Docs**: README only
- **Maven**: TBD (incubating)
- **Issue**: TBD

#### Spring AI Playground
- **GitHub**: `https://github.com/JM-Lab/spring-ai-playground` (original)
- **Docs**: README only
- **Maven**: N/A (self-hosted app)
- **Issue**: #14

#### Spring AG-UI
- **GitHub**: `https://github.com/workm8/ag-ui-4j` (original)
- **Docs**: `https://docs.ag-ui.com/`
- **Maven**: TBD
- **Issue**: #15

#### Spring AI Vaadin
- **GitHub**: `https://github.com/spring-ai-community/spring-ai-vaadin`
- **Docs**: README only
- **Maven**: TBD
- **Issue**: #5

## Quick Reference

### Projects with Published GitHub Pages
- ✅ Spring AI Agents (`spring-ai-community.github.io/spring-ai-agents`)
- ✅ Spring AI Bench (`spring-ai-community.github.io/spring-ai-bench`)

### Projects with Docs Built (Not Yet Published)
- 📝 Moonshot (Antora docs built, awaiting GitHub Pages publish)
- 📝 QianFan (Antora docs built, awaiting GitHub Pages publish)

### Projects with External Docs
- ✅ Spring AG-UI (`docs.ag-ui.com`)

### Projects with README only
- MCP Annotations
- MCP Security
- Spring AI Playground
- Spring AI Vaadin
