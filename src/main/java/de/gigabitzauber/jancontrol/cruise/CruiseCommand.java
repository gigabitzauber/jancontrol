package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.CruiseConfigRoot;

import static java.util.Objects.requireNonNull;

public final class CruiseCommand {

    private final JcLifecycle lifecycle;

    public CruiseCommand(JcLifecycle lifecycle) {
        this.lifecycle = requireNonNull(lifecycle, "lifecycle must not be null");
    }

    public void execute(CruiseConfigRoot config) {
        requireNonNull(config, "config must not be null");

        lifecycle.jcStart(config);
    }
}
