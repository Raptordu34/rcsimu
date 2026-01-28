package fr.ensma.a3.ia.business.processor;

import fr.ensma.a3.ia.business.api.IMotionDataProcessor;
import fr.ensma.a3.ia.business.model.DboxConfig;
import fr.ensma.a3.ia.business.model.ProcessedMotionData;
import fr.ensma.a3.ia.business.model.RawMotionData;

/**
 * Implémentation du traitement des données de mouvement
 *
 * Ce processor convertit les données brutes du MPU6050 (accélération, gyroscope)
 * en commandes de mouvement pour la DBOX (roll, pitch, heave, rpm, torque).
 *
 * VERSION 2 - AMÉLIORATIONS:
 * - Courbe de réponse S-curve pour des transitions fluides
 * - Zone morte (deadzone) pour éliminer le bruit capteur
 * - Rate limiting pour éviter les changements brusques
 * - Ratio accel/gyro configurable
 * - Plages de normalisation basées sur les specs MPU-6050
 * - Filtrage passe-bas activé par défaut
 *
 * PIPELINE DE TRAITEMENT:
 * 1. Extraction des données brutes
 * 2. Application de la zone morte
 * 3. Conversion en mouvements (roll, pitch, heave) avec S-curve
 * 4. Application des gains configurables
 * 5. Filtrage passe-bas (si activé)
 * 6. Rate limiting (si activé)
 * 7. Clamping final
 *
 * AXES DE REFERENCE:
 * - X = Longitudinal (avant-arrière du véhicule)
 * - Y = Latéral (gauche-droite du véhicule)
 * - Z = Vertical (haut-bas du véhicule)
 *
 * SPECS MPU-6050 (valeurs par défaut):
 * - Accéléromètre: ±2g (sensibilité 16384 LSB/g)
 * - Gyroscope: ±250°/s (sensibilité 131 LSB/(°/s))
 */
public class MotionDataProcessor implements IMotionDataProcessor {

    // ===========================================================================
    // CONSTANTES DE CONVERSION
    // ===========================================================================

    /**
     * Gravité terrestre (en g)
     * Utilisée pour retirer l'offset gravitationnel sur l'axe Z
     */
    private static final float GRAVITY = 1.0f;

    /**
     * Facteur de conversion gyroscope → mouvement
     * Normalise les degrés/seconde en contribution au mouvement
     */
    private static final float GYRO_SCALE = 0.02f;

    /**
     * Plage maximale de l'accéléromètre (en g) pour la deadzone
     */
    private static final float ACCEL_MAX_RANGE = 2.0f;

    /**
     * Plage maximale du gyroscope (en °/s) pour la deadzone
     */
    private static final float GYRO_MAX_RANGE = 250.0f;

    /**
     * Facteur de conversion accélération → RPM
     */
    private static final float RPM_FACTOR = 300.0f;

    /**
     * Facteur de conversion accélération → couple
     */
    private static final float TORQUE_FACTOR = 30.0f;

    /**
     * Rate limiting pour RPM (max delta par frame)
     */
    private static final float RPM_RATE_LIMIT = 500.0f;

    /**
     * Rate limiting pour Torque (max delta par frame)
     */
    private static final float TORQUE_RATE_LIMIT = 50.0f;

    // ===========================================================================
    // ATTRIBUTS
    // ===========================================================================

    /** Configuration du processor */
    private final DboxConfig config;

    /** Indicateur de disponibilité */
    private boolean ready;

    /** Dernières valeurs traitées (pour le filtrage) */
    private ProcessedMotionData lastProcessed;

    // ===========================================================================
    // CONSTRUCTEUR
    // ===========================================================================

    /**
     * Constructeur avec configuration par défaut
     */
    public MotionDataProcessor() {
        this(new DboxConfig());
    }

    /**
     * Constructeur avec configuration personnalisée
     *
     * @param config Configuration du processor
     */
    public MotionDataProcessor(DboxConfig config) {
        this.config = config;
        this.ready = true;
        this.lastProcessed = new ProcessedMotionData(); // Position neutre
    }

    // ===========================================================================
    // IMPLEMENTATION DE L'INTERFACE
    // ===========================================================================

    @Override
    public ProcessedMotionData process(RawMotionData rawData) {
        // Vérification de la validité des données
        if (rawData == null) {
            System.err.println("[MotionDataProcessor] Données null reçues");
            return null;
        }

        if (!rawData.isValid()) {
            System.err.println("[MotionDataProcessor] Données invalides: " + rawData);
            return null;
        }

        // === ETAPE 1: EXTRACTION DES DONNEES BRUTES ===

        float accelX = rawData.getAccelX();  // Accélération longitudinale
        float accelY = rawData.getAccelY();  // Accélération latérale
        float accelZ = rawData.getAccelZ();  // Accélération verticale
        float gyroX = rawData.getGyroX();    // Vitesse angulaire roulis
        float gyroY = rawData.getGyroY();    // Vitesse angulaire tangage
        float gyroZ = rawData.getGyroZ();    // Vitesse angulaire lacet (non utilisé)

        // === ETAPE 2: CONVERSION EN MOUVEMENTS DBOX ===

        // ROLL (Roulis) - Inclinaison gauche-droite
        // Utilise l'accélération latérale (virage) + gyroscope
        float roll = convertToRoll(accelY, gyroX);

        // PITCH (Tangage) - Inclinaison avant-arrière
        // Utilise l'accélération longitudinale (freinage/accélération) + gyroscope
        float pitch = convertToPitch(accelX, gyroY);

        // HEAVE (Pilonnement) - Mouvement vertical
        // Utilise l'accélération verticale (bosses, sauts)
        float heave = convertToHeave(accelZ);

        // RPM (Régime moteur)
        // Calculé depuis l'intensité de l'accélération
        float rpm = convertToRpm(accelX, accelY);

        // TORQUE (Couple moteur)
        // Calculé depuis l'intensité de l'accélération
        float torque = convertToTorque(accelX, accelY);

        // === ETAPE 3: APPLICATION DES GAINS CONFIGURABLES ===

        roll *= config.getRollGain();
        pitch *= config.getPitchGain();
        heave *= config.getHeaveGain();

        // === ETAPE 4: FILTRAGE OPTIONNEL (LISSAGE) ===

        if (config.isFilteringEnabled()) {
            roll = applyLowPassFilter(roll, lastProcessed.getRoll());
            pitch = applyLowPassFilter(pitch, lastProcessed.getPitch());
            heave = applyLowPassFilter(heave, lastProcessed.getHeave());
            rpm = applyLowPassFilter(rpm, lastProcessed.getRpm());
            torque = applyLowPassFilter(torque, lastProcessed.getTorque());
        }

        // === ETAPE 5: RATE LIMITING (transitions douces) ===

        if (config.isRateLimitingEnabled()) {
            float maxDelta = config.getMaxRateOfChange();
            roll = applyRateLimiting(roll, lastProcessed.getRoll(), maxDelta);
            pitch = applyRateLimiting(pitch, lastProcessed.getPitch(), maxDelta);
            heave = applyRateLimiting(heave, lastProcessed.getHeave(), maxDelta);

            // Rate limiting plus doux pour RPM/Torque (éviter les à-coups moteur)
            rpm = applyRateLimiting(rpm, lastProcessed.getRpm(), RPM_RATE_LIMIT);
            torque = applyRateLimiting(torque, lastProcessed.getTorque(), TORQUE_RATE_LIMIT);
        }

        // === ETAPE 6: CREATION DES DONNEES TRAITEES ===

        ProcessedMotionData processed = new ProcessedMotionData(
            rawData.getTimestamp(),
            roll,
            pitch,
            heave,
            rpm,
            torque
        );

        // Sauvegarde pour le prochain filtrage
        lastProcessed = processed.copy();

        return processed;
    }

    @Override
    public void reset() {
        lastProcessed = new ProcessedMotionData(); // Retour position neutre
        System.out.println("[MotionDataProcessor] Reset effectué");
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    // ===========================================================================
    // METHODES DE CONVERSION PRIVEES
    // ===========================================================================

    /**
     * Convertit l'accélération latérale et le gyroscope en roulis (roll)
     *
     * LOGIQUE:
     * - L'accélération latérale (accelY) donne la force centrifuge dans un virage
     * - Le gyroscope (gyroX) donne la vitesse de rotation autour de l'axe longitudinal
     * - Zone morte appliquée pour éliminer le bruit
     * - S-curve appliquée pour des transitions fluides
     * - Ratio accel/gyro configurable
     *
     * @param accelY Accélération latérale (g)
     * @param gyroX Vitesse angulaire de roulis (deg/s)
     * @return Roulis normalisé [-1.0, +1.0]
     */
    private float convertToRoll(float accelY, float gyroX) {
        // Application de la zone morte pour éliminer le bruit
        float cleanAccelY = applyDeadzone(accelY, config.getAccelDeadzone(), ACCEL_MAX_RANGE);
        float cleanGyroX = applyDeadzone(gyroX, config.getGyroDeadzone(), GYRO_MAX_RANGE);

        // Composantes avec plages configurables
        float accelComponent = cleanAccelY / config.getAccelYRange();
        float gyroComponent = cleanGyroX * GYRO_SCALE;

        // Combinaison avec poids configurable
        float accelW = config.getAccelWeight();
        float roll = accelW * accelComponent + (1.0f - accelW) * gyroComponent;

        // Application de la S-curve pour des transitions fluides
        roll = applySCurve(roll, config.getSCurveIntensity());

        return clamp(-roll, -1.0f, 1.0f);
    }

    /**
     * Convertit l'accélération longitudinale et le gyroscope en tangage (pitch)
     *
     * LOGIQUE:
     * - L'accélération longitudinale (accelX) donne le freinage/accélération
     * - Le gyroscope (gyroY) donne la vitesse de rotation avant-arrière
     * - Zone morte appliquée pour éliminer le bruit
     * - S-curve appliquée pour des transitions fluides
     *
     * @param accelX Accélération longitudinale (g)
     * @param gyroY Vitesse angulaire de tangage (deg/s)
     * @return Tangage normalisé [-1.0, +1.0]
     */
    private float convertToPitch(float accelX, float gyroY) {
        // Application de la zone morte
        float cleanAccelX = applyDeadzone(accelX, config.getAccelDeadzone(), ACCEL_MAX_RANGE);
        float cleanGyroY = applyDeadzone(gyroY, config.getGyroDeadzone(), GYRO_MAX_RANGE);

        // Composante accélération (freinage = pitch avant, accélération = pitch arrière)
        float accelComponent = -cleanAccelX / config.getAccelXRange();
        float gyroComponent = cleanGyroY * GYRO_SCALE;

        // Combinaison avec poids configurable
        float accelW = config.getAccelWeight();
        float pitch = accelW * accelComponent + (1.0f - accelW) * gyroComponent;

        // Application de la S-curve
        pitch = applySCurve(pitch, config.getSCurveIntensity());

        return clamp(pitch, -1.0f, 1.0f);
    }

    /**
     * Convertit l'accélération verticale en pilonnement (heave)
     *
     * LOGIQUE:
     * - L'accélération verticale (accelZ) mesure les mouvements verticaux
     * - On retire la gravité terrestre (1g) pour avoir la vraie accélération
     * - Bosses = accelZ > 1g, Trous = accelZ < 1g
     * - Zone morte réduite de moitié pour plus de sensibilité aux bosses
     *
     * @param accelZ Accélération verticale (g)
     * @return Pilonnement normalisé [-1.0, +1.0]
     */
    private float convertToHeave(float accelZ) {
        // Retrait de la gravité pour avoir l'accélération verticale nette
        float netAccelZ = accelZ - GRAVITY;

        // Zone morte plus petite pour le heave (sensibilité aux bosses)
        float cleanAccelZ = applyDeadzone(netAccelZ, config.getAccelDeadzone() * 0.5f, 1.0f);

        // Normalisation avec plage configurable
        float heave = cleanAccelZ / config.getAccelZRange();

        // Application de la S-curve
        heave = applySCurve(heave, config.getSCurveIntensity());

        return clamp(heave, -1.0f, 1.0f);
    }

    /**
     * Calcule le régime moteur simulé (RPM)
     *
     * LOGIQUE SIMPLE:
     * - Plus l'accélération est forte, plus le régime moteur est élevé
     * - On utilise la norme de l'accélération (magnitude)
     * - Le RPM de base est configurable via DboxConfig.engineIdleRpm
     *
     * @param accelX Accélération longitudinale (m/s²)
     * @param accelY Accélération latérale (m/s²)
     * @return RPM [0, engineMaxRpm]
     */
    private float convertToRpm(float accelX, float accelY) {
        // Calcul de l'intensité de l'accélération (norme euclidienne)
        float accelMagnitude = (float) Math.sqrt(accelX * accelX + accelY * accelY);

        // RPM de base (configurable) + facteur proportionnel à l'accélération
        float rpm = config.getEngineIdleRpm() + accelMagnitude * RPM_FACTOR;

        return clamp(rpm, 0.0f, config.getEngineMaxRpm());
    }

    /**
     * Calcule le couple moteur simulé (Torque)
     *
     * LOGIQUE SIMPLE:
     * - Similaire au RPM mais avec un facteur différent
     * - Le couple augmente avec l'intensité de l'accélération
     * - Le couple de base est configurable via DboxConfig.engineBaseTorque
     *
     * @param accelX Accélération longitudinale (m/s²)
     * @param accelY Accélération latérale (m/s²)
     * @return Couple [0, engineMaxTorque] N⋅m
     */
    private float convertToTorque(float accelX, float accelY) {
        float accelMagnitude = (float) Math.sqrt(accelX * accelX + accelY * accelY);
        float torque = config.getEngineBaseTorque() + accelMagnitude * TORQUE_FACTOR;
        return clamp(torque, 0.0f, config.getEngineMaxTorque());
    }

    /**
     * Applique un filtre passe-bas (lissage exponentiel)
     *
     * Formule: output = alpha * input + (1-alpha) * previous
     *
     * Où alpha = smoothingFactor (configuré dans DboxConfig)
     * - alpha proche de 1.0 = pas de filtrage (réactif)
     * - alpha proche de 0.0 = fort lissage (lent)
     *
     * @param currentValue Valeur actuelle
     * @param previousValue Valeur précédente
     * @return Valeur filtrée
     */
    private float applyLowPassFilter(float currentValue, float previousValue) {
        float alpha = config.getSmoothingFactor();
        return alpha * currentValue + (1.0f - alpha) * previousValue;
    }

    // ===========================================================================
    // NOUVELLES METHODES DE TRAITEMENT (Version 2)
    // ===========================================================================

    /**
     * Applique une courbe en S pour une réponse non-linéaire fluide.
     *
     * La S-curve produit des transitions douces en début et fin de mouvement,
     * avec une réponse plus agressive au milieu de la plage.
     *
     * Formule: output = sign(x) * |x|^(1 + intensity * (1 - |x|))
     *
     * Comportement:
     * - intensity = 0 : linéaire (pas de modification)
     * - intensity = 1 : S-curve maximale
     *
     * @param value Valeur d'entrée [-1.0, 1.0]
     * @param intensity Intensité de la courbe [0.0, 1.0]
     * @return Valeur transformée [-1.0, 1.0]
     */
    private float applySCurve(float value, float intensity) {
        if (intensity <= 0.0f) {
            return value;
        }

        float absValue = Math.abs(value);
        float sign = Math.signum(value);

        // Formule S-curve : exposant variable selon la position
        // Plus douce aux extrêmes (absValue proche de 0 ou 1)
        // Plus agressive au centre (absValue proche de 0.5)
        float exponent = 1.0f + intensity * (1.0f - absValue);
        float result = (float) Math.pow(absValue, exponent);

        return sign * result;
    }

    /**
     * Applique une zone morte avec transition douce.
     *
     * Les valeurs dans la deadzone sont ramenées à zéro.
     * Les valeurs hors deadzone sont remappées linéairement pour éviter
     * un saut brusque à la sortie de la zone morte.
     *
     * @param value Valeur d'entrée
     * @param deadzone Taille de la zone morte (valeur absolue)
     * @param maxRange Plage maximale pour le remapping
     * @return Valeur avec deadzone appliquée
     */
    private float applyDeadzone(float value, float deadzone, float maxRange) {
        float absValue = Math.abs(value);

        if (absValue <= deadzone) {
            return 0.0f;
        }

        // Remapping linéaire pour éviter le saut à la sortie de la deadzone
        // Exemple: deadzone=0.05, maxRange=2.0, value=0.10
        // -> (0.10 - 0.05) / (2.0 - 0.05) * 2.0 = 0.0513
        float sign = Math.signum(value);
        float remapped = (absValue - deadzone) / (maxRange - deadzone) * maxRange;

        return sign * remapped;
    }

    /**
     * Limite le taux de changement entre deux valeurs consécutives.
     *
     * Empêche les transitions trop brusques en limitant le delta max par frame.
     * Cela produit des mouvements plus fluides et naturels.
     *
     * @param currentValue Nouvelle valeur souhaitée
     * @param previousValue Valeur précédente
     * @param maxDelta Changement maximum autorisé par frame
     * @return Valeur limitée
     */
    private float applyRateLimiting(float currentValue, float previousValue, float maxDelta) {
        float delta = currentValue - previousValue;

        if (Math.abs(delta) > maxDelta) {
            return previousValue + Math.signum(delta) * maxDelta;
        }

        return currentValue;
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
}
