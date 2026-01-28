package fr.ensma.a3.ia.serviceapi;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class ControllerMessageDecoder implements Decoder.Text<ControllerMessage> {

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public ControllerMessage decode(String s) throws DecodeException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ControllerMessage readValue = mapper.readValue(s, ControllerMessage.class);
            return readValue;
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
    }
}