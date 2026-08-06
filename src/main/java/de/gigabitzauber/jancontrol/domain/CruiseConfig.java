package de.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.hash.Hashing;
import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.util.JcIoUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class CruiseConfig {

    private final Resource configResource;
    private final YAMLMapper mapper;

    private final AtomicReference<String> origHexHash = new AtomicReference<>();

    public CruiseConfig(Resource configResource, YAMLMapper mapper) {
        this.configResource = Objects.requireNonNull(configResource, "configResource must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");

        this.origHexHash.set(calcHash());
    }

    public CruiseConfigRoot load() {
        var rawConfigContent = JcIoUtil.readString(configResource).strip();

        CruiseConfigRoot root = new CruiseConfigRoot(List.of());
        if (!StringUtils.isBlank(rawConfigContent)) {
            try {
                root = mapper.readValue(rawConfigContent, CruiseConfigRoot.class);
            } catch (Exception e) {
                throw new JcException("Config file contains faulty YAML", e);
            }
        }

        this.origHexHash.set(calcHash());

        return root;
    }

    public boolean hasChanged() {
        return !origHexHash.get().equals(calcHash());
    }

    private String calcHash() {
        var contents = JcIoUtil.readString(configResource);
        return Hashing.sha256().hashString(contents, StandardCharsets.UTF_8).toString();
    }
}
