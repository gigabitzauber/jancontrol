package de.gigabitzauber.jancontrol.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class NamedDevice extends Device {
    private final String name;

    protected NamedDevice() {
        super(null);
        this.name = null;
    }

    protected NamedDevice(String name, String sysPath) {
        super(sysPath);
        this.name = name;
    }
}
