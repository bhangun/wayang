#!/usr/bin/env bash
set -euo pipefail

# Wayang Installer
# Supports: Linux, macOS, Windows (via WSL)
# Installation methods: curl/SSL, Homebrew, Chocolatey

readonly SCRIPT_NAME="$(basename "$0")"
readonly GITHUB_REPO="bhangun/wayang"
readonly INSTALL_DIR="${WAYANG_INSTALL_DIR:-$HOME/.wayang}"
readonly BIN_DIR="${WAYANG_BIN_DIR:-$HOME/.local/bin}"

# Colors for output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly NC='\033[0m' # No Color

# Logging functions
info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
    exit 1
}

# Helper functions
print_logo() {
    cat << 'EOF'
 _    _  __
| |  | |/ /
| |__| ' / 
|  __  <  
| |  | . \ 
|_|  |_|\_\
            
EOF
    echo "Wayang Installer - AI Agent Workflow Platform"
    echo "=============================================="
    echo
}

show_help() {
    cat << EOF
Usage: $SCRIPT_NAME [OPTIONS]

Options:
    -v, --version VERSION    Install specific version (default: latest)
    -d, --install-dir DIR    Installation directory (default: ~/.wayang)
    -b, --bin-dir DIR        Binary directory (default: ~/.local/bin)
    -m, --method METHOD      Installation method: curl, brew, choco (default: curl)
    -u, --uninstall          Uninstall Wayang
    -h, --help               Show this help message

Examples:
    $SCRIPT_NAME                          # Install latest version
    $SCRIPT_NAME -v v1.0.0                # Install specific version
    $SCRIPT_NAME -m brew                  # Install via Homebrew
    $SCRIPT_NAME -m choco                 # Install via Chocolatey
    $SCRIPT_NAME -u                       # Uninstall Wayang

Environment Variables:
    WAYANG_INSTALL_DIR    Installation directory
    WAYANG_BIN_DIR        Binary directory
EOF
}

detect_os() {
    local os
    case "$(uname -s)" in
        Linux*)     os="linux" ;;
        Darwin*)    os="osx" ;;
        MINGW*|MSYS*|CYGWIN*) os="windows" ;;
        *)          error "Unsupported operating system: $(uname -s)" ;;
    esac
    echo "$os"
}

detect_arch() {
    local arch
    case "$(uname -m)" in
        x86_64)  arch="x86_64" ;;
        aarch64|arm64) arch="aarch_64" ;;
        *)       error "Unsupported architecture: $(uname -m)" ;;
    esac
    echo "$arch"
}

get_latest_version() {
    local version
    version="$(curl -s "https://api.github.com/repos/$GITHUB_REPO/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')"
    if [[ -z "$version" ]]; then
        error "Failed to fetch latest version"
    fi
    echo "$version"
}

# Installation methods
install_with_curl() {
    local version="$1"
    local os="$2"
    local arch="$3"
    
    info "Installing Wayang $version for $os-$arch"
    
    local artifact_name="wayang-standalone-${os}-${arch}"
    local download_url="https://github.com/$GITHUB_REPO/releases/download/${version}/${artifact_name}"
    
    if [[ "$os" == "windows" ]]; then
        artifact_name="${artifact_name}.exe"
        download_url="${download_url}.exe"
    fi
    
    # Create directories
    mkdir -p "$INSTALL_DIR"
    mkdir -p "$BIN_DIR"
    
    # Download binary
    info "Downloading from: $download_url"
    if ! curl -fsSL -o "$INSTALL_DIR/$artifact_name" "$download_url"; then
        error "Failed to download Wayang binary"
    fi
    
    # Make executable
    if [[ "$os" != "windows" ]]; then
        chmod +x "$INSTALL_DIR/$artifact_name"
    fi
    
    # Create symlink
    local bin_name="wayang"
    if [[ "$os" == "windows" ]]; then
        bin_name="wayang.exe"
    fi
    
    ln -sf "$INSTALL_DIR/$artifact_name" "$BIN_DIR/$bin_name"
    
    # Add to PATH if not already present
    if [[ "$os" != "windows" ]]; then
        if [[ ":$PATH:" != *":$BIN_DIR:"* ]]; then
            warn "BIN_DIR ($BIN_DIR) is not in PATH"
            echo ""
            echo "Add the following to your shell profile (~/.bashrc, ~/.zshrc, etc.):"
            echo "  export PATH=\"$BIN_DIR:\$PATH\""
            echo ""
        fi
    fi
    
    success "Wayang $version installed successfully!"
    echo ""
    echo "Binary location: $BIN_DIR/$bin_name"
    echo ""
    echo "To get started:"
    echo "  wayang --help"
}

install_with_brew() {
    info "Installing Wayang via Homebrew..."
    
    if ! command -v brew &> /dev/null; then
        error "Homebrew is not installed. Install it from https://brew.sh"
    fi
    
    brew tap bhangun/tap
    brew install wayang
    
    success "Wayang installed successfully via Homebrew!"
}

install_with_choco() {
    info "Installing Wayang via Chocolatey..."
    
    if ! command -v choco &> /dev/null; then
        error "Chocolatey is not installed. Install it from https://chocolatey.org"
    fi
    
    choco install wayang -y
    
    success "Wayang installed successfully via Chocolatey!"
}

uninstall() {
    info "Uninstalling Wayang..."
    
    local os
    os="$(detect_os)"
    
    if [[ "$os" == "windows" ]]; then
        rm -f "$INSTALL_DIR/wayang.exe"
        rm -f "$BIN_DIR/wayang.exe"
    else
        rm -f "$INSTALL_DIR/wayang-standalone"*
        rm -f "$BIN_DIR/wayang"
    fi
    
    # Remove empty directories
    rmdir "$INSTALL_DIR" 2>/dev/null || true
    
    success "Wayang uninstalled successfully!"
}

verify_installation() {
    if command -v wayang &> /dev/null; then
        local version
        version="$(wayang --version 2>&1 || echo 'unknown')"
        success "Wayang is installed and working! Version: $version"
        return 0
    else
        warn "Wayang binary not found in PATH"
        return 1
    fi
}

# Main
main() {
    print_logo
    
    local version=""
    local method="curl"
    local uninstall_flag=false
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case "$1" in
            -v|--version)
                version="$2"
                shift 2
                ;;
            -d|--install-dir)
                INSTALL_DIR="$2"
                shift 2
                ;;
            -b|--bin-dir)
                BIN_DIR="$2"
                shift 2
                ;;
            -m|--method)
                method="$2"
                shift 2
                ;;
            -u|--uninstall)
                uninstall_flag=true
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                error "Unknown option: $1"
                ;;
        esac
    done
    
    # Handle uninstall
    if [[ "$uninstall_flag" == true ]]; then
        uninstall
        exit 0
    fi
    
    # Get version
    if [[ -z "$version" ]]; then
        version="$(get_latest_version)"
        info "Latest version: $version"
    fi
    
    # Install based on method
    case "$method" in
        curl)
            local os arch
            os="$(detect_os)"
            arch="$(detect_arch)"
            install_with_curl "$version" "$os" "$arch"
            verify_installation
            ;;
        brew)
            install_with_brew
            verify_installation
            ;;
        choco)
            install_with_choco
            verify_installation
            ;;
        *)
            error "Unknown installation method: $method. Use: curl, brew, or choco"
            ;;
    esac
}

main "$@"
