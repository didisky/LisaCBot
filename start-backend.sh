#!/bin/bash

# Script pour lancer le Backend LisaCBot
# Usage: ./start-backend.sh

# Couleurs pour les messages
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Obtenir le chemin absolu du répertoire du script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "${BLUE}🔧 Démarrage du Backend Spring Boot...${NC}"
echo "========================================"
echo ""

cd "$SCRIPT_DIR/lisacbot-backend"

echo "📍 Backend URL: http://localhost:8080"
echo ""

mvn spring-boot:run
