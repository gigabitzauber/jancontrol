package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private String name;
    private String sysPath;

    public Path safeSysPath() {
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
