package de.gigabitzauber.jancontrol.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.gigabitzauber.jancontrol.error.JcException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;

import static de.gigabitzauber.jancontrol.util.JcIoUtil.assertIsReadableFile;
import static de.gigabitzauber.jancontrol.util.JcIoUtil.assertIsWritableFile;

@Data
@SuperBuilder
@Accessors(fluent = true)
@NoArgsConstructor(force = true)
public abstract class Device {
    private final String sysPath;

    protected Device(String sysPath) {
        this.sysPath = sysPath;
    }

    @JsonIgnore
    protected final Path safeReadableSysPath() {
        return assertIsReadableFile(safeSysPath());
    }

    @JsonIgnore
    protected final Path safeWritableSysPath() {
        return assertIsWritableFile(safeSysPath());
    }

    private Path safeSysPath() {
        if (this.sysPath == null) {
            throw new JcException("Sys path is unset");
        }
        return Paths.get(this.sysPath);
    }
}
