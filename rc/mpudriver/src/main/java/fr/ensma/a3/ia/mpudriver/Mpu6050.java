package fr.ensma.a3.ia.mpudriver;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static fr.ensma.a3.ia.mpudriver.Mpu6050Constants.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * API Java pour le capteur MPU6050 (Accéléromètre + Gyroscope + Température).
 *
 * Cette classe utilise Pi4J v2 pour communiquer avec le MPU6050 via I2C.
 * Elle fournit une interface simple et performante pour :
 * - Initialiser et configurer le capteur
 * - Lire les données (accél, gyro, température)
 * - Gérer les erreurs et diagnostics
 *
 * UTILISATION TYPIQUE:
 * <pre>
 * Context pi4j = Pi4J.newAutoContext();
 * Mpu6050 mpu = new Mpu6050(pi4j, 1, MPU6050_ADDR_DEFAULT);
 *
 * Mpu6050Data data = new Mpu6050Data();
 * while (running) {
 *     mpu.readAll(data);
 *     data.printData();
 * }
 *
 * mpu.close();
 * pi4j.shutdown();
 * </pre>
 *
 * @author Projet BE Simulateur
 * @date 2025-12-12
 */
public class Mpu6050 implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Mpu6050.class);

    // ============================================================================
    // ATTRIBUTS
    // ============================================================================

    /** Contexte Pi4J */
    private final Context pi4j;

    /** Device I2C Pi4J */
    private final I2C i2cDevice;

    /** Adresse I2C du capteur */
    private final int address;

    /** Configuration actuelle */
    private Mpu6050Config config;

    /** Facteur de conversion accéléromètre (LSB/g) */
    private float accelScale;

    /** Facteur de conversion gyroscope (LSB/(°/s)) */
    private float gyroScale;

    /** Flag pour savoir si le device est initialisé */
    private boolean initialized;

    /** Compteur pour générer des IDs uniques */
    private static int instanceCounter = 0;

    // ============================================================================
    // CONSTRUCTEUR
    // ============================================================================

    /**
     * Crée une instance du MPU6050 et initialise la connexion I2C.
     *
     * @param pi4j Contexte Pi4J
     * @param bus Numéro du bus I2C (généralement 1 sur Raspberry Pi)
     * @param address Adresse I2C du capteur (MPU6050_ADDR_DEFAULT ou MPU6050_ADDR_ALT)
     * @throws IOException Si erreur d'initialisation I2C
     * @throws IllegalStateException Si le capteur n'est pas détecté
     */
    public Mpu6050(Context pi4j, int bus, int address) throws IOException {
        this.pi4j = pi4j;
        this.address = address;
        this.initialized = false;

        // Configuration I2C avec Pi4J v2 - ID unique avec compteur
        String uniqueId = "MPU6050-" + Integer.toHexString(address) + "-" + (instanceCounter++);
        I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
                .id(uniqueId)
                .bus(bus)
                .device(address)
                .build();

        // Créer le device I2C avec le provider linuxfs-i2c
        I2CProvider i2cProvider = pi4j.provider("linuxfs-i2c");
        this.i2cDevice = i2cProvider.create(i2cConfig);

        // Vérifier la connexion
        if (!testConnection()) {
            throw new IllegalStateException(
                String.format("MPU6050 non détecté à l'adresse 0x%02X", address)
            );
        }

        // Configuration par défaut
        configure(Mpu6050Config.createDefault());

        this.initialized = true;
        logger.info("[MPU6050] Initialisé avec succès (0x{:02X}, bus {})", address, bus);
    }

    /**
     * Constructeur simplifié avec adresse par défaut.
     *
     * @param pi4j Contexte Pi4J
     * @param bus Numéro du bus I2C
     * @throws IOException Si erreur d'initialisation
     */
    public Mpu6050(Context pi4j, int bus) throws IOException {
        this(pi4j, bus, MPU6050_ADDR_DEFAULT);
    }

    // ============================================================================
    // CONFIGURATION
    // ============================================================================

    /**
     * Configure le MPU6050 avec les paramètres spécifiés.
     *
     * @param config Configuration à appliquer
     * @throws IOException Si erreur I2C
     */
    public void configure(Mpu6050Config config) throws IOException {
        if (config == null) {
            throw new IllegalArgumentException("Configuration ne peut pas être null");
        }

        // 1. Réveil du capteur (sortir du mode sleep)
        //    Valeur 0x01 = clock source PLL with X axis gyroscope
        writeRegister(REG_PWR_MGMT_1, 0x01);

        // Attendre la stabilisation du capteur
        try {
            Thread.sleep(WAKEUP_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during sensor wakeup", e);
        }

        // 2. Configuration accéléromètre
        writeRegister(REG_ACCEL_CONFIG, config.getAccelRange().getRegisterValue());

        // 3. Configuration gyroscope
        writeRegister(REG_GYRO_CONFIG, config.getGyroRange().getRegisterValue());

        // 4. Configuration du filtre passe-bas (DLPF)
        writeRegister(REG_CONFIG, config.getDlpfMode().getRegisterValue());

        // 5. Configuration de la fréquence d'échantillonnage
        writeRegister(REG_SMPRT_DIV, config.getSampleRateDiv());

        // Sauvegarder la config et calculer les facteurs d'échelle
        this.config = config.copy();
        this.accelScale = config.getAccelRange().getScale();
        this.gyroScale = config.getGyroRange().getScale();

        logger.info("[MPU6050] Configuration appliquée : {}", config);
    }

    // ============================================================================
    // LECTURE DE DONNÉES
    // ============================================================================

    /**
     * Lit toutes les données du capteur (accél + gyro + temp).
     *
     * Effectue une lecture burst de 14 octets pour maximiser la performance.
     * Cette méthode est la plus efficace et recommandée pour une lecture à 100Hz.
     *
     * @param data Structure de données à remplir
     * @throws IOException Si erreur de lecture I2C
     */
    public void readAll(Mpu6050Data data) throws IOException {
        checkInitialized();

        // Lecture burst de 14 octets à partir de REG_ACCEL_XOUT_H
        // Format: [AccelX_H, AccelX_L, AccelY_H, AccelY_L, AccelZ_H, AccelZ_L,
        //          Temp_H, Temp_L, GyroX_H, GyroX_L, GyroY_H, GyroY_L, GyroZ_H, GyroZ_L]
        byte[] buffer = new byte[14];
        int bytesRead = i2cDevice.readRegister(REG_ACCEL_XOUT_H, buffer, 0, 14);

        if (bytesRead != 14) {
            throw new IOException("Lecture I2C incomplète: " + bytesRead + " bytes au lieu de 14");
        }

        // Conversion des données brutes
        // Bytes 0-5: Accéléromètre
        int accelXRaw = bytesToInt16(buffer[0], buffer[1]);
        int accelYRaw = bytesToInt16(buffer[2], buffer[3]);
        int accelZRaw = bytesToInt16(buffer[4], buffer[5]);

        data.setAccelX(accelXRaw / accelScale);
        data.setAccelY(accelYRaw / accelScale);
        data.setAccelZ(accelZRaw / accelScale);

        // Bytes 6-7: Température
        int tempRaw = bytesToInt16(buffer[6], buffer[7]);
        data.setTemperature(rawToTemperature(tempRaw));

        // Bytes 8-13: Gyroscope
        int gyroXRaw = bytesToInt16(buffer[8], buffer[9]);
        int gyroYRaw = bytesToInt16(buffer[10], buffer[11]);
        int gyroZRaw = bytesToInt16(buffer[12], buffer[13]);

        data.setGyroX(gyroXRaw / gyroScale);
        data.setGyroY(gyroYRaw / gyroScale);
        data.setGyroZ(gyroZRaw / gyroScale);

        // Timestamp
        data.setTimestampMs(System.currentTimeMillis());
    }

    /**
     * Lit uniquement l'accéléromètre.
     *
     * @return Tableau [accelX, accelY, accelZ] en g
     * @throws IOException Si erreur de lecture I2C
     */
    public float[] readAccel() throws IOException {
        checkInitialized();

        byte[] buffer = new byte[6];
        i2cDevice.readRegister(REG_ACCEL_XOUT_H, buffer, 0, 6);

        return new float[] {
            bytesToInt16(buffer[0], buffer[1]) / accelScale,
            bytesToInt16(buffer[2], buffer[3]) / accelScale,
            bytesToInt16(buffer[4], buffer[5]) / accelScale
        };
    }

    /**
     * Lit uniquement le gyroscope.
     *
     * @return Tableau [gyroX, gyroY, gyroZ] en °/s
     * @throws IOException Si erreur de lecture I2C
     */
    public float[] readGyro() throws IOException {
        checkInitialized();

        byte[] buffer = new byte[6];
        i2cDevice.readRegister(REG_GYRO_XOUT_H, buffer, 0, 6);

        return new float[] {
            bytesToInt16(buffer[0], buffer[1]) / gyroScale,
            bytesToInt16(buffer[2], buffer[3]) / gyroScale,
            bytesToInt16(buffer[4], buffer[5]) / gyroScale
        };
    }

    /**
     * Lit uniquement la température.
     *
     * @return Température en °C
     * @throws IOException Si erreur de lecture I2C
     */
    public float readTemperature() throws IOException {
        checkInitialized();

        byte[] buffer = new byte[2];
        i2cDevice.readRegister(REG_TEMP_OUT_H, buffer, 0, 2);

        int tempRaw = bytesToInt16(buffer[0], buffer[1]);
        return rawToTemperature(tempRaw);
    }

    // ============================================================================
    // DIAGNOSTIC
    // ============================================================================

    /**
     * Teste la connexion avec le capteur.
     *
     * Vérifie que le capteur répond correctement en lisant le registre WHO_AM_I.
     *
     * @return true si le capteur est détecté et répond correctement
     */
    public boolean testConnection() {
        try {
            int whoAmI = readRegister(REG_WHO_AM_I);
            return (whoAmI == WHO_AM_I_VALUE);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Affiche les informations de configuration.
     */
    public void printConfig() {
        logger.info("=== Configuration MPU6050 ===");
        logger.info(String.format("  Adresse I2C: 0x%02X", address));
        if (config != null) {
            config.printConfig();
        }
    }

    // ============================================================================
    // OPÉRATIONS I2C BAS NIVEAU
    // ============================================================================

    /**
     * Écrit une valeur dans un registre.
     *
     * @param register Adresse du registre
     * @param value Valeur à écrire (0-255)
     * @throws IOException Si erreur I2C
     */
    private void writeRegister(int register, int value) throws IOException {
        i2cDevice.writeRegister(register, (byte) value);
    }

    /**
     * Lit un octet depuis un registre.
     *
     * @param register Adresse du registre
     * @return Valeur lue (0-255)
     * @throws IOException Si erreur I2C
     */
    private int readRegister(int register) throws IOException {
        return i2cDevice.readRegister(register) & 0xFF;
    }

    // ============================================================================
    // UTILITAIRES
    // ============================================================================

    /**
     * Convertit 2 octets (MSB, LSB) en int16 signé.
     *
     * @param msb Octet de poids fort
     * @param lsb Octet de poids faible
     * @return Valeur signée sur 16 bits
     */
    private int bytesToInt16(byte msb, byte lsb) {
        return (short) (((msb & 0xFF) << 8) | (lsb & 0xFF));
    }

    /**
     * Vérifie que le device est initialisé.
     *
     * @throws IllegalStateException Si non initialisé
     */
    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("MPU6050 non initialisé");
        }
    }

    // ============================================================================
    // GETTERS
    // ============================================================================

    public Mpu6050Config getConfig() {
        return config != null ? config.copy() : null;
    }

    public int getAddress() {
        return address;
    }

    public boolean isInitialized() {
        return initialized;
    }

    // ============================================================================
    // FERMETURE
    // ============================================================================

    /**
     * Ferme la connexion I2C.
     *
     * IMPORTANT: Après l'appel à close(), n'oubliez pas de faire pi4j.shutdown()
     * dans votre code principal.
     */
    @Override
    public void close() {
        if (i2cDevice != null) {
            try {
                i2cDevice.close();
                initialized = false;
                logger.info("[MPU6050] Connexion fermée");
            } catch (Exception e) {
                logger.error("[MPU6050] Erreur lors de la fermeture: ", e);
            }
        }
    }
}
