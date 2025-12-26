# Déploiement sur Render - Guide Complet

## 🚀 Déploiement Automatique avec render.yaml

LisaCBot inclut un fichier `render.yaml` qui permet de déployer toute l'infrastructure automatiquement.

---

## Prérequis

1. **Compte Render** : Créez un compte gratuit sur [render.com](https://render.com)
2. **Repository GitHub** : Votre code doit être sur GitHub
3. **Secrets à générer** :
   ```bash
   # JWT Secret (256 bits minimum)
   openssl rand -base64 32

   # Admin Password (optionnel, peut rester "admin" pour tester)
   openssl rand -base64 16
   ```

---

## Option 1 : Déploiement Automatique (Recommandé)

### Étape 1 : Préparer le Repository

Assurez-vous que tous les fichiers sont commités et pushés :

```bash
git add .
git commit -m "Add Render deployment configuration"
git push origin main
```

### Étape 2 : Créer un Blueprint sur Render

1. **Connectez-vous** à [dashboard.render.com](https://dashboard.render.com)
2. Cliquez sur **"New +"** → **"Blueprint"**
3. Connectez votre repository GitHub **LisaCBot**
4. Render détectera automatiquement le fichier `render.yaml`
5. Donnez un nom au Blueprint : **"LisaCBot"**

### Étape 3 : Configurer les Secrets

Render vous demandera de définir les variables suivantes :

#### Variables Required :

1. **JWT_SECRET**
   - Générez avec : `openssl rand -base64 32`
   - Exemple : `aB3dF7gH9jK2lM4nP6qR8sT0uV1wX3yZ5aB7cD9eF1`

2. **ADMIN_PASSWORD**
   - Mot de passe admin
   - Exemple : `MonMotDePasseFort123!`
   - Ou générez : `openssl rand -base64 16`

### Étape 4 : Lancer le Déploiement

1. Cliquez **"Apply"**
2. Render va créer automatiquement :
   - ✅ Base de données PostgreSQL (lisacbot-db)
   - ✅ Backend Spring Boot (lisacbot-backend)
   - ✅ Frontend Angular + Nginx (lisacbot-frontend)

3. **Temps de déploiement** : ~10-15 minutes

### Étape 5 : Vérifier le Déploiement

1. **Backend** : `https://lisacbot-backend.onrender.com/api/status`
   - Devrait retourner un JSON avec le statut du bot

2. **Frontend** : `https://lisacbot-frontend.onrender.com`
   - Devrait afficher la page de login

3. **Login** :
   - Username : `admin`
   - Password : Le mot de passe que vous avez défini dans `ADMIN_PASSWORD`

---

## Option 2 : Déploiement Manuel (Alternative)

Si vous préférez créer les services manuellement :

### 1. Créer la Base de Données

1. **New +** → **PostgreSQL**
2. Configurez :
   - Name: `lisacbot-db`
   - Database: `lisacbot`
   - User: `lisacbot`
   - Region: `Frankfurt` (ou plus proche de vous)
   - Plan: **Free**
3. Cliquez **Create Database**
4. **Notez** l'**Internal Connection String** (commence par `postgresql://`)

### 2. Créer le Backend

1. **New +** → **Web Service**
2. Connectez votre repo GitHub
3. Configurez :
   - Name: `lisacbot-backend`
   - Region: `Frankfurt` (même que la DB)
   - Branch: `main`
   - Root Directory: `lisacbot-backend`
   - Environment: **Docker**
   - Plan: **Free**

4. **Environment Variables** :
   ```bash
   SPRING_DATASOURCE_URL=<Internal Connection String de la DB>
   SPRING_DATASOURCE_USERNAME=lisacbot
   SPRING_DATASOURCE_PASSWORD=<mot de passe de la DB>
   SPRING_JPA_HIBERNATE_DDL_AUTO=update

   ADMIN_USERNAME=admin
   ADMIN_PASSWORD=<votre-mot-de-passe>
   JWT_SECRET=<généré-avec-openssl-rand-base64-32>

   BOT_POLL_INTERVAL_SECONDS=60
   CORS_ALLOWED_ORIGINS=https://lisacbot-frontend.onrender.com
   JAVA_OPTS=-Xmx512m -Xms256m
   ```

5. Cliquez **Create Web Service**

### 3. Créer le Frontend

1. **New +** → **Web Service**
2. Connectez votre repo GitHub
3. Configurez :
   - Name: `lisacbot-frontend`
   - Region: `Frankfurt`
   - Branch: `main`
   - Root Directory: `lisacbot-frontend`
   - Environment: **Docker**
   - Plan: **Free**

4. **Environment Variables** :
   ```bash
   BACKEND_URL=https://lisacbot-backend.onrender.com
   ```

5. Cliquez **Create Web Service**

### 4. Mettre à jour CORS du Backend

Après la création du frontend, retournez dans les variables du **backend** et mettez à jour :

```bash
CORS_ALLOWED_ORIGINS=https://lisacbot-frontend.onrender.com
```

Puis redéployez le backend.

---

## 🔧 Configuration Post-Déploiement

### Changer le Mot de Passe Admin

1. Connectez-vous : `https://lisacbot-frontend.onrender.com`
2. Username: `admin` / Password: Votre `ADMIN_PASSWORD`
3. Cliquez sur **"Password"** 🔒 dans la navbar
4. Changez le mot de passe

### Vérifier les Logs

Dans le Render Dashboard :

```
Backend Logs → lisacbot-backend → "Logs"
Frontend Logs → lisacbot-frontend → "Logs"
Database Logs → lisacbot-db → "Logs"
```

### Health Checks

Les services incluent des health checks automatiques :

- **Backend** : `/api/status`
- **Frontend** : `/health`

Render vérifie automatiquement ces endpoints.

---

## ⚠️ Limitations du Plan Gratuit

Le plan gratuit de Render a des limitations :

### 1. **Inactivité (Spin Down)**
- Les services gratuits s'endorment après **15 minutes** d'inactivité
- **Premier accès** après inactivité : 30-60 secondes de délai (cold start)
- **Solution** : Passer au plan payant ($7/mois par service)

### 2. **Build Minutes**
- 500 minutes de build gratuit par mois
- Chaque déploiement prend ~5-10 minutes

### 3. **Base de Données**
- PostgreSQL gratuit : **1GB de stockage**
- Expire après **90 jours**
- **Solution** : Backup régulier et migration vers plan payant

### 4. **Réseau**
- 100 GB/mois de bande passante

---

## 🐛 Dépannage

### Le backend ne démarre pas

1. **Vérifier les logs** : Dashboard → lisacbot-backend → Logs
2. **Erreur courante** : JWT_SECRET non défini
   ```
   Solution: Ajouter JWT_SECRET dans Environment Variables
   ```

3. **Erreur DB** : Connection refused
   ```
   Solution: Vérifier que SPRING_DATASOURCE_URL utilise l'Internal Connection String
   ```

### Le frontend ne peut pas atteindre le backend

1. **Vérifier BACKEND_URL** :
   ```
   Dashboard → lisacbot-frontend → Environment → BACKEND_URL
   Devrait être: https://lisacbot-backend.onrender.com
   ```

2. **Vérifier CORS** :
   ```
   Dashboard → lisacbot-backend → Environment → CORS_ALLOWED_ORIGINS
   Devrait inclure: https://lisacbot-frontend.onrender.com
   ```

3. **Logs du frontend** :
   ```
   Vérifier dans les logs au démarrage:
   "Backend URL: https://lisacbot-backend.onrender.com"
   ```

### Le login ne fonctionne pas

1. **Vérifier JWT_SECRET** : Doit être défini dans le backend
2. **Vérifier ADMIN_PASSWORD** : Utilisez le mot de passe défini dans les env vars
3. **Logs backend** : Recherchez "SECURITY WARNING" dans les logs

### Build Failed

1. **Frontend build error** :
   ```bash
   # Vérifier en local d'abord:
   cd lisacbot-frontend
   npm install
   npm run build
   ```

2. **Backend build error** :
   ```bash
   # Vérifier en local:
   cd lisacbot-backend
   mvn clean package
   ```

### Erreur "Service Unavailable" (503)

- **Cause** : Service en cours de démarrage (cold start après inactivité)
- **Solution** : Attendez 30-60 secondes et rechargez

---

## 📊 Monitoring

### Vérifier le Bot

1. **Dashboard** : `https://lisacbot-frontend.onrender.com/dashboard`
2. **Status API** : `https://lisacbot-backend.onrender.com/api/status`

### Logs en Temps Réel

```bash
# Dans Render Dashboard
lisacbot-backend → Logs → Enable "Live Tail"
```

### Métriques

Render Dashboard affiche :
- CPU usage
- Memory usage
- Requests per second
- Build time

---

## 💰 Passer au Plan Payant

Pour éviter le spin down et avoir de meilleures performances :

### Plans Recommandés

**Backend** :
- Plan: **Starter** ($7/mois)
- RAM: 512MB
- Always On (pas de spin down)

**Frontend** :
- Plan: **Starter** ($7/mois)
- Always On

**Database** :
- Plan: **Starter** ($7/mois)
- 1GB RAM
- Pas d'expiration

**Total** : ~$21/mois pour une application toujours disponible

---

## 🔄 Mises à Jour

### Déploiement Automatique

Render déploie automatiquement à chaque push sur `main` :

```bash
git add .
git commit -m "Update feature"
git push origin main

# Render rebuild automatiquement backend et frontend
```

### Déploiement Manuel

Dans le Render Dashboard :
1. Sélectionnez le service
2. Cliquez **"Manual Deploy"** → **"Deploy latest commit"**

---

## 🗂️ Structure des Services Render

```
LisaCBot Blueprint
├── lisacbot-db (PostgreSQL)
│   ├── Database: lisacbot
│   ├── User: lisacbot
│   └── Internal URL: postgres://...
│
├── lisacbot-backend (Web Service)
│   ├── Port: 8080
│   ├── Health: /api/status
│   └── URL: https://lisacbot-backend.onrender.com
│
└── lisacbot-frontend (Web Service)
    ├── Port: 80
    ├── Health: /health
    ├── Proxy: /api → lisacbot-backend
    └── URL: https://lisacbot-frontend.onrender.com
```

---

## 🎓 Ressources

- **Render Docs** : https://render.com/docs
- **Render Blueprints** : https://render.com/docs/infrastructure-as-code
- **Render Dashboard** : https://dashboard.render.com
- **Support Render** : https://render.com/docs/support

---

## 📞 Support

Pour toute question :
1. Consultez les **Logs** dans Render Dashboard
2. Vérifiez cette documentation
3. Ouvrez une issue sur GitHub
