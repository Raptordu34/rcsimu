package fr.ensma.a3.ia.urmdriver;

/**
 * Constantes et commandes pour le capteur URM37 V3.2 (Serial Mode).
 */
public class Urm37Constants {

    // Configuration Série par défaut
    public static final int DEFAULT_BAUD_RATE = 9600;
    
    // Commandes du protocole (Format: Header, Data0, Data1, Checksum)
    // Lecture Distance: 0x22, 0x00, 0x00, 0x22
    public static final byte[] CMD_READ_DISTANCE = {(byte) 0x22, (byte) 0x00, (byte) 0x00, (byte) 0x22};
    
    // Lecture Température: 0x11, 0x00, 0x00, 0x11
    public static final byte[] CMD_READ_TEMP     = {(byte) 0x11, (byte) 0x00, (byte) 0x00, (byte) 0x11};

    // Réponses
    public static final byte RSP_DISTANCE_HEADER = 0x22;
    public static final byte RSP_TEMP_HEADER     = 0x11;
    public static final byte RSP_ERROR_HEADER    = (byte) 0xEE;

    public static final int MEASURE_DELAY_MS = 50;

    private Urm37Constants() {
        throw new AssertionError("Classe non instanciable");
    }
}
