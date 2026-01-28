# Raspberry Pi MPU6050 Business Layer (Java)

Ce module fournit une couche d'abstraction "métier" au-dessus du driver MPU6050. Il simplifie l'interaction avec le capteur en fournissant des données pré-traitées et packagées, prêtes à être consommées par l'application principale (simulateur, robot, etc.).

## ✨ Fonctionnalités

*   🧱 **Abstraction Haut-Niveau** : Masque la complexité de la communication I2C et de la configuration des registres.
*   📦 **Données Unifiées** : Fournit un objet `ProcessedMpuData` contenant toutes les métriques pertinentes (Accélération, Gyroscope, Température, Timestamp).
*   🛡️ **Gestion des Ressources** : Interface `AutoCloseable` pour une gestion propre des ressources via *try-with-resources*.
*   📝 **Journalisation** : Intégration de SLF4J pour le suivi des opérations.

## 🛠️ Prérequis

### Logiciel
*   **Java 21** ou supérieur
*   **Maven 3.8** ou supérieur
*   **Module Driver** : Le module `mpudriver` doit être installé dans votre référentiel Maven local.

### Dépendances
Ce projet dépend directement du module driver :
*   `fr.ensma.a3.ia:mpudriver:1.0-SNAPSHOT`

## 🚀 Installation et Compilation

Ce projet utilise Maven. 

> [!IMPORTANT]  
> **PRÉREQUIS INDISPENSABLE :** Vous devez impérativement compiler et installer le module **`mpudriver`** dans votre dépôt local Maven avant de tenter de compiler ce module.
>
> ```bash
> cd ../mpudriver
> mvn clean install
> ```

Une fois le driver installé, vous pouvez compiler ce module "Business" :

```bash
# Dans le dossier mpubusiness/
mvn clean install
```

Cela va générer le fichier JAR dans le dossier `target/` :
*   `mpu-business-1.0-SNAPSHOT.jar`

## 📦 Intégration dans votre projet

Pour utiliser cette couche métier dans votre application, ajoutez la dépendance suivante dans votre `pom.xml` :

```xml
<dependency>
    <groupId>fr.ensma.a3.ia</groupId>
    <artifactId>mpu-business</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## 💻 Exemple d'Utilisation

Voici comment utiliser le service `MpuBusiness` via son **Singleton** pour récupérer les données du capteur :

```java
import fr.ensma.a3.ia.mpubusiness.MpuBusiness;
import fr.ensma.a3.ia.mpubusiness.ProcessedMpuData;

public class Main {
    public static void main(String[] args) {
        
        try {
            // Récupération de l'instance unique du service (Singleton)
            MpuBusiness mpuService = MpuBusiness.getInstance();
            
            System.out.println("Démarrage de la lecture du MPU6050...");

            // Boucle de lecture
            for (int i = 0; i < 100; i++) {
                // Récupération des données traitées
                ProcessedMpuData data = mpuService.getData();

                if (data != null) {
                    System.out.println(data.toString());
                } else {
                    System.err.println("⚠️ Erreur de lecture.");
                }

                Thread.sleep(20);
            }
            
            // Fermeture propre à la fin de l'application
            mpuService.close();

        } catch (Exception e) {
            System.err.println("Erreur critique : " + e.getMessage());
        }
    }
}
```

## 📂 Structure du Projet

Les sources principales se trouvent dans `src/main/java/fr/ensma/a3/ia/mpubusiness/` :

*   `IMpuBusiness.java` : L'interface définissant le contrat du service.
*   `MpuBusiness.java` : L'implémentation concrète utilisant le `mpudriver`.
*   `ProcessedMpuData.java` : L'objet de transfert de données (DTO) contenant les valeurs physiques.