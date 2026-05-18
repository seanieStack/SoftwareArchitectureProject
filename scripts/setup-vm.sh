#!/usr/bin/env bash
# Oracle Cloud Ubuntu 22.04 — First-time server setup for E-Library
# Run this script once after provisioning the instance:
#   chmod +x setup-vm.sh && sudo ./setup-vm.sh
set -euo pipefail
export DEBIAN_FRONTEND=noninteractive

DOMAIN="elibrary.example.com"       # Replace with your actual domain
API_DOMAIN="api.elibrary.example.com"
CERTBOT_EMAIL="admin@example.com"   # Replace with your email for Let's Encrypt notices
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

# Some Oracle images include a default iptables reject before UFW chains.
# Keep HTTP/HTTPS reachable even on those images, and persist the rules.
for port in 80 443; do
    if ! iptables -C INPUT -p tcp --dport "$port" -j ACCEPT 2>/dev/null; then
        iptables -I INPUT 1 -p tcp --dport "$port" -j ACCEPT
    fi
done
apt-get install -y iptables-persistent
netfilter-persistent save

echo "=== [5/6] Configure NGINX ==="
mkdir -p /var/www/certbot
cat > /etc/nginx/sites-available/elibrary <<NGINX
server {
    listen 80;
    server_name $DOMAIN;

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}

server {
    listen 80;
    server_name $API_DOMAIN;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
NGINX
ln -sf /etc/nginx/sites-available/elibrary /etc/nginx/sites-enabled/elibrary
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx

echo "=== [6/6] Obtain SSL certificates ==="
certbot --nginx -d "$DOMAIN" -d "$API_DOMAIN" --non-interactive --agree-tos --email "$CERTBOT_EMAIL"

cp "$(dirname "$0")/../nginx/elibrary.conf" /etc/nginx/sites-available/elibrary
# Replace placeholder domain names with the real ones after Certbot creates cert files.
sed -i "s/api.elibrary.example.com/$API_DOMAIN/g" /etc/nginx/sites-available/elibrary
sed -i "s/elibrary.example.com/$DOMAIN/g" /etc/nginx/sites-available/elibrary
nginx -t
systemctl reload nginx

echo ""
echo "=== One-time application setup ==="
mkdir -p "$APP_DIR"

# Clone the Spring Cloud Config repository if you use an external config repo.
mkdir -p "$HOME/config-repo"
if [ "$CONFIG_REPO_URL" = "https://github.com/your-org/elibrary-config-repo.git" ]; then
    echo "Using bundled native config; set CONFIG_REPO_URL to clone an external config repo."
elif [ ! -d "$HOME/config-repo/.git" ]; then
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
