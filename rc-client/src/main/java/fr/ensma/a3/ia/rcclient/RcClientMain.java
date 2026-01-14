package fr.ensma.a3.ia.rcclient;

import fr.ensma.a3.ia.sensorsbusiness.SensorAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Point d'entrée du client RC.
 *
 * Usage:
 *   java -jar rc-client.jar [server-uri]
 *
 * Exemple:
 *   java -jar rc-client.jar ws://192.168.1.100:8080
 */
public class RcClientMain {

    private static final Logger logger = LoggerFactory.getLogger(RcClientMain.class);
    private static final String DEFAULT_SERVER_URI = "ws://localhost:8080";

    public static void main(String[] args) {
        String serverUri = args.length > 0 ? args[0] : DEFAULT_SERVER_URI;

        logger.info("=== RC Sensor Client ===");
        logger.info("Serveur: {}", serverUri);

        try {
            // Utilise SensorAggregator comme outil
            logger.info("Initialisation des capteurs...");
            SensorAggregator aggregator = new SensorAggregator();
            logger.info("Capteurs initialisés");

            // Client WebSocket
            URI uri = new URI(serverUri);
            SensorWebSocketClient client = new SensorWebSocketClient(uri, aggregator);

            // Arrêt propre
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Arrêt...");
                client.shutdown();
                try {
                    aggregator.close();
                } catch (Exception e) {
                    logger.error("Erreur fermeture: {}", e.getMessage());
                }
            }));

            // Connexion
            logger.info("Connexion à {}...", serverUri);
            if (!client.connectBlocking()) {
                logger.error("Connexion impossible");
                System.exit(1);
            }

            logger.info("Connecté. Ctrl+C pour arrêter");

            while (client.isOpen()) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            logger.error("Erreur: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
