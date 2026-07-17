package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.Fan;
import org.slf4j.Logger;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

public final class CruiseInstance extends JcSchedulable {

    static final Duration INITIAL_MAX_DELAY = Duration.ofMillis(666);

    private CruiseInstance(Fan fan, JcLifecycle lifecycle, Logger log) {
        super(
            new SimpleCruiseAlgorithm(fan, lifecycle, log),
            "fan cruise",
            INITIAL_MAX_DELAY,
            fan.interval());
    }

    public static CruiseInstance create(Fan fan, JcLifecycle lifecycle, Logger log) {
        requireNonNull(fan, "fan must not be null");
        requireNonNull(lifecycle, "lifecycle must not be null");
        requireNonNull(log, "log must not be null");

        return new CruiseInstance(fan, lifecycle, log);
    }
}
