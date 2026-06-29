package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;

@Data
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ReadOnlyDevice extends Device {
    public ReadOnlyDevice() {
        super();
    }

    public ReadOnlyDevice(String name, String sysPath) {
        super(name, sysPath);
    }

    @JsonIgnore
    public int read() {
        String rawValue = null;
        try {
            rawValue = Files.readString(safeSysPath());
        } catch (IOException e) {
            throw new JcException("Could not read value of device " + getName(), e);
        }


        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            throw new JcException("Value of device " + getName() + " is not a number.", e);
        }
    }
}
