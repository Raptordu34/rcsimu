package fr.ensma.a3.ia.mpubusiness;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import fr.ensma.a3.ia.mpubusiness.ProcessedMpuData;
import fr.ensma.a3.ia.mpudriver.Mpu6050;
import fr.ensma.a3.ia.mpudriver.Mpu6050Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Implémentation du service métier pour le capteur MPU6050.
 *
 * Cette classe encapsule le driver MPU6050 et fournit une API simple.
 * Implémentation en Singleton thread-safe pour garantir une instance unique.
 */
public class MpuBusiness implements IMpuBusiness, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MpuBusiness.class);

    private static volatile MpuBusiness instance;
    private static final Object lock = new Object();

    private final Context pi4j;
    private final Mpu6050 mpuDriver;
    private final int i2cBus;

    /**
     * Constructeur privé avec bus I2C par défaut (bus 1).
     *
     * @throws IOException Si le capteur ne peut pas être initialisé
     */
    private MpuBusiness() throws IOException {
        this(1);
    }

    /**
     * Constructeur privé avec bus I2C personnalisé.
     *
     * @param i2cBus Numéro du bus I2C (généralement 1 sur Raspberry Pi)
     * @throws IOException Si le capteur ne peut pas être initialisé
     */
    private MpuBusiness(int i2cBus) throws IOException {
        logger.info("Initialisation du service MPU6050 sur bus I2C {}", i2cBus);
        this.i2cBus = i2cBus;
        this.pi4j = Pi4J.newAutoContext();
        this.mpuDriver = new Mpu6050(pi4j, i2cBus);
    }

    /**
     * Récupère l'instance unique du service MPU6050 (bus I2C par défaut: 1).
     * Utilise le pattern Singleton avec double-checked locking.
     *
     * @return L'instance unique de MpuBusiness
     * @throws IOException Si l'initialisation échoue
     */
    public static MpuBusiness getInstance() throws IOException {
        return getInstance(1);
    }

    /**
     * Récupère l'instance unique du service MPU6050 avec un bus I2C spécifique.
     * Utilise le pattern Singleton avec double-checked locking.
     *
     * @param i2cBus Numéro du bus I2C
     * @return L'instance unique de MpuBusiness
     * @throws IOException Si l'initialisation échoue
     * @throws IllegalStateException Si une instance existe déjà avec un bus différent
     */
    public static MpuBusiness getInstance(int i2cBus) throws IOException {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new MpuBusiness(i2cBus);
                } else if (instance.i2cBus != i2cBus) {
                    throw new IllegalStateException(
                        "Instance déjà initialisée avec le bus I2C " + instance.i2cBus +
                        ". Impossible de créer une instance avec le bus " + i2cBus
                    );
                }
            }
        } else if (instance.i2cBus != i2cBus) {
            throw new IllegalStateException(
                "Instance déjà initialisée avec le bus I2C " + instance.i2cBus +
                ". Impossible de créer une instance avec le bus " + i2cBus
            );
        }
        return instance;
    }

    /**
     * Réinitialise l'instance singleton (utile pour les tests).
     * Ferme l'instance existante avant de la réinitialiser.
     */
    public static void resetInstance() {
        synchronized (lock) {
            if (instance != null) {
                instance.close();
                instance = null;
            }
        }
    }

    @Override
    public ProcessedMpuData getData() {
        try {
            Mpu6050Data rawData = new Mpu6050Data();
            mpuDriver.readAll(rawData);

            return new ProcessedMpuData(
                rawData.getAccelX(),
                rawData.getAccelY(),
                rawData.getAccelZ(),
                rawData.getGyroX(),
                rawData.getGyroY(),
                rawData.getGyroZ(),
                rawData.getTemperature(),
                rawData.getTimestampMs()
            );

        } catch (IOException e) {
            logger.error("Erreur lors de la lecture du capteur MPU6050", e);
            return null;
        }
    }

    @Override
    public void close() {
        try {
            logger.info("Fermeture du service MPU6050");
            mpuDriver.close();
            pi4j.shutdown();
        } catch (Exception e) {
            logger.error("Erreur lors de la fermeture du driver MPU6050", e);
        }
    }
}
