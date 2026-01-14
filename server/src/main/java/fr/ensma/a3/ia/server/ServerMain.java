package fr.ensma.a3.ia.server;

import fr.ensma.a3.ia.business.processor.MotionDataProcessor;
import fr.ensma.a3.ia.dbox.DboxController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Point d'entrée du serveur WebSocket Motion.
 *
 * Usage:
 *   java -jar motion-server.jar [port] [--no-dbox]
 *
 * Arguments:
 *   port      Port d'écoute (défaut: 8080)
 *   --no-dbox Désactive la D-BOX (mode test/log uniquement)
 */
public class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        boolean dboxEnabled = true;

        // Parsing des arguments
        for (String arg : args) {
            if (arg.equals("--no-dbox")) {
                dboxEnabled = false;
            } else {
                try {
                    port = Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                    logger.warn("Argument invalide: {}", arg);
                }
            }
        }

        logger.info("=== Motion WebSocket Server ===");
        logger.info("Port: {}", port);
        logger.info("D-BOX: {}", dboxEnabled ? "activée" : "désactivée");

        try {
            // Création du processeur
            MotionDataProcessor processor = new MotionDataProcessor();

            // Création du contrôleur D-BOX (si activé)
            DboxController dboxController = null;
            if (dboxEnabled) {
                try {
                    dboxController = new DboxController();
                    dboxController.connect();
                    dboxController.start();
                    logger.info("D-BOX connectée et démarrée");
                } catch (Exception e) {
                    logger.error("Erreur de connexion D-BOX: {}. Mode test activé.", e.getMessage());
                    dboxController = null;
                }
            }

            // Création du handler
            SensorDataHandler handler = dboxController != null
                ? new SensorDataHandler(processor, dboxController)
                : new SensorDataHandler(processor);

            // Création et démarrage du serveur
            MotionWebSocketServer server = new MotionWebSocketServer(port, handler);
            server.start();

            logger.info("Serveur en attente de connexions sur le port {}...", port);
            logger.info("Appuyez sur Ctrl+C pour arrêter");

            // Hook d'arrêt propre
            final DboxController finalDboxController = dboxController;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Arrêt du serveur...");
                server.shutdown();
                if (finalDboxController != null) {
                    try {
                        finalDboxController.stop();
                        finalDboxController.disconnect();
                        logger.info("D-BOX déconnectée");
                    } catch (Exception e) {
                        logger.error("Erreur lors de la déconnexion D-BOX: {}", e.getMessage());
                    }
                }
            }));

            // Boucle principale (attente)
            while (server.isRunning()) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            logger.error("Erreur fatale: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
