package fr.ensma.a3.ia.webcamframestreambusiness;

import java.nio.ByteBuffer;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public interface IWebcamFrameStream {

    void initializeWebcams();

    void initializeDriverWebcam();

    void initializeAssistantWebcam();

    ByteBuffer getDriverFrame();

    ByteBuffer getAssistantFrame();
}