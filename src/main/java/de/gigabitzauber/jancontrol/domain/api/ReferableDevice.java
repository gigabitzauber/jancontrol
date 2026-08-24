package de.gigabitzauber.jancontrol.domain.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class ReferableDevice extends Device {
    private final String ref;

    protected ReferableDevice() {
        super((String) null);
        this.ref = null;
    }

    protected ReferableDevice(String ref, String sysPath) {
        super(sysPath);
        this.ref = ref;
    }
}
