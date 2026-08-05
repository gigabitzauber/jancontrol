package de.gigabitzauber.jancontrol.domain;

import java.util.Collection;
import java.util.Set;

public record CruiseConfigRoot(Collection<Fan> fans) {
    public CruiseConfigRoot {
        if (fans == null) {
            fans = Set.of();
        }

        fans = Set.copyOf(fans);
    }
}
