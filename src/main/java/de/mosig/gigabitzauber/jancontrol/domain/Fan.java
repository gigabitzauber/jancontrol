package de.mosig.gigabitzauber.jancontrol.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record Fan(WriteableDevice device, Curve curve, List<ReadOnlyDevice> dependsOn) {
    public Fan {
        if (dependsOn == null) {
            dependsOn = List.of();
        }
        dependsOn = List.copyOf(dependsOn);
    }
}
