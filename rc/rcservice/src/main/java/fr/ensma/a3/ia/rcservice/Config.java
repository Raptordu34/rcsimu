package fr.ensma.a3.ia.rcservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private final Properties props = new Properties();

    public Config() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                logger.warn("config.properties introuvable, utilisation des valeurs par defaut");
            }
        } catch (IOException e) {
            logger.error("Erreur lecture config.properties", e);
        }
    }

    public boolean isBootEnabled() {
        return Boolean.parseBoolean(props.getProperty("boot", "false"));
    }

    public boolean isEthernet() {
        return Boolean.parseBoolean(props.getProperty("ethernetWaveshare", "false"));
    }
}
