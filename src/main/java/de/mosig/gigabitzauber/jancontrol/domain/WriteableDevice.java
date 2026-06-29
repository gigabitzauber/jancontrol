package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Data
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class WriteableDevice extends Device {
    public WriteableDevice() {
        super();
    }

    public WriteableDevice(String name, String sysPath) {
        super(name, sysPath);
    }

    @JsonIgnore
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
