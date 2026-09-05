package de.gigabitzauber.jancontrol.test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.gigabitzauber.jancontrol.domain.RpmDevice;

import java.io.IOException;

public final class RpmDeviceSerializer extends StdSerializer<RpmDevice> {
    public RpmDeviceSerializer() {
        super(RpmDevice.class);
    }

    @Override
    public void serialize(RpmDevice value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        var ref = value.ref();
        var slot = value.slot();
        var allowIdle = value.allowIdle();
        var sysName = value.sysName();
        var activationThreshold = value.activationThreshold();
        var driver = value.driver().name().toLowerCase();

        gen.writeStartObject();
        gen.writeStringField("ref", ref);
        gen.writeStringField("sysName", sysName);
        gen.writeNumberField("slot", slot);
        gen.writeStringField("driver", driver);
        gen.writeBooleanField("allowIdle", allowIdle);
        gen.writeNumberField("activationThreshold", activationThreshold);
        gen.writeEndObject();
    }
}
