package fr.ensma.a3.ia.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO pour les données du capteur URM37 (ultrason).
 * Structure JSON reçue du client RC.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrmDataDTO {

    private float distanceCm;
    private float temperature;
    private long timestampMs;

    public UrmDataDTO() {
    }

    public float getDistanceCm() {
        return distanceCm;
    }

    public void setDistanceCm(float distanceCm) {
        this.distanceCm = distanceCm;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    @Override
    public String toString() {
        return String.format(
            "UrmDataDTO{distance=%.1fcm, temp=%.1f}",
            distanceCm, temperature
        );
    }
}
