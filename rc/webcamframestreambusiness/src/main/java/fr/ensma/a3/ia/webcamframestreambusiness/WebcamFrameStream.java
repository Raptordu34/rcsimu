package fr.ensma.a3.ia.webcamframestreambusiness;

import fr.ensma.a3.ia.javacvwebcamframestream.IJavaCVWebcamFrameStreamAPI;
import fr.ensma.a3.ia.javacvwebcamframestream.JavaBindWebcamFrame;
import fr.ensma.a3.ia.javacvwebcamframestream.JavaCVWebcamFrameStream;
import fr.ensma.a3.ia.javacvwebcamframestream.JavaCisMJPEGWebcamFrameStream;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class WebcamFrameStream implements IWebcamFrameStream {

    private static WebcamFrameStream instance = null;

    private IJavaCVWebcamFrameStreamAPI webcamDriver;

    private IJavaCVWebcamFrameStreamAPI assistantDriver;

    private WebcamFrameStream() {
        //webcamDriver = new JavaCVWebcamFrameStream();    
        //webcamDriver = new JavaCisMJPEGWebcamFrameStream();
        webcamDriver = new JavaBindWebcamFrame();
        assistantDriver = new JavaBindWebcamFrame();
    }
    
    public static synchronized WebcamFrameStream getInstance() {
        if (instance == null) {
            instance = new WebcamFrameStream();
        }
        return instance;
    }
    
    @Override
    public java.nio.ByteBuffer getDriverFrame() {
        return webcamDriver.getFrame();
    }
    
    @Override
    public java.nio.ByteBuffer getAssistantFrame() {
        return assistantDriver.getFrame();
    }
    
    @Override
    public void initializeWebcams() {
        webcamDriver.initializeStream(0);
        assistantDriver.initializeStream(1);
    }

    @Override
    public void initializeDriverWebcam() {
        webcamDriver.initializeStream(0);
    }

    @Override
    public void initializeAssistantWebcam() {
        assistantDriver.initializeStream(1);
    }
}
