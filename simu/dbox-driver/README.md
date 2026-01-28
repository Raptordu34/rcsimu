# DBOX Driver

Driver Java (FFM/Panama) pour le contrôle des plateformes de mouvement **D-BOX**.  
Gère la communication bas niveau avec le SDK natif C++ pour le contrôle des axes (Roll, Pitch, Heave) et des vibrations.

## Prérequis

- **Windows 10/11 64-bit**
- **Java 25**
- **Maven 3.9+**
- **D-BOX Control Panel** (installé et fonctionnel avec le matériel)
- **Visual Studio** (pour la compilation C++)

## Installation et Déploiement

### 1. Compilation Native (C++)

> **Important :** À exécuter depuis le **Developer Command Prompt for VS**.

```batch
cd scripts
compile_wrapper.bat
```
*Génère les DLLs dans `dbox-driver/lib/`.*

### 2. Compilation Java

```powershell
mvn clean install
```

### 3. Installation Système (CRITIQUE)

Pour que le driver fonctionne, une DLL spécifique doit être placée dans un dossier système global.

**Pourquoi ?**  
Le runtime D-BOX (`dbxLive64.dll`) tente d'accéder à des ressources dans `ProgramData`. En environnement Java, si la DLL n'est pas déjà présente dans ce dossier précis, le chargement échoue immédiatement (pas de fallback automatique comme en C++ pur).

**Commande d'installation (Admin) :**

```batch
mkdir "C:\ProgramData\D-BOX\Gaming\LiveMotion\LPSIMUDevSim" 2>nul
copy /Y "lib\dbxLive64.dll" "C:\ProgramData\D-BOX\Gaming\LiveMotion\LPSIMUDevSim\dbxLive64.dll"
```

### 4. Déploiement Runtime

Pour toute application utilisant ce driver :
1.  Créer un dossier `lib/` à côté de l'exécutable/JAR.
2.  Y copier les 3 DLLs du driver :
    -   `dbox_controller_wrapper.dll`
    -   `SimpleDboxAPI.dll`
    -   `dbxLive64.dll`

## Utilisation Maven

```xml
<dependency>
    <groupId>fr.ensma.a3.ia</groupId>
    <artifactId>dbox-driver</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```