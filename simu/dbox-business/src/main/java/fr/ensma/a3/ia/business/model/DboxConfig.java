package fr.ensma.a3.ia.business.model;

/**
 * Configuration du système DBOX
 *
 * Cette classe contient tous les paramètres configurables pour le traitement
 * des données et la communication avec la DBOX.
 */
public class DboxConfig {

    // ===========================================================================
    // CONFIGURATION TRAITEMENT DES DONNEES
    // ===========================================================================

    /** Facteur de gain pour le roulis (roll) [0.0, 2.0] */
    private float rollGain;

    /** Facteur de gain pour le tangage (pitch) [0.0, 2.0] */
    private float pitchGain;

    /** Facteur de gain pour le pilonnement (heave) [0.0, 2.0] */
    private float heaveGain;

    /** Activation du filtrage des données */
    private boolean filteringEnabled;

    /** Facteur de lissage pour le filtre passe-bas [0.0, 1.0] */
    private float smoothingFactor;

    // ===========================================================================
    // COURBE DE REPONSE (S-CURVE)
    // ===========================================================================

    /** Intensité de la S-curve [0.0=linéaire, 1.0=S-curve max] */
    private float sCurveIntensity;

    // ===========================================================================
    // ZONE MORTE (DEADZONE)
    // ===========================================================================

    /** Zone morte accéléromètre [0.0, 0.5] en g */
    private float accelDeadzone;

    /** Zone morte gyroscope [0.0, 50.0] en °/s */
    private float gyroDeadzone;

    // ===========================================================================
    // RATE LIMITING (Limitation du taux de changement)
    // ===========================================================================

    /** Taux de changement max par frame pour roll/pitch/heave [0.01, 1.0] */
    private float maxRateOfChange;

    /** Activation du rate limiting */
    private boolean rateLimitingEnabled;

    // ===========================================================================
    // RATIO ACCELEROMETRE/GYROSCOPE
    // ===========================================================================

    /** Poids de l'accéléromètre [0.0, 1.0] (gyro = 1 - accelWeight) */
    private float accelWeight;

    // ===========================================================================
    // PLAGES DE NORMALISATION (basées sur specs MPU-6050)
    // ===========================================================================

    /** Plage d'accélération latérale pour roll (en g) */
    private float accelYRange;

    /** Plage d'accélération longitudinale pour pitch (en g) */
    private float accelXRange;

    /** Plage d'accélération verticale pour heave (en g) */
    private float accelZRange;

    // ===========================================================================
    // CONFIGURATION MOTEUR SIMULE
    // ===========================================================================

    /** Régime moteur au ralenti (RPM) - utilisé comme base pour le calcul du RPM */
    private float engineIdleRpm;

    /** Régime moteur maximal (RPM) */
    private float engineMaxRpm;

    /** Couple moteur de base au ralenti (N⋅m) - utilisé comme base pour le calcul du couple */
    private float engineBaseTorque;

    /** Couple moteur maximal (N⋅m) */
    private float engineMaxTorque;

    // ===========================================================================
    // CONSTRUCTEUR PAR DEFAUT (Valeurs recommandées)
    // ===========================================================================

    /**
     * Constructeur avec valeurs par défaut optimales
     */
    public DboxConfig() {

        // Gains par défaut (1.0 = pas d'amplification)
        this.rollGain = 1.0f;
        this.pitchGain = 1.0f;
        this.heaveGain = 1.0f;

        // Filtrage activé par défaut pour un rendu plus fluide
        this.filteringEnabled = true;
        this.smoothingFactor = 0.4f;  // Bon compromis réactivité/lissage

        // S-Curve pour des transitions douces
        this.sCurveIntensity = 0.7f;

        // Zone morte pour éliminer le bruit capteur
        this.accelDeadzone = 0.05f;  // ~50mg RMS noise du MPU-6050
        this.gyroDeadzone = 5.0f;    // Élimine la dérive gyro au repos

        // Rate limiting pour des transitions fluides
        this.maxRateOfChange = 0.15f;  // ~140ms pour 0→1 à 50Hz
        this.rateLimitingEnabled = true;

        // Ratio accel/gyro (90% accel par défaut)
        this.accelWeight = 0.9f;

        // Plages de normalisation optimisées pour MPU-6050 (±2g, ±250°/s)
        this.accelYRange = 0.8f;  // Plus sensible aux virages
        this.accelXRange = 0.8f;  // Plus sensible au freinage/accélération
        this.accelZRange = 0.3f;  // Très réactif aux bosses

        // Configuration moteur par défaut
        this.engineIdleRpm = 0.0f;
        this.engineMaxRpm = 6000.0f;
        this.engineBaseTorque = 0.0f;
        this.engineMaxTorque = 450.0f;
    }

    // ===========================================================================
    // GETTERS ET SETTERS
    // ===========================================================================

    public float getRollGain() {
        return rollGain;
    }

    public void setRollGain(float rollGain) {
        this.rollGain = clamp(rollGain, 0.0f, 2.0f);
    }

    public float getPitchGain() {
        return pitchGain;
    }

    public void setPitchGain(float pitchGain) {
        this.pitchGain = clamp(pitchGain, 0.0f, 2.0f);
    }

    public float getHeaveGain() {
        return heaveGain;
    }

    public void setHeaveGain(float heaveGain) {
        this.heaveGain = clamp(heaveGain, 0.0f, 2.0f);
    }

    public boolean isFilteringEnabled() {
        return filteringEnabled;
    }

    public void setFilteringEnabled(boolean filteringEnabled) {
        this.filteringEnabled = filteringEnabled;
    }

    public float getSmoothingFactor() {
        return smoothingFactor;
    }

    public void setSmoothingFactor(float smoothingFactor) {
        this.smoothingFactor = clamp(smoothingFactor, 0.0f, 1.0f);
    }

    // --- S-Curve ---

    public float getSCurveIntensity() {
        return sCurveIntensity;
    }

    public void setSCurveIntensity(float sCurveIntensity) {
        this.sCurveIntensity = clamp(sCurveIntensity, 0.0f, 1.0f);
    }

    // --- Deadzone ---

    public float getAccelDeadzone() {
        return accelDeadzone;
    }

    public void setAccelDeadzone(float accelDeadzone) {
        this.accelDeadzone = clamp(accelDeadzone, 0.0f, 0.5f);
    }

    public float getGyroDeadzone() {
        return gyroDeadzone;
    }

    public void setGyroDeadzone(float gyroDeadzone) {
        this.gyroDeadzone = clamp(gyroDeadzone, 0.0f, 50.0f);
    }

    // --- Rate Limiting ---

    public float getMaxRateOfChange() {
        return maxRateOfChange;
    }

    public void setMaxRateOfChange(float maxRateOfChange) {
        this.maxRateOfChange = clamp(maxRateOfChange, 0.01f, 1.0f);
    }

    public boolean isRateLimitingEnabled() {
        return rateLimitingEnabled;
    }

    public void setRateLimitingEnabled(boolean rateLimitingEnabled) {
        this.rateLimitingEnabled = rateLimitingEnabled;
    }

    // --- Ratio Accel/Gyro ---

    public float getAccelWeight() {
        return accelWeight;
    }

    public void setAccelWeight(float accelWeight) {
        this.accelWeight = clamp(accelWeight, 0.0f, 1.0f);
    }

    // --- Plages de normalisation ---

    public float getAccelYRange() {
        return accelYRange;
    }

    public void setAccelYRange(float accelYRange) {
        this.accelYRange = clamp(accelYRange, 0.1f, 2.0f);
    }

    public float getAccelXRange() {
        return accelXRange;
    }

    public void setAccelXRange(float accelXRange) {
        this.accelXRange = clamp(accelXRange, 0.1f, 2.0f);
    }

    public float getAccelZRange() {
        return accelZRange;
    }

    public void setAccelZRange(float accelZRange) {
        this.accelZRange = clamp(accelZRange, 0.1f, 2.0f);
    }

    // --- Moteur ---

    public float getEngineIdleRpm() {
        return engineIdleRpm;
    }

    public void setEngineIdleRpm(float engineIdleRpm) {
        this.engineIdleRpm = clamp(engineIdleRpm, 0.0f, 2000.0f);
    }

    public float getEngineMaxRpm() {
        return engineMaxRpm;
    }

    public void setEngineMaxRpm(float engineMaxRpm) {
        this.engineMaxRpm = clamp(engineMaxRpm, 1000.0f, 10000.0f);
    }

    public float getEngineBaseTorque() {
        return engineBaseTorque;
    }

    public void setEngineBaseTorque(float engineBaseTorque) {
        this.engineBaseTorque = clamp(engineBaseTorque, 0.0f, 500.0f);
    }

    public float getEngineMaxTorque() {
        return engineMaxTorque;
    }

    public void setEngineMaxTorque(float engineMaxTorque) {
        this.engineMaxTorque = clamp(engineMaxTorque, 50.0f, 1000.0f);
    }

    // ===========================================================================
    // METHODES UTILITAIRES
    // ===========================================================================

    /**
     * Limite une valeur à un intervalle donné
     */
    private float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Crée une copie de la configuration
     */
    public DboxConfig copy() {
        DboxConfig copy = new DboxConfig();
        // UDP
        // copy.udpHost = this.udpHost;
        // copy.udpPort = this.udpPort;
        // copy.udpTimeout = this.udpTimeout;
        // Gains
        copy.rollGain = this.rollGain;
        copy.pitchGain = this.pitchGain;
        copy.heaveGain = this.heaveGain;
        // Filtrage
        copy.filteringEnabled = this.filteringEnabled;
        copy.smoothingFactor = this.smoothingFactor;
        // S-Curve
        copy.sCurveIntensity = this.sCurveIntensity;
        // Deadzone
        copy.accelDeadzone = this.accelDeadzone;
        copy.gyroDeadzone = this.gyroDeadzone;
        // Rate Limiting
        copy.maxRateOfChange = this.maxRateOfChange;
        copy.rateLimitingEnabled = this.rateLimitingEnabled;
        // Ratio Accel/Gyro
        copy.accelWeight = this.accelWeight;
        // Plages de normalisation
        copy.accelYRange = this.accelYRange;
        copy.accelXRange = this.accelXRange;
        copy.accelZRange = this.accelZRange;
        // Moteur
        copy.engineIdleRpm = this.engineIdleRpm;
        copy.engineMaxRpm = this.engineMaxRpm;
        copy.engineBaseTorque = this.engineBaseTorque;
        copy.engineMaxTorque = this.engineMaxTorque;
        return copy;
    }

    @Override
    public String toString() {
        return String.format(
            "DboxConfig{gains=(%.2f,%.2f,%.2f), sCurve=%.2f, deadzone=(%.2fg,%.1f°/s), " +
            "rateLimit=%s(%.2f), accelWeight=%.2f, ranges=(%.2f,%.2f,%.2f)g, filtering=%s}",
            rollGain, pitchGain, heaveGain,
            sCurveIntensity,
            accelDeadzone, gyroDeadzone,
            rateLimitingEnabled, maxRateOfChange,
            accelWeight,
            accelXRange, accelYRange, accelZRange,
            filteringEnabled
        );
    }
}
