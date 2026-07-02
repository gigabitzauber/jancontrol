package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

import static de.mosig.gigabitzauber.jancontrol.util.JcIoUtil.assertReadable;

@Data
public abstract class Device {
    private final String sysPath;

    protected Device() {
        this.sysPath = null;
    }

    protected Device(String sysPath) {
        this.sysPath = sysPath;
    }

    @JsonIgnore
    protected final Path safeSysPath() {
        if (this.sysPath == null) {
            throw new JcException("System path is unset");
        }
        var result = Paths.get(this.sysPath);
        assertReadable(result);
        return result;
    }
}
