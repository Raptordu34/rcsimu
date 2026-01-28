package fr.ensma.a3.ia.serviceapi;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class ControllerMessage {

    private byte streer;

    private byte throttle;

    private byte brake;

    private byte horirontalPanAssistantCamera;

    private byte verticalPanAssistantCamera;

    private boolean resetPanAssistantCamera;

    public float getStreer() {
        return streer;
    }

    public void setStreer(byte streer) {
        this.streer = streer;
    }

    public byte getThrottle() {
        return throttle;
    }

    public void setThrottle(byte throttle) {
        this.throttle = throttle;
    }

    public float getBrake() {
        return brake;
    }

    public void setBrake(byte brake) {
        this.brake = brake;
    }

    public byte getHorirontalPanAssistantCamera() {
        return horirontalPanAssistantCamera;
    }

    public void setHorirontalPanAssistantCamera(byte horirontalPanAssistantCamera) {
        this.horirontalPanAssistantCamera = horirontalPanAssistantCamera;
    }

    public byte getVerticalPanAssistantCamera() {
        return verticalPanAssistantCamera;
    }

    public void setVerticalPanAssistantCamera(byte verticalPanAssistantCamera) {
        this.verticalPanAssistantCamera = verticalPanAssistantCamera;
    }

    public boolean isResetPanAssistantCamera() {
        return resetPanAssistantCamera;
    }

    public void setResetPanAssistantCamera(boolean resetPanAssistantCamera) {
        this.resetPanAssistantCamera = resetPanAssistantCamera;
    }
}
