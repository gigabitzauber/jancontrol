package de.gigabitzauber.jancontrol.test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.gigabitzauber.jancontrol.domain.TemperatureDevice;

import java.io.IOException;

public final class TemperatureDeviceSerializer extends StdSerializer<TemperatureDevice> {
    public TemperatureDeviceSerializer() {
        super(TemperatureDevice.class);
    }

    @Override
    public void serialize(TemperatureDevice value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        var ref = value.ref();
        var slot = value.slot();
        var sysName = value.sysName();

        gen.writeStartObject();
        gen.writeStringField("ref", ref);
        gen.writeStringField("sysName", sysName);
        gen.writeNumberField("slot", slot);
        gen.writeEndObject();
    }
}
