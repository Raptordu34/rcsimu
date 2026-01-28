# Speedometer UI

Ce projet est une interface utilisateur affichant deux compteurs de vitesse, un pour chaque roue (gauche et droite). L'application se connecte à un serveur WebSocket pour recevoir en temps réel les données de vitesse et de glissement.

## Aperçu

L'interface affiche deux jauges circulaires représentant la vitesse des roues gauche et droite. La couleur des jauges change en fonction de la vitesse, et un indicateur de "Glissement!" apparaît si un glissement est détecté.

## Technologies

*   [Vue.js](https://vuejs.org/)
*   [Vite](https://vitejs.dev/)
*   [Syncfusion Circular Gauge](https://www.syncfusion.com/vue-components/vue-circular-gauge) for Vue
*   WebSocket pour la communication en temps réel

## Installation et Lancement

1.  **Prérequis :** Assurez-vous d'avoir [Node.js](https://nodejs.org/) (version ^20.19.0 || >=22.12.0) installé.

2.  **Cloner le dépôt et se placer dans le bon dossier :**
    ```sh
    cd speedometer-app
    ```

3.  **Installer les dépendances :**
    ```sh
    npm install
    ```

4.  **Lancer le serveur de développement :**
    ```sh
    npm run dev
    ```

L'application sera alors accessible à l'adresse indiquée dans le terminal (généralement `http://localhost:5173`).

## Configuration

L'application tente de se connecter à un serveur WebSocket à l'adresse `ws://192.168.196.15:8080/speedmessage/speedmessage`. Cette URL est codée en dur dans le fichier `speedometer-app/src/App.vue`. Modifiez-la si votre serveur WebSocket a une adresse différente.

Le serveur WebSocket doit envoyer des messages JSON avec la structure suivante :
```json
{
  "leftWheel": 80,
  "rightWheel": 82,
  "slip": false
}
```