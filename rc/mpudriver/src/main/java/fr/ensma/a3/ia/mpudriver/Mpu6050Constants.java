package fr.ensma.a3.ia.mpudriver;

/**
 * Constantes et définitions pour le capteur MPU6050.
 *
 * Ce fichier regroupe :
 * - Adresses I2C
 * - Registres du capteur
 * - Enums pour les plages de mesure
 * - Facteurs de conversion
 *
 * @author Projet BE Simulateur
 * @date 2025-12-12
 */
public class Mpu6050Constants {

    // ============================================================================
    // ADRESSES I2C
    // ============================================================================

    /** Adresse I2C par défaut (pin AD0 = LOW) */
    public static final int MPU6050_ADDR_DEFAULT = 0x68;

    /** Adresse I2C alternative (pin AD0 = HIGH) */
    public static final int MPU6050_ADDR_ALT = 0x69;

    // ============================================================================
    // REGISTRES MPU6050
    // ============================================================================

    /** Registre d'identification du circuit */
    public static final int REG_WHO_AM_I = 0x75;

    /** Registre de gestion de l'alimentation */
    public static final int REG_PWR_MGMT_1 = 0x6B;

    /** Registre de configuration générale */
    public static final int REG_CONFIG = 0x1A;

    /** Registre de configuration du gyroscope */
    public static final int REG_GYRO_CONFIG = 0x1B;

    /** Registre de configuration de l'accéléromètre */
    public static final int REG_ACCEL_CONFIG = 0x1C;

    /** Registre du diviseur de fréquence d'échantillonnage */
    public static final int REG_SMPRT_DIV = 0x19;

    /** Registre de début des données (burst read 14 bytes) */
    public static final int REG_ACCEL_XOUT_H = 0x3B;

    /** Registre de début des données du gyroscope */
    public static final int REG_GYRO_XOUT_H = 0x43;

    /** Registre de début des données de température */
    public static final int REG_TEMP_OUT_H = 0x41;

    /** Valeur attendue du registre WHO_AM_I */
    public static final int WHO_AM_I_VALUE = 0x68;

    // ============================================================================
    // ENUMS - PLAGES DE MESURE
    // ============================================================================

    /**
     * Plages de mesure de l'accéléromètre.
     * Chaque plage a une sensibilité différente (LSB/g).
     */
    public enum AccelRange {
        /** ±2g (16384 LSB/g) - Haute précision */
        RANGE_2G(0x00, 16384.0f),

        /** ±4g (8192 LSB/g) */
        RANGE_4G(0x08, 8192.0f),

        /** ±8g (4096 LSB/g) */
        RANGE_8G(0x10, 4096.0f),

        /** ±16g (2048 LSB/g) - Faible précision, grande plage */
        RANGE_16G(0x18, 2048.0f);

        private final int registerValue;
        private final float scale;

        AccelRange(int registerValue, float scale) {
            this.registerValue = registerValue;
            this.scale = scale;
        }

        public int getRegisterValue() {
            return registerValue;
        }

        public float getScale() {
            return scale;
        }

        /**
         * Trouve l'enum correspondant à une valeur de registre.
         */
        public static AccelRange fromRegisterValue(int value) {
            for (AccelRange range : values()) {
                if (range.registerValue == value) {
                    return range;
                }
            }
            return RANGE_2G; // Par défaut
        }
    }

    /**
     * Plages de mesure du gyroscope.
     * Chaque plage a une sensibilité différente (LSB/(°/s)).
     */
    public enum GyroRange {
        /** ±250°/s (131 LSB/(°/s)) - Haute précision */
        RANGE_250(0x00, 131.0f),

        /** ±500°/s (65.5 LSB/(°/s)) */
        RANGE_500(0x08, 65.5f),

        /** ±1000°/s (32.8 LSB/(°/s)) */
        RANGE_1000(0x10, 32.8f),

        /** ±2000°/s (16.4 LSB/(°/s)) - Faible précision, grande plage */
        RANGE_2000(0x18, 16.4f);

        private final int registerValue;
        private final float scale;

        GyroRange(int registerValue, float scale) {
            this.registerValue = registerValue;
            this.scale = scale;
        }

        public int getRegisterValue() {
            return registerValue;
        }

        public float getScale() {
            return scale;
        }

        /**
         * Trouve l'enum correspondant à une valeur de registre.
         */
        public static GyroRange fromRegisterValue(int value) {
            for (GyroRange range : values()) {
                if (range.registerValue == value) {
                    return range;
                }
            }
            return RANGE_250; // Par défaut
        }
    }

    /**
     * Filtre passe-bas numérique (DLPF - Digital Low Pass Filter).
     * Compromis entre bande passante et délai.
     */
    public enum DlpfMode {
        /** Bande passante 260Hz, délai 0ms - Pas de filtrage */
        DLPF_260HZ(0),

        /** Bande passante 184Hz, délai 2ms */
        DLPF_184HZ(1),

        /** Bande passante 94Hz, délai 3ms */
        DLPF_94HZ(2),

        /** Bande passante 44Hz, délai 4.9ms */
        DLPF_44HZ(3),

        /** Bande passante 21Hz, délai 8.5ms - Bon compromis */
        DLPF_21HZ(4),

        /** Bande passante 10Hz, délai 13.8ms */
        DLPF_10HZ(5),

        /** Bande passante 5Hz, délai 19ms - Filtrage maximum */
        DLPF_5HZ(6);

        private final int registerValue;

        DlpfMode(int registerValue) {
            this.registerValue = registerValue;
        }

        public int getRegisterValue() {
            return registerValue;
        }

        /**
         * Trouve l'enum correspondant à une valeur de registre.
         */
        public static DlpfMode fromRegisterValue(int value) {
            for (DlpfMode mode : values()) {
                if (mode.registerValue == value) {
                    return mode;
                }
            }
            return DLPF_21HZ; // Par défaut
        }
    }

    // ============================================================================
    // CONSTANTES DE CONFIGURATION
    // ============================================================================

    /** Temps d'attente après réveil du capteur (millisecondes) */
    public static final int WAKEUP_DELAY_MS = 100;

    /** Nombre maximum de tentatives pour les opérations I2C */
    public static final int MAX_RETRY = 3;

    /** Délai entre les tentatives (microsecondes) */
    public static final int RETRY_DELAY_US = 1000;

    // ============================================================================
    // FORMULES DE CONVERSION
    // ============================================================================

    /**
     * Formule de conversion température.
     * Température (°C) = (valeur_brute / 340.0) + 36.53
     */
    public static final float TEMP_SENSITIVITY = 340.0f;
    public static final float TEMP_OFFSET = 36.53f;

    /**
     * Convertit la valeur brute de température en °C.
     */
    public static float rawToTemperature(int rawValue) {
        return (rawValue / TEMP_SENSITIVITY) + TEMP_OFFSET;
    }

    /**
     * Calcule la fréquence d'échantillonnage à partir du diviseur.
     * Fréquence = 1000 Hz / (1 + diviseur)
     */
    public static float calculateSampleRate(int sampleRateDiv) {
        return 1000.0f / (1 + sampleRateDiv);
    }

    // Empêcher l'instanciation de cette classe de constantes
    private Mpu6050Constants() {
        throw new AssertionError("Classe de constantes non instanciable");
    }
}
