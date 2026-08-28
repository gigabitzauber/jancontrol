package de.gigabitzauber.jancontrol.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import de.gigabitzauber.jancontrol.domain.TemperatureDevice;
import de.gigabitzauber.jancontrol.util.HwmonDirResolver;

import java.io.IOException;

import static de.gigabitzauber.jancontrol.util.JcNodeUtil.assertNode;
import static java.util.Objects.requireNonNull;

public final class TemperatureDeviceDeserializer extends StdDeserializer<TemperatureDevice> {

    private final HwmonDirResolver hwmonDirResolver;

    public TemperatureDeviceDeserializer(HwmonDirResolver hwmonDirResolver) {
        super(RpmDevice.class);
        this.hwmonDirResolver = requireNonNull(hwmonDirResolver, "hwmonDirResolver must not be null");
    }

    @Override
    public TemperatureDevice deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode parent = ctxt.readTree(p);
        var ref = assertNode(parent, "ref");
        var sysName = assertNode(parent, "sysName");
        var slot = assertNode(parent, "slot");

        var hwmonDir = hwmonDirResolver.findHwmonDir(sysName);

        var sysPath = hwmonDir.resolve("temp" + slot + "_input").toString();
        return TemperatureDevice.builder()
            .sysName(sysName)
            .slot(Integer.parseInt(slot))
            .sysPath(sysPath)
            .ref(ref)
            .build();
    }
}
