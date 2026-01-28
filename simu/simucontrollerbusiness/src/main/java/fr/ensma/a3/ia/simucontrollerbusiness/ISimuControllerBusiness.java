package fr.ensma.a3.ia.simucontrollerbusiness;

public interface ISimuControllerBusiness {

    float getSteer();

    float getThrottle();

    float getBrake();

    void updateRumble();

    float getHorizontalPanAssistantCamera();

    float getVerticalPanAssistantCamera();

    boolean getInitPanAssistantCamera();
}
