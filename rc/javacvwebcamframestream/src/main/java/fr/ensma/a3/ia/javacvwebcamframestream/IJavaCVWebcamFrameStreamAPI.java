package fr.ensma.a3.ia.javacvwebcamframestream;

import java.nio.ByteBuffer;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public interface IJavaCVWebcamFrameStreamAPI {

    /**
     * Initialize the webcam stream.
     *
     * TODO: ajouter les paramètres de configuration : résolution, format, etc.
     * 
     * @param deviceNumber The device number of the webcam to initialize
     */
    boolean initializeStream(int deviceNumber);

    /**
     * Get the current frame from the webcam as a ByteBuffer.
     * 
     * @return  A ByteBuffer containing the current frame from the webcam
     */
    ByteBuffer getFrame();

    void closeStream();
}
