# Raspberry Pi URM37 Business Layer (Java)

Ce module fournit une couche d'abstraction "métier" au-dessus du driver URM37. Il simplifie l'interaction avec le capteur ultrasonique en fournissant des données pré-traitées et packagées, prêtes à être consommées par l'application principale (simulateur, robot, etc.).

## ✨ Fonctionnalités

*   🧱 **Abstraction Haut-Niveau** : Masque la complexité de la communication série (UART) et de la gestion des ressources.
*   📦 **Données Unifiées** : Fournit un objet `ProcessedUrmData` contenant toutes les métriques pertinentes (Distance, Température, Timestamp).
*   🛡️ **Singleton & Thread-Safe** : Gestion centralisée de l'accès au port série pour éviter les conflits.
*   📝 **Journalisation** : Intégration de SLF4J pour le suivi des opérations.

## 🛠️ Prérequis

### Logiciel
*   **Java 21** ou supérieur
*   **Maven 3.8** ou supérieur
*   **Module Driver** : Le module `urmdriver` doit être installé dans votre référentiel Maven local.

### Dépendances
Ce projet dépend directement du module driver :
*   `fr.ensma.a3.ia:urm37-driver:1.0-SNAPSHOT`

## 🚀 Installation et Compilation

Ce projet utilise Maven.

> [!IMPORTANT]
> **PRÉREQUIS INDISPENSABLE :** Vous devez impérativement compiler et installer le module **`urmdriver`** dans votre dépôt local Maven avant de tenter de compiler ce module.
>
> ```bash
> cd ../urmdriver
> mvn clean install
> ```

Une fois le driver installé, vous pouvez compiler ce module "Business" :

```bash
# Dans le dossier urmbusiness/
mvn clean install
```

Cela va générer le fichier JAR dans le dossier `target/` :
*   `urm-business-1.0-SNAPSHOT.jar`

## 📦 Intégration dans votre projet

Pour utiliser cette couche métier dans votre application, ajoutez la dépendance suivante dans votre `pom.xml` :

```xml
<dependency>
    <groupId>fr.ensma.a3.ia</groupId>
    <artifactId>urm-business</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## 💻 Exemple d'Utilisation

Voici comment utiliser le service `UrmBusiness` via son **Singleton** pour récupérer les données du capteur :

```java
import fr.ensma.a3.ia.urmbusiness.UrmBusiness;
import fr.ensma.a3.ia.urmbusiness.ProcessedUrmData;

public class Main {
    public static void main(String[] args) {
        
        try {
            // Récupération de l'instance unique du service (Singleton)
            // Par défaut sur /dev/ttyS0, utilisez getInstance("/dev/ttyUSB0") si besoin
            UrmBusiness urmService = UrmBusiness.getInstance();
            
            System.out.println("Démarrage de la lecture du URM37...");

            // Boucle de lecture
            for (int i = 0; i < 20; i++) {
                // Récupération des données traitées
                ProcessedUrmData data = urmService.getData();

                if (data != null) {
                    System.out.printf("Distance: %.1f cm | Temp: %.1f °C%n", 
                        data.getDistanceCm(), data.getTemperature());
                } else {
                    System.err.println("⚠️ Erreur de lecture.");
                }

                // Pause pour respecter la cadence du capteur
                Thread.sleep(100);
            }
            
            // Fermeture propre à la fin de l'application
            urmService.close();

        } catch (Exception e) {
            System.err.println("Erreur critique : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## 📂 Structure du Projet

Les sources principales se trouvent dans `src/main/java/fr/ensma/a3/ia/urmbusiness/` :

*   `IUrmBusiness.java` : L'interface définissant le contrat du service.
*   `UrmBusiness.java` : L'implémentation concrète utilisant le `urmdriver` (Singleton).
*   `ProcessedUrmData.java` : L'objet de transfert de données (DTO) contenant les valeurs physiques.