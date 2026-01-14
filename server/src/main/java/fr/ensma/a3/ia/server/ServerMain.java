package fr.ensma.a3.ia.server;

import fr.ensma.a3.ia.business.service.MotionService;
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
            // Création du service métier (encapsule processeur + driver)
            MotionService motionService = new MotionService(dboxEnabled);

            // Démarrage du service
            if (!motionService.start()) {
                logger.error("Échec du démarrage du service: {}", motionService.getLastError());
                if (dboxEnabled) {
                    logger.info("Passage en mode test (sans D-BOX)...");
                    motionService = new MotionService(false);
                    motionService.start();
                }
            }

            // Création du handler
            SensorDataHandler handler = new SensorDataHandler(motionService);

            // Création et démarrage du serveur
            MotionWebSocketServer server = new MotionWebSocketServer(port, handler);
            server.start();

            logger.info("Serveur en attente de connexions sur le port {}...", port);
            logger.info("Appuyez sur Ctrl+C pour arrêter");

            // Hook d'arrêt propre
            final MotionService finalMotionService = motionService;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Arrêt du serveur...");
                server.shutdown();
                finalMotionService.close();
                logger.info("Service arrêté proprement");
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
