package de.gigabitzauber.jancontrol.domain.api;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true)
public abstract class HwmonDevice extends ReferableDevice {
    private final String sysName;
    @Size(min = 1)
    private final int slot;

    public HwmonDevice(String sysPath, String ref, String sysName, int slot) {
        super(ref, sysPath);
        this.sysName = sysName;
        this.slot = slot;
    }
}
