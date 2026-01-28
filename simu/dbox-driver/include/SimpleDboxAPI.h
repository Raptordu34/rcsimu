/**
 * @file SimpleDboxAPI.h
 * @brief API simplifiée pour contrôler la plateforme DBOX 6DOF Motion Simulator
 *
 * Cette API encapsule la complexité du SDK DBOX LiveMotion et fournit une interface
 * simple et intuitive pour contrôler les mouvements de la plateforme.
 *
 * Exemple d'utilisation basique:
 * @code
 *   SimpleDboxController dbox;
 *
 *   if (!dbox.connect()) {
 *       std::cerr << "Erreur: " << dbox.getLastError() << std::endl;
 *       return -1;
 *   }
 *
 *   dbox.start();
 *
 *   // Boucle principale à 100Hz
 *   while (running) {
 *       dbox.update(roll, pitch, heave, rpm, torque);
 *       std::this_thread::sleep_for(std::chrono::milliseconds(10));
 *   }
 *
 *   dbox.stop();
 *   dbox.disconnect();
 * @endcode
 *
 * @author DBOX Integration Team
 * @date 2024
 */

#ifndef SIMPLE_DBOX_API_H
#define SIMPLE_DBOX_API_H

// ===========================================================================
// MACROS D'EXPORT DLL
// ===========================================================================
// Ces macros permettent d'exporter ou d'importer les symboles de la classe
// selon qu'on compile la DLL ou qu'on l'utilise dans un autre programme.
//
// - Lors de la COMPILATION de la DLL : définir SIMPLEDBOXAPI_EXPORTS
//   → SIMPLEDBOXAPI_API devient __declspec(dllexport)
//
// - Lors de l'UTILISATION de la DLL : ne PAS définir SIMPLEDBOXAPI_EXPORTS
//   → SIMPLEDBOXAPI_API devient __declspec(dllimport)

#ifdef _WIN32
    #ifdef SIMPLEDBOXAPI_EXPORTS
        #define SIMPLEDBOXAPI_API __declspec(dllexport)
    #else
        #define SIMPLEDBOXAPI_API __declspec(dllimport)
    #endif
#else
    #define SIMPLEDBOXAPI_API
#endif

#include <string>

/**
 * @class SimpleDboxController
 * @brief Contrôleur simplifié pour la plateforme DBOX 6DOF
 *
 * Cette classe fournit une interface haut niveau pour contrôler la plateforme DBOX.
 * Elle gère automatiquement:
 * - L'initialisation du SDK DBOX LiveMotion
 * - L'enregistrement des types d'événements et structures
 * - La configuration par défaut optimale
 * - La validation et le clamping des valeurs
 * - Le cycle de vie complet (connexion, démarrage, arrêt, déconnexion)
 *
 * IMPORTANT: Cette classe suit le pattern RAII (Resource Acquisition Is Initialization).
 * Assurez-vous d'appeler disconnect() avant la destruction de l'objet.
 *
 * ===========================================================================
 * EXPORT DLL : Explication de SIMPLEDBOXAPI_API (pour étudiants)
 * ===========================================================================
 *
 * La macro SIMPLEDBOXAPI_API devant "class" indique au compilateur Windows
 * que cette classe doit être rendue accessible depuis l'extérieur de la DLL.
 *
 * - Lors de la compilation de la DLL : cette macro devient __declspec(dllexport)
 *   → Les méthodes de la classe sont EXPORTÉES dans SimpleDboxAPI.dll
 *
 * - Lors de l'utilisation de la DLL : cette macro devient __declspec(dllimport)
 *   → Le compilateur sait qu'il doit CHERCHER ces méthodes dans la DLL
 *
 * Sans cette macro, la classe ne serait pas visible de l'extérieur de la DLL !
 */
class SIMPLEDBOXAPI_API SimpleDboxController {
public:
    // ========================================================================
    // CONSTRUCTEUR ET DESTRUCTEUR
    // ========================================================================

    /**
     * @brief Constructeur par défaut
     *
     * Initialise les variables membres avec des valeurs par défaut sûres.
     * Ne se connecte PAS à la DBOX (appeler connect() explicitement).
     */
    SimpleDboxController();

    /**
     * @brief Destructeur
     *
     * ATTENTION: Si disconnect() n'a pas été appelé, affiche un warning.
     * Il est recommandé d'appeler explicitement disconnect() avant destruction.
     */
    ~SimpleDboxController();

    // ========================================================================
    // GESTION DU CYCLE DE VIE (LIFECYCLE)
    // ========================================================================

    /**
     * @brief Connecte et initialise la plateforme DBOX
     *
     * Cette méthode effectue toutes les opérations nécessaires pour préparer
     * la plateforme DBOX à recevoir des commandes:
     * 1. Initialize le SDK DBOX LiveMotion
     * 2. Enregistre les types d'événements (SIM_CONFIG, MOTION_CONFIG, MOTION_DATA)
     * 3. Enregistre les structures de données correspondantes
     * 4. Ouvre la connexion avec le hardware DBOX
     * 5. Envoie la configuration par défaut (SimConfig et MotionConfig)
     *
     * PREREQUIS:
     * - Le DBOX Control Panel doit être lancé et actif
     * - Le hardware DBOX doit être connecté et sous tension
     * - Aucune autre application ne doit contrôler la DBOX
     *
     * @return true si la connexion et l'initialisation ont réussi
     * @return false en cas d'erreur (voir getLastError() pour détails)
     *
     * @note Cette méthode peut prendre quelques secondes pour s'exécuter
     * @note Ne peut être appelée qu'une seule fois (idempotence non garantie)
     *
     * @see disconnect()
     * @see isConnected()
     */
    bool connect();

    /**
     * @brief Déconnecte et nettoie toutes les ressources DBOX
     *
     * Cette méthode effectue un arrêt propre de la plateforme:
     * 1. Arrête la simulation si elle est en cours (appelle stop())
     * 2. Retourne la plateforme à la position neutre
     * 3. Ferme la connexion avec le hardware (Close)
     * 4. Libère les ressources du SDK (Terminate)
     *
     * IMPORTANT: Toujours appeler cette méthode avant de quitter l'application
     * pour éviter de laisser la plateforme dans un état instable.
     *
     * @note Cette méthode est sûre à appeler même si connect() a échoué
     * @note Peut prendre quelques secondes (fade-out + retour neutre)
     *
     * @see connect()
     * @see stop()
     */
    void disconnect();

    /**
     * @brief Démarre la simulation avec fade-in progressif
     *
     * Active la simulation et permet à la plateforme de bouger selon les
     * commandes de mouvement envoyées via setMotion() ou update().
     *
     * Le démarrage inclut un fade-in automatique pour éviter les mouvements
     * brusques et garantir la sécurité des utilisateurs.
     *
     * PREREQUIS:
     * - connect() doit avoir été appelé avec succès
     * - La plateforme ne doit pas déjà être en cours d'exécution
     *
     * @return true si le démarrage a réussi
     * @return false si déjà démarré ou si non connecté
     *
     * @note Les commandes de mouvement peuvent être envoyées avant start(),
     *       mais ne seront appliquées qu'après l'appel à start()
     *
     * @see stop()
     * @see isRunning()
     */
    bool start();

    /**
     * @brief Arrête la simulation avec fade-out progressif
     *
     * Met en pause la simulation et ramène progressivement la plateforme
     * à la position neutre via un fade-out.
     *
     * COMPORTEMENT:
     * - Fade-out progressif (pas d'arrêt brutal)
     * - Retour automatique à la position neutre (0, 0, 0)
     * - Les commandes de mouvement ne sont plus appliquées
     * - La connexion reste active (pas besoin de reconnecter)
     *
     * @return true si l'arrêt a réussi
     * @return false si déjà arrêté ou si non connecté
     *
     * @note Après stop(), vous pouvez rappeler start() sans reconnecter
     * @note Pour un arrêt complet, appelez disconnect()
     *
     * @see start()
     * @see disconnect()
     * @see resetToNeutral()
     */
    bool stop();

    // ========================================================================
    // CONTROLE DES MOUVEMENTS
    // ========================================================================

    /**
     * @brief Définit les mouvements de translation et rotation de la plateforme
     *
     * Cette méthode configure les 3 degrés de liberté principaux:
     *
     * PARAMETRES (tous normalisés entre -1.0 et +1.0):
     *
     * @param roll Mouvement de ROULIS (gauche-droite)
     *             -1.0 = inclinaison maximale à gauche
     *              0.0 = position neutre
     *             +1.0 = inclinaison maximale à droite
     *             Exemple: virage serré en voiture
     *
     * @param pitch Mouvement de TANGAGE (avant-arrière)
     *              -1.0 = inclinaison maximale arrière (cabré)
     *               0.0 = position neutre
     *              +1.0 = inclinaison maximale avant (piqué)
     *              Exemple: freinage fort (pitch avant)
     *
     * @param heave Mouvement VERTICAL (haut-bas)
     *              -1.0 = descente maximale
     *               0.0 = position neutre
     *              +1.0 = montée maximale
     *              Exemple: passage de bosse, saut
     *
     * VALIDATION AUTOMATIQUE:
     * - Les valeurs sont automatiquement clampées à [-1.0, +1.0]
     * - Pas besoin de valider avant l'appel
     *
     * PERFORMANCE:
     * - Appel très rapide (< 1ms)
     * - Peut être appelé à haute fréquence (100-1000 Hz)
     * - Ne bloque pas (mise à jour immédiate)
     *
     * @note Les valeurs ne sont appliquées que si start() a été appelé
     * @note Les vibrations (RPM/Torque) restent inchangées
     *
     * @see setVibration()
     * @see update()
     */
    void setMotion(float roll, float pitch, float heave);

    /**
     * @brief Définit les vibrations du moteur simulé
     *
     * Configure les vibrations pour simuler un moteur thermique.
     * Ces vibrations sont indépendantes des mouvements (roll/pitch/heave).
     *
     * @param rpm Régime moteur en tours/minute
     *            Plage recommandée: 750 à 6000 RPM
     *            750 = ralenti moteur
     *            2000-3000 = conduite normale
     *            6000 = régime maximal
     *            Valeurs automatiquement clampées à [0, 10000]
     *
     * @param torque Couple moteur en Newton-mètres
     *               Plage recommandée: 0 à 450 N⋅m
     *               0 = pas de couple (roue libre)
     *               100 = couple faible (ralenti)
     *               450 = couple maximal (pleine charge)
     *               Valeurs automatiquement clampées à [0, 1000]
     *
     * EFFET SUR LA PLATEFORME:
     * - RPM élevé = vibrations haute fréquence
     * - Torque élevé = vibrations plus intenses
     * - Simule fidèlement les sensations d'un moteur réel
     *
     * USAGE TYPIQUE:
     * - Voiture au ralenti: rpm=750, torque=50
     * - Accélération forte: rpm=4000, torque=350
     * - Frein moteur: rpm=5000, torque=100
     *
     * @note Les mouvements (roll/pitch/heave) restent inchangés
     * @note Les vibrations sont additives aux mouvements principaux
     *
     * @see setMotion()
     * @see update()
     */
    void setVibration(float rpm, float torque);

    /**
     * @brief Met à jour TOUS les paramètres de mouvement en une seule fois
     *
     * Méthode pratique pour définir simultanément les mouvements et vibrations.
     * Équivalent à appeler setMotion() puis setVibration().
     *
     * @param roll Roulis normalisé [-1.0, +1.0]
     * @param pitch Tangage normalisé [-1.0, +1.0]
     * @param heave Pilonnement normalisé [-1.0, +1.0]
     * @param rpm Régime moteur [0, 10000] RPM (défaut: 1000)
     * @param torque Couple moteur [0, 1000] N⋅m (défaut: 100)
     *
     * AVANTAGES:
     * - Mise à jour atomique de tous les paramètres
     * - Un seul appel au lieu de deux
     * - Plus performant si vous modifiez tout
     * - Code plus lisible
     *
     * USAGE RECOMMANDÉ:
     * Utilisez cette méthode dans votre boucle principale si vous mettez
     * à jour tous les paramètres à chaque itération.
     *
     * @code
     *   while (running) {
     *       // Calcul des valeurs...
     *       dbox.update(roll, pitch, heave, rpm, torque);
     *       std::this_thread::sleep_for(10ms);
     *   }
     * @endcode
     *
     * @note Tous les paramètres sont validés et clampés automatiquement
     * @note Cette méthode envoie immédiatement les données à la DBOX
     *
     * @see setMotion()
     * @see setVibration()
     */
    void update(float roll, float pitch, float heave,
                float rpm = 1000.0f, float torque = 100.0f);

    /**
     * @brief Retourne immédiatement la plateforme à la position neutre
     *
     * Réinitialise tous les paramètres de mouvement à leurs valeurs neutres:
     * - Roll = 0.0 (horizontal)
     * - Pitch = 0.0 (horizontal)
     * - Heave = 0.0 (hauteur moyenne)
     * - RPM = 1000 (ralenti)
     * - Torque = 100 (couple minimal)
     *
     * COMPORTEMENT:
     * - Transition immédiate (pas de fade)
     * - La simulation continue de tourner (si start() a été appelé)
     * - Utile pour réinitialiser rapidement entre deux scénarios
     *
     * DIFFERENCE avec stop():
     * - resetToNeutral(): position neutre mais simulation active
     * - stop(): position neutre ET arrêt de la simulation (fade-out)
     *
     * USAGE TYPIQUE:
     * - Changement de scène/niveau dans un jeu
     * - Repositionnement entre deux tests
     * - Bouton "Reset" dans une interface utilisateur
     *
     * @note La plateforme doit être connectée (connect() appelé)
     * @note Pas besoin que start() soit appelé
     *
     * @see stop()
     * @see update()
     */
    void resetToNeutral();

    // ========================================================================
    // INTERROGATION D'ETAT (STATUS)
    // ========================================================================

    /**
     * @brief Vérifie si la plateforme est connectée
     *
     * @return true si connect() a été appelé avec succès et disconnect() pas encore appelé
     * @return false si non connecté ou après disconnect()
     *
     * USAGE:
     * Utilisez cette méthode pour vérifier l'état avant d'envoyer des commandes.
     *
     * @code
     *   if (dbox.isConnected()) {
     *       dbox.setMotion(roll, pitch, heave);
     *   }
     * @endcode
     *
     * @note Cette méthode est très rapide (simple lecture booléenne)
     * @note Ne vérifie PAS l'état réel du hardware (juste l'état logiciel)
     *
     * @see connect()
     * @see disconnect()
     */
    bool isConnected() const;

    /**
     * @brief Vérifie si la simulation est en cours d'exécution
     *
     * @return true si start() a été appelé et stop() pas encore appelé
     * @return false si non démarré, arrêté, ou non connecté
     *
     * USAGE:
     * Utilisez cette méthode pour vérifier si les mouvements sont actifs.
     *
     * @code
     *   if (!dbox.isRunning()) {
     *       dbox.start();
     *   }
     * @endcode
     *
     * @note Les commandes de mouvement peuvent être envoyées même si !isRunning()
     * @note Elles seront simplement ignorées jusqu'à l'appel de start()
     *
     * @see start()
     * @see stop()
     */
    bool isRunning() const;

    /**
     * @brief Récupère le dernier message d'erreur
     *
     * Retourne une description détaillée de la dernière erreur survenue.
     *
     * @return std::string Description de l'erreur, ou chaîne vide si aucune erreur
     *
     * USAGE TYPIQUE:
     * @code
     *   if (!dbox.connect()) {
     *       std::cerr << "ERREUR DBOX: " << dbox.getLastError() << std::endl;
     *       return -1;
     *   }
     * @endcode
     *
     * MESSAGES D'ERREUR POSSIBLES:
     * - "DBOX LiveMotion initialization failed"
     * - "Failed to register event types"
     * - "Failed to register data structures"
     * - "Failed to open DBOX connection - Is DBOX Control Panel running?"
     * - "Failed to send configuration - DBOX not responding"
     * - "Not connected - call connect() first"
     * - "Already running - call stop() first"
     *
     * @note Le message persiste jusqu'à la prochaine erreur
     * @note Les opérations réussies ne réinitialisent PAS le message
     *
     * @see connect()
     * @see start()
     */
    std::string getLastError() const;

    // ========================================================================
    // CONFIGURATION AVANCEE (Optionnel)
    // ========================================================================

    /**
     * @brief Configure le gain maître global de la simulation
     *
     * Ajuste l'intensité globale de tous les mouvements.
     *
     * @param gain_db Gain en décibels
     *                Plage valide: -12.0 à +12.0 dB
     *                -12 dB = mouvements très atténués (50% amplitude)
     *                  0 dB = amplitude normale (défaut recommandé)
     *                +12 dB = mouvements amplifiés (200% amplitude)
     *
     * QUAND UTILISER:
     * - Pour adapter l'intensité selon le confort utilisateur
     * - Pour réduire les mouvements dans un espace restreint
     * - Pour augmenter l'intensité dans un simulateur sportif
     *
     * ATTENTION:
     * - Des gains élevés (+6 à +12) peuvent être inconfortables
     * - Des gains négatifs (-12 à -6) réduisent le réalisme
     * - Doit être appelé AVANT connect()
     *
     * @note Valeur par défaut: 0.0 dB (pas d'amplification)
     * @note Automatiquement clampé à [-12.0, +12.0]
     *
     * @see connect()
     */
    void setMasterGain(float gain_db);

    /**
     * @brief Configure la plage de régime moteur simulé
     *
     * Définit les limites min/max du régime moteur pour les vibrations.
     *
     * @param idle_rpm Régime au ralenti (défaut: 750 RPM)
     *                 Plage recommandée: 500-1000 RPM
     *
     * @param max_rpm Régime maximal (défaut: 6000 RPM)
     *                Plage recommandée: 4000-8000 RPM
     *
     * EXEMPLES DE CONFIGURATION:
     * - Moteur diesel: setEngineRange(600, 4500)
     * - Moteur essence standard: setEngineRange(750, 6000)
     * - Moteur sportif: setEngineRange(1000, 8000)
     * - Moteur électrique (pas de vibrations): setEngineRange(0, 0)
     *
     * @note Doit être appelé AVANT connect()
     * @note Les valeurs RPM passées à setVibration() sont indépendantes
     *
     * @see setVibration()
     * @see connect()
     */
    void setEngineRange(float idle_rpm, float max_rpm);

    /**
     * @brief Configure le couple moteur maximal simulé
     *
     * @param max_torque Couple maximal en N⋅m (défaut: 450)
     *                   Plage recommandée: 100-1000 N⋅m
     *
     * EXEMPLES:
     * - Petite voiture: setMaxTorque(150)
     * - Berline standard: setMaxTorque(300)
     * - Voiture sportive: setMaxTorque(600)
     * - Camion/SUV: setMaxTorque(800)
     *
     * @note Doit être appelé AVANT connect()
     * @note Affecte l'intensité des vibrations
     *
     * @see setVibration()
     */
    void setMaxTorque(float max_torque);

private:
    // ========================================================================
    // VARIABLES MEMBRES PRIVEES
    // ========================================================================

    // --- Etat de la connexion ---
    bool m_connected;    ///< true si connect() a réussi
    bool m_running;      ///< true si start() a été appelé

    // --- Gestion des erreurs ---
    std::string m_lastError;  ///< Dernier message d'erreur détaillé

    // --- Dernières valeurs de mouvement (cache) ---
    float m_roll;      ///< Dernier roulis envoyé [-1.0, +1.0]
    float m_pitch;     ///< Dernier tangage envoyé [-1.0, +1.0]
    float m_heave;     ///< Dernier pilonnement envoyé [-1.0, +1.0]
    float m_rpm;       ///< Dernier régime moteur envoyé [0, 10000]
    float m_torque;    ///< Dernier couple moteur envoyé [0, 1000]

    // --- Configuration (définie avant connect) ---
    float m_masterGain;      ///< Gain global en dB [-12, +12]
    float m_engineIdleRpm;   ///< Régime ralenti [500, 1000]
    float m_engineMaxRpm;    ///< Régime maximal [4000, 8000]
    float m_engineMaxTorque; ///< Couple maximal [100, 1000]

    // ========================================================================
    // METHODES PRIVEES (HELPERS)
    // ========================================================================

    /**
     * @brief Envoie les données de mouvement actuelles à la DBOX
     *
     * Utilise les valeurs stockées dans m_roll, m_pitch, m_heave, m_rpm, m_torque
     * pour construire une structure MotionData et l'envoyer via PostEvent().
     *
     * PREREQUIS:
     * - La connexion doit être établie (m_connected == true)
     * - Les valeurs membres doivent être valides
     *
     * @note Appelée automatiquement par update(), setMotion(), setVibration()
     * @note Ne fait rien si non connecté (fail-safe)
     */
    void sendMotionData();

    /**
     * @brief Limite une valeur flottante à un intervalle donné
     *
     * @param value Valeur à limiter
     * @param min Borne minimale
     * @param max Borne maximale
     * @return float Valeur clampée dans [min, max]
     *
     * Exemple: clamp(1.5f, -1.0f, 1.0f) retourne 1.0f
     */
    float clamp(float value, float min, float max) const;

    /**
     * @brief Enregistre un message d'erreur interne
     *
     * @param error Message d'erreur à stocker dans m_lastError
     *
     * @note Utilisé par toutes les méthodes publiques en cas d'échec
     */
    void setError(const std::string& error);
};

#endif // SIMPLE_DBOX_API_H
