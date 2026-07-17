package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.FanMode;
import org.slf4j.Logger;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

public final class ModeEnforcer extends JcSchedulable {

    static final Duration INITIAL_DELAY = Duration.ofMillis(123);
    static final Duration POLL_INTERVAL = Duration.ofMillis(555);

    private ModeEnforcer(Fan fan, FanMode modeToEnforce, Logger log) {
        super(() -> {
            var currentMode = fan.getCurrentMode();
            if (currentMode != modeToEnforce) {
                log.warn("Encountered external change of fan mode for {}. Enforcing mode {}", fan.device().getName(), modeToEnforce);
                fan.setMode(modeToEnforce);
            }
        }, INITIAL_DELAY, POLL_INTERVAL);
    }

    public static ModeEnforcer create(Fan fan, FanMode modeToEnforce, Logger log) {
        requireNonNull(fan, "fan must not be null");
        requireNonNull(modeToEnforce, "modeToEnforce must not be null");
        requireNonNull(log, "log must not be null");

        return new ModeEnforcer(fan, modeToEnforce, log);
    }
}
