package de.mosig.gigabitzauber.jancontrol.config;

import de.mosig.gigabitzauber.jancontrol.JcLifecycle;
import de.mosig.gigabitzauber.jancontrol.domain.CruiseConfig;

import static java.util.Objects.requireNonNull;

public final class CruiseCommand {

    private final JcLifecycle lifecycle;

    public CruiseCommand(JcLifecycle lifecycle) {
        this.lifecycle = requireNonNull(lifecycle, "lifecycle must not be null");
    }

    public void execute(CruiseConfig config) {
        requireNonNull(config, "config must not be null");

        for (var fan : config.fans()) {
            lifecycle.register(fan);
        }
    }
}
