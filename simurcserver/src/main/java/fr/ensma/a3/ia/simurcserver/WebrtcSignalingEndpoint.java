package fr.ensma.a3.ia.simurcserver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
@ServerEndpoint(value = "/signaling")
public class WebrtcSignalingEndpoint {
    private static List<Session> sessions = new CopyOnWriteArrayList<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        for (Session current : sessions) {
            if (current.isOpen() && !session.getId().equals(current.getId())) {
                current.getBasicRemote().sendText(message);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }
}
