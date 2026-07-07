package de.mosig.gigabitzauber.jancontrol.cruise;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public final class CruiseInstance implements FutureCallback<Object> {

    private final Fan fan;
    private final Duration interval;
    private final ListeningScheduledExecutorService executor;
    private final Logger log;

    private CruiseInstance(Fan fan, ListeningScheduledExecutorService executor, Logger log) {
        this.fan = requireNonNull(fan, "fan must not be null");
        this.interval = fan.interval();
        this.executor = requireNonNull(executor, "executor must not be null");
        this.log = requireNonNull(log, "log must not be null");
    }

    public static CruiseInstance create(Fan fan, ListeningScheduledExecutorService executor, Logger log) {
        return new CruiseInstance(fan, executor, log);
    }
    
    public void schedule() {
        var cruiseHandle = executor.scheduleAtFixedRate(
            new SimpleCruiseAlgorithm(fan, log),
            interval.toMillis(),
            interval.toMillis(),
            TimeUnit.MILLISECONDS);

        Futures.addCallback(cruiseHandle, this, executor);
    }

    @Override
    public void onSuccess(Object result) {

    }

    @Override
    public void onFailure(@Nullable Throwable t) {
        log.error("Encountered error when working with fan", t);
        schedule();
    }
}
