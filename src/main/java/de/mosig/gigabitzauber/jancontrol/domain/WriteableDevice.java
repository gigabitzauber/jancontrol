package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WriteableDevice extends Device {
    public WriteableDevice(String name, String sysPath) {
        super(name, sysPath);
    }

    public void write(int value) {
        var rawValue = Integer.toString(value);
        try {
            Files.writeString(safeSysPath(), rawValue,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.SYNC);
        } catch (IOException e) {
            throw new JcException("Could not write to device " + getName(), e);
        }
    }
}
