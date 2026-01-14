package fr.ensma.a3.ia.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ensma.a3.ia.business.api.IMotionService;
import fr.ensma.a3.ia.business.model.RawMotionData;
import fr.ensma.a3.ia.server.dto.MpuDataDTO;
import fr.ensma.a3.ia.server.dto.SensorDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorDataHandler {

    private static final Logger logger = LoggerFactory.getLogger(SensorDataHandler.class);

    private final ObjectMapper objectMapper;
    private final IMotionService motionService;

    private long messageCount = 0;
    private long lastLogTime = 0;

    public SensorDataHandler(IMotionService motionService) {
        this.objectMapper = new ObjectMapper();
        this.motionService = motionService;
    }

    public void handleMessage(String jsonMessage) {
        try {
            SensorDataDTO dto = objectMapper.readValue(jsonMessage, SensorDataDTO.class);
            messageCount++;

            // --- AFFICHAGE COMPLET (ACC + GYRO) ---
            if (dto.hasMpuData()) {
                MpuDataDTO mpu = dto.getMpuData();
                // Affichage Accéléromètre ET Gyroscope sur une ligne
                System.out.print(String.format("\rRECU >> #%d | Acc: [%.2f, %.2f, %.2f] | Gyro: [%.2f, %.2f, %.2f]      ",
                    messageCount,
                    mpu.getAccelX(),
                    mpu.getAccelY(),
                    mpu.getAccelZ(),
                    mpu.getGyroX(),
                    mpu.getGyroY(),
                    mpu.getGyroZ()
                ));
            }
            // --------------------------------------

            if (!dto.hasMpuData()) {
                return;
            }

            RawMotionData rawData = convertToRawMotionData(dto);

            // Traitement et envoi via le service métier
            if (!motionService.processAndSend(rawData)) {
                String error = motionService.getLastError();
                if (error != null && !error.isEmpty()) {
                    System.out.println();
                    logger.warn("Erreur de traitement: {}", error);
                }
            }

        } catch (Exception e) {
            System.out.println(); // Saut de ligne pour ne pas écraser l'erreur
            logger.error("Erreur de traitement du message: {}", e.getMessage());
        }
    }

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

    public long getMessageCount() {
        return messageCount;
    }
}
