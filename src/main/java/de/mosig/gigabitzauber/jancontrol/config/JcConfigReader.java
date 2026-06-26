package de.mosig.gigabitzauber.jancontrol.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.mosig.gigabitzauber.jancontrol.domain.JcConfig;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class JcConfigReader {

    private final YAMLMapper mapper;

    @Autowired
    public JcConfigReader(YAMLMapper yamlMapper) {
        this.mapper = yamlMapper;
    }

    public JcConfig readConfig(Resource configResource) {
        var configContent = readRawConfig(configResource);

        try {
            return mapper.readValue(configContent, JcConfig.class);
        } catch (JsonProcessingException e) {
            throw new JcException("Could not read config file.", e);
        }
    }

    private String readRawConfig(Resource configResource) {
        try {
            return configResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JcException("Could not read config resource", e);
        }
    }
}
