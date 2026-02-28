#!/bin/bash
set -uxo pipefail  # removed -e so script continues on errors

# Install Docker using the official convenience script
if ! command -v docker &> /dev/null; then
    echo ">>> Installing Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    rm get-docker.sh
else
    echo ">>> Docker already installed, skipping installation"
    docker --version
fi

# Add user to docker group
# Note: Use safe fallback for $USER which may be unset in non-interactive shells with set -u
USER=${USER:-$(whoami)}
sudo usermod -aG docker "$USER" || true

# Configure Docker daemon for DinD/restricted environment
# Key: Copy daemon.json BEFORE starting Docker (per forum recommendations)
sudo mkdir -p /etc/docker
sudo cp .cursor/daemon.json /etc/docker/daemon.json

echo "============================================"
echo "Attempting to start Docker daemon..."
echo "============================================"

# Try service first (works after config is in place for some users)
echo ">>> Trying 'sudo service docker start'..."
sudo service docker start 2>&1 || true
sleep 3

DOCKER_READY=false
# Must use sudo - docker group not active in current shell yet
if sudo docker info >/dev/null 2>&1; then
    echo ">>> Docker started via service!"
    DOCKER_READY=true
else
    echo ">>> Service start didn't work, trying manual dockerd..."

    # Kill any existing processes
    sudo pkill dockerd 2>/dev/null || true
    sleep 1

    # Start dockerd manually - config is in /etc/docker/daemon.json, no flags needed!
    # Background dockerd directly so $! captures its PID (not a tee subprocess).
    # /tmp/dockerd.log is world-writable so the redirect works without sudo.
    # shellcheck disable=SC2024
    sudo dockerd >> /tmp/dockerd.log 2>&1 &
    DOCKERD_PID=$!
    echo ">>> dockerd PID: $DOCKERD_PID"

    # Wait for Docker daemon to be ready (use sudo!)
    for i in $(seq 1 15); do
        if sudo docker info >/dev/null 2>&1; then
            echo ">>> Docker daemon is ready!"
            DOCKER_READY=true
            break
        fi

        # Check if process died
        if ! ps -p $DOCKERD_PID > /dev/null 2>&1; then
            echo ">>> dockerd process died"
            echo ">>> Log:"
            cat /tmp/dockerd.log || true
            break
        fi

        echo ">>> Attempt $i/15 - waiting..."
        sleep 2
    done
fi

echo "============================================"
echo "Docker setup result: DOCKER_READY=$DOCKER_READY"
echo "============================================"

if [ "$DOCKER_READY" = "true" ]; then
    # Note: 666 is permissive but acceptable in Cursor agent's isolated environment
    sudo chmod 666 /var/run/docker.sock 2>/dev/null || true
    sudo docker info
    echo ">>> Testing with alpine (--network=none required)..."
    sudo docker run --rm --network=none alpine echo "Docker works!" || echo "Container test failed"
else
    echo ">>> DOCKERD LOG:"
    cat /tmp/dockerd.log 2>/dev/null || echo "(no log)"
    echo ">>> WARNING: Docker is not available during install"
    echo ">>> The 'start' command will try again when environment loads"
fi

# Mark this as a Cursor cloud agent environment.
# Used by secret-guard.sh and other cloud-agent-only behaviors.
# Written to /etc/environment (read by PAM for all sessions, including
# non-interactive shells where .bashrc is skipped) AND to ~/.bashrc
# (for interactive shells). Not set in local dev or repo mise.toml.
export BITSO_CLOUD_AGENT=1
if ! grep -qF 'BITSO_CLOUD_AGENT' /etc/environment 2>/dev/null; then
    echo 'BITSO_CLOUD_AGENT=1' | sudo tee -a /etc/environment >/dev/null
fi

# Set environment variables for Testcontainers and Docker compatibility
cat >> ~/.bashrc << 'EOF'
export DOCKER_HOST=unix:///var/run/docker.sock
export TESTCONTAINERS_RYUK_DISABLED=true
export TESTCONTAINERS_CHECKS_DISABLE=true
export BITSO_CLOUD_AGENT=1
EOF

# Create .docker-java.properties for Testcontainers Docker API compatibility
# This sets the Docker API version to match the DinD environment
echo ">>> Configuring Testcontainers Docker API version..."
cat > ~/.docker-java.properties << 'EOF'
api.version=1.44
EOF

# Authenticate with GitHub Container Registry (ghcr.io) using GITHUB_TOKEN
if [ -n "${GITHUB_TOKEN:-}" ]; then
    echo ">>> Authenticating with ghcr.io..."
    if [ "$DOCKER_READY" = "true" ]; then
        # Use GITHUB_ACTOR if available, otherwise use 'oauth2accesstoken' as username
        GHCR_USER="${GITHUB_ACTOR:-oauth2accesstoken}"

        # Temporarily disable xtrace to avoid exposing GITHUB_TOKEN
        # Capture current shell options state
        XTRACE_STATE=$([[ $- == *x* ]] && echo "on" || echo "off")
        set +x

        # Run docker login without xtrace enabled
        echo "$GITHUB_TOKEN" | docker login ghcr.io -u "$GHCR_USER" --password-stdin && \
            echo ">>> Successfully authenticated with ghcr.io" || \
            echo ">>> WARNING: Failed to authenticate with ghcr.io"

        # Restore original xtrace state
        if [ "$XTRACE_STATE" = "on" ]; then
            set -x
        fi
    else
        echo ">>> WARNING: Docker not ready, skipping ghcr.io auth (will retry in start.sh)"
    fi
else
    echo ">>> WARNING: GITHUB_TOKEN not set, skipping ghcr.io authentication"
fi

# Install Java 21 (for this project)
# Note: Hardcoded linux/x64 - adjust to aarch64 if running on ARM-based agents
curl -L -o /tmp/temurin21.tar.gz https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse
sudo mkdir -p /opt/java/temurin-21
sudo tar -xzf /tmp/temurin21.tar.gz -C /opt/java/temurin-21 --strip-components=1
rm /tmp/temurin21.tar.gz

echo 'export JAVA_HOME=/opt/java/temurin-21' >> ~/.bashrc
# shellcheck disable=SC2016
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc

# Install Terraform CLI 1.11.0
if ! command -v terraform &> /dev/null; then
    echo ">>> Installing Terraform CLI 1.11.0..."
    TERRAFORM_VERSION="1.11.0"
    # Detect architecture
    ARCH=$(uname -m)
    if [ "$ARCH" = "x86_64" ]; then
        TERRAFORM_ARCH="amd64"
    elif [ "$ARCH" = "aarch64" ] || [ "$ARCH" = "arm64" ]; then
        TERRAFORM_ARCH="arm64"
    else
        echo ">>> WARNING: Unsupported architecture: $ARCH, defaulting to amd64"
        TERRAFORM_ARCH="amd64"
    fi

    curl -L -o /tmp/terraform.zip "https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_${TERRAFORM_ARCH}.zip"
    sudo unzip -o /tmp/terraform.zip -d /usr/local/bin
    sudo chmod +x /usr/local/bin/terraform
    rm /tmp/terraform.zip
    echo ">>> Terraform CLI ${TERRAFORM_VERSION} installed successfully"
else
    echo ">>> Terraform already installed, skipping installation"
    terraform --version
fi

# Install mise (dev tool manager and task runner) - pinned version for predictability
MISE_VERSION="v2026.2.21"
export PATH="$HOME/.local/bin:$PATH"
if ! command -v mise &> /dev/null; then
    echo ">>> Installing mise ${MISE_VERSION}..."
    curl -fsSL https://mise.run | MISE_VERSION="${MISE_VERSION}" sh
    # shellcheck disable=SC2016
    if ! grep -Fq 'eval "$(~/.local/bin/mise activate bash)"' ~/.bashrc; then
        echo 'eval "$(~/.local/bin/mise activate bash)"' >> ~/.bashrc
    fi
    if command -v mise &> /dev/null; then
        echo ">>> mise ${MISE_VERSION} installed successfully"
        mise --version
        mise trust 2>/dev/null || true
    else
        echo ">>> WARNING: mise install completed but binary is not available in PATH"
    fi
else
    echo ">>> mise already installed, skipping installation"
    mise --version
    mise trust 2>/dev/null || true
fi

# Ensure mise shims are in PATH for this script and all subsequent commands.
# mise activate writes shims to this directory; without it, tools installed
# via `mise use -g` (like hk) won't be found by `command -v` during install.
MISE_SHIMS="$HOME/.local/share/mise/shims"
if [ -d "$MISE_SHIMS" ] && ! echo "$PATH" | grep -qF "$MISE_SHIMS"; then
    export PATH="$MISE_SHIMS:$PATH"
fi
# Also persist the shims path in .bashrc so interactive shells find them
# shellcheck disable=SC2016
if ! grep -qF 'mise/shims' ~/.bashrc 2>/dev/null; then
    echo 'export PATH="$HOME/.local/share/mise/shims:$PATH"' >> ~/.bashrc
fi

# Install hk (git hook manager) via mise
# No Homebrew or AirLock restrictions in cloud agent environments
HK_VERSION="1.36.0"
if ! command -v hk &> /dev/null; then
    if command -v mise &> /dev/null; then
        echo ">>> Installing hk ${HK_VERSION} via mise..."
        mise use -g hk@${HK_VERSION}
        mise reshim 2>/dev/null || true
        hash -r 2>/dev/null || true
        if command -v hk &> /dev/null; then
            echo ">>> hk ${HK_VERSION} installed successfully"
            hk --version
        else
            echo ">>> hk installed but shim not yet in PATH, adding installs dir..."
            HK_BIN_DIR="$HOME/.local/share/mise/installs/hk/${HK_VERSION}"
            if [ -x "$HK_BIN_DIR/hk" ]; then
                export PATH="$HK_BIN_DIR:$PATH"
                echo ">>> hk ${HK_VERSION} available at $HK_BIN_DIR"
                hk --version
            else
                echo ">>> WARNING: hk binary not found at $HK_BIN_DIR"
            fi
        fi
    else
        echo ">>> WARNING: mise not available, skipping hk installation"
    fi
else
    echo ">>> hk already installed, skipping installation"
    hk --version
fi

# Install Buf CLI (protobuf linter) - pinned version for predictability
BUF_VERSION="1.66.0"
if ! command -v buf &> /dev/null; then
    echo ">>> Installing Buf CLI v${BUF_VERSION}..."
    ARCH=$(uname -m)
    curl -sSL -o /tmp/buf \
        "https://github.com/bufbuild/buf/releases/download/v${BUF_VERSION}/buf-Linux-${ARCH}"
    sudo mv /tmp/buf /usr/local/bin/buf
    sudo chmod +x /usr/local/bin/buf
    echo ">>> Buf CLI v${BUF_VERSION} installed successfully"
    buf --version
else
    echo ">>> Buf already installed, skipping installation"
    buf --version
fi

# Install Bitso gRPC Linter - pinned version for predictability
# Provides: buf-plugin-rpc-minimal-documentation, buf-plugin-no-failure-usage,
#           buf-plugin-nested-payload-request-response, buf-plugin-method-extractor,
#           grpc-compliance-validate-repository
GRPC_LINTER_VERSION="v1.6.8"
if ! command -v grpc-compliance-validate-repository &> /dev/null; then
    echo ">>> Installing Bitso gRPC Linter ${GRPC_LINTER_VERSION}..."
    if [ -n "${GITHUB_TOKEN:-}" ]; then
        XTRACE_STATE=$([[ $- == *x* ]] && echo "on" || echo "off")
        set +x
        GH_TOKEN="${GITHUB_TOKEN}" gh release download "${GRPC_LINTER_VERSION}" \
            --repo bitsoex/bitso-grpc-linter \
            --pattern "*linux_amd64*" \
            --output /tmp/bitso-grpc-linter.tar.gz
        if [ "$XTRACE_STATE" = "on" ]; then set -x; fi

        tar -xzf /tmp/bitso-grpc-linter.tar.gz -C /tmp
        for bin in buf-plugin-rpc-minimal-documentation buf-plugin-no-failure-usage \
                   buf-plugin-nested-payload-request-response buf-plugin-method-extractor \
                   grpc-compliance-validate-repository; do
            if [ -f "/tmp/${bin}" ]; then
                sudo mv "/tmp/${bin}" /usr/local/bin/
                sudo chmod +x "/usr/local/bin/${bin}"
            fi
        done
        rm -f /tmp/bitso-grpc-linter.tar.gz /tmp/README.md
        echo ">>> Bitso gRPC Linter installed successfully"
        grpc-compliance-validate-repository --help 2>&1 | head -1 || true
    else
        echo ">>> WARNING: GITHUB_TOKEN not set, skipping Bitso gRPC Linter installation"
    fi
else
    echo ">>> Bitso gRPC Linter already installed, skipping installation"
    grpc-compliance-validate-repository --help 2>&1 | head -1 || true
fi

# Install credential scrubber script for use by start.sh's background loop.
# Note: cron/crontab is not available in the cloud agent container, so
# start.sh runs a background scrub loop instead.
SCRUB_SCRIPT="$HOME/.local/bin/scrub-credentials.sh"
mkdir -p "$HOME/.local/bin"
if cp .cursor/scripts/scrub-credentials.sh "$SCRUB_SCRIPT" && chmod +x "$SCRUB_SCRIPT"; then
    echo ">>> Credential scrubber script installed to $SCRUB_SCRIPT"
else
    echo ">>> WARNING: Failed to install credential scrubber script to $SCRUB_SCRIPT"
fi

# Install secret-guard to a stable absolute path so hooks.json can reference it.
GUARD_SCRIPT="$HOME/.local/bin/secret-guard.sh"
if cp .cursor/scripts/secret-guard.sh "$GUARD_SCRIPT" && chmod +x "$GUARD_SCRIPT"; then
    echo ">>> Secret guard script installed to $GUARD_SCRIPT"
else
    echo ">>> WARNING: Failed to install secret guard script to $GUARD_SCRIPT"
fi

# Install user-global hooks.json so Cursor picks up beforeShellExecution
# even if project-level .cursor/hooks.json is not respected in cloud agents.
# Uses absolute path to the installed secret-guard script.
CURSOR_CONFIG_DIR="$HOME/.cursor"
mkdir -p "$CURSOR_CONFIG_DIR"
cat > "$CURSOR_CONFIG_DIR/hooks.json" << HOOKSJSONEOF
{
  "version": 1,
  "hooks": {
    "beforeShellExecution": [
      {
        "command": "bash $GUARD_SCRIPT",
        "timeout": 5
      }
    ]
  }
}
HOOKSJSONEOF
echo ">>> User-global hooks.json installed to $CURSOR_CONFIG_DIR/hooks.json"

# Note: Environment variables are written to .bashrc but won't be available in this session
# The 'start' script or a new shell session will have them loaded
echo "Install completed successfully"
