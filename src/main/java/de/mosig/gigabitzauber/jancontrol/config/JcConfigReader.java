package de.mosig.gigabitzauber.jancontrol.config;

import de.mosig.gigabitzauber.jancontrol.domain.JcConfig;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public final class JcConfigReader {

    public static JcConfig readConfig(Resource configResource) {
        var configContent = readRawConfig(configResource);

        return new Yaml().loadAs(configContent, JcConfig.class);
    }

    private static String readRawConfig(Resource configResource) {
        try {
            return configResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JcException("Could not read config resource", e);
        }
    }
}
