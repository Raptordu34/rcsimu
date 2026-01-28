# Raspberry Pi Sensor Aggregator (Java)

Ce module est un agrégateur de données qui unifie les flux provenant des modules métier **MPU6050** (Business) et **URM37** (Business). Il permet de récupérer, en un seul appel, une vue complète de l'état des capteurs du système.

## ✨ Fonctionnalités

*   🔗 **Fusion de Données** : Combine les objets `ProcessedMpuData` et `ProcessedUrmData` en un unique objet `AllSensorData`.
*   🛡️ **Résilience** : Gestion d'erreur granulaire. Si un capteur échoue, l'agrégateur continue de fournir les données des capteurs valides (les données manquantes sont `null`).
*   ⚡ **Centralisation** : Simplifie l'architecture en offrant un point d'entrée unique pour l'initialisation et la lecture de tous les capteurs.
*   📝 **Journalisation** : Intégration de SLF4J pour le suivi global des capteurs.

## 🛠️ Prérequis

### Logiciel
*   **Java 17** ou supérieur
*   **Maven 3.8** ou supérieur
*   **Modules Business** : Les modules `mpu-business` et `urm-business` doivent être installés dans votre référentiel Maven local.

### Dépendances
Ce projet dépend directement des couches business :
*   `fr.ensma.a3.ia:mpu-business:1.0-SNAPSHOT`
*   `fr.ensma.a3.ia:urm-business:1.0-SNAPSHOT`

## 🚀 Installation et Compilation

Ce projet utilise Maven.

> [!IMPORTANT]
> **ORDRE DE COMPILATION :** Assurez-vous d'avoir compilé et installé les dépendances dans l'ordre suivant :
> 1. `mpudriver` & `urmdriver`
> 2. `mpubusiness` & `urmbusiness`
> 3. Enfin, ce module `sensorsbusiness`

```bash
# Compilation des dépendances (si ce n'est pas déjà fait)
cd ../mpubusiness && mvn clean install
cd ../urmbusiness && mvn clean install

# Compilation de l'agrégateur
cd ../sensorsbusiness
mvn clean install
```

Cela va générer le fichier JAR dans le dossier `target/` :
*   `sensor-aggregator-1.0-SNAPSHOT.jar`

## 📦 Intégration dans votre projet

Pour utiliser cet agrégateur dans votre application, ajoutez la dépendance suivante dans votre `pom.xml` :

```xml
<dependency>
    <groupId>fr.ensma.a3.ia</groupId>
    <artifactId>sensor-aggregator</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## 💻 Exemple d'Utilisation

Voici comment utiliser le `SensorAggregator` pour surveiller l'ensemble du système :

```java
import fr.ensma.a3.ia.sensorsbusiness.SensorAggregator;
import fr.ensma.a3.ia.sensorsbusiness.AllSensorData;
import fr.ensma.a3.ia.mpubusiness.ProcessedMpuData;
import fr.ensma.a3.ia.urmbusiness.ProcessedUrmData;

public class Main {
    public static void main(String[] args) {
        
        // Initialisation de l'agrégateur (MPU sur Bus 1, URM sur /dev/ttyS0)
        try (SensorAggregator aggregator = new SensorAggregator(1, "/dev/ttyS0")) {
            
            System.out.println("Démarrage de la surveillance des capteurs...");

            for (int i = 0; i < 50; i++) {
                // Lecture unifiée
                AllSensorData globalData = aggregator.getAllData();

                if (globalData != null && globalData.hasValidData()) {
                    
                    // Accès aux données MPU6050
                    ProcessedMpuData mpu = globalData.getMpuData();
                    if (mpu != null) {
                        System.out.printf("[MPU] Accel Z: %.2fg | ", mpu.getAccelZ());
                    } else {
                        System.out.print("[MPU] ERREUR | ");
                    }

                    // Accès aux données URM37
                    ProcessedUrmData urm = globalData.getUrmData();
                    if (urm != null) {
                        System.out.printf("[URM] Dist: %.1f cm%n", urm.getDistanceCm());
                    } else {
                        System.out.println("[URM] ERREUR");
                    }

                } else {
                    System.err.println("⚠️ Panne générale des capteurs !");
                }

                Thread.sleep(100);
            }
            
        } catch (Exception e) {
            System.err.println("Erreur critique : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## 📂 Structure du Projet

Les sources principales se trouvent dans `src/main/java/fr/ensma/a3/ia/sensorsbusiness/` :

*   `ISensorAggregator.java` : Interface définissant le contrat de l'agrégateur.
*   `SensorAggregator.java` : Implémentation qui instancie et gère les services `MpuBusiness` et `UrmBusiness`.
*   `AllSensorData.java` : Objet conteneur (DTO) regroupant les résultats des deux capteurs.