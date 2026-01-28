package fr.ensma.a3.ia.simurcserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import jakarta.websocket.Session;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class SessionRegistry<T> {

    private Session sender;

    private final List<Session> receivers = new ArrayList<>();

    public synchronized void register(Session session, String mode) {
        if ("sender".equals(mode)) {
            if (sender != null && sender.isOpen()) {
                try {
                    sender.close(); 
                } catch (IOException e) {
                    System.err.println("Erreur lors de la fermeture de l'ancien sender : " + e.getMessage());
                }
            }
            sender = session;
            System.out.println("Sender connected.");
        } else if ("receiver".equals(mode)) {
            receivers.add(session);
            System.out.println("Receiver connected.");
        } else {
            System.out.println("Mode inconnu : " + mode);
        }
    }

    public synchronized void broadcastJSON(Session source, T message) {
        // Sender only
        if (source != sender) {
            return;
        }

        receivers.removeIf(s -> !s.isOpen());

        for (Session s : receivers) {
            try {
                s.getBasicRemote().sendObject(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void broadcastBinary(Session source, byte[] message) {
        // Sender only
        if (source != sender) {
            return;
        }

        receivers.removeIf(s -> !s.isOpen());

        for (Session s : receivers) {
            try {
                s.getBasicRemote().sendBinary(ByteBuffer.wrap(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
