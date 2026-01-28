# RCService

Ce projet implémente la couche service du côté de la voiture télécommandée (RC).

## Dépendances du projet

- javacvwebcamframestream

## Comment compiler

Avant de compiler le projet, veuillez vous assurer que les projets dont dépend RCService ont été construits (`mvn clean install`).

```
mvn clean package
```

## Comment exécuter

S'assurer que le projet SimuRCServer est déployé : ws://localhost:8026/simurcserver

```
java -cp "target/classes:target/dependency/*" fr.ensma.a3.ia.rcservice.RCServiceLauncher
```