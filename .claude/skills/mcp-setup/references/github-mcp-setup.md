# GitHub MCP Server Setup

The GitHub MCP server runs via Docker and requires a GitHub Personal Access Token and Cloudflare CA certificates (for Bitso network).

## Contents

- [Prerequisites](#prerequisites)
- [Cloudflare Certificate Setup](#cloudflare-certificate-setup)
- [Readiness Check](#readiness-check)
- [Manual Docker Setup (if needed)](#manual-docker-setup-if-needed)
- [Server Configuration](#server-configuration)
- [How Certificate Mounting Works](#how-certificate-mounting-works)

---
## Prerequisites

1. **Docker** - Must be installed and running
2. **GITHUB_TOKEN** - Environment variable with your GitHub PAT
3. **Cloudflare CA Certificate** - Required for TLS verification through Bitso's network

## Cloudflare Certificate Setup

Bitso uses Cloudflare for network security, which requires custom CA certificates. Without these certificates, you'll see errors like:

```text
tls: failed to verify certificate: x509: certificate signed by unknown authority
```

### Automatic Setup (Recommended)

The readiness script will automatically search for existing Cloudflare certificates on your filesystem and copy them to the standard location:

```bash
./.cursor/scripts/check-github-mcp-readiness.sh
```

The script searches these locations for certificates:

1. Common locations first (faster): `~/Downloads`, `~/Desktop`, `~/Documents`, `/tmp`
2. Fallback: entire home directory (`~`) if not found in common locations

### Manual Setup (If Automatic Fails)

If the automatic search doesn't find certificates:

#### Option 1: Search filesystem manually

```bash
# Search for existing Cloudflare certificates anywhere on your system
find ~ -name '*cloudflare*certificate*.pem' 2>/dev/null

# Once found, create directory and copy
mkdir -p ~/cloudflare-certificates
cp /path/to/found/2025_cloudflare_ca_certificate.pem ~/cloudflare-certificates/
cp /path/to/found/2025_cloudflare_ca_certificate.crt ~/cloudflare-certificates/
```

#### Option 2: Download from Confluence

1. Download the Cloudflare CA certificates from:
   [Configure Cloudflare CA certificate to applications](https://bitsomx.atlassian.net/wiki/spaces/SEA/pages/4358963539/Configure+Cloudflare+CA+certificate+to+applications)

2. Copy to the standard location:

   ```bash
   mkdir -p ~/cloudflare-certificates
   cp ~/Downloads/2025_cloudflare_ca_certificate.pem ~/cloudflare-certificates/
   cp ~/Downloads/2025_cloudflare_ca_certificate.crt ~/cloudflare-certificates/
   ```

### Verify the Setup

```bash
ls -la ~/cloudflare-certificates/
# Should show:
# 2025_cloudflare_ca_certificate.pem
# 2025_cloudflare_ca_certificate.crt
```

## Readiness Check

Run the readiness script to verify your setup (includes certificate verification):

```bash
./global/scripts/check-github-mcp-readiness.sh
```

## Manual Docker Setup (if needed)

If the image is not available locally:

```bash
# Pull the image
docker pull ghcr.io/github/github-mcp-server:0.30.2

# If authentication is required
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

## Server Configuration

The GitHub MCP server is configured in read-only mode with lockdown enabled, providing access to:

- Context (current user)
- Dependabot alerts
- Code security features
- Secret protection
- Security advisories
- Labels management
- Pull requests

## How Certificate Mounting Works

The MCP configuration mounts the Cloudflare certificate into the Docker container:

```json
{
    "github": {
        "command": "docker",
        "args": [
            "run", "-i", "--rm",
            "-v", "~/cloudflare-certificates/2025_cloudflare_ca_certificate.pem:/etc/ssl/custom/cf-custom-ca.pem:ro",
            "-e", "SSL_CERT_FILE=/etc/ssl/custom/cf-custom-ca.pem",
            ...
        ]
    }
}
```

- `-v`: Mounts the local certificate file into the container (read-only)
- `-e SSL_CERT_FILE`: Tells the container to use this certificate for TLS verification
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/mcp-setup/references/github-mcp-setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

