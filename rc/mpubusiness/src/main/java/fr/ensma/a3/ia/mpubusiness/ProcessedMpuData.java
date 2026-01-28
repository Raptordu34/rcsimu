package fr.ensma.a3.ia.mpubusiness;

/**
 * Données du capteur MPU6050.
 *
 * Contient les mesures brutes du capteur IMU (accéléromètre + gyroscope).
 */
public class ProcessedMpuData {

    private final float accelX;
    private final float accelY;
    private final float accelZ;
    private final float gyroX;
    private final float gyroY;
    private final float gyroZ;
    private final float temperature;
    private final long timestampMs;

    public ProcessedMpuData(float accelX, float accelY, float accelZ,
                            float gyroX, float gyroY, float gyroZ,
                            float temperature, long timestampMs) {
        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;
        this.gyroX = gyroX;
        this.gyroY = gyroY;
        this.gyroZ = gyroZ;
        this.temperature = temperature;
        this.timestampMs = timestampMs;
    }

    public float getAccelX() {
        return accelX;
    }

    public float getAccelY() {
        return accelY;
    }

    public float getAccelZ() {
        return accelZ;
    }

    public float getGyroX() {
        return gyroX;
    }

    public float getGyroY() {
        return gyroY;
    }

    public float getGyroZ() {
        return gyroZ;
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
            "ProcessedMpuData{accel=(%.2f,%.2f,%.2f)g, gyro=(%.1f,%.1f,%.1f)°/s, temp=%.1f°C, timestamp=%d}",
            accelX, accelY, accelZ,
            gyroX, gyroY, gyroZ,
            temperature, timestampMs
        );
    }
}
