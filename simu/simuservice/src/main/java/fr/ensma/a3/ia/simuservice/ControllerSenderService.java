package fr.ensma.a3.ia.simuservice;

import java.net.URI;
import java.util.List;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ensma.a3.ia.serviceapi.ControllerMessage;
import fr.ensma.a3.ia.serviceapi.ControllerMessageEncoder;
import fr.ensma.a3.ia.simucontrollerbusiness.ISimuControllerBusiness;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class ControllerSenderService {

	private static final Logger logger = LoggerFactory.getLogger(ControllerSenderService.class);

	private ISimuControllerBusiness simuControllerBusiness;

	public ControllerSenderService(ISimuControllerBusiness simuControllerBusiness) {
		this.simuControllerBusiness = simuControllerBusiness;
	}

	private byte floatToByte(float value) {
		float clamped = Math.max(-1.0f, Math.min(1.0f, value));
		return (byte) (clamped * 100.0f);
	}

	public void connect(String wsUrl) {
		try {
			final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
					.encoders(List.of(ControllerMessageEncoder.class)).build();
			ClientManager client = ClientManager.createClient();

			Session currentSession = client.connectToServer(new Endpoint() {
				@Override
				public void onOpen(Session session, EndpointConfig config) {
					logger.info("Connecte au controleur");
				}
			}, cec, new URI(wsUrl));

			Runnable sendTask = () -> {
				try {
					while (!Thread.currentThread().isInterrupted() && currentSession.isOpen()) {
						ControllerMessage newMessage = new ControllerMessage();
						newMessage.setThrottle(floatToByte(simuControllerBusiness.getThrottle()));
						newMessage.setBrake(floatToByte(simuControllerBusiness.getBrake()));
						newMessage.setHorirontalPanAssistantCamera(
								floatToByte(simuControllerBusiness.getHorizontalPanAssistantCamera()));
						newMessage.setVerticalPanAssistantCamera(
								floatToByte(simuControllerBusiness.getVerticalPanAssistantCamera()));
						newMessage.setStreer(floatToByte(simuControllerBusiness.getSteer()));
						newMessage.setResetPanAssistantCamera(simuControllerBusiness.getInitPanAssistantCamera());

						logger.trace("Envoi: throttle={} brake={} steer={}",
								newMessage.getThrottle(), newMessage.getBrake(), newMessage.getStreer());

						currentSession.getBasicRemote().sendObject(newMessage);
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					logger.debug("Thread controleur interrompu");
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					logger.error("Erreur envoi controleur: {}", e.getMessage());
				}
			};

			Thread senderThread = new Thread(sendTask, "ControllerSender");
			senderThread.setDaemon(false);
			senderThread.start();
		} catch (Exception e) {
			logger.error("Impossible de se connecter au controleur", e);
		}
	}
}
