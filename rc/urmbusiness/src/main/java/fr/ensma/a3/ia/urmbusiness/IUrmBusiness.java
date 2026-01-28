package fr.ensma.a3.ia.urmbusiness;

import fr.ensma.a3.ia.urmbusiness.ProcessedUrmData;

/**
 * Interface métier pour le capteur URM37.
 *
 * Fournit une API simple pour récupérer les données du capteur ultrasonique.
 */
public interface IUrmBusiness {

    /**
     * Récupère les données actuelles du capteur URM37.
     *
     * @return Données du capteur, ou null si erreur/capteur non disponible
     */
    ProcessedUrmData getData();

    /**
     * Ferme proprement les ressources (port série, etc.).
     */
    void close();
}
