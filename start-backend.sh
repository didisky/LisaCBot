#!/bin/bash

# Script pour lancer le Backend Spring Boot
# Usage: ./start-backend.sh

# Obtenir le chemin absolu du répertoire du script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Vérifier que nous sommes sur macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo "❌ Ce script est conçu pour macOS"
    exit 1
fi

echo "🔧 Lancement du Backend Spring Boot..."

osascript <<EOF
tell application "Terminal"
    do script "cd \"$SCRIPT_DIR/lisacbot-backend\" && echo \"🔧 Backend - Spring Boot (Port 8080)\" && echo \"========================================\" && mvn spring-boot:run"
end tell
EOF

echo "✅ Backend démarré dans un nouveau terminal"
echo "📍 URL: http://localhost:8080"
