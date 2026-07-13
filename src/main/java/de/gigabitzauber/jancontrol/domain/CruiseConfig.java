package de.gigabitzauber.jancontrol.domain;

import java.util.Collection;
import java.util.Set;

public record CruiseConfig(Collection<Fan> fans) {
    public CruiseConfig {
        if (fans == null) {
            fans = Set.of();
        }

        fans = Set.copyOf(fans);
    }
}
