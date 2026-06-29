package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
public abstract class Device {
    private final String name;
    private final String sysPath;

    protected Device() {
        this.name = null;
        this.sysPath = null;
    }

    protected Device(String name, String sysPath) {
        this.name = name;
        this.sysPath = sysPath;
    }

    @JsonIgnore
    public Path safeSysPath() {
        if (this.sysPath == null) {
            throw new JcException("System path is unset");
        }
        Path result = Paths.get(this.sysPath);
        if (!Files.exists(result)) {
            throw new JcException("Could not find sys fs path: " + sysPath);
        } else if (Files.isDirectory(result)) {
            throw new JcException("Sys fs path is not a file: " + sysPath);
        } else {
            return result;
        }
    }
}
