package fr.ensma.a3.ia.javacvwebcamframestream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class JavaCisMJPEGWebcamFrameStream implements IJavaCVWebcamFrameStreamAPI {

    private Process libcameraProcess;
    private InputStream libcamOut;
    private volatile ByteBuffer latestFrame; // dernier frame dispo
    private Thread readerThread;
    private boolean running = false;

    @Override
    public boolean initializeStream(int deviceNumber) {
        try {
            libcameraProcess = new ProcessBuilder(
                    "libcamera-vid",
                    "-t", "0",
                    "--width", "320",
                    "--height", "240",
                    "--codec", "mjpeg",
                    "-o", "-"
            ).redirectErrorStream(true)
             .start();

            libcamOut = libcameraProcess.getInputStream();
            running = true;

            // Thread de lecture continue
            readerThread = new Thread(() -> {
                byte[] buffer = new byte[8192];
                int prev = 0, curr;
                while (running) {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int bytesRead;
                        boolean frameDone = false;

                        while (!frameDone && (bytesRead = libcamOut.read(buffer)) != -1) {
                            for (int i = 0; i < bytesRead; i++) {
                                curr = buffer[i] & 0xFF;
                                baos.write(curr);
                                if (prev == 0xFF && curr == 0xD9) { // fin JPEG
                                    frameDone = true;
                                    break;
                                }
                                prev = curr;
                            }
                        }

                        if (baos.size() > 0) {
                            latestFrame = ByteBuffer.wrap(baos.toByteArray());
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading frame: " + e.getMessage());
                        break;
                    }
                }
            }, "MJPEGReaderThread");

            readerThread.setDaemon(true);
            readerThread.start();

            System.out.println("Webcam initialized on device " + deviceNumber);
            return true;

        } catch (IOException e) {
            System.err.println("Error initializing video stream: " + e.getMessage());
            return false;
        }
    }

    @Override
    public ByteBuffer getFrame() {
        return latestFrame; // retourne toujours le frame le plus récent
    }

    @Override
    public void closeStream() {
        // TODO Auto-generated method stub
        running = false;
        try {
            if (libcamOut != null) libcamOut.close();
        } catch (IOException ignored) {}
        if (libcameraProcess != null) libcameraProcess.destroy();
    }
}