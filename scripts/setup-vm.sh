#!/usr/bin/env bash
# Oracle Cloud Ubuntu 22.04 — First-time server setup for E-Library
# Run this script once after provisioning the instance:
#   chmod +x setup-vm.sh && sudo ./setup-vm.sh
set -euo pipefail

DOMAIN="elibrary.example.com"       # Replace with your actual domain
API_DOMAIN="api.elibrary.example.com"
CONFIG_REPO_URL="https://github.com/your-org/elibrary-config-repo.git"  # Replace
APP_DIR="/opt/elibrary"

echo "=== [1/6] System update ==="
apt-get update && apt-get upgrade -y

echo "=== [2/6] Install dependencies ==="
apt-get install -y curl git nginx certbot python3-certbot-nginx ufw

echo "=== [3/6] Install Docker ==="
if ! command -v docker &>/dev/null; then
    curl -fsSL https://get.docker.com | sh
    usermod -aG docker ubuntu
fi

echo "=== [4/6] Configure UFW firewall ==="
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

echo "=== [5/6] Configure NGINX ==="
mkdir -p /var/www/certbot
cp "$(dirname "$0")/../nginx/elibrary.conf" /etc/nginx/sites-available/elibrary
# Replace placeholder domain names with the real ones
sed -i "s/elibrary.example.com/$DOMAIN/g" /etc/nginx/sites-available/elibrary
sed -i "s/api.elibrary.example.com/$API_DOMAIN/g" /etc/nginx/sites-available/elibrary
ln -sf /etc/nginx/sites-available/elibrary /etc/nginx/sites-enabled/elibrary
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx

echo "=== [6/6] Obtain SSL certificates ==="
certbot --nginx -d "$DOMAIN" -d "$API_DOMAIN" --non-interactive --agree-tos --email admin@example.com
systemctl reload nginx

echo ""
echo "=== One-time application setup ==="
mkdir -p "$APP_DIR"

# Clone the Spring Cloud Config repository so config-service can mount it
if [ ! -d "$HOME/config-repo/.git" ]; then
    git clone "$CONFIG_REPO_URL" "$HOME/config-repo"
    echo "Config repo cloned to $HOME/config-repo"
else
    echo "Config repo already present at $HOME/config-repo — skipping clone"
fi

echo ""
echo "============================================================"
echo " Setup complete!"
echo " Next steps:"
echo "  1. Copy your .env file to $APP_DIR/.env"
echo "  2. Push code to 'main' — GitHub Actions will handle the rest"
echo "============================================================"
