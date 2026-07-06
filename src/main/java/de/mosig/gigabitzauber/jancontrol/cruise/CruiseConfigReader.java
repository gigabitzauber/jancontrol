package de.mosig.gigabitzauber.jancontrol.cruise;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.mosig.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public final class CruiseConfigReader {

    private final YAMLMapper mapper;

    @Autowired
    public CruiseConfigReader(YAMLMapper yamlMapper) {
        this.mapper = requireNonNull(yamlMapper, "mapper must not be null");
    }

    public CruiseConfig readConfig(Resource configResource) {
        var configContent = readRawConfig(configResource);

        CruiseConfig result = null;
        try {
            result = mapper.readValue(configContent, CruiseConfig.class);
        } catch (Exception e) {
            throw new JcException("Config file contains faulty YAML", e);
        }

        return result;
    }

    private String readRawConfig(Resource configResource) {
        try {
            return configResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new JcException("Could not read config file", e);
        }
    }
}
