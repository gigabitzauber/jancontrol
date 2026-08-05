package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.domain.CruiseConfig;
import org.springframework.context.Lifecycle;

import java.time.Duration;

public class CruiseConfigWatchdog extends JcSchedulable {
    protected CruiseConfigWatchdog(CruiseConfig config, Lifecycle lifecycle) {
        super(() -> {
        }, "CruiseConfigWatchdog", Duration.ofMillis(1234), Duration.ofMillis(500));
    }
}
