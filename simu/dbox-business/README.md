# DBOX Business

Couche métier ("Business") du simulateur D-BOX.  
Elle reçoit les données brutes (accéléromètre/gyroscope via WebSocket ou simulation), applique le traitement du signal (fusion, lissage, s-curve), et pilote le siège via le driver `dbox-driver`.

## Prérequis

- **Java 25**
- **Maven 3.9+**
- Le module `dbox-driver` doit être installé localement.

## Déploiement

### 1. Installation du Driver

Le driver est une dépendance directe. Il doit être compilé et installé dans le repository Maven local.

```powershell
cd ../dbox-driver
mvn clean install
```

### 2. Compilation du Business

Dans ce répertoire (`dbox-business`) :

```powershell
mvn clean install
```

### 3. Préparation à l'Exécution (DLLs)

Le driver utilisé par ce module a besoin des DLLs natives.  
Pour l'exécution, **toutes les DLLs** doivent être présentes dans un dossier `lib/` à la racine de l'exécution.

```powershell
mkdir lib
copy ..\dbox-driver\lib\*.dll lib\
```
*(Cela copiera `dbox_controller_wrapper.dll`, `SimpleDboxAPI.dll` et `dbxLive64.dll`)*

> **Rappel :** N'oubliez pas l'installation système de `dbxLive64.dll` dans `ProgramData` (voir README du `dbox-driver`).

### 4. Exécution (Test de Simulation)

Lancer le runner de simulation intégré :

```powershell
mvn exec:java -Dexec.mainClass="fr.ensma.a3.ia.business.MotionSimulationRunner" `
    --enable-native-access=ALL-UNNAMED
```

## Intégration Maven

```xml
<dependency>
    <groupId>fr.ensma.a3.ia</groupId>
    <artifactId>dbox-business</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```