package fr.ensma.a3.ia.rcclient.dto;

import fr.ensma.a3.ia.mpubusiness.ProcessedMpuData;

/**
 * DTO pour sérialiser les données MPU6050 en JSON.
 */
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

    public static MpuDataDTO fromProcessedData(ProcessedMpuData data) {
        if (data == null) return null;

        MpuDataDTO dto = new MpuDataDTO();
        dto.accelX = data.getAccelX();
        dto.accelY = data.getAccelY();
        dto.accelZ = data.getAccelZ();
        dto.gyroX = data.getGyroX();
        dto.gyroY = data.getGyroY();
        dto.gyroZ = data.getGyroZ();
        dto.temperature = data.getTemperature();
        dto.timestampMs = data.getTimestampMs();
        return dto;
    }

    public float getAccelX() { return accelX; }
    public void setAccelX(float accelX) { this.accelX = accelX; }

    public float getAccelY() { return accelY; }
    public void setAccelY(float accelY) { this.accelY = accelY; }

    public float getAccelZ() { return accelZ; }
    public void setAccelZ(float accelZ) { this.accelZ = accelZ; }

    public float getGyroX() { return gyroX; }
    public void setGyroX(float gyroX) { this.gyroX = gyroX; }

    public float getGyroY() { return gyroY; }
    public void setGyroY(float gyroY) { this.gyroY = gyroY; }

    public float getGyroZ() { return gyroZ; }
    public void setGyroZ(float gyroZ) { this.gyroZ = gyroZ; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public long getTimestampMs() { return timestampMs; }
    public void setTimestampMs(long timestampMs) { this.timestampMs = timestampMs; }
}
