package de.gigabitzauber.jancontrol.cruise;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.FanMode;
import de.gigabitzauber.jancontrol.error.JcSchedulableException;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public final class ModeEnforcer implements JcSchedulable {

    static final int INITIAL_DELAY_MILLIS = 123;
    static final int POLL_INTERVAL_MILLIS = 555;

    private final Fan fan;
    private final FanMode modeToEnforce;

    private ModeEnforcer(Fan fan, FanMode modeToEnforce) {
        this.fan = requireNonNull(fan);
        this.modeToEnforce = requireNonNull(modeToEnforce);
    }

    public static ModeEnforcer create(Fan fan, FanMode modeToEnforce) {
        return new ModeEnforcer(fan, modeToEnforce);
    }

    @Override
    public void schedule(ListeningScheduledExecutorService executor, FutureCallback<Object> callback) {
        fan.setMode(modeToEnforce);
        var enforceThreadHandle = executor.scheduleAtFixedRate(() -> {
                var currentMode = fan.getCurrentMode();
                if (currentMode != this.modeToEnforce) {
                    throw new JcSchedulableException("Encountered external change of fan mode for " + fan.device().getName() + ". Enforcing mode " + modeToEnforce, this);
                }
            },
            INITIAL_DELAY_MILLIS,
            POLL_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS);
        Futures.addCallback(enforceThreadHandle, callback, executor);
    }
}
