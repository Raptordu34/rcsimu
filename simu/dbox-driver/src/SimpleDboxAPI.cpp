/**
 * @file SimpleDboxAPI.cpp
 * @brief Implémentation de l'API simplifiée pour contrôler la plateforme DBOX
 *
 * Ce fichier contient l'implémentation complète de la classe SimpleDboxController.
 * Il encapsule toute la complexité du SDK DBOX LiveMotion pour fournir une interface
 * simple et intuitive.
 *
 * DEPENDANCES:
 * - dboxLiveMotion.h : SDK officiel DBOX
 * - Windows.h : Pour Sleep (temporisation)
 * - iostream : Pour logging d'erreurs
 *
 * @author DBOX Integration Team
 * @date 2024
 *
 * NOTE: Logging convention:
 * All output to std::cout/std::cerr must be prefixed with "[SimpleDboxAPI] "
 */

#include "SimpleDboxAPI.h"
#include "LiveMotion/dboxLiveMotion.h"
#include <Windows.h>
#include <iostream>
#include <algorithm>  // Pour std::min, std::max

// ============================================================================
// CONSTANTES PRIVEES
// ============================================================================

/**
 * Clé d'application unique pour DBOX
 * Cette clé identifie l'application auprès du système DBOX.
 * Elle doit être unique pour chaque application développée.
 */
static const char* const APP_KEY = "LPSIMUDevSim";

/**
 * Numéro de build de l'application
 * Incrémentez ce numéro à chaque nouvelle version de votre application.
 */
static const dbox::U32 APP_BUILD = 1000;

// ============================================================================
// STRUCTURES DBOX (Copie depuis l'API originale)
// ============================================================================

/**
 * @struct SimConfig
 * @brief Configuration générale du simulateur DBOX
 *
 * Cette structure définit les paramètres globaux de la simulation.
 * Elle doit être envoyée une fois au démarrage via PostEvent(SIM_CONFIG).
 */
struct SimConfig {
    dbox::F32 MasterGain;      ///< Gain principal en décibels [-12.0, +12.0]
    dbox::F32 MasterSpectrum;  ///< Filtrage fréquentiel en dB (généralement 0.0)

    // Macro DBOX pour l'introspection de structure
    // Permet au SDK de connaître les champs et leurs types
    DBOX_STRUCTINFO_BEGIN()
    DBOX_STRUCTINFO_FIELD(SimConfig, MasterGain, dbox::FT_FLOAT32, dbox::FM_MASTER_GAIN_DB)
    DBOX_STRUCTINFO_FIELD(SimConfig, MasterSpectrum, dbox::FT_FLOAT32, dbox::FM_MASTER_SPECTRUM_DB)
    DBOX_STRUCTINFO_END()
};

/**
 * @struct MotionConfig
 * @brief Configuration du moteur simulé pour les vibrations
 *
 * Cette structure définit les paramètres du moteur virtuel qui génère
 * les vibrations. Elle doit être envoyée une fois au démarrage.
 */
struct MotionConfig {
    dbox::F32 EngineRpmIdle;    ///< Régime au ralenti en RPM (ex: 750)
    dbox::F32 EngineRpmMax;     ///< Régime maximum en RPM (ex: 6000)
    dbox::F32 EngineTorqueMax;  ///< Couple maximum en N⋅m (ex: 450)

    DBOX_STRUCTINFO_BEGIN()
    DBOX_STRUCTINFO_FIELD(MotionConfig, EngineRpmIdle, dbox::FT_FLOAT32, dbox::FM_ENGINE_RPM_IDLE)
    DBOX_STRUCTINFO_FIELD(MotionConfig, EngineRpmMax, dbox::FT_FLOAT32, dbox::FM_ENGINE_RPM_MAX)
    DBOX_STRUCTINFO_FIELD(MotionConfig, EngineTorqueMax, dbox::FT_FLOAT32, dbox::FM_ENGINE_TORQUE_MAX)
    DBOX_STRUCTINFO_END()
};

/**
 * @struct MotionData
 * @brief Données de mouvement temps réel
 *
 * Cette structure contient les valeurs de mouvement envoyées à chaque frame.
 * Elle doit être envoyée en continu (typiquement à 100 Hz) via PostEvent(MOTION_DATA).
 */
struct MotionData {
    dbox::F32 Roll;         ///< Roulis normalisé [-1.0, +1.0]
    dbox::F32 Pitch;        ///< Tangage normalisé [-1.0, +1.0]
    dbox::F32 Heave;        ///< Pilonnement normalisé [-1.0, +1.0]
    dbox::F32 EngineRpm;    ///< Régime moteur [0, 10000] RPM
    dbox::F32 EngineTorque; ///< Couple moteur [0, 1000] N⋅m

    DBOX_STRUCTINFO_BEGIN()
    DBOX_STRUCTINFO_FIELD(MotionData, Roll, dbox::FT_FLOAT32, dbox::FM_RAW_ROLL)
    DBOX_STRUCTINFO_FIELD(MotionData, Pitch, dbox::FT_FLOAT32, dbox::FM_RAW_PITCH)
    DBOX_STRUCTINFO_FIELD(MotionData, Heave, dbox::FT_FLOAT32, dbox::FM_RAW_HEAVE)
    DBOX_STRUCTINFO_FIELD(MotionData, EngineRpm, dbox::FT_FLOAT32, dbox::FM_ENGINE_RPM)
    DBOX_STRUCTINFO_FIELD(MotionData, EngineTorque, dbox::FT_FLOAT32, dbox::FM_ENGINE_TORQUE)
    DBOX_STRUCTINFO_END()
};

/**
 * @enum AppEvents
 * @brief Identifiants des événements DBOX
 *
 * Chaque type d'événement doit avoir un ID unique.
 * Ces IDs sont utilisés pour enregistrer et envoyer les événements.
 */
enum AppEvents {
    SIM_CONFIG = 1000,      ///< Configuration simulation (SimConfig)
    MOTION_CONFIG = 2000,   ///< Configuration moteur (MotionConfig)
    MOTION_DATA = 3000,     ///< Données de mouvement (MotionData)
};

// ============================================================================
// CONSTRUCTEUR ET DESTRUCTEUR
// ============================================================================

SimpleDboxController::SimpleDboxController()
    : m_connected(false)
    , m_running(false)
    , m_lastError("")
    , m_roll(0.0f)
    , m_pitch(0.0f)
    , m_heave(0.0f)
    , m_rpm(1000.0f)
    , m_torque(100.0f)
    , m_masterGain(0.0f)
    , m_engineIdleRpm(750.0f)
    , m_engineMaxRpm(6000.0f)
    , m_engineMaxTorque(450.0f)
{
    // Constructeur : initialise tous les membres à des valeurs sûres par défaut
    // Pas de connexion automatique - l'utilisateur doit appeler connect() explicitement
}

SimpleDboxController::~SimpleDboxController()
{
    // Destructeur : vérifie si disconnect() a été appelé
    if (m_connected) {
        // WARNING: Mauvaise pratique de ne pas appeler disconnect() avant destruction
        std::cerr << "[SimpleDboxAPI] WARNING: Destructeur appelé sans disconnect() préalable!" << std::endl;
        std::cerr << "[SimpleDboxAPI] Appel automatique de disconnect() pour éviter les fuites de ressources." << std::endl;

        // Tentative de nettoyage automatique (non recommandé mais sécuritaire)
        disconnect();
    }
}

// ============================================================================
// GESTION DU CYCLE DE VIE
// ============================================================================

bool SimpleDboxController::connect()
{
    // === ETAPE 0 : Vérifications préalables ===

    if (m_connected) {
        setError("Already connected - call disconnect() first");
        return false;
    }

    std::cout << "[SimpleDboxAPI] Connexion à la plateforme DBOX..." << std::endl;

    // === ETAPE 1 : Initialisation du SDK DBOX LiveMotion ===

    std::cout << "[SimpleDboxAPI] 1/5 - Initialisation du SDK DBOX..." << std::endl;

    // Initialize() prépare le SDK et vérifie que le DBOX Control Panel est actif
    // Note: Initialize() ne retourne pas de valeur (void)
    dbox::LiveMotion::Initialize(APP_KEY, APP_BUILD);

    // === ETAPE 2 : Enregistrement des événements avec leurs structures ===

    std::cout << "[SimpleDboxAPI] 2/5 - Enregistrement des événements..." << std::endl;

    // RegisterEvent() enregistre à la fois le type d'événement ET sa structure
    // Syntaxe: RegisterEvent(ID, Mode, StructInfo)
    //   - ID: identifiant unique de l'événement
    //   - Mode: EM_CONFIG_UPDATE pour configs, EM_FRAME_UPDATE pour data temps réel
    //   - StructInfo: métadonnées de la structure (via GetStructInfo())

    dbox::LiveMotion::RegisterEvent(SIM_CONFIG, dbox::EM_CONFIG_UPDATE, SimConfig::GetStructInfo());
    dbox::LiveMotion::RegisterEvent(MOTION_CONFIG, dbox::EM_CONFIG_UPDATE, MotionConfig::GetStructInfo());
    dbox::LiveMotion::RegisterEvent(MOTION_DATA, dbox::EM_FRAME_UPDATE, MotionData::GetStructInfo());

    // === ETAPE 3 : Ouverture de la connexion hardware ===

    std::cout << "[SimpleDboxAPI] 3/5 - Connexion au hardware DBOX..." << std::endl;

    // Open() établit la connexion avec le hardware physique
    // Retourne un code d'erreur (LMR_SUCCESS si OK)
    // Peut échouer si :
    // - DBOX Control Panel n'est pas lancé
    // - Hardware DBOX non connecté ou hors tension
    // - Une autre application contrôle déjà la DBOX
    if (dbox::LiveMotion::Open() != dbox::LMR_SUCCESS) {
        setError("Failed to open DBOX connection - Check hardware and Control Panel");
        dbox::LiveMotion::Terminate();
        return false;
    }

    // === ETAPE 4 : Envoi de la configuration simulation ===

    std::cout << "[SimpleDboxAPI] 4/5 - Envoi de la configuration simulation..." << std::endl;

    // Prépare la configuration générale avec les valeurs définies
    SimConfig simConfig;
    simConfig.MasterGain = m_masterGain;        // Gain global (défaut: 0.0 dB)
    simConfig.MasterSpectrum = 0.0f;             // Pas de filtrage fréquentiel

    // PostEvent() envoie la structure au système DBOX
    // Note: PostEvent() ne retourne pas de valeur (void)
    dbox::LiveMotion::PostEvent(SIM_CONFIG, simConfig);

    // === ETAPE 5 : Envoi de la configuration moteur ===

    std::cout << "[SimpleDboxAPI] 5/5 - Envoi de la configuration moteur..." << std::endl;

    // Prépare la configuration du moteur virtuel pour les vibrations
    MotionConfig motionConfig;
    motionConfig.EngineRpmIdle = m_engineIdleRpm;      // Ralenti (défaut: 750 RPM)
    motionConfig.EngineRpmMax = m_engineMaxRpm;        // Maximum (défaut: 6000 RPM)
    motionConfig.EngineTorqueMax = m_engineMaxTorque;  // Couple max (défaut: 450 N⋅m)

    // PostEvent() envoie la configuration moteur
    dbox::LiveMotion::PostEvent(MOTION_CONFIG, motionConfig);

    // === SUCCESS : Connexion établie ===

    m_connected = true;
    std::cout << "[SimpleDboxAPI] Connexion réussie! Plateforme prête." << std::endl;

    return true;
}

void SimpleDboxController::disconnect()
{
    // Vérification : est-ce qu'on est connecté ?
    if (!m_connected) {
        // Pas d'erreur, juste un no-op silencieux (idempotence)
        return;
    }

    std::cout << "[SimpleDboxAPI] Déconnexion de la plateforme DBOX..." << std::endl;

    // === ETAPE 1 : Arrêter la simulation si elle tourne ===

    if (m_running) {
        std::cout << "[SimpleDboxAPI] Arrêt de la simulation en cours..." << std::endl;
        stop();  // Arrête proprement avec fade-out
    }

    // === ETAPE 2 : Retour à la position neutre ===

    std::cout << "[SimpleDboxAPI] Retour à la position neutre..." << std::endl;
    resetToNeutral();

    // Petite pause pour laisser le temps à la plateforme de se repositionner
    Sleep(500);  // 500 ms

    // === ETAPE 3 : Fermeture de la connexion hardware ===

    std::cout << "[SimpleDboxAPI] Fermeture de la connexion hardware..." << std::endl;
    dbox::LiveMotion::Close();

    // === ETAPE 4 : Libération des ressources du SDK ===

    std::cout << "[SimpleDboxAPI] Libération des ressources SDK..." << std::endl;
    dbox::LiveMotion::Terminate();

    // === ETAPE 5 : Réinitialisation de l'état interne ===

    m_connected = false;
    m_running = false;

    std::cout << "[SimpleDboxAPI] Déconnexion terminée." << std::endl;
}

bool SimpleDboxController::start()
{
    // === VERIFICATION : Est-ce qu'on peut démarrer ? ===

    if (!m_connected) {
        setError("Not connected - call connect() first");
        return false;
    }

    if (m_running) {
        setError("Already running - call stop() first to restart");
        return false;
    }

    std::cout << "[SimpleDboxAPI] Démarrage de la simulation..." << std::endl;

    // === DEMARRAGE : Start() active la simulation avec fade-in ===

    // Start() :
    // - Active le traitement des événements MOTION_DATA
    // - Applique un fade-in progressif (2-3 secondes typiquement)
    // - Permet aux mouvements d'être appliqués physiquement
    // Note: Start() ne retourne pas de valeur (void)
    dbox::LiveMotion::Start();

    m_running = true;
    std::cout << "[SimpleDboxAPI] Simulation démarrée! Vous pouvez maintenant envoyer des commandes." << std::endl;

    return true;
}

bool SimpleDboxController::stop()
{
    // === VERIFICATION : Est-ce qu'on peut arrêter ? ===

    if (!m_connected) {
        setError("Not connected");
        return false;
    }

    if (!m_running) {
        // Pas une erreur, juste déjà arrêté (idempotence)
        return true;
    }

    std::cout << "[SimpleDboxAPI] Arrêt de la simulation..." << std::endl;

    // === ARRET : Stop() désactive la simulation avec fade-out ===

    // Stop() :
    // - Applique un fade-out progressif vers position neutre
    // - Désactive le traitement des événements MOTION_DATA
    // - La connexion reste active (pas besoin de reconnecter)
    // Note: Stop() ne retourne pas de valeur (void)
    dbox::LiveMotion::Stop();

    m_running = false;
    std::cout << "[SimpleDboxAPI] Simulation arrêtée." << std::endl;

    return true;
}

// ============================================================================
// CONTROLE DES MOUVEMENTS
// ============================================================================

void SimpleDboxController::setMotion(float roll, float pitch, float heave)
{
    // === VALIDATION ET CLAMPING AUTOMATIQUE ===

    // Limite les valeurs à l'intervalle [-1.0, +1.0]
    // Ceci garantit que les valeurs sont toujours dans la plage valide
    // même si l'utilisateur fournit des valeurs hors limites
    m_roll = clamp(roll, -1.0f, 1.0f);
    m_pitch = clamp(pitch, -1.0f, 1.0f);
    m_heave = clamp(heave, -1.0f, 1.0f);

    // Les valeurs RPM et Torque restent inchangées (dernières valeurs)

    // === ENVOI IMMEDIAT ===

    // Envoie les nouvelles valeurs à la DBOX
    sendMotionData();
}

void SimpleDboxController::setVibration(float rpm, float torque)
{
    // === VALIDATION ET CLAMPING AUTOMATIQUE ===

    // RPM : Plage [0, 10000] (large pour compatibilité)
    // Valeurs recommandées : 750 à 6000 RPM
    m_rpm = clamp(rpm, 0.0f, 10000.0f);

    // Torque : Plage [0, 1000] N⋅m (large pour compatibilité)
    // Valeurs recommandées : 0 à 450 N⋅m
    m_torque = clamp(torque, 0.0f, 1000.0f);

    // Les valeurs Roll/Pitch/Heave restent inchangées

    // === ENVOI IMMEDIAT ===

    sendMotionData();
}

void SimpleDboxController::update(float roll, float pitch, float heave,
                                   float rpm, float torque)
{
    // === MISE A JOUR ATOMIQUE DE TOUS LES PARAMETRES ===

    // Cette méthode est optimisée pour mettre à jour toutes les valeurs
    // en une seule fois, ce qui est plus efficace que d'appeler
    // setMotion() puis setVibration() séparément.

    // Validation et clamping de tous les paramètres
    m_roll = clamp(roll, -1.0f, 1.0f);
    m_pitch = clamp(pitch, -1.0f, 1.0f);
    m_heave = clamp(heave, -1.0f, 1.0f);
    m_rpm = clamp(rpm, 0.0f, 10000.0f);
    m_torque = clamp(torque, 0.0f, 1000.0f);

    // Envoi unique des données complètes
    sendMotionData();
}

void SimpleDboxController::resetToNeutral()
{
    // === RETOUR IMMEDIAT A LA POSITION NEUTRE ===

    std::cout << "[SimpleDboxAPI] Retour à la position neutre..." << std::endl;

    // Position neutre : tous les mouvements à zéro
    m_roll = 0.0f;    // Horizontal (pas d'inclinaison gauche-droite)
    m_pitch = 0.0f;   // Horizontal (pas d'inclinaison avant-arrière)
    m_heave = 0.0f;   // Hauteur moyenne

    // Vibrations minimales (ralenti)
    m_rpm = 1000.0f;   // RPM au ralenti
    m_torque = 100.0f; // Couple minimal

    // Envoi immédiat de la position neutre
    sendMotionData();
}

// ============================================================================
// INTERROGATION D'ETAT
// ============================================================================

bool SimpleDboxController::isConnected() const
{
    // Retourne simplement l'état de connexion
    // Pas de vérification hardware (rapide)
    return m_connected;
}

bool SimpleDboxController::isRunning() const
{
    // Retourne simplement l'état de simulation
    // Pas de vérification hardware (rapide)
    return m_running;
}

std::string SimpleDboxController::getLastError() const
{
    // Retourne le dernier message d'erreur enregistré
    return m_lastError;
}

// ============================================================================
// CONFIGURATION AVANCEE
// ============================================================================

void SimpleDboxController::setMasterGain(float gain_db)
{
    // === VALIDATION ET CLAMPING ===

    // Le gain maître doit être dans [-12.0, +12.0] dB
    m_masterGain = clamp(gain_db, -12.0f, +12.0f);

    // === AVERTISSEMENT SI DEJA CONNECTE ===

    if (m_connected) {
        std::cerr << "[SimpleDboxAPI] WARNING: setMasterGain() appelé après connect()!" << std::endl;
        std::cerr << "[SimpleDboxAPI] Le gain sera appliqué à la prochaine connexion." << std::endl;
        std::cerr << "[SimpleDboxAPI] Pour appliquer maintenant, appelez disconnect() puis connect()." << std::endl;
    }
}

void SimpleDboxController::setEngineRange(float idle_rpm, float max_rpm)
{
    // === VALIDATION ===

    // Idle RPM : minimum 0, maximum 2000 (pour éviter des configs absurdes)
    m_engineIdleRpm = clamp(idle_rpm, 0.0f, 2000.0f);

    // Max RPM : minimum 1000, maximum 10000
    m_engineMaxRpm = clamp(max_rpm, 1000.0f, 10000.0f);

    // === VERIFICATION DE COHERENCE ===

    // S'assurer que max > idle
    if (m_engineMaxRpm <= m_engineIdleRpm) {
        std::cerr << "[SimpleDboxAPI] WARNING: Max RPM <= Idle RPM!" << std::endl;
        std::cerr << "[SimpleDboxAPI] Ajustement automatique: Max RPM = Idle RPM + 1000" << std::endl;
        m_engineMaxRpm = m_engineIdleRpm + 1000.0f;
    }

    // === AVERTISSEMENT SI DEJA CONNECTE ===

    if (m_connected) {
        std::cerr << "[SimpleDboxAPI] WARNING: setEngineRange() appelé après connect()!" << std::endl;
        std::cerr << "[SimpleDboxAPI] Les valeurs seront appliquées à la prochaine connexion." << std::endl;
    }
}

void SimpleDboxController::setMaxTorque(float max_torque)
{
    // === VALIDATION ET CLAMPING ===

    // Couple maximal : minimum 50 N⋅m, maximum 1000 N⋅m
    m_engineMaxTorque = clamp(max_torque, 50.0f, 1000.0f);

    // === AVERTISSEMENT SI DEJA CONNECTE ===

    if (m_connected) {
        std::cerr << "[SimpleDboxAPI] WARNING: setMaxTorque() appelé après connect()!" << std::endl;
        std::cerr << "[SimpleDboxAPI] La valeur sera appliquée à la prochaine connexion." << std::endl;
    }
}

// ============================================================================
// METHODES PRIVEES (HELPERS)
// ============================================================================

void SimpleDboxController::sendMotionData()
{
    // === VERIFICATION : Peut-on envoyer ? ===

    if (!m_connected) {
        // Pas connecté : échec silencieux (pas d'erreur pour ne pas polluer)
        // L'utilisateur peut appeler setMotion() avant connect() sans problème
        return;
    }

    // === CONSTRUCTION DE LA STRUCTURE MOTION DATA ===

    MotionData data;
    data.Roll = m_roll;       // Roulis [-1.0, +1.0]
    data.Pitch = m_pitch;     // Tangage [-1.0, +1.0]
    data.Heave = m_heave;     // Pilonnement [-1.0, +1.0]
    data.EngineRpm = m_rpm;   // Régime moteur [0, 10000]
    data.EngineTorque = m_torque;  // Couple [0, 1000]

    // === ENVOI A LA DBOX ===

    // PostEvent() envoie la structure au système DBOX
    // Cette opération est très rapide (< 1ms typiquement)
    // Elle ne bloque pas et retourne immédiatement
    // Note: PostEvent() ne retourne pas de valeur (void)
    dbox::LiveMotion::PostEvent(MOTION_DATA, data);

    // NOTE IMPORTANTE SUR LE TIMING :
    // Cette méthode envoie les données immédiatement, mais ne bloque pas.
    // Pour un contrôle fluide, appelez cette méthode (ou update()) dans une boucle
    // à 100 Hz (toutes les 10 ms) pour synchroniser avec le taux de rafraîchissement DBOX.
    //
    // Exemple de boucle de contrôle :
    //
    //   while (running) {
    //       dbox.update(roll, pitch, heave, rpm, torque);
    //       std::this_thread::sleep_for(std::chrono::milliseconds(10));  // 100 Hz
    //   }
}

float SimpleDboxController::clamp(float value, float min, float max) const
{
    // === CLAMPING SIMPLE ===

    // Si valeur < min, retourne min
    // Si valeur > max, retourne max
    // Sinon, retourne value

    if (value < min) return min;
    if (value > max) return max;
    return value;

    // Alternative avec std::algorithm (plus lisible mais même performance) :
    // return std::max(min, std::min(value, max));
}

void SimpleDboxController::setError(const std::string& error)
{
    // === ENREGISTREMENT DE L'ERREUR ===

    // Stocke le message d'erreur pour consultation ultérieure via getLastError()
    m_lastError = error;

    // Log l'erreur sur la sortie d'erreur standard pour le débogage
    std::cerr << "[SimpleDboxAPI] ERROR: " << error << std::endl;

    // NOTE : On ne lève PAS d'exception, conformément au design choisi
    // L'utilisateur doit vérifier les valeurs de retour (bool) et appeler getLastError()
}
