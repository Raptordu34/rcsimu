package fr.ensma.a3.ia.rcservice;

import java.net.URI;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.ensma.a3.ia.sensorsbusiness.AllSensorData;
import fr.ensma.a3.ia.sensorsbusiness.ISensorAggregator;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

/**
 * Service d'envoi des donnees des capteurs vers le serveur WebSocket.
 *
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class SensorFlowSenderService {

	private static final Logger logger = LoggerFactory.getLogger(SensorFlowSenderService.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final int SEND_INTERVAL_MS = 50;

	private final ISensorAggregator sensorAggregator;

	public SensorFlowSenderService(ISensorAggregator sensorAggregator) {
		this.sensorAggregator = sensorAggregator;
	}

	public void connect(String wsUrl) {
		try {
			final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
			ClientManager client = ClientManager.createClient();

			Session currentSession = client.connectToServer(new Endpoint() {
				@Override
				public void onOpen(Session session, EndpointConfig config) {
					logger.info("Connecte au serveur");
				}
			}, cec, new URI(wsUrl));

			Runnable sendTask = () -> {
				try {
					while (!Thread.currentThread().isInterrupted() && currentSession.isOpen()) {
						String jsonMessage = buildSensorDataJson();
						if (jsonMessage != null) {
							currentSession.getBasicRemote().sendText(jsonMessage);
						}
						Thread.sleep(SEND_INTERVAL_MS);
					}
				} catch (InterruptedException e) {
					logger.debug("Thread interrompu");
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					logger.error("Erreur envoi: {}", e.getMessage());
				}
			};

			Thread senderThread = new Thread(sendTask, "SensorFlowSender");
			senderThread.setDaemon(false);
			senderThread.start();
		} catch (Exception e) {
			logger.error("Impossible de se connecter au serveur", e);
		}
	}

	private String buildSensorDataJson() {
		if (sensorAggregator == null) {
			return null;
		}

		AllSensorData allData = sensorAggregator.getAllData();
		if (allData == null || !allData.hasMpuData()) {
			return null;
		}

		try {
			ObjectNode root = objectMapper.createObjectNode();
			root.put("timestampMs", allData.getTimestampMs());

			ObjectNode mpuNode = objectMapper.createObjectNode();
			mpuNode.put("accelX", allData.getMpuAccelX());
			mpuNode.put("accelY", allData.getMpuAccelY());
			mpuNode.put("accelZ", allData.getMpuAccelZ());
			mpuNode.put("gyroX", allData.getMpuGyroX());
			mpuNode.put("gyroY", allData.getMpuGyroY());
			mpuNode.put("gyroZ", allData.getMpuGyroZ());
			mpuNode.put("temperature", allData.getMpuTemperature());
			mpuNode.put("timestampMs", allData.getMpuTimestampMs());

			root.set("mpuData", mpuNode);

			return objectMapper.writeValueAsString(root);
		} catch (Exception e) {
			logger.error("Erreur serialisation JSON: {}", e.getMessage());
			return null;
		}
	}
}
