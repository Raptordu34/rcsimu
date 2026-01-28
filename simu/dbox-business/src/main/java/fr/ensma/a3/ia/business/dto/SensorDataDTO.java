package fr.ensma.a3.ia.business.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO pour les données agrégées de tous les capteurs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorDataDTO {

    private long timestampMs;
    private MpuDataDTO mpuData;

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

    public boolean hasMpuData() {
        return mpuData != null;
    }
}
