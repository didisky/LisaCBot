#!/bin/bash

# Script pour lancer le Frontend Angular
# Usage: ./start-frontend.sh

# Obtenir le chemin absolu du répertoire du script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Vérifier que nous sommes sur macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo "❌ Ce script est conçu pour macOS"
    exit 1
fi

echo "🎨 Lancement du Frontend Angular..."

osascript <<EOF
tell application "Terminal"
    do script "export PATH=\"/opt/homebrew/opt/node@22/bin:\$PATH\" && cd \"$SCRIPT_DIR/lisacbot-frontend\" && echo \"🎨 Frontend - Angular (Port 4200)\" && echo \"======================================\" && ng serve"
end tell
EOF

echo "✅ Frontend démarré dans un nouveau terminal"
echo "📍 URL: http://localhost:4200"
