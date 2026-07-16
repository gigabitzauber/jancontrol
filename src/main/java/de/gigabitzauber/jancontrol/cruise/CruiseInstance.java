package de.gigabitzauber.jancontrol.cruise;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.Fan;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public final class CruiseInstance implements JcSchedulable {

    static final int INITIAL_DELAY_UPPER_BOUND_MILLIS = 666;
    private static final Random RND = new Random();

    private final Fan fan;
    private final Duration interval;
    private final JcLifecycle lifecycle;
    private final Logger log;

    private CruiseInstance(Fan fan, JcLifecycle lifecycle, Logger log) {
        this.fan = requireNonNull(fan, "fan must not be null");
        this.interval = fan.interval();
        this.lifecycle = requireNonNull(lifecycle, "lifecycle must not be null");
        this.log = requireNonNull(log, "log must not be null");
    }

    public static CruiseInstance create(Fan fan, JcLifecycle lifecycle, Logger log) {
        return new CruiseInstance(fan, lifecycle, log);
    }

    @Override
    public void schedule(ListeningScheduledExecutorService executor, FutureCallback<Object> callback) {
        requireNonNull(executor, "executor must not be null");
        requireNonNull(callback, "callback must not be null");
        
        var initialDelayMillis = RND.nextInt(INITIAL_DELAY_UPPER_BOUND_MILLIS);
        var cruiseHandle = executor.scheduleAtFixedRate(
            new SimpleCruiseAlgorithm(fan, lifecycle, log),
            initialDelayMillis,
            interval.toMillis(),
            TimeUnit.MILLISECONDS);

        Futures.addCallback(cruiseHandle, callback, executor);
    }
}
