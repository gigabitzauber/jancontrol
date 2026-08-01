package de.gigabitzauber.jancontrol.domain;

import de.gigabitzauber.jancontrol.domain.api.FanMode;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Objects;

@EqualsAndHashCode
@Accessors(fluent = true)
@ToString(onlyExplicitlyIncluded = true)
public final class RegisteredFan {

    private final Fan fan;
    @ToString.Include
    private final String name;
    private final int origRpmPercentage;
    private final FanMode origFanMode;

    public RegisteredFan(Fan fan) {
        Objects.requireNonNull(fan, "fan cannot be null");

        this.fan = fan;
        this.name = fan.device().getName();
        this.origRpmPercentage = fan.device().read();
        this.origFanMode = fan.device().getMode();
    }

    public void restoreOrigSettings() {
        this.fan.device().write(origRpmPercentage);
        this.fan.device().setMode(origFanMode);
    }
}
