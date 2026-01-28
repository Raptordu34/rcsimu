package fr.ensma.a3.ia.sensorsbusiness;

import fr.ensma.a3.ia.mpubusiness.ProcessedMpuData;
import fr.ensma.a3.ia.urmbusiness.ProcessedUrmData;

/**
 * Données combinées de tous les capteurs.
 *
 * Contient les données du MPU6050 (IMU) et de l'URM37 (ultrason).
 * Chaque capteur peut être null en cas d'erreur.
 */
public class AllSensorData {

    private final ProcessedMpuData mpuData;
    private final ProcessedUrmData urmData;
    private final long timestampMs;

    public AllSensorData(ProcessedMpuData mpuData, ProcessedUrmData urmData) {
        this.mpuData = mpuData;
        this.urmData = urmData;
        this.timestampMs = System.currentTimeMillis();
    }

    /**
     * @return Données du MPU6050, ou null si capteur en erreur
     */
    public ProcessedMpuData getMpuData() {
        return mpuData;
    }

    /**
     * @return Données de l'URM37, ou null si capteur en erreur
     */
    public ProcessedUrmData getUrmData() {
        return urmData;
    }

    /**
     * @return Timestamp de l'agrégation (ms)
     */
    public long getTimestampMs() {
        return timestampMs;
    }

    /**
     * Vérifie si au moins un capteur a des données valides.
     *
     * @return true si au moins un capteur est opérationnel
     */
    public boolean hasValidData() {
        return mpuData != null || urmData != null;
    }

    /**
     * @return true si les données MPU sont disponibles
     */
    public boolean hasMpuData() {
        return mpuData != null;
    }

    /**
     * @return true si les données URM sont disponibles
     */
    public boolean hasUrmData() {
        return urmData != null;
    }

    // =========================================================================
    // Accesseurs directs aux valeurs MPU (encapsulation)
    // =========================================================================

    /**
     * @return Acceleration X (g), ou 0 si MPU indisponible
     */
    public float getMpuAccelX() {
        return mpuData != null ? mpuData.getAccelX() : 0;
    }

    /**
     * @return Acceleration Y (g), ou 0 si MPU indisponible
     */
    public float getMpuAccelY() {
        return mpuData != null ? mpuData.getAccelY() : 0;
    }

    /**
     * @return Acceleration Z (g), ou 0 si MPU indisponible
     */
    public float getMpuAccelZ() {
        return mpuData != null ? mpuData.getAccelZ() : 0;
    }

    /**
     * @return Vitesse angulaire X (deg/s), ou 0 si MPU indisponible
     */
    public float getMpuGyroX() {
        return mpuData != null ? mpuData.getGyroX() : 0;
    }

    /**
     * @return Vitesse angulaire Y (deg/s), ou 0 si MPU indisponible
     */
    public float getMpuGyroY() {
        return mpuData != null ? mpuData.getGyroY() : 0;
    }

    /**
     * @return Vitesse angulaire Z (deg/s), ou 0 si MPU indisponible
     */
    public float getMpuGyroZ() {
        return mpuData != null ? mpuData.getGyroZ() : 0;
    }

    /**
     * @return Temperature (C), ou 0 si MPU indisponible
     */
    public float getMpuTemperature() {
        return mpuData != null ? mpuData.getTemperature() : 0;
    }

    /**
     * @return Timestamp MPU (ms), ou 0 si MPU indisponible
     */
    public long getMpuTimestampMs() {
        return mpuData != null ? mpuData.getTimestampMs() : 0;
    }

    // =========================================================================
    // Accesseurs directs aux valeurs URM (encapsulation)
    // =========================================================================

    /**
     * @return Distance mesuree (cm), ou -1 si URM indisponible
     */
    public float getUrmDistanceCm() {
        return urmData != null ? urmData.getDistanceCm() : -1;
    }

    /**
     * @return Timestamp URM (ms), ou 0 si URM indisponible
     */
    public long getUrmTimestampMs() {
        return urmData != null ? urmData.getTimestampMs() : 0;
    }

    @Override
    public String toString() {
        return String.format(
            "AllSensorData{mpu=%s, urm=%s, timestamp=%d}",
            mpuData != null ? "OK" : "ERROR",
            urmData != null ? "OK" : "ERROR",
            timestampMs
        );
    }
}
