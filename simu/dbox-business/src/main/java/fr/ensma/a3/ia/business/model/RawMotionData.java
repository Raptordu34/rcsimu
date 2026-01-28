package fr.ensma.a3.ia.business.model;

/**
 * Données brutes reçues du service (WebSocket)
 * Ces données proviennent de la voiture RC via le capteur MPU6050
 *
 * Ces données sont les mêmes que celles envoyées par le Raspberry Pi
 * et doivent être traitées avant d'être envoyées à la DBOX.
 */
public class RawMotionData {

    // ===========================================================================
    // ATTRIBUTS
    // ===========================================================================

    /**
     * Timestamp de la mesure (millisecondes depuis epoch)
     * Permet de synchroniser les données et détecter les retards
     */
    private long timestamp;

    /**
     * Accélération selon l'axe X en m/s²
     * X = axe longitudinal (avant-arrière du véhicule)
     */
    private float accelX;

    /**
     * Accélération selon l'axe Y en m/s²
     * Y = axe latéral (gauche-droite du véhicule)
     */
    private float accelY;

    /**
     * Accélération selon l'axe Z en m/s²
     * Z = axe vertical (haut-bas du véhicule)
     */
    private float accelZ;

    /**
     * Vitesse angulaire autour de l'axe X en degrés/seconde
     * Rotation X = roulis (roll)
     */
    private float gyroX;

    /**
     * Vitesse angulaire autour de l'axe Y en degrés/seconde
     * Rotation Y = tangage (pitch)
     */
    private float gyroY;

    /**
     * Vitesse angulaire autour de l'axe Z en degrés/seconde
     * Rotation Z = lacet (yaw)
     */
    private float gyroZ;

    /**
     * Température du capteur en °C (optionnel)
     * Peut être utilisé pour la calibration thermique
     */
    private float temperature;

    // ===========================================================================
    // CONSTRUCTEURS
    // ===========================================================================

    /**
     * Constructeur par défaut
     */
    public RawMotionData() {
        this.timestamp = System.currentTimeMillis();
        this.temperature = 25.0f; // Température par défaut
    }

    /**
     * Constructeur complet
     *
     * @param timestamp Timestamp de la mesure
     * @param accelX Accélération X (m/s²)
     * @param accelY Accélération Y (m/s²)
     * @param accelZ Accélération Z (m/s²)
     * @param gyroX Vitesse angulaire X (deg/s)
     * @param gyroY Vitesse angulaire Y (deg/s)
     * @param gyroZ Vitesse angulaire Z (deg/s)
     * @param temperature Température (°C)
     */
    public RawMotionData(long timestamp, float accelX, float accelY, float accelZ,
                        float gyroX, float gyroY, float gyroZ, float temperature) {
        this.timestamp = timestamp;
        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;
        this.gyroX = gyroX;
        this.gyroY = gyroY;
        this.gyroZ = gyroZ;
        this.temperature = temperature;
    }

    // ===========================================================================
    // GETTERS ET SETTERS
    // ===========================================================================

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

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

    // ===========================================================================
    // METHODES UTILITAIRES
    // ===========================================================================

    /**
     * Vérifie si les données sont valides
     *
     * @return true si les données semblent cohérentes
     */
    public boolean isValid() {
        // Vérification des plages raisonnables pour un véhicule RC
        // Accélération : max ±40 m/s² (environ 4G - suffisant pour une voiture RC)
        if (Math.abs(accelX) > 40 || Math.abs(accelY) > 40 || Math.abs(accelZ) > 40) {
            return false;
        }

        // Gyroscope : max ±2000 deg/s (plage typique MPU6050)
        if (Math.abs(gyroX) > 2000 || Math.abs(gyroY) > 2000 || Math.abs(gyroZ) > 2000) {
            return false;
        }

        // Température : plage raisonnable -40°C à +85°C (plage MPU6050)
        if (temperature < -40 || temperature > 85) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format(
            "RawMotionData{timestamp=%d, accel=(%.2f, %.2f, %.2f), gyro=(%.2f, %.2f, %.2f), temp=%.1f°C}",
            timestamp, accelX, accelY, accelZ, gyroX, gyroY, gyroZ, temperature
        );
    }
}
