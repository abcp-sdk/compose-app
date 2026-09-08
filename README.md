# agent-compose-app

Compose Multiplatform agent chat app. Shared Compose UI in `commonMain`
against a platform-injected `AgentPort`; the desktop (JVM) target wires the
port over `agent-sdk-kotlin`. Breakpoints agree with the other clients:
<600 = single column (drawer/bottom bar), >=600 = split, >=1024 = wide.

```
AGENT_BASE_URL=https://agent.example.com AGENT_TOKEN=devtoken gradle :desktopRun
```
