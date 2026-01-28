package fr.ensma.a3.ia.mpudriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ensma.a3.ia.mpudriver.Mpu6050Constants.AccelRange;
import fr.ensma.a3.ia.mpudriver.Mpu6050Constants.DlpfMode;
import fr.ensma.a3.ia.mpudriver.Mpu6050Constants.GyroRange;

/**
 * Configuration du capteur MPU6050.
 *
 * Cette classe représente tous les paramètres configurables du MPU6050 :
 * - Plages de mesure (accéléromètre, gyroscope)
 * - Filtre passe-bas numérique (DLPF)
 * - Fréquence d'échantillonnage
 *
 * @author Projet BE Simulateur
 * @date 2025-12-12
 */
public class Mpu6050Config {

    private static final Logger logger = LoggerFactory.getLogger(Mpu6050Config.class);

    // ============================================================================
    // ATTRIBUTS
    // ============================================================================

    /** Plage de mesure de l'accéléromètre */
    private AccelRange accelRange;

    /** Plage de mesure du gyroscope */
    private GyroRange gyroRange;

    /** Mode du filtre passe-bas numérique */
    private DlpfMode dlpfMode;

    /** Diviseur de fréquence d'échantillonnage (0-255) */
    private int sampleRateDiv;

    // ============================================================================
    // CONSTRUCTEURS
    // ============================================================================

    /**
     * Constructeur avec configuration par défaut.
     *
     * Configuration recommandée pour une utilisation générale :
     * - Accéléromètre: ±2g (haute précision)
     * - Gyroscope: ±250°/s (haute précision)
     * - DLPF: 21Hz (bon compromis bruit/latence)
     * - Fréquence: 125Hz (1000Hz / (1 + 7))
     */
    public Mpu6050Config() {
        this.accelRange = AccelRange.RANGE_2G;
        this.gyroRange = GyroRange.RANGE_250;
        this.dlpfMode = DlpfMode.DLPF_21HZ;
        this.sampleRateDiv = 7; // 125 Hz
    }

    /**
     * Constructeur avec paramètres personnalisés.
     *
     * @param accelRange Plage de l'accéléromètre
     * @param gyroRange Plage du gyroscope
     * @param dlpfMode Mode du filtre passe-bas
     * @param sampleRateDiv Diviseur de fréquence (0-255)
     */
    public Mpu6050Config(AccelRange accelRange, GyroRange gyroRange,
                         DlpfMode dlpfMode, int sampleRateDiv) {
        this.accelRange = accelRange;
        this.gyroRange = gyroRange;
        this.dlpfMode = dlpfMode;
        setSampleRateDiv(sampleRateDiv); // Validation dans le setter
    }

    // ============================================================================
    // MÉTHODES STATIQUES - CONFIGURATIONS PRÉDÉFINIES
    // ============================================================================

    /**
     * Configuration par défaut (équivalent C: MPU6050_CONFIG_DEFAULT).
     *
     * @return Configuration standard recommandée
     */
    public static Mpu6050Config createDefault() {
        return new Mpu6050Config();
    }

    /**
     * Configuration haute précision.
     * Idéal pour des mesures précises de mouvements lents.
     *
     * @return Configuration haute précision
     */
    public static Mpu6050Config createHighPrecision() {
        return new Mpu6050Config(
            AccelRange.RANGE_2G,
            GyroRange.RANGE_250,
            DlpfMode.DLPF_5HZ,
            9  // 100 Hz
        );
    }

    /**
     * Configuration haute vitesse.
     * Idéal pour des mouvements rapides (voiture RC).
     *
     * @return Configuration haute vitesse
     */
    public static Mpu6050Config createHighSpeed() {
        return new Mpu6050Config(
            AccelRange.RANGE_8G,
            GyroRange.RANGE_1000,
            DlpfMode.DLPF_94HZ,
            0  // 1000 Hz
        );
    }

    /**
     * Configuration pour voiture RC (recommandé pour ce projet).
     * Compromis entre précision et réactivité.
     *
     * @return Configuration optimisée pour voiture RC
     */
    public static Mpu6050Config createForRC() {
        return new Mpu6050Config(
            AccelRange.RANGE_4G,
            GyroRange.RANGE_500,
            DlpfMode.DLPF_21HZ,
            4  // 200 Hz
        );
    }

    // ============================================================================
    // GETTERS ET SETTERS
    // ============================================================================

    public AccelRange getAccelRange() {
        return accelRange;
    }

    public void setAccelRange(AccelRange accelRange) {
        this.accelRange = accelRange;
    }

    public GyroRange getGyroRange() {
        return gyroRange;
    }

    public void setGyroRange(GyroRange gyroRange) {
        this.gyroRange = gyroRange;
    }

    public DlpfMode getDlpfMode() {
        return dlpfMode;
    }

    public void setDlpfMode(DlpfMode dlpfMode) {
        this.dlpfMode = dlpfMode;
    }

    public int getSampleRateDiv() {
        return sampleRateDiv;
    }

    /**
     * Définit le diviseur de fréquence d'échantillonnage.
     *
     * @param sampleRateDiv Diviseur (0-255)
     * @throws IllegalArgumentException si la valeur est hors limites
     */
    public void setSampleRateDiv(int sampleRateDiv) {
        if (sampleRateDiv < 0 || sampleRateDiv > 255) {
            throw new IllegalArgumentException(
                "Sample rate divider must be between 0 and 255, got: " + sampleRateDiv
            );
        }
        this.sampleRateDiv = sampleRateDiv;
    }

    /**
     * Calcule la fréquence d'échantillonnage réelle.
     *
     * @return Fréquence en Hz
     */
    public float getSampleRate() {
        return Mpu6050Constants.calculateSampleRate(sampleRateDiv);
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================

    /**
     * Affiche la configuration sous forme lisible.
     *
     * @return Description textuelle de la configuration
     */
    @Override
    public String toString() {
        return String.format(
            "Mpu6050Config{accel=%s, gyro=%s, dlpf=%s, sampleRate=%.1fHz}",
            accelRange,
            gyroRange,
            dlpfMode,
            getSampleRate()
        );
    }

    /**
     * Crée une copie de cette configuration.
     *
     * @return Nouvelle instance avec les mêmes valeurs
     */
    public Mpu6050Config copy() {
        return new Mpu6050Config(accelRange, gyroRange, dlpfMode, sampleRateDiv);
    }

    /**
     * Affiche les détails de configuration (équivalent C: mpu6050_print_config).
     */
    public void printConfig() {
        logger.info("=== Configuration MPU6050 ===");
        logger.info("  Accéléromètre: {}", accelRange);
        logger.info("  Gyroscope: {}", gyroRange);
        logger.info("  Filtre passe-bas: {}", dlpfMode);
        logger.info(String.format("  Fréquence échantillonnage: %.1f Hz", getSampleRate()));
        logger.info("==============================");
    }
}
