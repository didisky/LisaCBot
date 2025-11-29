# LisaCBot - Guide de Déploiement Docker

## Prérequis

- Docker Engine 24.0+
- Docker Compose 2.20+
- 2GB RAM minimum (4GB recommandé)
- 10GB d'espace disque

## Scripts de déploiement

Deux scripts sont fournis pour simplifier le déploiement :

- `deploy.sh` - Pour le développement et les opérations quotidiennes
- `deploy-prod.sh` - Pour le déploiement en production avec git pull et backups automatiques

### Script de développement (deploy.sh)

```bash
# Afficher l'aide
./deploy.sh help

# Déployer tout (rebuild complet)
./deploy.sh all

# Déployer uniquement le backend
./deploy.sh backend

# Déployer uniquement le frontend
./deploy.sh frontend

# Rebuild rapide (avec cache)
./deploy.sh quick
./deploy.sh quick backend
./deploy.sh quick frontend

# Voir les logs
./deploy.sh logs
./deploy.sh logs backend

# Statut des services
./deploy.sh status

# Redémarrer
./deploy.sh restart
./deploy.sh restart backend

# Backup/Restore base de données
./deploy.sh backup
./deploy.sh restore backup_20250129_123456.sql

# Tout nettoyer (supprime les données)
./deploy.sh clean
```

### Script de production (deploy-prod.sh)

```bash
# Déploiement complet avec git pull et backup automatique
./deploy-prod.sh

# Déployer une branche spécifique
./deploy-prod.sh develop

# Rollback de la base de données
./deploy-prod.sh rollback
```

## Déploiement rapide

### 1. Configuration

```bash
# Créer le fichier .env
cp .env.example .env

# Éditer et définir le mot de passe PostgreSQL
nano .env
```

### 2. Lancement avec le script

```bash
# Option 1 : Utiliser le script (recommandé)
./deploy.sh all

# Option 2 : Commande Docker Compose directe
docker-compose up -d
```

L'application sera accessible sur :
- Frontend : http://localhost
- Backend API : http://localhost:8080/api
- PostgreSQL : localhost:5432

### 3. Vérification

```bash
# Avec le script
./deploy.sh status

# Ou avec docker-compose
docker-compose ps
docker-compose logs -f
```

### 4. Arrêt et nettoyage

```bash
# Avec le script
./deploy.sh stop

# Tout nettoyer (supprime les données)
./deploy.sh clean

# Ou avec docker-compose
docker-compose down
docker-compose down -v  # Supprime aussi les volumes
```

## Déploiement sur un serveur de production

### Option 1 : VPS avec Docker (Recommandé)

#### Sur votre serveur (Ubuntu/Debian)

```bash
# Installer Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Installer Docker Compose
sudo apt-get update
sudo apt-get install docker-compose-plugin

# Cloner le projet
git clone <votre-repo> lisacbot
cd lisacbot

# Configuration
cp .env.example .env
nano .env  # Éditez la configuration

# Lancer
sudo docker-compose up -d
```

#### Configurer un reverse proxy pour HTTPS (Nginx)

```bash
# Installer certbot
sudo apt-get install certbot python3-certbot-nginx

# Modifier docker-compose.yml pour exposer sur un autre port
# Changez "80:80" en "8081:80" dans le service frontend

# Configuration Nginx sur le host
sudo nano /etc/nginx/sites-available/lisacbot
```

Contenu de la configuration Nginx :

```nginx
server {
    listen 80;
    server_name votre-domaine.com;

    location / {
        proxy_pass http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

```bash
# Activer la configuration
sudo ln -s /etc/nginx/sites-available/lisacbot /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# Obtenir un certificat SSL
sudo certbot --nginx -d votre-domaine.com
```

### Option 2 : Configuration avancée avec SSL intégré

Modifier `docker-compose.yml` pour ajouter Traefik comme reverse proxy :

```yaml
# Ajoutez ce service
traefik:
  image: traefik:v2.10
  command:
    - "--providers.docker=true"
    - "--entrypoints.web.address=:80"
    - "--entrypoints.websecure.address=:443"
    - "--certificatesresolvers.letsencrypt.acme.email=votre@email.com"
    - "--certificatesresolvers.letsencrypt.acme.storage=/letsencrypt/acme.json"
    - "--certificatesresolvers.letsencrypt.acme.httpchallenge.entrypoint=web"
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock:ro
    - traefik_letsencrypt:/letsencrypt
  networks:
    - lisacbot-network
```

## Configuration de production recommandée

### Variables d'environnement (.env)

```bash
# PostgreSQL - Utilisez des mots de passe forts
POSTGRES_DB=lisacbot_prod
POSTGRES_USER=lisacbot_prod
POSTGRES_PASSWORD=<générer-un-mot-de-passe-fort>

# Backend
BOT_POLL_INTERVAL_SECONDS=60
JAVA_OPTS=-Xmx1024m -Xms512m

# Si vous utilisez Traefik
DOMAIN=votre-domaine.com
```

### Sécurité

1. **Firewall** : N'exposez que les ports nécessaires
```bash
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS
sudo ufw enable
```

2. **Ne pas exposer PostgreSQL** : Commentez le mapping de port dans docker-compose.yml :
```yaml
postgres:
  # ports:
  #   - "5432:5432"  # Ne pas exposer en production
```

3. **Logs** : Les logs sont automatiquement limités (10MB max, 3 fichiers)

4. **Backups PostgreSQL** :
```bash
# Backup
docker-compose exec postgres pg_dump -U lisacbot_prod lisacbot_prod > backup.sql

# Restore
cat backup.sql | docker-compose exec -T postgres psql -U lisacbot_prod lisacbot_prod
```

### Monitoring

```bash
# Vérifier les ressources
docker stats

# Logs en temps réel
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres

# Health checks
curl http://localhost/health
curl http://localhost:8080/actuator/health
```

## Mises à jour

### En développement

```bash
# Modifier votre code, puis :

# Backend modifié
./deploy.sh quick backend

# Frontend modifié
./deploy.sh quick frontend

# Les deux modifiés
./deploy.sh quick
```

### En production

```bash
# Avec le script (recommandé - inclut backup automatique)
./deploy-prod.sh

# Ou manuellement
git pull
docker-compose up -d --build
docker-compose ps
docker-compose logs -f
```

Le script `deploy-prod.sh` effectue automatiquement :
1. Vérifications pré-déploiement
2. Backup de la base de données
3. Pull du code depuis git
4. Rebuild et redéploiement
5. Health checks
6. Affichage du résumé

## Troubleshooting

### Le backend ne démarre pas
```bash
# Vérifier les logs
docker-compose logs backend

# Vérifier que PostgreSQL est bien démarré
docker-compose ps postgres
```

### Le frontend ne se connecte pas au backend
- Vérifiez que le backend est accessible sur http://backend:8080 depuis le conteneur frontend
- Vérifiez les logs nginx : `docker-compose logs frontend`

### Erreur de mémoire Java
- Augmentez `JAVA_OPTS` dans `.env` : `-Xmx1024m -Xms512m`

### Base de données corrompue
```bash
# Arrêter tout
docker-compose down

# Supprimer le volume (ATTENTION : perte de données)
docker volume rm lisacbot_postgres_data

# Redémarrer
docker-compose up -d
```

## Architecture

```
                    ┌──────────────┐
                    │   Internet   │
                    └──────┬───────┘
                           │
                    ┌──────▼────────┐
                    │     Nginx     │
                    │   (Frontend)  │
                    │   Port 80     │
                    └──────┬────────┘
                           │
                ┬──────────┼──────────┬
                │          │          │
         ┌──────▼───┐ ┌───▼──────┐ ┌─▼────────┐
         │ Angular  │ │  Spring  │ │   Postgres│
         │   SPA    │ │   Boot   │ │    DB     │
         │          │ │  :8080   │ │   :5432   │
         └──────────┘ └─────┬────┘ └────▲──────┘
                            │           │
                            └───────────┘
```

## Ressources

- Docker Hub : https://hub.docker.com/
- Docker Compose : https://docs.docker.com/compose/
- Let's Encrypt : https://letsencrypt.org/
- Nginx : https://nginx.org/
