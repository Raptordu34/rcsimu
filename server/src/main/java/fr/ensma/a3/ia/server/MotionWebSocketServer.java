package fr.ensma.a3.ia.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Serveur WebSocket pour recevoir les données des capteurs RC.
 *
 * Ce serveur accepte les connexions des clients (Raspberry Pi)
 * et transmet les données reçues au SensorDataHandler pour traitement.
 */
public class MotionWebSocketServer extends WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(MotionWebSocketServer.class);

    private final SensorDataHandler handler;
    private volatile boolean running = false;

    /**
     * Crée un serveur WebSocket sur le port spécifié.
     *
     * @param port Port d'écoute (ex: 8080)
     * @param handler Handler pour traiter les données reçues
     */
    public MotionWebSocketServer(int port, SensorDataHandler handler) {
        super(new InetSocketAddress(port));
        this.handler = handler;
        setReuseAddr(true);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String clientAddress = conn.getRemoteSocketAddress().toString();
        logger.info("Nouvelle connexion: {}", clientAddress);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String clientAddress = conn.getRemoteSocketAddress().toString();
        logger.info("Connexion fermée: {} (code={}, reason={})", clientAddress, code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            handler.handleMessage(message);
        } catch (Exception e) {
            logger.error("Erreur lors du traitement du message: {}", e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        String clientAddress = conn != null ? conn.getRemoteSocketAddress().toString() : "unknown";
        logger.error("Erreur WebSocket (client={}): {}", clientAddress, ex.getMessage());
    }

    @Override
    public void onStart() {
        running = true;
        logger.info("Serveur WebSocket démarré sur le port {}", getPort());
    }

    /**
     * Vérifie si le serveur est en cours d'exécution.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Arrête le serveur proprement.
     */
    public void shutdown() {
        try {
            running = false;
            stop(1000);
            logger.info("Serveur WebSocket arrêté");
        } catch (InterruptedException e) {
            logger.error("Erreur lors de l'arrêt du serveur: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
