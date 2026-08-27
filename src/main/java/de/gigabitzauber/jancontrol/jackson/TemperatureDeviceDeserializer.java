package de.gigabitzauber.jancontrol.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import de.gigabitzauber.jancontrol.domain.TemperatureDevice;
import de.gigabitzauber.jancontrol.util.JcIoUtil;

import java.io.IOException;
import java.nio.file.Path;

import static de.gigabitzauber.jancontrol.util.JcNodeUtil.assertNode;

public final class TemperatureDeviceDeserializer extends StdDeserializer<TemperatureDevice> {

    private final Path hwmonClassDir;

    public TemperatureDeviceDeserializer(Path hwmonClassDir) {
        super(RpmDevice.class);
        this.hwmonClassDir = hwmonClassDir;
    }

    @Override
    public TemperatureDevice deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode parent = ctxt.readTree(p);
        var ref = assertNode(parent, "ref");
        var sysName = assertNode(parent, "sysName");
        var slot = assertNode(parent, "slot");

        var hwmonDir = JcIoUtil.findHwmonDir(hwmonClassDir, sysName);

        var sysPath = hwmonDir.resolve("temp" + slot + "_input").toString();
        return TemperatureDevice.builder()
            .sysName(sysName)
            .slot(Integer.parseInt(slot))
            .sysPath(sysPath)
            .ref(ref)
            .build();
    }
}
