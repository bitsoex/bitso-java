# Troubleshooting

Common MCP configuration issues and solutions.

## Contents

- [Certificate Errors](#certificate-errors) (L14-L31)
- [Docker Issues](#docker-issues) (L32-L59)
- [Environment Variables](#environment-variables) (L60-L75)
- [IntelliJ/Copilot CLI Issues](#intellijcopilot-cli-issues) (L76-L86)
- [Readiness Checks](#readiness-checks) (L87-L99)

---
## Certificate Errors

### Error: TLS Certificate Verification Failed

```text
tls: failed to verify certificate: x509: certificate signed by unknown authority
```

**Solution**: Set up Cloudflare certificates:

```bash
mkdir -p ~/cloudflare-certificates
# Copy certificates from Downloads or find on filesystem
find ~ -name '*cloudflare*certificate*.pem' 2>/dev/null
```

See `references/github-mcp-setup.md` for detailed instructions.

## Docker Issues

### Error: Docker Image Not Found

```text
Unable to find image 'ghcr.io/github/github-mcp-server:0.30.2' locally
```

**Solution**: Pull the image manually:

```bash
docker pull ghcr.io/github/github-mcp-server:0.30.2
```

If authentication is required:

```bash
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

### Error: Docker Not Running

```text
Cannot connect to the Docker daemon
```

**Solution**: Start Docker Desktop or the Docker daemon.

## Environment Variables

### Error: GITHUB_TOKEN Not Set

```text
Error: GITHUB_TOKEN environment variable is required
```

**Solution**: Set the environment variable:

```bash
export GITHUB_TOKEN="your-github-pat"
```

Or add to your shell profile (`~/.zshrc` or `~/.bashrc`).

## IntelliJ/Copilot CLI Issues

### Error: MCP configuration not found

**Solution**: Ensure configuration is in the correct path:

- IntelliJ: `~/.config/github-copilot/intellij/mcp.json`
- Copilot CLI: `~/.copilot/mcp-config.json`

See `java/commands/add-sonarqube-mcp-to-intellij-and-copilot-cli.md` for setup.

## Readiness Checks

Run the readiness script to diagnose issues:

```bash
./global/scripts/check-github-mcp-readiness.sh
```

This validates:
- Docker is running
- GITHUB_TOKEN is set
- Cloudflare certificates are in place
- Docker image is available
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/mcp-setup/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

