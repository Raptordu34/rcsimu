package fr.ensma.a3.ia.urmbusiness;

/**
 * Données du capteur URM37.
 *
 * Contient les mesures brutes du capteur ultrasonique.
 */
public class ProcessedUrmData {

    private final float distanceCm;
    private final float temperature;
    private final long timestampMs;

    public ProcessedUrmData(float distanceCm, float temperature, long timestampMs) {
        this.distanceCm = distanceCm;
        this.temperature = temperature;
        this.timestampMs = timestampMs;
    }

    public float getDistanceCm() {
        return distanceCm;
    }

    public float getTemperature() {
        return temperature;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    @Override
    public String toString() {
        return String.format(
            "ProcessedUrmData{distance=%.1fcm, temp=%.1f°C, timestamp=%d}",
            distanceCm, temperature, timestampMs
        );
    }
}
