package fr.ensma.a3.ia.rcclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ensma.a3.ia.rcclient.dto.AllSensorDataDTO;
import fr.ensma.a3.ia.sensorsbusiness.AllSensorData;
import fr.ensma.a3.ia.sensorsbusiness.ISensorAggregator;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client WebSocket pour envoyer les données des capteurs au serveur SIMU.
 */
public class SensorWebSocketClient extends WebSocketClient {

    private static final Logger logger = LoggerFactory.getLogger(SensorWebSocketClient.class);

    private final ISensorAggregator sensorAggregator;
    private final ObjectMapper objectMapper;
    private final int sendIntervalMs;
    private final AtomicBoolean running;

    private Thread senderThread;
    private long messagesSent = 0;

    public SensorWebSocketClient(URI serverUri, ISensorAggregator sensorAggregator, int sendIntervalMs) {
        super(serverUri);
        this.sensorAggregator = sensorAggregator;
        this.objectMapper = new ObjectMapper();
        this.sendIntervalMs = sendIntervalMs;
        this.running = new AtomicBoolean(false);
    }

    public SensorWebSocketClient(URI serverUri, ISensorAggregator sensorAggregator) {
        this(serverUri, sensorAggregator, 20); // 50Hz par défaut
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("Connecté au serveur: {}", getURI());
        startSending();
    }

    @Override
    public void onMessage(String message) {
        logger.debug("Message reçu: {}", message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Connexion fermée (code={}, reason={})", code, reason);
        stopSending();
    }

    @Override
    public void onError(Exception ex) {
        logger.error("Erreur WebSocket: {}", ex.getMessage());
    }

    private void startSending() {
        if (running.getAndSet(true)) return;

        senderThread = new Thread(() -> {
            logger.info("Démarrage envoi (intervalle={}ms)", sendIntervalMs);

            while (running.get() && isOpen()) {
                try {
                    sendSensorData();
                    Thread.sleep(sendIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Erreur envoi: {}", e.getMessage());
                }
            }
        }, "SensorSender");

        senderThread.start();
    }

    private void stopSending() {
        running.set(false);
        if (senderThread != null) {
            senderThread.interrupt();
        }
    }

    private void sendSensorData() throws Exception {
        AllSensorData data = sensorAggregator.getAllData();

        if (data == null || !data.hasValidData()) {
            return;
        }

        AllSensorDataDTO dto = AllSensorDataDTO.fromAllSensorData(data);
        String json = objectMapper.writeValueAsString(dto);
        send(json);
        messagesSent++;

        if (messagesSent % 50 == 0) {
            logger.debug("Messages envoyés: {}", messagesSent);
        }
    }

    public void shutdown() {
        stopSending();
        close();
    }

    public long getMessagesSent() {
        return messagesSent;
    }
}
