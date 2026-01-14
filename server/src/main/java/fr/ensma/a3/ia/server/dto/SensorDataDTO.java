package fr.ensma.a3.ia.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO pour les données agrégées de tous les capteurs.
 * Structure JSON reçue du client RC (AllSensorData).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorDataDTO {

    private long timestampMs;
    private MpuDataDTO mpuData;
    private UrmDataDTO urmData;

    public SensorDataDTO() {
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    public MpuDataDTO getMpuData() {
        return mpuData;
    }

    public void setMpuData(MpuDataDTO mpuData) {
        this.mpuData = mpuData;
    }

    public UrmDataDTO getUrmData() {
        return urmData;
    }

    public void setUrmData(UrmDataDTO urmData) {
        this.urmData = urmData;
    }

    /**
     * Vérifie si les données MPU sont présentes et valides.
     */
    public boolean hasMpuData() {
        return mpuData != null;
    }

    /**
     * Vérifie si les données URM sont présentes.
     */
    public boolean hasUrmData() {
        return urmData != null;
    }

    @Override
    public String toString() {
        return String.format(
            "SensorDataDTO{timestamp=%d, mpu=%s, urm=%s}",
            timestampMs,
            mpuData != null ? "OK" : "null",
            urmData != null ? "OK" : "null"
        );
    }
}
