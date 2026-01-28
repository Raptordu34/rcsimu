# Interface Web Pilote / Conducteur

Interface web simple basée sur **Vue.js** et **Bootstrap** permettant d’afficher un **flux caméra** et des **informations de conduite / télémétrie** en temps réel.

---

## 🚀 Fonctionnalités

- 📹 Composant d’affichage caméra
- 📊 Affichage d’informations de conduite (vitesse, direction, batterie, etc.)
- 📱 Interface responsive (grille Bootstrap)
- ⚡ Développé avec Vue 3 et Vite
- 🧱 Architecture modulaire basée sur des composants

---

## 🛠️ Technologies utilisées

- **Vue.js 3**
- **Vite**
- **Bootstrap 5**
- **Bootstrap Icons**

---

## 📁 Structure du projet

src/
├── components/
│ ├── Camera.vue # Composant caméra
│ └── DrivingInfo.vue # Panneau d’informations de conduite
│
├── views/
│ └── DashboardView.vue # Vue principale du tableau de bord
│
├── assets/ # CSS, images et ressources du template
│
├── App.vue
└── main.js


---

## 📦 Installation

### 1. Cloner le dépôt
```bash
git clone <url-du-repo>
cd <nom-du-projet>
```
2. Installer les dépendances
```bash
npm install
```
3. Lancer le serveur de développement
```bash
npm run dev
```
4. Si besoin, mettre à jour Node.js
```bash
# Download and install nvm:
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash

# in lieu of restarting the shell
\. "$HOME/.nvm/nvm.sh"

# Download and install Node.js:
nvm install 24

# Verify the Node.js version:
node -v # Should print "v24.13.0".

# Verify npm version:
npm -v # Should print "11.6.2".
```

L’application est ensuite accessible à l’adresse :

http://localhost:5173

🧩 Composants principaux
`Camera.vue`

Composant dédié à l’affichage du flux caméra.
Actuellement, un placeholder est utilisé, mais il peut être remplacé par :

    - un flux vidéo USB

    - une caméra IP

    - WebRTC ou MJPEG

DrivingInfo.vue

Affiche des informations de conduite telles que :

    - Vitesse

    - Angle de direction

    - Niveau de batterie

(Les valeurs sont pour le moment simulées.)