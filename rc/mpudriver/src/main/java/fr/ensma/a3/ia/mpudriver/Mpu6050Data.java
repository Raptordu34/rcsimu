package fr.ensma.a3.ia.mpudriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Données lues du capteur MPU6050.
 *
 * Cette classe contient toutes les mesures d'un cycle de lecture :
 * - Accélération 3 axes (g)
 * - Vitesse angulaire 3 axes (°/s)
 * - Température (°C)
 * - Timestamp (secondes Unix)
 *
 * Équivalent C: struct mpu6050_data_t
 *
 * @author Projet BE Simulateur
 * @date 2025-12-12
 */
public class Mpu6050Data {

    private static final Logger logger = LoggerFactory.getLogger(Mpu6050Data.class);

    // ============================================================================
    // ATTRIBUTS
    // ============================================================================

    /** Accélération axe X (en g) */
    private float accelX;

    /** Accélération axe Y (en g) */
    private float accelY;

    /** Accélération axe Z (en g) */
    private float accelZ;

    /** Vitesse angulaire axe X (en °/s) */
    private float gyroX;

    /** Vitesse angulaire axe Y (en °/s) */
    private float gyroY;

    /** Vitesse angulaire axe Z (en °/s) */
    private float gyroZ;

    /** Température (en °C) */
    private float temperature;

    /** Timestamp de la lecture (millisecondes depuis epoch Unix) */
    private long timestampMs;

    // ============================================================================
    // CONSTRUCTEUR
    // ============================================================================

    /**
     * Constructeur par défaut (toutes valeurs à zéro).
     */
    public Mpu6050Data() {
        this.timestampMs = System.currentTimeMillis();
    }

    /**
     * Constructeur avec toutes les valeurs.
     *
     * @param accelX Accélération X (g)
     * @param accelY Accélération Y (g)
     * @param accelZ Accélération Z (g)
     * @param gyroX Vitesse angulaire X (°/s)
     * @param gyroY Vitesse angulaire Y (°/s)
     * @param gyroZ Vitesse angulaire Z (°/s)
     * @param temperature Température (°C)
     * @param timestampMs Timestamp (ms)
     */
    public Mpu6050Data(float accelX, float accelY, float accelZ,
                       float gyroX, float gyroY, float gyroZ,
                       float temperature, long timestampMs) {
        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;
        this.gyroX = gyroX;
        this.gyroY = gyroY;
        this.gyroZ = gyroZ;
        this.temperature = temperature;
        this.timestampMs = timestampMs;
    }

    // ============================================================================
    // GETTERS ET SETTERS
    // ============================================================================

    public float getAccelX() {
        return accelX;
    }

    public void setAccelX(float accelX) {
        this.accelX = accelX;
    }

    public float getAccelY() {
        return accelY;
    }

    public void setAccelY(float accelY) {
        this.accelY = accelY;
    }

    public float getAccelZ() {
        return accelZ;
    }

    public void setAccelZ(float accelZ) {
        this.accelZ = accelZ;
    }

    public float getGyroX() {
        return gyroX;
    }

    public void setGyroX(float gyroX) {
        this.gyroX = gyroX;
    }

    public float getGyroY() {
        return gyroY;
    }

    public void setGyroY(float gyroY) {
        this.gyroY = gyroY;
    }

    public float getGyroZ() {
        return gyroZ;
    }

    public void setGyroZ(float gyroZ) {
        this.gyroZ = gyroZ;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    /**
     * Retourne le timestamp en secondes (avec précision décimale).
     *
     * @return Timestamp en secondes
     */
    public double getTimestampSeconds() {
        return timestampMs / 1000.0;
    }

    // ============================================================================
    // MÉTHODES CALCULÉES
    // ============================================================================

    /**
     * Calcule la norme de l'accélération totale.
     *
     * @return Magnitude de l'accélération (g)
     */
    public float getAccelMagnitude() {
        return (float) Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
    }

    /**
     * Calcule la norme de la vitesse angulaire totale.
     *
     * @return Magnitude de la vitesse angulaire (°/s)
     */
    public float getGyroMagnitude() {
        return (float) Math.sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ);
    }

    /**
     * Vérifie si les données sont dans des limites raisonnables.
     * Utile pour détecter des erreurs de lecture I2C.
     *
     * @return true si les données semblent valides
     */
    public boolean isValid() {
        // Vérifier que l'accélération totale est proche de 1g (gravité)
        // Tolérance : 0.5g à 2.5g (garde large pour mouvements dynamiques)
        float accelMag = getAccelMagnitude();
        if (accelMag < 0.5f || accelMag > 2.5f) {
            return false;
        }

        // Température raisonnable (-40°C à +85°C selon datasheet)
        if (temperature < -40.0f || temperature > 85.0f) {
            return false;
        }

        // Vérifier qu'il n'y a pas de NaN ou Infinity
        return !Float.isNaN(accelX) && !Float.isNaN(accelY) && !Float.isNaN(accelZ) &&
               !Float.isNaN(gyroX) && !Float.isNaN(gyroY) && !Float.isNaN(gyroZ) &&
               !Float.isNaN(temperature) &&
               !Float.isInfinite(accelX) && !Float.isInfinite(accelY) && !Float.isInfinite(accelZ);
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================

    /**
     * Affiche les données formatées (équivalent C: mpu6050_print_data).
     */
    public void printData() {
        if (logger.isInfoEnabled()) {
            logger.info(String.format(
                "Accel: X=%+6.3fg Y=%+6.3fg Z=%+6.3fg | Gyro: X=%+7.2f°/s Y=%+7.2f°/s Z=%+7.2f°/s | Temp: %5.1f°C | Time: %.3fs",
                accelX, accelY, accelZ,
                gyroX, gyroY, gyroZ,
                temperature, getTimestampSeconds()));
        }
    }

    /**
     * Représentation textuelle courte.
     *
     * @return String avec valeurs principales
     */
    @Override
    public String toString() {
        return String.format(
            "Mpu6050Data{accel=(%.2f,%.2f,%.2f)g, gyro=(%.1f,%.1f,%.1f)°/s, temp=%.1f°C}",
            accelX, accelY, accelZ,
            gyroX, gyroY, gyroZ,
            temperature
        );
    }

    /**
     * Crée une copie de cette donnée.
     *
     * @return Nouvelle instance avec les mêmes valeurs
     */
    public Mpu6050Data copy() {
        return new Mpu6050Data(
            accelX, accelY, accelZ,
            gyroX, gyroY, gyroZ,
            temperature, timestampMs
        );
    }

    /**
     * Réinitialise toutes les valeurs à zéro et met à jour le timestamp.
     */
    public void reset() {
        this.accelX = 0;
        this.accelY = 0;
        this.accelZ = 0;
        this.gyroX = 0;
        this.gyroY = 0;
        this.gyroZ = 0;
        this.temperature = 0;
        this.timestampMs = System.currentTimeMillis();
    }
}
