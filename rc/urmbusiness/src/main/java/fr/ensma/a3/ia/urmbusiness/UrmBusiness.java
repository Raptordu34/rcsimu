package fr.ensma.a3.ia.urmbusiness;

import fr.ensma.a3.ia.urmbusiness.IUrmBusiness;
import fr.ensma.a3.ia.urmdriver.Urm37;
import fr.ensma.a3.ia.urmdriver.Urm37Data;
import fr.ensma.a3.ia.urmbusiness.ProcessedUrmData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Implémentation du service métier pour le capteur URM37.
 *
 * Cette classe encapsule le driver URM37 et fournit une API simple.
 * Implémentation en Singleton thread-safe pour garantir une instance unique.
 */
public class UrmBusiness implements IUrmBusiness, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(UrmBusiness.class);

    private static volatile UrmBusiness instance;
    private static final Object lock = new Object();

    private final Urm37 urmDriver;
    private final String portName;

    /**
     * Constructeur privé avec port série par défaut.
     *
     * @throws IOException Si le port série ne peut pas être ouvert
     */
    private UrmBusiness() throws IOException {
        this("/dev/serial0");
    }

    /**
     * Constructeur privé avec port série personnalisé.
     *
     * @param portName Nom du port série (ex: /dev/ttyS0)
     * @throws IOException Si le port série ne peut pas être ouvert
     */
    private UrmBusiness(String portName) throws IOException {
        logger.info("Initialisation du service URM37 sur {}", portName);
        this.portName = portName;
        this.urmDriver = new Urm37(portName);
    }

    /**
     * Récupère l'instance unique du service URM37 (port série par défaut: /dev/ttyS0).
     * Utilise le pattern Singleton avec double-checked locking.
     *
     * @return L'instance unique de UrmBusiness
     * @throws IOException Si l'initialisation échoue
     */
    public static UrmBusiness getInstance() throws IOException {
        return getInstance("/dev/serial0");
    }

    /**
     * Récupère l'instance unique du service URM37 avec un port série spécifique.
     * Utilise le pattern Singleton avec double-checked locking.
     *
     * @param portName Nom du port série (ex: /dev/ttyS0)
     * @return L'instance unique de UrmBusiness
     * @throws IOException Si l'initialisation échoue
     * @throws IllegalStateException Si une instance existe déjà avec un port différent
     */
    public static UrmBusiness getInstance(String portName) throws IOException {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new UrmBusiness(portName);
                } else if (!instance.portName.equals(portName)) {
                    throw new IllegalStateException(
                        "Instance déjà initialisée avec le port " + instance.portName +
                        ". Impossible de créer une instance avec le port " + portName
                    );
                }
            }
        } else if (!instance.portName.equals(portName)) {
            throw new IllegalStateException(
                "Instance déjà initialisée avec le port " + instance.portName +
                ". Impossible de créer une instance avec le port " + portName
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
    public ProcessedUrmData getData() {
        try {
            Urm37Data rawData = new Urm37Data();
            urmDriver.readAll(rawData);

            return new ProcessedUrmData(
                rawData.getDistanceCm(),
                rawData.getTemperature(),
                rawData.getTimestampMs()
            );

        } catch (IOException e) {
            logger.error("Erreur lors de la lecture du capteur URM37", e);
            return null;
        }
    }

    @Override
    public void close() {
        try {
            logger.info("Fermeture du service URM37");
            urmDriver.close();
        } catch (Exception e) {
            logger.error("Erreur lors de la fermeture du driver URM37", e);
        }
    }
}
