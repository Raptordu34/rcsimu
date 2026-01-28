# SimuControllerBusiness

Couche métier ("Business") du projet. Elle consomme le driver `windowsinputapicontroller` pour fournir des commandes de haut niveau (direction, accélération, freinage) indépendantes du matériel.

## Prérequis

- **Java 25**
- **Maven 3.9+**
- Le module `windowsinputapicontroller` doit être installé localement (`mvn install`).

## Déploiement

### 1. Installation des Dépendances

Assurez-vous d'avoir compilé et installé le driver :

```powershell
cd ../windowsinputapicontroller
mvn clean install
```

### 2. Compilation du Business

Dans ce répertoire :

```powershell
mvn clean install
```

### 3. Préparation à l'Exécution (DLL)

Le driver a besoin de la DLL native (`sim_input.dll`) pour fonctionner.
Créez un dossier `lib` et copiez-y la DLL générée par le projet driver.

```powershell
mkdir lib
copy ..\windowsinputapicontroller\build\bin\sim_input.dll lib\
```

### 4. Validation

Pour valider le bon fonctionnement de la couche métier et la réception des entrées :

Voir : `simu/simutest/README.md` -> Section **Test du Contrôleur**.