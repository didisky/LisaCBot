#!/bin/bash

# Script pour lancer le Frontend LisaCBot
# Usage: ./start-frontend.sh

# Couleurs pour les messages
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Obtenir le chemin absolu du répertoire du script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "${GREEN}🎨 Démarrage du Frontend Angular...${NC}"
echo "======================================"
echo ""

cd "$SCRIPT_DIR/lisacbot-frontend"

echo "📍 Frontend URL: http://localhost:4200"
echo ""

ng serve
