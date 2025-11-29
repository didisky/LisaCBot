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

# Démarrer PostgreSQL
echo -e "${BLUE}🗄️  Démarrage de PostgreSQL...${NC}"

# Vérifier si Docker est disponible
if command -v docker &> /dev/null; then
    # Utiliser Docker Compose
    if docker ps | grep -q "lisacbot-postgres"; then
        echo "✓ PostgreSQL (Docker) est déjà en cours d'exécution"
    else
        echo "⚡ Lancement du conteneur PostgreSQL..."
        docker compose up -d postgres
        echo "⏳ Attente que PostgreSQL soit prêt..."
        sleep 5
        echo "✓ PostgreSQL (Docker) démarré"
    fi
else
    # Utiliser Homebrew PostgreSQL
    if brew services list | grep -q "postgresql@15.*started"; then
        echo "✓ PostgreSQL (Homebrew) est déjà en cours d'exécution"
    else
        echo "⚡ Démarrage de PostgreSQL (Homebrew)..."
        brew services start postgresql@15
        sleep 2
        echo "✓ PostgreSQL (Homebrew) démarré"
    fi
fi
echo ""

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
echo "   • Frontend:  http://localhost:4200"
echo "   • Backend:   http://localhost:8080"
echo "   • Database:  localhost:5432"
echo ""
echo "💡 Deux nouveaux terminaux ont été ouverts."
echo "   Fermez-les pour arrêter Backend et Frontend."
echo ""
if command -v docker &> /dev/null; then
    echo "🛑 Pour arrêter PostgreSQL (Docker):"
    echo "   docker compose down"
else
    echo "🛑 Pour arrêter PostgreSQL (Homebrew):"
    echo "   brew services stop postgresql@15"
fi
echo ""
