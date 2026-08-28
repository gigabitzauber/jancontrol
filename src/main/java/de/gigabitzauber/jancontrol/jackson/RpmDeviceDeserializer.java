package de.gigabitzauber.jancontrol.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import de.gigabitzauber.jancontrol.drivers.hwmon.JcHwmonDrivers;
import de.gigabitzauber.jancontrol.util.HwmonDirResolver;

import java.io.IOException;

import static de.gigabitzauber.jancontrol.util.JcNodeUtil.assertNode;
import static de.gigabitzauber.jancontrol.util.JcNodeUtil.safeGetNode;
import static java.util.Objects.requireNonNull;

public final class RpmDeviceDeserializer extends StdDeserializer<RpmDevice> {

    private final HwmonDirResolver hwmonDirResolver;

    public RpmDeviceDeserializer(HwmonDirResolver hwmonDirResolver) {
        super(RpmDevice.class);
        this.hwmonDirResolver = requireNonNull(hwmonDirResolver, "hwmonDirResolver must not be null");
    }

    @Override
    public RpmDevice deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode parent = ctxt.readTree(p);
        var ref = assertNode(parent, "ref");
        var sysName = assertNode(parent, "sysName");
        var rawSlot = assertNode(parent, "slot");
        var slot = Integer.parseInt(rawSlot);

        var baseRpmDevice = RpmDevice.builder().build();
        var hwmonDriver = baseRpmDevice.driver();
        var hwmonDriverName = safeGetNode(parent, "driver");
        if (hwmonDriverName != null) {
            hwmonDriver = JcHwmonDrivers.fromCfgName(hwmonDriverName);
        }

        var allowIdle = baseRpmDevice.allowIdle();
        var rawAllowIdle = safeGetNode(parent, "allowIdle");
        if (rawAllowIdle != null) {
            allowIdle = Boolean.parseBoolean(rawAllowIdle);
        }

        var activationThreshold = baseRpmDevice.activationThreshold();
        var rawActivationThreshold = safeGetNode(parent, "activationThreshold");
        if (rawActivationThreshold != null) {
            activationThreshold = Integer.parseInt(rawActivationThreshold);
        }

        var hwmonDir = hwmonDirResolver.findHwmonDir(sysName);

        var sysPath = hwmonDir.resolve("pwm" + slot).toString();
        return RpmDevice.builder()
            .driver(hwmonDriver)
            .allowIdle(allowIdle)
            .ref(ref)
            .sysName(sysName)
            .slot(slot)
            .sysPath(sysPath)
            .activationThreshold(activationThreshold)
            .build();
    }
}
