package fr.ensma.a3.ia.rcservice;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ensma.a3.ia.webcamframestreambusiness.IWebcamFrameStream;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class WebcamAssistantFrameStreamSenderService {

	private static final Logger logger = LoggerFactory.getLogger(WebcamAssistantFrameStreamSenderService.class);

	private IWebcamFrameStream refWebcamBusiness;
	private final AtomicBoolean sendInProgress = new AtomicBoolean(false);

	public WebcamAssistantFrameStreamSenderService(IWebcamFrameStream pRefWebcamBusiness) {
		this.refWebcamBusiness = pRefWebcamBusiness;
	}

	public void connect(String wsUrl) {
		try {
			final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
			ClientManager client = ClientManager.createClient();

			Session currentSession = client.connectToServer(new Endpoint() {
				@Override
				public void onOpen(Session session, EndpointConfig config) {
					logger.info("Webcam Assistant connectee");
				}
			}, cec, new URI(wsUrl));

			Runnable sendTask = () -> {
				try {
					refWebcamBusiness.initializeAssistantWebcam();

					while (!Thread.currentThread().isInterrupted() && currentSession.isOpen()) {
						ByteBuffer frame = refWebcamBusiness.getAssistantFrame();
						if (frame != null) {
							if (sendInProgress.compareAndSet(false, true)) {
								currentSession.getAsyncRemote().sendBinary(frame, result -> {
									sendInProgress.set(false);
									if (!result.isOK()) {
										logger.debug("Erreur envoi frame assistant: {}", result.getException().getMessage());
									}
								});
							}
						}
						Thread.sleep(5);
					}
				} catch (InterruptedException e) {
					logger.debug("Thread webcam assistant interrompu");
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					logger.error("Erreur webcam assistant: {}", e.getMessage());
				}
			};

			Thread senderThread = new Thread(sendTask, "WebcamAssistant");
			senderThread.setDaemon(false);
			senderThread.start();
		} catch (Exception e) {
			logger.error("Impossible de connecter la webcam assistant", e);
		}
	}
}
