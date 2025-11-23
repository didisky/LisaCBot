#!/bin/bash

# Script pour lancer LisaCBot (Frontend + Backend)
# Usage: ./start.sh

echo "🚀 Démarrage de LisaCBot..."
echo ""

# Couleurs pour les messages
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Obtenir le chemin absolu du répertoire du script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Vérifier que nous sommes sur macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo "❌ Ce script est conçu pour macOS"
    exit 1
fi

# Créer le script backend
cat > /tmp/lisacbot_backend.sh << 'BACKEND_SCRIPT'
#!/bin/bash
cd "/Users/jerome/Documents/git_clone/LisaCBot/lisacbot-backend"
echo "🔧 Backend - Spring Boot (Port 8080)"
echo "========================================"
mvn spring-boot:run
BACKEND_SCRIPT
chmod +x /tmp/lisacbot_backend.sh

# Créer le script frontend
cat > /tmp/lisacbot_frontend.sh << 'FRONTEND_SCRIPT'
#!/bin/bash
export PATH="/opt/homebrew/opt/node@22/bin:$PATH"
cd "/Users/jerome/Documents/git_clone/LisaCBot/lisacbot-frontend"
echo "🎨 Frontend - Angular (Port 4200)"
echo "======================================"
ng serve
FRONTEND_SCRIPT
chmod +x /tmp/lisacbot_frontend.sh

echo -e "${BLUE}📦 Lancement du Backend Spring Boot...${NC}"
osascript -e 'tell application "Terminal" to do script "/tmp/lisacbot_backend.sh"'

# Attendre un peu avant de lancer le frontend
sleep 2

echo -e "${GREEN}🎨 Lancement du Frontend Angular...${NC}"
osascript -e 'tell application "Terminal" to do script "/tmp/lisacbot_frontend.sh"'

echo ""
echo -e "${GREEN}✅ Les applications sont en cours de démarrage!${NC}"
echo ""
echo "📍 URLs:"
echo "   • Frontend: http://localhost:4200"
echo "   • Backend:  http://localhost:8080"
echo ""
echo "💡 Deux nouveaux terminaux ont été ouverts."
echo "   Fermez-les pour arrêter les applications."
echo ""
