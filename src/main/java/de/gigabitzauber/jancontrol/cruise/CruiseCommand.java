package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.CruiseConfigRoot;
import org.slf4j.Logger;

import static java.util.Objects.requireNonNull;

public final class CruiseCommand {

    private final JcLifecycle lifecycle;
    private final Logger log;

    public CruiseCommand(JcLifecycle lifecycle, Logger log) {
        this.lifecycle = requireNonNull(lifecycle, "lifecycle must not be null");
        this.log = requireNonNull(log, "log must not be null");
    }

    public void execute(CruiseConfigRoot config) {
        requireNonNull(config, "config must not be null");

        if (config.fans().isEmpty()) {
            log.warn("No fans specified. Running in NOP mode.");
            lifecycle.nop();
        } else {
            for (var fan : config.fans()) {
                lifecycle.register(fan);
            }
        }
    }
}
