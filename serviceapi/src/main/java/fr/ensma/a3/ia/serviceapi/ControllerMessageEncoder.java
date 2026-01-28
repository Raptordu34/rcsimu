package fr.ensma.a3.ia.serviceapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

/**
 * @author Mickael BARON (mickael.baron@ensma.fr)
 */
public class ControllerMessageEncoder implements Encoder.Text<ControllerMessage> {

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String encode(ControllerMessage object) throws EncodeException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();

            return "ERROR";
        }
    }
}
