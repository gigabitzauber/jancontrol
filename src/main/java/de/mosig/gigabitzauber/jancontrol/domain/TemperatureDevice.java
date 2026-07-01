package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class TemperatureDevice extends Device {
    public TemperatureDevice() {
        super();
    }

    public TemperatureDevice(String name, String sysPath) {
        super(name, sysPath);
    }

    @JsonIgnore
    public int read() {
        String rawValue = readRaw();

        var cleanValueStr = rawValue.strip();
        var readValue = -1;
        try {
            readValue = Integer.parseInt(cleanValueStr);
        } catch (NumberFormatException e) {
            throw new JcException("Value of device '" + getName() + "' is not a number.", e);
        }

        return fromValue(readValue);
    }

    @JsonIgnore
    public String readRaw() {
        try {
            return Files.readString(safeSysPath()).strip();
        } catch (IOException e) {
            throw new JcException("Could not read value of device '" + getName() + "'", e);
        }
    }
}
