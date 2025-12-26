# 🚀 Déploiement Render - Démarrage Rapide (5 minutes)

## Étapes Ultra-Rapides

### 1. Générer les Secrets

```bash
# JWT Secret
openssl rand -base64 32

# Admin Password (optionnel)
openssl rand -base64 16
```

**Copiez ces valeurs** quelque part, vous en aurez besoin.

---

### 2. Push sur GitHub

```bash
git add .
git commit -m "Add Render deployment configuration"
git push origin main
```

---

### 3. Créer le Blueprint sur Render

1. Allez sur [render.com](https://render.com) et connectez-vous
2. Cliquez **"New +"** → **"Blueprint"**
3. Connectez votre repo GitHub **"LisaCBot"**
4. Render détectera automatiquement `render.yaml`
5. Blueprint Name: **"LisaCBot"**
6. Cliquez **"Continue"**

---

### 4. Configurer les 2 Variables Requises

Render vous demandera :

**JWT_SECRET** :
- Collez le résultat de `openssl rand -base64 32`

**ADMIN_PASSWORD** :
- Tapez votre mot de passe admin (ou collez le résultat d'openssl)

---

### 5. Déployer

1. Cliquez **"Apply"**
2. Attendez 10-15 minutes ⏳
3. Render va créer :
   - ✅ Base de données PostgreSQL
   - ✅ Backend Spring Boot
   - ✅ Frontend Angular

---

### 6. Accéder à l'Application

Une fois le déploiement terminé :

**URL** : `https://lisacbot-frontend.onrender.com`

**Login** :
- Username: `admin`
- Password: Votre `ADMIN_PASSWORD`

---

## ⚠️ Important : Première Utilisation

### Cold Start (Spin Down)
Le plan gratuit met les services en veille après 15 minutes d'inactivité.

**Premier accès** → 30-60 secondes d'attente (normal !)

### Changez votre mot de passe
1. Connectez-vous
2. Cliquez **"Password"** 🔒
3. Changez le mot de passe

---

## 🐛 Problèmes Fréquents

### "Service Unavailable" (503)
→ Service en démarrage après inactivité, attendez 60s

### Login ne fonctionne pas
→ Vérifiez que vous utilisez le bon `ADMIN_PASSWORD`

### Backend unreachable
→ Vérifiez les logs : Dashboard → lisacbot-backend → Logs

---

## 📋 Checklist Complète

- [ ] Générer JWT_SECRET et ADMIN_PASSWORD
- [ ] Push le code sur GitHub
- [ ] Créer le Blueprint sur Render
- [ ] Configurer JWT_SECRET et ADMIN_PASSWORD
- [ ] Cliquer "Apply" et attendre
- [ ] Accéder à l'application
- [ ] Changer le mot de passe admin

---

## 📖 Documentation Complète

Pour plus de détails : [RENDER_DEPLOYMENT.md](RENDER_DEPLOYMENT.md)

---

## 💡 Astuce

Pour éviter les cold starts, passez au plan payant ($7/mois par service) ou utilisez un service comme [UptimeRobot](https://uptimerobot.com/) pour ping votre app toutes les 5 minutes.
