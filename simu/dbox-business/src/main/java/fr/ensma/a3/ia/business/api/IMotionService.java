package fr.ensma.a3.ia.business.api;

import fr.ensma.a3.ia.business.model.DboxConfig;
import fr.ensma.a3.ia.business.model.RawMotionData;

/**
 * Interface du service de contrôle de mouvement DBOX
 *
 * Cette interface définit le contrat principal pour le service métier
 * qui gère le pipeline complet:
 * Réception données brutes → Traitement → Envoi UDP → DBOX
 *
 * Ce service est destiné à être appelé par la couche Service (WebSocket)
 * qui reçoit les données du Raspberry Pi.
 */
public interface IMotionService extends AutoCloseable {

    // ===========================================================================
    // GESTION DU CYCLE DE VIE
    // ===========================================================================

    /**
     * Démarre le service de contrôle de mouvement
     *
     * Cette méthode initialise:
     * - Le processeur de données
     * - Le client UDP
     * - La connexion avec le serveur DBOX C++
     *
     * @return true si le démarrage a réussi, false sinon
     * @throws IllegalStateException si le service est déjà démarré
     */
    boolean start();

    /**
     * Arrête le service de contrôle de mouvement
     *
     * Cette méthode effectue un arrêt propre:
     * - Ferme la connexion UDP
     * - Réinitialise le processeur
     * - Libère les ressources
     *
     * @return true si l'arrêt a réussi, false sinon
     */
    boolean stop();

    /**
     * Vérifie si le service est en cours d'exécution
     *
     * @return true si le service est démarré et actif
     */
    boolean isRunning();

    /**
     * Ferme le service et libère toutes les ressources
     *
     * Cette méthode appelle stop() si le service est en cours d'exécution,
     * puis ferme le contrôleur DBOX.
     */
    @Override
    void close();

    // ===========================================================================
    // TRAITEMENT DES DONNEES
    // ===========================================================================

    /**
     * Traite et envoie des données de mouvement à la DBOX
     *
     * Cette méthode est appelée par la couche Service (WebSocket) pour
     * chaque paquet de données reçu du Raspberry Pi.
     *
     * Pipeline:
     * 1. Validation des données brutes
     * 2. Traitement via IMotionDataProcessor
     * 3. Envoi UDP vers le serveur C++
     *
     * @param rawData Données brutes reçues du Raspberry Pi
     * @return true si le traitement et l'envoi ont réussi, false sinon
     * @throws IllegalStateException si le service n'est pas démarré
     */
    boolean processAndSend(RawMotionData rawData);

    /**
     * Traite et envoie des données de mouvement à partir d'un message JSON
     *
     * Cette méthode est la surcharge principale appelée par la couche Service.
     * Elle parse le JSON reçu du WebSocket et délègue au traitement standard.
     *
     * @param jsonMessage Message JSON brut reçu du WebSocket
     * @return true si le parsing, traitement et envoi ont réussi, false sinon
     * @throws IllegalStateException si le service n'est pas démarré
     */
    boolean processAndSend(String jsonMessage);

    /**
     * Envoie une commande de position neutre à la DBOX
     *
     * Utile pour:
     * - Réinitialiser la position entre deux scénarios
     * - Revenir à la position de repos
     *
     * @return true si l'envoi a réussi, false sinon
     */
    boolean sendNeutralPosition();

    // ===========================================================================
    // CONFIGURATION
    // ===========================================================================

    /**
     * Récupère la configuration actuelle
     *
     * @return Configuration actuelle (copie)
     */
    DboxConfig getConfig();

    /**
     * Met à jour la configuration
     *
     * Note: Certains paramètres nécessitent un redémarrage pour être appliqués
     * (ex: port UDP)
     *
     * @param config Nouvelle configuration
     * @throws IllegalArgumentException si la configuration est invalide
     */
    void updateConfig(DboxConfig config);

    // ===========================================================================
    // DIAGNOSTICS ET MONITORING
    // ===========================================================================

    /**
     * Récupère le dernier message d'erreur
     *
     * @return Message d'erreur, ou chaîne vide si aucune erreur
     */
    String getLastError();

    /**
     * Récupère le nombre de paquets traités avec succès
     *
     * @return Nombre de paquets traités depuis le démarrage
     */
    long getProcessedPacketCount();

    /**
     * Récupère le nombre d'erreurs d'envoi UDP
     *
     * @return Nombre d'erreurs d'envoi depuis le démarrage
     */
    long getErrorCount();

    /**
     * Réinitialise les compteurs de statistiques
     */
    void resetStatistics();
}
