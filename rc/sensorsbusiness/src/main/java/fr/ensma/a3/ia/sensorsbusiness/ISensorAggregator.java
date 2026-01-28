package fr.ensma.a3.ia.sensorsbusiness;

import fr.ensma.a3.ia.sensorsbusiness.AllSensorData;

/**
 * Interface de l'agrégateur de capteurs.
 *
 * Combine les données de tous les capteurs (MPU6050 + URM37) en un seul packet.
 */
public interface ISensorAggregator {

    /**
     * Récupère toutes les données des capteurs en un seul appel.
     *
     * @return Données combinées de tous les capteurs, ou null si tous les capteurs sont en erreur
     */
    AllSensorData getAllData();

    /**
     * Ferme proprement toutes les ressources des capteurs.
     */
    void close();
}
