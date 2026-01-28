package fr.ensma.a3.ia.business.api;

import fr.ensma.a3.ia.business.model.ProcessedMotionData;
import fr.ensma.a3.ia.business.model.RawMotionData;

/**
 * Interface pour le traitement des données de mouvement
 *
 * Cette interface définit le contrat pour convertir les données brutes
 * du capteur MPU6050 en données traitées prêtes pour la DBOX.
 *
 * Implémentations possibles:
 * - Traitement simple (conversion directe)
 * - Traitement avec filtrage (Kalman, passe-bas)
 * - Traitement avec calibration
 */
public interface IMotionDataProcessor {

    /**
     * Traite les données brutes et produit des données prêtes pour la DBOX
     *
     * Cette méthode effectue:
     * 1. Validation des données brutes
     * 2. Conversion accélération/gyroscope → roll/pitch/heave
     * 3. Normalisation dans les plages DBOX [-1.0, +1.0]
     * 4. Calcul des vibrations moteur (RPM, torque) si applicable
     * 5. Application des gains et filtres configurés
     *
     * @param rawData Données brutes du capteur MPU6050
     * @return Données traitées prêtes pour la DBOX, ou null si données invalides
     */
    ProcessedMotionData process(RawMotionData rawData);

    /**
     * Réinitialise l'état interne du processor
     *
     * Utile pour:
     * - Réinitialiser les filtres (états précédents)
     * - Réinitialiser la calibration
     * - Repartir de zéro après une erreur
     */
    void reset();

    /**
     * Vérifie si le processor est prêt à traiter des données
     *
     * @return true si le processor est initialisé et prêt
     */
    boolean isReady();
}
