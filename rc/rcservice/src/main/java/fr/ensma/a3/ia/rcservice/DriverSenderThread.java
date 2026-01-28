package fr.ensma.a3.ia.rcservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ensma.a3.ia.servocamerabusiness.ServoCameraBusiness;
import fr.ensma.a3.ia.servocontrolbusiness.DriverData;
import fr.ensma.a3.ia.servocontrolbusiness.IServoControlBusinessAPI;
import fr.ensma.a3.ia.servocontrolbusiness.ServoControlBusinessAPI;
import jakarta.websocket.Session;

public class DriverSenderThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(DriverSenderThread.class);

    private DriverData values;
    private volatile int cameraHor;
    private volatile int cameraVert;
    private volatile long timeoutMessage = 1000;
    private volatile long lastMessageTime;
    private Session sess;

    public DriverSenderThread(DriverData values, Session s) {
        super("DriverSender");
        this.values = values;
        sess = s;
        lastMessageTime = System.currentTimeMillis();
    }

    public void updateCamera(int hor, int vert) {
        this.cameraHor = hor;
        this.cameraVert = vert;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    @Override
    public void run() {
        IServoControlBusinessAPI driver = null;
        ServoCameraBusiness cameraBusiness = null;

        try {
            cameraBusiness = new ServoCameraBusiness();
            driver = new ServoControlBusinessAPI();
            driver.launchCommands(values);

            values.setInvertAccel(true);
            values.setStart(1);
            driver.launchCommands(values);

            logger.info("Driver initialise");

            while (!Thread.currentThread().isInterrupted() && sess.isOpen()) {
                try {
                    if (System.currentTimeMillis() - lastMessageTime > timeoutMessage) {
                        logger.warn("Timeout message, arret du driver");
                        break;
                    }

                    driver.launchCommands(values);
                    cameraBusiness.rotationAxeHori(cameraHor);
                    cameraBusiness.rotationAxeVert(cameraVert);

                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    logger.debug("Thread driver interrompu");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Erreur driver", e);
        } finally {
            if (driver != null) {
                driver.closeDriver();
            }
            logger.info("Driver arrete");
        }
    }
}
