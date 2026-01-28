package fr.ensma.a3.ia.urmdriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Urm37Data {
    private static final Logger logger = LoggerFactory.getLogger(Urm37Data.class);

    private float distanceCm;
    private float temperature;
    private long timestampMs;

    public Urm37Data() {
        this.timestampMs = System.currentTimeMillis();
    }

    // Getters et Setters
    public float getDistanceCm() { return distanceCm; }
    public void setDistanceCm(float distanceCm) { this.distanceCm = distanceCm; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public long getTimestampMs() { return timestampMs; }
    public void setTimestampMs(long timestampMs) { this.timestampMs = timestampMs; }

    public void printData() {
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Distance: %.1f cm | Temp: %.1f °C | Time: %d ms",
                          distanceCm, temperature, timestampMs));
        }
    }
}
