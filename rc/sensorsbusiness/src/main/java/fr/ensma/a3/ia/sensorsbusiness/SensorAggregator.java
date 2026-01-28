package fr.ensma.a3.ia.sensorsbusiness;

import fr.ensma.a3.ia.sensorsbusiness.ISensorAggregator;
import fr.ensma.a3.ia.mpubusiness.MpuBusiness;
import fr.ensma.a3.ia.urmbusiness.UrmBusiness;
import fr.ensma.a3.ia.mpubusiness.ProcessedMpuData;
import fr.ensma.a3.ia.urmbusiness.ProcessedUrmData;
import fr.ensma.a3.ia.sensorsbusiness.AllSensorData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Agrégateur de données des capteurs MPU6050 et URM37.
 *
 * Cette classe combine les données de tous les capteurs en un seul packet.
 *
 * OPTIMISATION: L'URM37 est lu dans un thread séparé car sa lecture est lente
 * (~500ms-1s). Le MPU6050 est lu à haute fréquence (50Hz+) tandis que l'URM
 * est lu en arrière-plan et sa dernière valeur est mise en cache.
 *
 * Gestion d'erreur : Si un capteur est en erreur, il retourne null pour ce capteur,
 * mais continue de fournir les données des autres capteurs disponibles.
 */
public class SensorAggregator implements ISensorAggregator, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(SensorAggregator.class);

    /** Intervalle de lecture URM37 en ms (lecture lente, pas besoin de 50Hz) */
    private static final int URM_READ_INTERVAL_MS = 200;

    private final MpuBusiness mpuService;
    private final UrmBusiness urmService;

    /** Dernière valeur URM mise en cache (lecture asynchrone) */
    private final AtomicReference<ProcessedUrmData> cachedUrmData = new AtomicReference<>(null);

    /** Thread de lecture URM */
    private Thread urmReaderThread;

    /** Flag pour arrêter le thread URM */
    private final AtomicBoolean urmRunning = new AtomicBoolean(false);

    /**
     * Constructeur avec configuration par dÃ©faut.
     *
     * @throws IOException Si l'un des capteurs ne peut pas Ãªtre initialisÃ©
     */
    public SensorAggregator() throws IOException {
        this(1, "/dev/ttyAMA0");
    }

    /**
     * Constructeur avec configuration personnalisée.
     *
     * @param mpuI2cBus Bus I2C pour le MPU6050 (généralement 1)
     * @param urmSerialPort Port série pour l'URM37 (ex: /dev/ttyS0)
     * @throws IOException Si l'un des capteurs ne peut pas être initialisé
     */
    public SensorAggregator(int mpuI2cBus, String urmSerialPort) throws IOException {
        logger.info("Initialisation de l'agrégateur de capteurs");

        MpuBusiness tempMpu = null;
        UrmBusiness tempUrm = null;

        try {
            tempMpu = MpuBusiness.getInstance(mpuI2cBus);
            logger.info("MPU6050 initialisé avec succès");
        } catch (IOException e) {
            logger.error("Erreur lors de l'initialisation du MPU6050", e);
        }

        try {
            tempUrm = UrmBusiness.getInstance(urmSerialPort);
            logger.info("URM37 initialisé avec succès");
        } catch (IOException e) {
            logger.error("Erreur lors de l'initialisation de l'URM37", e);
        }

        this.mpuService = tempMpu;
        this.urmService = tempUrm;

        if (this.mpuService == null && this.urmService == null) {
            throw new IOException("Aucun capteur n'a pu être initialisé");
        }

        // Démarrer le thread de lecture URM en arrière-plan
        startUrmReaderThread();
    }

    /**
     * Démarre le thread de lecture asynchrone de l'URM37.
     * Ce thread lit l'URM37 en continu et met à jour le cache.
     */
    private void startUrmReaderThread() {
        if (urmService == null) {
            logger.info("URM37 non disponible, pas de thread de lecture");
            return;
        }

        urmRunning.set(true);
        urmReaderThread = new Thread(() -> {
            logger.info("Thread URM démarré (intervalle={}ms)", URM_READ_INTERVAL_MS);

            while (urmRunning.get()) {
                try {
                    ProcessedUrmData data = urmService.getData();
                    if (data != null) {
                        cachedUrmData.set(data);
                    }
                    Thread.sleep(URM_READ_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.warn("Erreur lecture URM: {}", e.getMessage());
                }
            }

            logger.info("Thread URM arrêté");
        }, "URM-Reader");

        urmReaderThread.setDaemon(true);
        urmReaderThread.start();
    }

    @Override
    public AllSensorData getAllData() {
        ProcessedMpuData mpuData = null;

        // Lire le MPU6050 (lecture rapide, synchrone)
        if (mpuService != null) {
            mpuData = mpuService.getData();
            if (mpuData == null) {
                logger.warn("Échec de lecture du MPU6050");
            }
        }

        // Utiliser la valeur URM mise en cache (lecture asynchrone)
        ProcessedUrmData urmData = cachedUrmData.get();

        // Si tous les capteurs sont en erreur, retourner null
        if (mpuData == null && urmData == null) {
            logger.error("Tous les capteurs sont en erreur");
            return null;
        }

        return new AllSensorData(mpuData, urmData);
    }

    @Override
    public void close() {
        logger.info("Fermeture de l'agrégateur de capteurs");

        // Arrêter le thread URM
        urmRunning.set(false);
        if (urmReaderThread != null) {
            urmReaderThread.interrupt();
            try {
                urmReaderThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (mpuService != null) {
            try {
                MpuBusiness.resetInstance();
            } catch (Exception e) {
                logger.error("Erreur lors de la fermeture du MPU6050", e);
            }
        }

        if (urmService != null) {
            try {
                UrmBusiness.resetInstance();
            } catch (Exception e) {
                logger.error("Erreur lors de la fermeture de l'URM37", e);
            }
        }
    }
}
