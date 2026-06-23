package de.mosig.gigabitzauber.jancontrol.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private String name;
    private String sysPath;

    public Path safeSysPath() {
        return Paths.get(this.sysPath);
    }
}
