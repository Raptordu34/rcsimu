package fr.ensma.a3.ia.javacvwebcamframestream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class JavaCVWebcamFrameStream implements IJavaCVWebcamFrameStreamAPI {

    private OpenCVFrameGrabber grabber;

    private static final Java2DFrameConverter converter = new Java2DFrameConverter();
    private static final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private static final int IMAGE_WIDTH = 320;
    private static final int IMAGE_HEIGHT = 240;

    // Reusable ImageWriter and params (avoid recreation per frame)
    private ImageWriter jpegWriter;
    private ImageWriteParam jpegWriteParam;

    @Override
    public boolean initializeStream(int deviceNumber) {
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.close();
            } catch (Exception ignore) {
            }
        }

        grabber = new OpenCVFrameGrabber(deviceNumber);
        grabber.setImageWidth(IMAGE_WIDTH);
        grabber.setImageHeight(IMAGE_HEIGHT);
        grabber.setVideoOption("preset", "ultrafast");
        grabber.setVideoOption("tune", "zerolatency");

        try {
            grabber.start();

            Frame testFrame = grabber.grab();
            if (testFrame == null) {
                System.err.println("Unable to capture an image from the device: " + deviceNumber);
                grabber.stop();
                grabber.close();
                grabber = null;
                return false;
            }

            // Initialize reusable JPEG writer and params once
            jpegWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            jpegWriteParam = jpegWriter.getDefaultWriteParam();
            jpegWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegWriteParam.setCompressionQuality(0.5f);

            System.out.println("Webcam initialized on the device: " + deviceNumber);
            return true;

        } catch (Exception e) {
            System.err.println("Error initializing the video stream: " + e.getMessage());
            try {
                grabber.close();
            } catch (Exception ignore) {
            }
            grabber = null;
            return false;
        }
    }

    @Override
    public ByteBuffer getFrame() {
        try {
            Frame grabbedFrame = grabber.grab();
            if (grabbedFrame != null) {
                // Image already captured at 320x240 by grabber, no need to resize
                BufferedImage img = converter.convert(grabbedFrame);

                baos.reset();

                try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                    jpegWriter.setOutput(ios);
                    jpegWriter.write(null, new IIOImage(img, null, null), jpegWriteParam);
                } catch (IOException e) {
                    System.err.println("Error during JPEG compression: " + e.getMessage());
                    return null;
                }

                return ByteBuffer.wrap(baos.toByteArray());
            } else {
                System.err.println("Unable to capture an image.");
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void closeStream() {
        if (jpegWriter != null) {
            jpegWriter.dispose();
            jpegWriter = null;
        }
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.close();
            } catch (Exception ignore) {
            }
            grabber = null;
        }
    }
}