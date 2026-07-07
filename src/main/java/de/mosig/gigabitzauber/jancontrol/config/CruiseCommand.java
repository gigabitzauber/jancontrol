package de.mosig.gigabitzauber.jancontrol.config;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.mosig.gigabitzauber.jancontrol.JcLifecycle;
import de.mosig.gigabitzauber.jancontrol.cruise.CruiseInstance;
import de.mosig.gigabitzauber.jancontrol.domain.CruiseConfig;
import org.slf4j.Logger;

import static java.util.Objects.requireNonNull;

public final class CruiseCommand {

    private final ListeningScheduledExecutorService fanCruiseExecutor;
    private final JcLifecycle lifecycle;
    private final Logger log;

    public CruiseCommand(ListeningScheduledExecutorService fanCruiseExecutor, JcLifecycle lifecycle, Logger log) {
        this.fanCruiseExecutor = requireNonNull(fanCruiseExecutor, "fanCruiseExecutor must not be null");
        this.lifecycle = requireNonNull(lifecycle, "lifecycle must not be null");
        this.log = requireNonNull(log, "log must not be null");
    }

    public void execute(CruiseConfig config) {
        requireNonNull(config, "config must not be null");

        for (var fan : config.fans()) {
            lifecycle.register(fan);
            CruiseInstance.create(fan, fanCruiseExecutor, log).schedule();
        }
    }
}
