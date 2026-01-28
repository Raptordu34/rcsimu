# WindowsInputAPIController

Composant JNI/FFM permettant l'interface entre Java et les API d'entrée Windows (DirectInput/XInput) via une DLL native.

## Prérequis

- **Windows 10/11**
- **Java 25** (JDK configuré dans le PATH)
- **Maven 3.9+**
- **CMake 3.15+**
- **Visual Studio Build Tools** (Workload "Desktop development with C++")

## Déploiement

### 1. Compilation de la DLL Native

Ouvrir le **Developer Command Prompt for VS**.

```cmd
cd windowsinputapicontroller
mkdir build
cd build
cmake ..
cmake --build .
```

### 2. Organisation des Fichiers

Pour le déploiement, il est recommandé de placer la DLL dans un dossier `lib` à la racine de l'exécution.

```powershell
mkdir ..\lib
copy bin\sim_input.dll ..\lib\
```

### 3. Compilation Java

Dans un terminal standard (PowerShell ou CMD) à la racine du module :

```powershell
cd ..
mvn clean install
```

### 4. Validation

Pour tester que le driver fonctionne correctement (chargement DLL, détection manette), utilisez l'outil de validation dans le dossier `simutest`.

Voir : `simu/simutest/README.md` -> Section **Test du Contrôleur**.
