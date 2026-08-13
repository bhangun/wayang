# wayang-tool-nono

Capability-based sandboxing for Wayang tools using [nono](https://github.com/nolabs-ai/nono) — a Rust library that wraps OS-native sandbox primitives (macOS Seatbelt, Linux Landlock).

---

## Overview

`wayang-tool-nono` provides two complementary execution modes:

| Mode | Class | Use when |
|---|---|---|
| **Subprocess isolation** | `NonoProcessExecutor` | Main Wayang JVM must stay unrestricted |
| **Direct sandbox** | `NonoSandbox.apply()` | Child JVM / standalone tool only |

> ⚠️ `NonoSandbox.apply()` is **irreversible** for the calling OS process. In the main Wayang platform always use `NonoProcessExecutor`, which forks a child JVM (`NonoSubprocessLauncher`) that applies the sandbox and exits.

---

## Architecture

```
┌─────────────────────────────────┐
│  Wayang Platform JVM (unsandboxed) │
│                                 │
│  NonoSandboxToolExecutor        │
│     │ builds NonoSandboxConfig  │
│     │ from ToolInvocation params│
│     ▼                           │
│  NonoProcessExecutor            │
│     │ forks child JVM           │
└─────┼───────────────────────────┘
      │ stdin: LaunchRequest JSON
      ▼
┌─────────────────────────────────┐
│  Child JVM (sandboxed)          │
│                                 │
│  NonoSubprocessLauncher.main()  │
│    1. Parse LaunchRequest JSON  │
│    2. Configure NonoSandbox     │
│    3. nono_sandbox_apply() ◄── IRREVERSIBLE │
│    4. ProcessBuilder → command  │
│    5. Return LaunchResult JSON  │
└─────────────────────────────────┘
```

---

## Full FFM API Coverage

The `NonoSandbox` class binds the complete `nono.h` C FFI surface:

### Filesystem Capabilities
| Method | nono.h function |
|---|---|
| `allowPath(path, mode)` | `nono_capability_set_allow_path` |
| `allowFile(path, mode)` | `nono_capability_set_allow_file` |
| `isPathCovered(path)` | `nono_capability_set_path_covered` |
| `getFsCapabilityCount()` | `nono_capability_set_fs_count` |
| `deduplicate()` | `nono_capability_set_deduplicate` |

### Network Capabilities
| Method | nono.h function |
|---|---|
| `setNetworkMode(mode)` | `nono_capability_set_set_network_mode` |
| `setNetworkBlocked(blocked)` | `nono_capability_set_set_network_blocked` |
| `setProxyPort(port)` | `nono_capability_set_set_proxy_port` |
| `getNetworkMode()` | `nono_capability_set_network_mode` |
| `getProxyPort()` | `nono_capability_set_proxy_port` |
| `isNetworkBlocked()` | `nono_capability_set_is_network_blocked` |

### Command Filtering
| Method | nono.h function |
|---|---|
| `allowCommand(cmd)` | `nono_capability_set_allow_command` |
| `blockCommand(cmd)` | `nono_capability_set_block_command` |

### Permission Queries (safe — does not apply sandbox)
| Method | nono.h function |
|---|---|
| `isPathAllowed(path, mode)` | `nono_query_context_query_path` |
| `isNetworkAllowed()` | `nono_query_context_query_network` |

### State Serialization
| Method | nono.h function |
|---|---|
| `toJson()` | `nono_sandbox_state_from_caps` + `nono_sandbox_state_to_json` |
| `NonoSandbox.fromJson(json)` | `nono_sandbox_state_from_json` + `nono_sandbox_state_to_caps` |

### Utility
| Method | nono.h function |
|---|---|
| `getSummary()` | `nono_capability_set_summary` |
| `NonoSandbox.isSupported()` | `nono_sandbox_is_supported` |
| `NonoSandbox.version()` | `nono_version` |

---

## Usage

### 1. Subprocess-isolated execution (recommended)

```java
NonoSandboxConfig config = new NonoSandboxConfig()
    .addAllowedPath("/workspace/agent", "READ_WRITE")
    .addBlockedCommand("curl")
    .addBlockedCommand("wget")
    .withNetworkMode(NonoNetworkMode.BLOCKED);

NonoProcessExecutor executor = new NonoProcessExecutor(config);
SandboxExecutionResult result = executor.execute(
    "mvn test",
    "/workspace/agent",
    120_000
);
System.out.println("Exit: " + result.exitCode());
System.out.println("Output: " + result.stdout());
```

### 2. Via ToolInvocation params (auto-extracted by `NonoSandboxToolExecutor`)

```json
{
  "command": "pytest tests/",
  "timeout_seconds": 60,
  "__sandbox_allowed_paths": ["/workspace/agent"],
  "__sandbox_blocked_commands": ["curl", "wget", "nc"],
  "__sandbox_network_mode": "BLOCKED"
}
```

### 3. Direct sandbox (child JVM / test only)

```java
// CAUTION: Irreversible for this process!
try (NonoSandbox sandbox = new NonoSandbox()) {
    sandbox.allowPath("/tmp/work", NonoAccessMode.READ_WRITE);
    sandbox.setNetworkMode(NonoNetworkMode.BLOCKED);
    sandbox.blockCommand("curl");
    sandbox.apply(); // ← irreversible
    // All code after this point runs under sandbox restrictions
}
```

---

## Build

The Rust `nono_ffi` shared library is built automatically during `mvn compile` via `exec-maven-plugin`:

```bash
mvn clean install -pl Families/wayang/modules/tool/wayang-tool-nono
```

Prerequisites:
- Rust 1.94+ (`rustup install 1.94`)
- Java 22+

The resulting native library is copied to `target/classes/` and bundled with the JAR.

---

## SandboxProvider Configuration

When using the `SandboxProvider` SPI, extended Nono-specific options can be set via the `SandboxConfiguration` environment variable map:

| Key | Values | Default |
|---|---|---|
| `nono.network.mode` | `BLOCKED`, `ALLOW_ALL`, `PROXY_ONLY` | `BLOCKED` |
| `nono.network.proxy.port` | integer port number | — |
| `nono.commands.blocked` | comma-separated list | — |
| `nono.commands.allowed` | comma-separated list | — |

---

## CI/CD

The [nono-build.yml](../../.github/workflows/nono-build.yml) workflow:
- Builds the Rust `nono_ffi` library on both Ubuntu and macOS
- Runs the Java test suite on both platforms
- Uploads test reports as artifacts

---

## Module Structure

```
wayang-tool-nono/
├── src/main/java/tech/kayys/wayang/tool/nono/
│   ├── NonoSandbox.java              # Full FFM bindings for nono.h
│   ├── NonoSandboxConfig.java        # Config POJO + LaunchRequest/Result
│   ├── NonoSandboxProvider.java      # Sandbox SPI provider (CDI bean)
│   ├── NonoSandboxToolExecutor.java  # ToolExecutor decorator (subprocess isolation)
│   ├── NonoSubprocessLauncher.java   # Child JVM entry point
│   ├── NonoProcessExecutor.java      # Forks child JVM for isolated execution
│   ├── NonoAccessMode.java           # READ / WRITE / READ_WRITE enum
│   ├── NonoNetworkMode.java          # BLOCKED / ALLOW_ALL / PROXY_ONLY enum
│   └── NonoException.java            # Unchecked exception for FFM errors
└── src/test/java/...
    └── NonoSandboxTest.java          # Full capability test suite
```
