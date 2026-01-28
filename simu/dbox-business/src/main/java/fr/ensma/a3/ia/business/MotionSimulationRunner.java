package fr.ensma.a3.ia.business;

import fr.ensma.a3.ia.business.model.DboxConfig;
import fr.ensma.a3.ia.business.model.ProcessedMotionData;
import fr.ensma.a3.ia.business.model.RawMotionData;
import fr.ensma.a3.ia.business.processor.MotionDataProcessor;
import fr.ensma.a3.ia.dbox.DboxController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulateur de mouvement pour tester la chaîne de transmission complète.
 *
 * Ce programme:
 * 1. Génère des données MPU synthétiques (sinusoidales)
 * 2. Les passe au MotionDataProcessor
 * 3. Envoie le résultat via DboxController (Panama FFM)
 *
 * Prérequis:
 * - Java 21+ avec --enable-native-access=ALL-UNNAMED
 * - DBOX Control Panel lancé avec hardware connecté
 * - DLLs disponibles: dbxLive64.dll, SimpleDboxAPI.dll, dbox_controller_wrapper.dll
 */
public class MotionSimulationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MotionSimulationRunner.class);

    public static void main(String[] args) {
        logger.info("=== D-BOX MOTION SIMULATION STARTED ===");

        // 1. Vérifier le chargement des DLLs
        if (!DboxController.isNativeLibraryLoaded()) {
            logger.error("ERREUR: DLLs natives non chargées! Cause: {}", DboxController.getNativeLibraryLoadError());
            return;
        }

        // 2. Initialisation des composants
        DboxConfig config = new DboxConfig();
        MotionDataProcessor processor = new MotionDataProcessor(config);

        // 3. Utilisation de try-with-resources pour auto-close
        try (DboxController dbox = new DboxController()) {

            // 4. Connexion au siège DBOX
            logger.info("Connecting to DBOX platform...");
            if (!dbox.connect()) {
                logger.error("Failed to connect: {}", dbox.getLastError());
                return;
            }
            logger.info("Connected!");

            // 5. Démarrage de la simulation (fade-in)
            logger.info("Starting motion simulation...");
            if (!dbox.start()) {
                logger.error("Failed to start: {}", dbox.getLastError());
                return;
            }
            logger.info("Motion started!");

            // 6. Boucle de simulation
            logger.info("Running simulation (10 seconds)...");

            long startTime = System.currentTimeMillis();
            double time = 0.0;

            while (time < 10.0) {
                // Temps simulé en secondes
                time = (System.currentTimeMillis() - startTime) / 1000.0;

                // --- GENERATION DONNEES BRUTES (Simulation) ---
                float accelX = (float) (Math.sin(time * 0.5) * 5.0);
                float accelY = (float) (Math.cos(time * 0.3) * 8.0);
                float accelZ = (float) (9.81 + Math.sin(time * 2.0) * 1.0);

                float gyroX = accelY * 2.0f;
                float gyroY = accelX * 2.0f;
                float gyroZ = 0.0f;

                RawMotionData rawData = new RawMotionData(
                    System.currentTimeMillis(),
                    accelX, accelY, accelZ,
                    gyroX, gyroY, gyroZ,
                    25.0f
                );

                // --- TRAITEMENT ---
                ProcessedMotionData processedData = processor.process(rawData);

                // --- ENVOI DIRECT VIA FFM ---
                if (processedData != null) {
                    dbox.update(
                        processedData.getRoll(),
                        processedData.getPitch(),
                        processedData.getHeave(),
                        processedData.getRpm(),
                        processedData.getTorque()
                    );

                    // Affichage périodique (toutes les ~1 seconde)
                    if ((System.currentTimeMillis() % 1000) < 20) {
                        logger.info("[SIM] Time: {}s | Roll:{} Pitch:{} Heave:{}",
                            String.format("%.1f", time),
                            String.format("%.2f", processedData.getRoll()),
                            String.format("%.2f", processedData.getPitch()),
                            String.format("%.2f", processedData.getHeave()));
                    }
                }

                // 50 Hz loop (20ms)
                Thread.sleep(20);
            }

            // 7. Retour à la position neutre
            logger.info("Returning to neutral position...");
            dbox.resetToNeutral();
            Thread.sleep(500);

            // 8. Arrêt de la simulation (fade-out)
            logger.info("Stopping motion...");
            dbox.stop();

        } catch (InterruptedException e) {
            logger.warn("Simulation interrupted.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Error during simulation", e);
        }

        logger.info("=== D-BOX MOTION SIMULATION ENDED ===");
    }
}
