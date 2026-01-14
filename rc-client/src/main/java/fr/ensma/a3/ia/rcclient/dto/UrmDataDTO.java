package fr.ensma.a3.ia.rcclient.dto;

import fr.ensma.a3.ia.urmbusiness.ProcessedUrmData;

/**
 * DTO pour sérialiser les données URM37 en JSON.
 */
public class UrmDataDTO {

    private float distanceCm;
    private float temperature;
    private long timestampMs;

    public UrmDataDTO() {
    }

    public static UrmDataDTO fromProcessedData(ProcessedUrmData data) {
        if (data == null) return null;

        UrmDataDTO dto = new UrmDataDTO();
        dto.distanceCm = data.getDistanceCm();
        dto.temperature = data.getTemperature();
        dto.timestampMs = data.getTimestampMs();
        return dto;
    }

    public float getDistanceCm() { return distanceCm; }
    public void setDistanceCm(float distanceCm) { this.distanceCm = distanceCm; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public long getTimestampMs() { return timestampMs; }
    public void setTimestampMs(long timestampMs) { this.timestampMs = timestampMs; }
}
