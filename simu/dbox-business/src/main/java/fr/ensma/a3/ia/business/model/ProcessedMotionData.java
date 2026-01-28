package fr.ensma.a3.ia.business.model;

/**
 * Données de mouvement traitées et prêtes à être envoyées à la DBOX
 *
 * Toutes les valeurs sont normalisées, validées et prêtes à être
 * transmises au driver DBOX via UDP.
 *
 * Cette classe correspond exactement aux paramètres attendus par
 * SimpleDboxController::update(roll, pitch, heave, rpm, torque)
 */
public class ProcessedMotionData {

    // ===========================================================================
    // CONSTANTES DE VALIDATION
    // ===========================================================================

    /** Valeur minimale pour roll, pitch, heave (normalisés) */
    public static final float MIN_NORMALIZED = -1.0f;

    /** Valeur maximale pour roll, pitch, heave (normalisés) */
    public static final float MAX_NORMALIZED = 1.0f;

    /** Régime moteur minimal (RPM) */
    public static final float MIN_RPM = 0.0f;

    /** Régime moteur maximal (RPM) */
    public static final float MAX_RPM = 10000.0f;

    /** Couple moteur minimal (N⋅m) */
    public static final float MIN_TORQUE = 0.0f;

    /** Couple moteur maximal (N⋅m) */
    public static final float MAX_TORQUE = 1000.0f;

    // ===========================================================================
    // ATTRIBUTS
    // ===========================================================================

    /**
     * Timestamp de traitement (millisecondes depuis epoch)
     */
    private long timestamp;

    /**
     * Roulis (Roll) normalisé [-1.0, +1.0]
     * -1.0 = inclinaison maximale à gauche
     *  0.0 = position neutre
     * +1.0 = inclinaison maximale à droite
     */
    private float roll;

    /**
     * Tangage (Pitch) normalisé [-1.0, +1.0]
     * -1.0 = inclinaison maximale arrière (cabré)
     *  0.0 = position neutre
     * +1.0 = inclinaison maximale avant (piqué)
     */
    private float pitch;

    /**
     * Pilonnement (Heave) normalisé [-1.0, +1.0]
     * -1.0 = descente maximale
     *  0.0 = position neutre
     * +1.0 = montée maximale
     */
    private float heave;

    /**
     * Régime moteur simulé [0, 10000] RPM
     * Valeurs recommandées : 750-6000 RPM
     */
    private float rpm;

    /**
     * Couple moteur simulé [0, 1000] N⋅m
     * Valeurs recommandées : 0-450 N⋅m
     */
    private float torque;

    // ===========================================================================
    // CONSTRUCTEURS
    // ===========================================================================

    /**
     * Constructeur par défaut (position neutre, moteur arrêté)
     */
    public ProcessedMotionData() {
        this.timestamp = System.currentTimeMillis();
        this.roll = 0.0f;
        this.pitch = 0.0f;
        this.heave = 0.0f;
        this.rpm = 0.0f;      // Moteur arrêté (configurable via DboxConfig.engineIdleRpm)
        this.torque = 0.0f;   // Pas de couple (configurable via DboxConfig)
    }

    /**
     * Constructeur complet avec validation automatique
     *
     * @param timestamp Timestamp du traitement
     * @param roll Roulis [-1.0, +1.0]
     * @param pitch Tangage [-1.0, +1.0]
     * @param heave Pilonnement [-1.0, +1.0]
     * @param rpm Régime moteur [0, 10000]
     * @param torque Couple moteur [0, 1000]
     */
    public ProcessedMotionData(long timestamp, float roll, float pitch, float heave,
                              float rpm, float torque) {
        this.timestamp = timestamp;
        // Clamping automatique dans les setters
        setRoll(roll);
        setPitch(pitch);
        setHeave(heave);
        setRpm(rpm);
        setTorque(torque);
    }

    /**
     * Constructeur simplifié (mouvements uniquement)
     * RPM et Torque sont mis à des valeurs par défaut
     */
    public ProcessedMotionData(float roll, float pitch, float heave) {
        this();
        setRoll(roll);
        setPitch(pitch);
        setHeave(heave);
    }

    // ===========================================================================
    // GETTERS ET SETTERS (avec validation)
    // ===========================================================================

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = clamp(roll, MIN_NORMALIZED, MAX_NORMALIZED);
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = clamp(pitch, MIN_NORMALIZED, MAX_NORMALIZED);
    }

    public float getHeave() {
        return heave;
    }

    public void setHeave(float heave) {
        this.heave = clamp(heave, MIN_NORMALIZED, MAX_NORMALIZED);
    }

    public float getRpm() {
        return rpm;
    }

    public void setRpm(float rpm) {
        this.rpm = clamp(rpm, MIN_RPM, MAX_RPM);
    }

    public float getTorque() {
        return torque;
    }

    public void setTorque(float torque) {
        this.torque = clamp(torque, MIN_TORQUE, MAX_TORQUE);
    }

    // ===========================================================================
    // METHODES UTILITAIRES
    // ===========================================================================

    /**
     * Limite une valeur à un intervalle donné
     *
     * @param value Valeur à limiter
     * @param min Borne minimale
     * @param max Borne maximale
     * @return Valeur clampée dans [min, max]
     */
    private float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Vérifie si les données sont dans les plages valides
     * (redondant car les setters clampent, mais utile pour la validation)
     */
    public boolean isValid() {
        return roll >= MIN_NORMALIZED && roll <= MAX_NORMALIZED
            && pitch >= MIN_NORMALIZED && pitch <= MAX_NORMALIZED
            && heave >= MIN_NORMALIZED && heave <= MAX_NORMALIZED
            && rpm >= MIN_RPM && rpm <= MAX_RPM
            && torque >= MIN_TORQUE && torque <= MAX_TORQUE;
    }

    /**
     * Réinitialise à la position neutre (moteur arrêté)
     */
    public void reset() {
        this.roll = 0.0f;
        this.pitch = 0.0f;
        this.heave = 0.0f;
        this.rpm = 0.0f;
        this.torque = 0.0f;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format(
            "ProcessedMotionData{timestamp=%d, roll=%.3f, pitch=%.3f, heave=%.3f, rpm=%.1f, torque=%.1f}",
            timestamp, roll, pitch, heave, rpm, torque
        );
    }

    /**
     * Crée une copie des données
     */
    public ProcessedMotionData copy() {
        return new ProcessedMotionData(timestamp, roll, pitch, heave, rpm, torque);
    }
}
