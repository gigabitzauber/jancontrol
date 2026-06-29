package de.mosig.gigabitzauber.jancontrol.domain;

import java.util.Collection;
import java.util.Set;

public record JcConfig(Collection<Fan> fans) {
    public JcConfig {
        if (fans == null) {
            fans = Set.of();
        }

        fans = Set.copyOf(fans);
    }
}
