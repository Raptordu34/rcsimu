# SimuService

Point d'entrée principal ("Main Application") du simulateur côté PC.  
Ce service orchestre les communications entre :
- Le contrôleur de jeu (Volant/Manette) via `simucontrollerbusiness`
- La plateforme de mouvement D-BOX via `dbox-business`
- Le serveur central (ou Raspberry Pi) via WebSocket (`serviceapi`)

## Prérequis

- **Java 25**
- **Maven 3.9+**
- **D-BOX System** : `dbxLive64.dll` installée dans `ProgramData` (voir README `dbox-driver`).
- **Dépendances locales** : Les modules suivants doivent être compilés (`mvn install`) :
    - `windowsinputapicontroller` (Driver Entrées)
    - `simucontrollerbusiness` (Business Entrées)
    - `dbox-driver` (Driver Mouvement)
    - `dbox-business` (Business Mouvement)
    - `serviceapi` (Communication)

## Déploiement

### 1. Compilation

Compilez ce projet pour générer l'exécutable et rassembler les dépendances Java.

```powershell
mvn clean package
```

### 2. Aggregation des Bibliothèques Natives (CRITIQUE)

Ce projet est l'exécutable final. Il doit contenir **toutes** les DLLs natives utilisées par ses dépendances dans son répertoire d'exécution (dossier `lib/`).

Créez le dossier `lib` et copiez-y les fichiers nécessaires :

```powershell
mkdir lib

# 1. DLL du Driver Contrôleur (sim_input.dll)
copy ..\windowsinputapicontroller\build\bin\sim_input.dll lib\

# 2. DLLs du Driver D-BOX (dbox_controller_wrapper.dll, SimpleDboxAPI.dll, dbxLive64.dll)
copy ..\dbox-driver\lib\*.dll lib\
```

Votre dossier `lib/` doit contenir **4 fichiers DLL**.

### 3. Exécution

Lancer l'application en pointant Java vers le dossier `lib` pour les natives.

**Commande complète :**

```powershell
java --enable-native-access=ALL-UNNAMED `
     -cp "target/classes;target/dependency/*" `
     fr.ensma.a3.ia.simuservice.SimuServiceLauncher
```

**Options disponibles :**
- `--no-controller` : Démarre sans écouter le volant/manette.
- `--no-sensor` : Démarre sans se connecter aux capteurs de mouvement.
- `--no-dbox` : Démarre sans se connecter a la dbox.

Exemple : `... SimuServiceLauncher --no-sensor`