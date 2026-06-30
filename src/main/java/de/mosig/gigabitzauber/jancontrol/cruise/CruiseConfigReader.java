package de.mosig.gigabitzauber.jancontrol.cruise;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.mosig.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public final class CruiseConfigReader {

    private final YAMLMapper mapper;
    @Getter
    private CruiseConfig config = new CruiseConfig(Set.of());

    @Autowired
    public CruiseConfigReader(YAMLMapper yamlMapper) {
        this.mapper = yamlMapper;
    }

    public CruiseConfig readConfig(Resource configResource) {
        var configContent = readRawConfig(configResource);

        CruiseConfig result = null;
        try {
            result = mapper.readValue(configContent, CruiseConfig.class);
        } catch (JsonProcessingException e) {
            throw new JcException("Could not read config file.", e);
        }

        this.config = result;
        return result;
    }

    private String readRawConfig(Resource configResource) {
        try {
            return configResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JcException("Could not read config resource", e);
        }
    }
}
