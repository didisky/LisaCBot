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

# Lancer le backend
echo -e "${BLUE}📦 Lancement du Backend Spring Boot...${NC}"
"$SCRIPT_DIR/start-backend.sh"

# Attendre un peu avant de lancer le frontend
sleep 2

# Lancer le frontend
echo -e "${GREEN}🎨 Lancement du Frontend Angular...${NC}"
"$SCRIPT_DIR/start-frontend.sh"

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
