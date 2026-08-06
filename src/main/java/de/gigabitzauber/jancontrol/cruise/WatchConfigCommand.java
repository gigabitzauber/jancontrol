package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.CruiseConfig;
import org.slf4j.Logger;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class WatchConfigCommand {
    private final FanCruiseExecutor executor;
    private final JcLifecycle lifecycle;
    private final Logger log;

    public WatchConfigCommand(FanCruiseExecutor executor, JcLifecycle lifecycle, Logger log) {
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
        this.lifecycle = requireNonNull(lifecycle, "lifecycle must not be null");
        this.log = requireNonNull(log, "log must not be null");
    }

    public void execute(CruiseConfig config) {
        CruiseConfigWatchdog.create(config, lifecycle, log).schedule(executor, lifecycle);
    }
}
