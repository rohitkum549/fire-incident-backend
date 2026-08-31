#!/usr/bin/env bash
# Helper script to install uv, specify-cli, and initialize spec-kit for this project.
set -e

echo "=============================================="
echo " Starting Spec Kit Installation Helper Script "
echo "=============================================="

# 1. Check/Install uv
if ! command -v uv &> /dev/null; then
    echo "[-] uv not found in PATH. Attempting to install uv..."
    
    # Try using the standalone curl installer
    if command -v curl &> /dev/null; then
        curl -LsSf https://astral.sh/uv/install.sh | sh
        
        # Source the newly installed uv env
        if [ -f "$HOME/.local/bin/env" ]; then
            source "$HOME/.local/bin/env"
        fi
        # Ensure it is in PATH for the rest of this script
        export PATH="$HOME/.local/bin:$PATH"
        export PATH="$HOME/.cargo/bin:$PATH"
    else
        echo "[!] curl is not installed. Trying snap..."
        sudo snap install astral-uv
    fi
else
    echo "[+] uv is already installed."
fi

# Final check for uv
if ! command -v uv &> /dev/null; then
    echo "[x] Error: uv could not be found or installed. Please install uv manually and rerun this script."
    exit 1
fi

echo "[+] uv is ready: $(uv --version)"

# 2. Install specify-cli
echo "=== Installing specify-cli ==="
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git --force

# Ensure specify is available in current session PATH
export PATH="$HOME/.local/bin:$PATH"

if ! command -v specify &> /dev/null; then
    echo "[x] specify-cli installed but specify command not found in PATH."
    echo "Please ensure $HOME/.local/bin is in your PATH."
    exit 1
fi

echo "[+] specify-cli is ready: $(specify --version)"

# 3. Initialize Spec Kit for the project
echo "=== Initializing Spec Kit for this project ==="
specify init .

echo ""
echo "=============================================="
echo " Setup complete! "
echo "=============================================="
