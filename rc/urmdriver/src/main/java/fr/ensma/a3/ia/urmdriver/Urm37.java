package fr.ensma.a3.ia.urmdriver;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static fr.ensma.a3.ia.urmdriver.Urm37Constants.*;

/**
 * API Java pour le capteur ultrason URM37 V3.2 via UART.
 * Utilise jSerialComm (bibliothèque Java pure, pas de daemon nécessaire).
 *
 * @author Projet BE Simulateur
 */
public class Urm37 implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Urm37.class);

    private final SerialPort serialPort;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private boolean initialized = false;

    /**
     * Constructeur - Initialise la communication UART avec le capteur URM37.
     *
     * @param devicePath Chemin du port série (ex: "/dev/ttyS0")
     * @throws IOException Si le port ne peut pas être ouvert
     */
    public Urm37(String devicePath) throws IOException {
        // Ouvrir le port série
        this.serialPort = SerialPort.getCommPort(devicePath);

        // Configuration : 9600 bauds, 8 bits de données, 1 bit de stop, pas de parité (8N1)
        serialPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

        // Ouvrir le port
        if (!serialPort.openPort()) {
            throw new IOException("Impossible d'ouvrir le port série : " + devicePath);
        }

        // Récupérer les flux d'entrée/sortie
        this.inputStream = serialPort.getInputStream();
        this.outputStream = serialPort.getOutputStream();
        this.initialized = true;

        logger.info("[URM37] Initialisé sur {} @ 9600 bauds", devicePath);
    }

    /**
     * Lit la distance et la température et remplit l'objet data.
     *
     * @param data Objet à remplir avec les données lues
     * @throws IOException En cas d'erreur de communication
     */
    public void readAll(Urm37Data data) throws IOException {
        data.setDistanceCm(readDistance());
        try {
            Thread.sleep(10); // Court délai entre les commandes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        data.setTemperature(readTemperature());
        data.setTimestampMs(System.currentTimeMillis());
    }

    /**
     * Lit la distance mesurée par le capteur.
     *
     * @return Distance en centimètres, ou -1.0f en cas d'erreur
     * @throws IOException En cas d'erreur de communication
     */
    public float readDistance() throws IOException {
        // Envoyer la commande de lecture de distance
        outputStream.write(CMD_READ_DISTANCE);
        outputStream.flush();

        // Lire la réponse (4 octets: Header, HighByte, LowByte, Sum)
        byte[] response = new byte[4];
        int bytesRead = 0;
        long startTime = System.currentTimeMillis();

        while (bytesRead < 4 && (System.currentTimeMillis() - startTime) < 500) {
            int available = inputStream.available();
            if (available > 0) {
                int read = inputStream.read(response, bytesRead, 4 - bytesRead);
                if (read > 0) {
                    bytesRead += read;
                }
            } else {
                try { Thread.sleep(5); } catch (InterruptedException e) {}
            }
        }

        if (bytesRead == 4 && response[0] == RSP_DISTANCE_HEADER) {
            // Calcul de la distance : (HighByte << 8) | LowByte
            int dist = ((response[1] & 0xFF) << 8) | (response[2] & 0xFF);
            return (float) dist;
        }

        return -1.0f; // Erreur
    }

    /**
     * Lit la température mesurée par le capteur.
     *
     * @return Température en degrés Celsius, ou -999f en cas d'erreur
     * @throws IOException En cas d'erreur de communication
     */
    public float readTemperature() throws IOException {
        // Envoyer la commande de lecture de température
        outputStream.write(CMD_READ_TEMP);
        outputStream.flush();

        // Lire la réponse (4 octets)
        byte[] response = new byte[4];
        int bytesRead = 0;
        long startTime = System.currentTimeMillis();

        while (bytesRead < 4 && (System.currentTimeMillis() - startTime) < 500) {
            int available = inputStream.available();
            if (available > 0) {
                int read = inputStream.read(response, bytesRead, 4 - bytesRead);
                if (read > 0) {
                    bytesRead += read;
                }
            } else {
                try { Thread.sleep(5); } catch (InterruptedException e) {}
            }
        }

        if (bytesRead == 4 && response[0] == RSP_TEMP_HEADER) {
            // Extraction de la température : 12 bits de données (4 bits LSB du high byte + 8 bits du low byte)
            int tempRaw = ((response[1] & 0x0F) << 8) | (response[2] & 0xFF);
            float temp = (float) tempRaw / 10.0f;

            // Gestion du signe (bit 4 du high byte)
            if ((response[1] & 0xF0) > 0) {
                temp = -temp;
            }

            return temp;
        }

        return -999f; // Erreur
    }

    /**
     * Ferme le port série et libère les ressources.
     */
    @Override
    public void close() {
        if (serialPort != null && serialPort.isOpen()) {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                // Ignorer les erreurs de fermeture des flux
            }
            serialPort.closePort();
            initialized = false;
            logger.info("[URM37] Port série fermé");
        }
    }

    /**
     * Vérifie si le port série est ouvert et initialisé.
     *
     * @return true si initialisé, false sinon
     */
    public boolean isInitialized() {
        return initialized && serialPort.isOpen();
    }
}
