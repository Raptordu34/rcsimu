package fr.ensma.a3.ia.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ensma.a3.ia.business.model.ProcessedMotionData;
import fr.ensma.a3.ia.business.model.RawMotionData;
import fr.ensma.a3.ia.business.processor.MotionDataProcessor;
import fr.ensma.a3.ia.dbox.DboxController;
import fr.ensma.a3.ia.server.dto.MpuDataDTO;
import fr.ensma.a3.ia.server.dto.SensorDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler pour traiter les données capteurs reçues via WebSocket.
 *
 * Responsabilités:
 * 1. Désérialiser le JSON en DTO
 * 2. Convertir le DTO en RawMotionData
 * 3. Traiter via MotionDataProcessor
 * 4. Envoyer les commandes à la D-BOX
 */
public class SensorDataHandler {

    private static final Logger logger = LoggerFactory.getLogger(SensorDataHandler.class);

    private final ObjectMapper objectMapper;
    private final MotionDataProcessor processor;
    private final DboxController dboxController;
    private final boolean dboxEnabled;

    private long messageCount = 0;
    private long lastLogTime = 0;

    /**
     * Crée un handler avec traitement D-BOX activé.
     *
     * @param processor Processeur de données motion
     * @param dboxController Contrôleur D-BOX
     */
    public SensorDataHandler(MotionDataProcessor processor, DboxController dboxController) {
        this.objectMapper = new ObjectMapper();
        this.processor = processor;
        this.dboxController = dboxController;
        this.dboxEnabled = (dboxController != null);
    }

    /**
     * Crée un handler sans D-BOX (mode test/log uniquement).
     *
     * @param processor Processeur de données motion
     */
    public SensorDataHandler(MotionDataProcessor processor) {
        this(processor, null);
    }

    /**
     * Traite un message JSON reçu du client RC.
     *
     * @param jsonMessage Message JSON contenant SensorDataDTO
     */
    public void handleMessage(String jsonMessage) {
        try {
            // 1. Désérialisation JSON → DTO
            SensorDataDTO sensorData = objectMapper.readValue(jsonMessage, SensorDataDTO.class);

            // 2. Vérification des données MPU (obligatoires pour le mouvement)
            if (!sensorData.hasMpuData()) {
                logger.warn("Message sans données MPU, ignoré");
                return;
            }

            // 3. Conversion DTO → RawMotionData
            RawMotionData rawData = convertToRawMotionData(sensorData);

            // 4. Traitement via MotionDataProcessor
            ProcessedMotionData processed = processor.process(rawData);

            if (processed == null) {
                logger.warn("Traitement a retourné null");
                return;
            }

            // 5. Envoi à la D-BOX (si activé)
            if (dboxEnabled) {
                sendToDbox(processed);
            }

            // 6. Log périodique (toutes les secondes)
            logPeriodically(processed);

        } catch (Exception e) {
            logger.error("Erreur de traitement du message: {}", e.getMessage());
        }
    }

    /**
     * Convertit le DTO en RawMotionData pour le processeur.
     */
    private RawMotionData convertToRawMotionData(SensorDataDTO dto) {
        MpuDataDTO mpu = dto.getMpuData();
        return new RawMotionData(
            mpu.getTimestampMs(),
            mpu.getAccelX(),
            mpu.getAccelY(),
            mpu.getAccelZ(),
            mpu.getGyroX(),
            mpu.getGyroY(),
            mpu.getGyroZ(),
            mpu.getTemperature()
        );
    }

    /**
     * Envoie les données traitées à la D-BOX.
     */
    private void sendToDbox(ProcessedMotionData data) {
        try {
            dboxController.update(
                data.getRoll(),
                data.getPitch(),
                data.getHeave(),
                data.getRpm(),
                data.getTorque()
            );
        } catch (Exception e) {
            logger.error("Erreur d'envoi à la D-BOX: {}", e.getMessage());
        }
    }

    /**
     * Log périodique pour éviter de spammer les logs.
     */
    private void logPeriodically(ProcessedMotionData data) {
        messageCount++;
        long now = System.currentTimeMillis();

        if (now - lastLogTime >= 1000) {
            logger.info("Messages reçus: {} | roll={}, pitch={}, heave={}",
                messageCount,
                String.format("%.2f", data.getRoll()),
                String.format("%.2f", data.getPitch()),
                String.format("%.2f", data.getHeave()));
            lastLogTime = now;
        }
    }

    /**
     * Retourne le nombre de messages traités.
     */
    public long getMessageCount() {
        return messageCount;
    }
}
