# Wayang Installation Scripts

This directory contains installation scripts and templates for distributing Wayang.

## Installation Methods

### 1. curl/SSL (Universal)

```bash
# Install latest version
curl -fsSL https://raw.githubusercontent.com/bhangun/wayang/main/scripts/install.sh | bash

# Install specific version
curl -fsSL https://raw.githubusercontent.com/bhangun/wayang/main/scripts/install.sh | bash -s -- -v v1.0.0

# Install to custom directory
curl -fsSL https://raw.githubusercontent.com/bhangun/wayang/main/scripts/install.sh | bash -s -- -d /opt/wayang -b /usr/local/bin
```

### 2. Homebrew (macOS & Linux)

```bash
# Add the tap
brew tap bhangun/tap

# Install Wayang
brew install wayang

# Upgrade
brew upgrade wayang
```

### 3. Chocolatey (Windows)

```powershell
# Install Wayang
choco install wayang

# Upgrade
choco upgrade wayang
```

### 4. SDKMAN! (Java/JVM)

```bash
# Install Wayang
sdk install wayang

# Use specific version
sdk use wayang 1.0.0
```

### 5. Docker

```bash
# Pull and run
docker pull bhangun/wayang:latest
docker run -it bhangun/wayang:latest

# JVM version (larger, faster startup)
docker pull bhangun/wayang:jvm
docker run -it bhangun/wayang:jvm
```

## Manual Installation

1. Download the binary for your platform from the [releases page](https://github.com/bhangun/wayang/releases)
2. Make it executable: `chmod +x wayang-standalone-*`
3. Move to your PATH: `sudo mv wayang-standalone-* /usr/local/bin/wayang`

## Available Binaries

| Platform | Architecture | Binary |
|----------|-------------|--------|
| Linux | x86_64 | wayang-standalone-linux-x86_64 |
| Linux | ARM64 | wayang-standalone-linux-aarch_64 |
| macOS | x86_64 | wayang-standalone-osx-x86_64 |
| macOS | ARM64 (M1/M2) | wayang-standalone-osx-aarch_64 |
| Windows | x86_64 | wayang-standalone-windows-x86_64.exe |
| JVM (any) | any | wayang-runtime-standalone-*.jar |

## Uninstall

### curl/SSL Installation
```bash
curl -fsSL https://raw.githubusercontent.com/bhangun/wayang/main/scripts/install.sh | bash -s -- -u
```

### Homebrew
```bash
brew uninstall wayang
brew untap bhangun/tap
```

### Chocolatey
```powershell
choco uninstall wayang
```

### SDKMAN!
```bash
sdk uninstall wayang
```

## Verification

After installation, verify:

```bash
wayang --version
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `WAYANG_INSTALL_DIR` | Installation directory | `~/.wayang` |
| `WAYANG_BIN_DIR` | Binary directory (symlink) | `~/.local/bin` |

## Troubleshooting

### Permission denied
```bash
# Make script executable
chmod +x install.sh
```

### Not in PATH
Add to your shell profile:
```bash
export PATH="$HOME/.local/bin:$PATH"
```

### macOS Gatekeeper
```bash
# Remove quarantine attribute
xattr -d com.apple.quarantine /path/to/wayang
```

## For Maintainers

### Release Process

1. Build all artifacts using the release workflow
2. JReleaser will automatically:
   - Create GitHub release
   - Update Homebrew tap
   - Publish to Chocolatey
   - Push Docker images
   - Update SDKMAN!

### Adding New Platforms

1. Add build job in `.github/workflows/release.yml`
2. Update `jreleaser.yml` with new artifact path
3. Update installation script `scripts/install.sh`
