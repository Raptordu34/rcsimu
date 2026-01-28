package fr.ensma.a3.ia.business.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ensma.a3.ia.business.api.IMotionDataProcessor;
import fr.ensma.a3.ia.business.api.IMotionService;
import fr.ensma.a3.ia.business.dto.MpuDataDTO;
import fr.ensma.a3.ia.business.dto.SensorDataDTO;
import fr.ensma.a3.ia.business.model.DboxConfig;
import fr.ensma.a3.ia.business.model.ProcessedMotionData;
import fr.ensma.a3.ia.business.model.RawMotionData;
import fr.ensma.a3.ia.business.processor.MotionDataProcessor;
import fr.ensma.a3.ia.dbox.DboxController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implémentation du service de contrôle de mouvement DBOX
 *
 * Cette classe encapsule :
 * - Le processeur de données (MotionDataProcessor)
 * - Le contrôleur DBOX (DboxController)
 *
 * Elle permet au serveur de n'avoir qu'une seule dépendance vers la couche business,
 * sans accéder directement au driver.
 */
public class MotionService implements IMotionService, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MotionService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final IMotionDataProcessor processor;
    private final DboxController dboxController;
    private final boolean dboxEnabled;

    private DboxConfig config;
    private boolean running;
    private String lastError;

    private long processedPacketCount;
    private long errorCount;

    /**
     * Constructeur avec DBOX activée
     */
    public MotionService() {
        this(true);
    }

    /**
     * Constructeur avec option d'activation DBOX
     *
     * @param enableDbox true pour activer la connexion DBOX, false pour mode test
     */
    public MotionService(boolean enableDbox) {
        this.processor = new MotionDataProcessor();
        this.config = new DboxConfig();
        this.running = false;
        this.lastError = "";
        this.processedPacketCount = 0;
        this.errorCount = 0;

        if (enableDbox) {
            DboxController controller = null;
            try {
                controller = new DboxController();
                logger.info("DboxController créé avec succès");
            } catch (Exception e) {
                logger.warn("Impossible de créer DboxController: {}. Mode test activé.", e.getMessage());
                this.lastError = e.getMessage();
            }
            this.dboxController = controller;
            this.dboxEnabled = (controller != null);
        } else {
            this.dboxController = null;
            this.dboxEnabled = false;
            logger.info("Mode test activé (DBOX désactivée)");
        }
    }

    // ===========================================================================
    // GESTION DU CYCLE DE VIE
    // ===========================================================================

    @Override
    public boolean start() {
        if (running) {
            throw new IllegalStateException("Le service est déjà démarré");
        }

        logger.info("Démarrage du MotionService...");

        if (dboxEnabled && dboxController != null) {
            try {
                if (!dboxController.connect()) {
                    lastError = "Échec de connexion DBOX: " + dboxController.getLastError();
                    logger.error(lastError);
                    return false;
                }

                if (!dboxController.start()) {
                    lastError = "Échec de démarrage DBOX: " + dboxController.getLastError();
                    logger.error(lastError);
                    dboxController.disconnect();
                    return false;
                }

                logger.info("DBOX connectée et démarrée");
            } catch (Exception e) {
                lastError = "Exception lors du démarrage DBOX: " + e.getMessage();
                logger.error(lastError, e);
                return false;
            }
        } else {
            logger.info("Mode test: DBOX non connectée");
        }

        processor.reset();
        running = true;
        logger.info("MotionService démarré avec succès");
        return true;
    }

    @Override
    public boolean stop() {
        if (!running) {
            return true;
        }

        logger.info("Arrêt du MotionService...");

        if (dboxEnabled && dboxController != null) {
            try {
                dboxController.stop();
                dboxController.disconnect();
                logger.info("DBOX arrêtée et déconnectée");
            } catch (Exception e) {
                lastError = "Erreur lors de l'arrêt DBOX: " + e.getMessage();
                logger.error(lastError, e);
            }
        }

        processor.reset();
        running = false;
        logger.info("MotionService arrêté");
        return true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Vérifie si la DBOX est activée et connectée
     *
     * @return true si la DBOX est active
     */
    public boolean isDboxEnabled() {
        return dboxEnabled;
    }

    // ===========================================================================
    // TRAITEMENT DES DONNEES
    // ===========================================================================

    @Override
    public boolean processAndSend(RawMotionData rawData) {
        if (!running) {
            throw new IllegalStateException("Le service n'est pas démarré");
        }

        if (rawData == null) {
            return false;
        }

        try {
            // 1. Traitement des données brutes
            ProcessedMotionData processed = processor.process(rawData);

            if (processed == null) {
                return false;
            }

            // 2. Envoi à la DBOX (si activée)
            if (dboxEnabled && dboxController != null) {
                dboxController.update(
                    processed.getRoll(),
                    processed.getPitch(),
                    processed.getHeave(),
                    processed.getRpm(),
                    processed.getTorque()
                );
            }

            processedPacketCount++;
            return true;

        } catch (Exception e) {
            errorCount++;
            lastError = "Erreur de traitement: " + e.getMessage();
            logger.error(lastError, e);
            return false;
        }
    }

    @Override
    public boolean processAndSend(String jsonMessage) {
        if (!running) {
            throw new IllegalStateException("Le service n'est pas démarré");
        }

        if (jsonMessage == null || jsonMessage.isBlank()) {
            return false;
        }

        // Ignorer les messages non-JSON (ex: "IMU + Ultrasound")
        String trimmed = jsonMessage.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            logger.debug("Message ignoré (non-JSON): {}", trimmed);
            return false;
        }

        try {
            SensorDataDTO sensorData = objectMapper.readValue(jsonMessage, SensorDataDTO.class);

            if (!sensorData.hasMpuData()) {
                logger.debug("Message JSON reçu sans données MPU");
                return false;
            }

            MpuDataDTO mpu = sensorData.getMpuData();
            RawMotionData rawData = new RawMotionData(
                mpu.getTimestampMs(),
                mpu.getAccelX(),
                mpu.getAccelY(),
                mpu.getAccelZ(),
                mpu.getGyroX(),
                mpu.getGyroY(),
                mpu.getGyroZ(),
                mpu.getTemperature()
            );

            return processAndSend(rawData);

        } catch (Exception e) {
            errorCount++;
            lastError = "Erreur de parsing JSON: " + e.getMessage();
            logger.error(lastError, e);
            return false;
        }
    }

    @Override
    public boolean sendNeutralPosition() {
        if (!running) {
            return false;
        }

        if (dboxEnabled && dboxController != null) {
            try {
                dboxController.resetToNeutral();
                return true;
            } catch (Exception e) {
                lastError = "Erreur envoi position neutre: " + e.getMessage();
                logger.error(lastError, e);
                return false;
            }
        }
        return true;
    }

    // ===========================================================================
    // CONFIGURATION
    // ===========================================================================

    @Override
    public DboxConfig getConfig() {
        return config.copy();
    }

    @Override
    public void updateConfig(DboxConfig newConfig) {
        if (newConfig == null) {
            throw new IllegalArgumentException("La configuration ne peut pas être null");
        }
        this.config = newConfig.copy();

        // Appliquer les paramètres moteur si la DBOX est active
        if (dboxEnabled && dboxController != null && running) {
            try {
                dboxController.setEngineRange(config.getEngineIdleRpm(), config.getEngineMaxRpm());
                dboxController.setMaxTorque(config.getEngineMaxTorque());
            } catch (Exception e) {
                logger.warn("Impossible d'appliquer la config moteur: {}", e.getMessage());
            }
        }
    }

    // ===========================================================================
    // DIAGNOSTICS ET MONITORING
    // ===========================================================================

    @Override
    public String getLastError() {
        return lastError;
    }

    @Override
    public long getProcessedPacketCount() {
        return processedPacketCount;
    }

    @Override
    public long getErrorCount() {
        return errorCount;
    }

    @Override
    public void resetStatistics() {
        processedPacketCount = 0;
        errorCount = 0;
        lastError = "";
    }

    // ===========================================================================
    // AUTOCLOSEABLE
    // ===========================================================================

    @Override
    public void close() {
        if (running) {
            stop();
        }
        if (dboxController != null) {
            try {
                dboxController.close();
            } catch (Exception e) {
                logger.error("Erreur lors de la fermeture du DboxController", e);
            }
        }
    }
}
