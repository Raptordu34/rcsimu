package fr.ensma.a3.ia.mpubusiness;

import fr.ensma.a3.ia.mpubusiness.ProcessedMpuData;

/**
 * Interface métier pour le capteur MPU6050.
 *
 * Fournit une API simple pour récupérer les données du capteur IMU.
 */
public interface IMpuBusiness {

    /**
     * Récupère les données actuelles du capteur MPU6050.
     *
     * @return Données du capteur, ou null si erreur/capteur non disponible
     */
    ProcessedMpuData getData();

    /**
     * Ferme proprement les ressources I2C.
     */
    void close();
}
