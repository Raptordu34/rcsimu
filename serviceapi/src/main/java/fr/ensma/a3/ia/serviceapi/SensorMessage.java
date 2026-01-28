package fr.ensma.a3.ia.serviceapi;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class SensorMessage {

    private float aX;

    private float aY;

    private float aZ;

    private float gX;

    private float gY;

    private float gZ;

    private float temperature;

    private long timestampMs;

    public float getaX() {
        return aX;
    }

    public float getaY() {
        return aY;
    }

    public float getaZ() {
        return aZ;
    }

    public float getgX() {
        return gX;
    }

    public float getgY() {
        return gY;
    }

    public float getgZ() {
        return gZ;
    }

    public float getTemperature() {
        return temperature;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setaX(float aX) {
        this.aX = aX;
    }

    public void setaY(float aY) {
        this.aY = aY;
    }

    public void setaZ(float aZ) {
        this.aZ = aZ;
    }

    public void setgX(float gX) {
        this.gX = gX;
    }

    public void setgY(float gY) {
        this.gY = gY;
    }

    public void setgZ(float gZ) {
        this.gZ = gZ;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

}
