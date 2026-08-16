package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.CruiseConfig;
import org.slf4j.Logger;

import java.time.Duration;

public final class CruiseConfigWatchdog extends JcSchedulable {
    private CruiseConfigWatchdog(CruiseConfig config, JcLifecycle lifecycle, Logger log) {
        super(new JcOp(
            "CruiseConfigWatchdog",
            () -> {
                if (config.hasChanged()) {
                    log.info("Encountered changes in config. Reloading..");
                    lifecycle.restart(config.load());
                }
            }
        ), Duration.ofMillis(1234), Duration.ofMillis(5000));
    }

    public static CruiseConfigWatchdog create(CruiseConfig config, JcLifecycle lifecycle, Logger log) {
        return new CruiseConfigWatchdog(config, lifecycle, log);
    }
}
