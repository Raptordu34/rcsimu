package fr.ensma.a3.ia.rcclient.dto;

import fr.ensma.a3.ia.sensorsbusiness.AllSensorData;

/**
 * DTO pour sérialiser AllSensorData en JSON.
 */
public class AllSensorDataDTO {

    private long timestampMs;
    private MpuDataDTO mpuData;
    private UrmDataDTO urmData;

    public AllSensorDataDTO() {
    }

    public static AllSensorDataDTO fromAllSensorData(AllSensorData data) {
        if (data == null) return null;

        AllSensorDataDTO dto = new AllSensorDataDTO();
        dto.timestampMs = data.getTimestampMs();
        dto.mpuData = MpuDataDTO.fromProcessedData(data.getMpuData());
        dto.urmData = UrmDataDTO.fromProcessedData(data.getUrmData());
        return dto;
    }

    public long getTimestampMs() { return timestampMs; }
    public void setTimestampMs(long timestampMs) { this.timestampMs = timestampMs; }

    public MpuDataDTO getMpuData() { return mpuData; }
    public void setMpuData(MpuDataDTO mpuData) { this.mpuData = mpuData; }

    public UrmDataDTO getUrmData() { return urmData; }
    public void setUrmData(UrmDataDTO urmData) { this.urmData = urmData; }
}
