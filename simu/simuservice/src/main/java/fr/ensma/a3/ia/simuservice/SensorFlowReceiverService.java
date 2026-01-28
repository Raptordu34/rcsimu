package fr.ensma.a3.ia.simuservice;

import java.net.URI;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ensma.a3.ia.business.api.IMotionService;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class SensorFlowReceiverService {

    private static final Logger logger = LoggerFactory.getLogger(SensorFlowReceiverService.class);

    private final IMotionService motionService;

    public SensorFlowReceiverService(IMotionService motionService) {
        this.motionService = motionService;
    }

    public void connect(String wsUrl) {
        try {
            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
            ClientManager client = ClientManager.createClient();

            client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    logger.info("Connecte au flux capteurs");
                    session.addMessageHandler(new MessageHandler.Whole<String>() {

                        @Override
                        public void onMessage(String message) {
                            motionService.processAndSend(message);
                        }
                    });
                }
            }, cec, new URI(wsUrl));
        } catch (Exception e) {
            logger.error("Impossible de se connecter au flux capteurs", e);
        }
    }
}
