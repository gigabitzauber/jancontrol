package de.mosig.gigabitzauber.jancontrol.cruise;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.mosig.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class CruiseConfigReader {

    private final YAMLMapper mapper;

    @Autowired
    public CruiseConfigReader(YAMLMapper yamlMapper) {
        this.mapper = yamlMapper;
    }

    public CruiseConfig readConfig(Resource configResource) {
        var configContent = readRawConfig(configResource);

        try {
            return mapper.readValue(configContent, CruiseConfig.class);
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
