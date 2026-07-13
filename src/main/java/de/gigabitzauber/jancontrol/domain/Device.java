package de.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.gigabitzauber.jancontrol.error.JcException;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

import static de.gigabitzauber.jancontrol.util.JcIoUtil.assertReadable;
import static de.gigabitzauber.jancontrol.util.JcIoUtil.assertWritable;

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
    protected final Path safeReadableSysPath() {
        return assertReadable(safeSysPath());
    }

    @JsonIgnore
    protected final Path safeWritableSysPath() {
        return assertWritable(safeSysPath());
    }

    private Path safeSysPath() {
        if (this.sysPath == null) {
            throw new JcException("Sys path is unset");
        }
        return Paths.get(this.sysPath);
    }
}
