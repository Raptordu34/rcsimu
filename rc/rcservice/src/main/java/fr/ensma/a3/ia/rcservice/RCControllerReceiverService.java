package fr.ensma.a3.ia.rcservice;

import java.net.URI;
import java.util.List;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ensma.a3.ia.serviceapi.ControllerMessage;
import fr.ensma.a3.ia.serviceapi.ControllerMessageDecoder;
import fr.ensma.a3.ia.servocontrolbusiness.DriverData;
import fr.ensma.a3.ia.servocontrolbusiness.EAxisInputType;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class RCControllerReceiverService {

    private static final Logger logger = LoggerFactory.getLogger(RCControllerReceiverService.class);

    private DriverSenderThread driverThread;
    private Integer cameraHor = 1500;
    private Integer cameraVert = 1600;
    private DriverData values = new DriverData(EAxisInputType.DUAL_AXIS);

    public void connect(String wsUrl) {
        try {
            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
                    .decoders(List.of(ControllerMessageDecoder.class)).build();
            ClientManager client = ClientManager.createClient();

            Session currentSession = client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    logger.info("Connecte au controleur RC");

                    session.addMessageHandler(new MessageHandler.Whole<ControllerMessage>() {
                        @Override
                        public void onMessage(ControllerMessage message) {
                            cameraHor = (int) message.getHorirontalPanAssistantCamera();
                            cameraVert = -(int) message.getVerticalPanAssistantCamera();

                            if (driverThread != null && driverThread.isAlive()) {
                                driverThread.updateCamera(cameraHor, cameraVert);
                                driverThread.setLastMessageTime(System.currentTimeMillis());
                            }

                            values.setDirection((int) message.getStreer());
                            values.setAxisInputType(EAxisInputType.DUAL_AXIS);
                            values.setAccelerate((int) message.getThrottle());
                            values.setReverse((int) message.getBrake());

                            logger.trace("Recu: camH={} camV={} accel={} rev={} dir={}",
                                    cameraHor, cameraVert, values.getAccelerate(),
                                    values.getReverse(), values.getDirection());
                        }
                    });
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    logger.info("Connexion fermee: {}", closeReason.getReasonPhrase());
                    if (driverThread != null && driverThread.isAlive()) {
                        driverThread.interrupt();
                    }
                }

                @Override
                public void onError(Session session, Throwable thr) {
                    logger.error("Erreur WebSocket", thr);
                    if (driverThread != null && driverThread.isAlive()) {
                        driverThread.interrupt();
                    }
                }
            }, cec, new URI(wsUrl));

            driverThread = new DriverSenderThread(values, currentSession);
            driverThread.setDaemon(false);
            driverThread.start();

        } catch (Exception e) {
            logger.error("Impossible de se connecter au controleur", e);
        }
    }
}
