package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ReadOnlyDevice extends Device {
    public ReadOnlyDevice(String name, String sysPath) {
        super(name, sysPath);
    }

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
