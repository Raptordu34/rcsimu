package fr.ensma.a3.ia.simurcserver;

import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
@ServerEndpoint(value = "/rccontroller/{mode}")
public class RCControllerEndpoint {

    private static final SessionRegistry<String> hub = new SessionRegistry<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("mode") String mode) {
        System.out.println("RCControllerEndpoint.onOpen()");

        hub.register(session, mode);
    }

    @OnMessage
    public void onMessage(Session session, String message) {       
        hub.broadcastJSON(session, message);
    }
}
