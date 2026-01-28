# SimuRCServer

Ce projet agit comme un relais afin que le composant RC et Simu puisse communiquer car la communication directe est impossible. Chaque fonctionnalité (capteurs, contrôle, webcam, données) sont indépendants afin que les fréquences de rafraichissement puissent être différentes. SimuRCServer joue le rôle de passe-plat avec comme fonctionnement de n'autoriser qu'un émetteur et plusieurs récepteurs.

## Comment compiler

Avant de compiler le projet, veuillez vous assurer que les projets dont dépend RCService ont été construits (`mvn clean install`).

```
mvn clean package
```

## Comment exécuter

```
mvn clean liberty:run
```

## Comment construire l'image Docker

```
docker build --tag simurcserver .
```
