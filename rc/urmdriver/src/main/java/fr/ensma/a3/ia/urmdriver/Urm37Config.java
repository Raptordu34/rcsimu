package fr.ensma.a3.ia.urmdriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration du capteur URM37 V3.2.
 *
 * Cette classe représente les paramètres configurables :
 * - Mode de fonctionnement (Série par défaut)
 * - Vitesse de transmission (Baudrate)
 * - Seuil d'alerte de distance (utilisé pour le pin COMP/Trig)
 * - Fréquence de rafraîchissement logicielle
 *
 * @author Projet BE Simulateur
 * @date 2025-12-12
 */
public class Urm37Config {

    private static final Logger logger = LoggerFactory.getLogger(Urm37Config.class);

    // ============================================================================
    // ATTRIBUTS
    // ============================================================================

    /** Vitesse de communication (Généralement 9600) */
    private int baudRate;

    /** Seuil de distance pour la sortie automatique (cm) */
    private int distanceThreshold;

    /** Intervalle entre deux mesures (ms) - Équivalent du sampleRateDiv */
    private int measurementIntervalMs;

    // ============================================================================
    // CONSTRUCTEURS
    // ============================================================================

    /**
     * Constructeur avec configuration par défaut.
     * * - Baudrate: 9600
     * - Seuil: 50 cm
     * - Intervalle: 50 ms (soit ~20Hz)
     */
    public Urm37Config() {
        this.baudRate = 9600;
        this.distanceThreshold = 50;
        this.measurementIntervalMs = 50;
    }

    /**
     * Constructeur avec paramètres personnalisés.
     *
     * @param baudRate Vitesse série
     * @param threshold Seuil d'alerte en cm
     * @param interval Intervalle entre mesures en ms
     */
    public Urm37Config(int baudRate, int threshold, int interval) {
        this.baudRate = baudRate;
        this.distanceThreshold = threshold;
        setMeasurementIntervalMs(interval);
    }

    // ============================================================================
    // MÉTHODES STATIQUES - CONFIGURATIONS PRÉDÉFINIES
    // ============================================================================

    /**
     * Configuration standard recommandée.
     */
    public static Urm37Config createDefault() {
        return new Urm37Config();
    }

    /**
     * Configuration pour une détection très réactive (proche).
     * Idéal pour l'évitement d'urgence.
     */
    public static Urm37Config createHighSpeed() {
        return new Urm37Config(
            9600,
            20,  // Seuil proche
            33   // ~30 Hz (maximum physique du capteur)
        );
    }

    /**
     * Configuration pour voiture RC.
     * Bon compromis pour éviter les interférences d'échos.
     */
    public static Urm37Config createForRC() {
        return new Urm37Config(
            9600,
            100, // Seuil large
            60   // ~16 Hz (très stable)
        );
    }

    // ============================================================================
    // GETTERS ET SETTERS
    // ============================================================================

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public int getDistanceThreshold() {
        return distanceThreshold;
    }

    public void setDistanceThreshold(int distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
    }

    public int getMeasurementIntervalMs() {
        return measurementIntervalMs;
    }

    /**
     * Définit l'intervalle entre deux mesures.
     * @param interval Ms (minimum 33ms pour laisser l'écho se dissiper)
     */
    public void setMeasurementIntervalMs(int interval) {
        if (interval < 33) {
            throw new IllegalArgumentException(
                "L'intervalle doit être > 33ms pour éviter les erreurs d'écho."
            );
        }
        this.measurementIntervalMs = interval;
    }

    /**
     * Calcule la fréquence de lecture théorique.
     */
    public float getSampleRateHz() {
        return 1000.0f / measurementIntervalMs;
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================

    @Override
    public String toString() {
        return String.format(
            "Urm37Config{baud=%d, threshold=%dcm, rate=%.1fHz}",
            baudRate,
            distanceThreshold,
            getSampleRateHz()
        );
    }

    public Urm37Config copy() {
        return new Urm37Config(baudRate, distanceThreshold, measurementIntervalMs);
    }

    public void printConfig() {
        logger.info("=== Configuration URM37 V3.2 ===");
        logger.info("  Vitesse Série: {} baud", baudRate);
        logger.info("  Seuil Alerte: {} cm", distanceThreshold);
        logger.info(String.format("  Fréquence Rafraîchissement: %.1f Hz", getSampleRateHz()));
        logger.info("================================");
    }
}
