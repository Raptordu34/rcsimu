package fr.ensma.a3.ia.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO pour les données du capteur MPU6050.
 * Structure JSON reçue du client RC.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MpuDataDTO {

    private float accelX;
    private float accelY;
    private float accelZ;
    private float gyroX;
    private float gyroY;
    private float gyroZ;
    private float temperature;
    private long timestampMs;

    public MpuDataDTO() {
    }

    public float getAccelX() {
        return accelX;
    }

    public void setAccelX(float accelX) {
        this.accelX = accelX;
    }

    public float getAccelY() {
        return accelY;
    }

    public void setAccelY(float accelY) {
        this.accelY = accelY;
    }

    public float getAccelZ() {
        return accelZ;
    }

    public void setAccelZ(float accelZ) {
        this.accelZ = accelZ;
    }

    public float getGyroX() {
        return gyroX;
    }

    public void setGyroX(float gyroX) {
        this.gyroX = gyroX;
    }

    public float getGyroY() {
        return gyroY;
    }

    public void setGyroY(float gyroY) {
        this.gyroY = gyroY;
    }

    public float getGyroZ() {
        return gyroZ;
    }

    public void setGyroZ(float gyroZ) {
        this.gyroZ = gyroZ;
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
            "MpuDataDTO{accel=(%.2f,%.2f,%.2f), gyro=(%.1f,%.1f,%.1f), temp=%.1f}",
            accelX, accelY, accelZ, gyroX, gyroY, gyroZ, temperature
        );
    }
}
