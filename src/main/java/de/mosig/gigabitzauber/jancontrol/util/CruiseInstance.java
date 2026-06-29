package de.mosig.gigabitzauber.jancontrol.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.mosig.gigabitzauber.jancontrol.command.CruiseCommand;
import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class CruiseInstance implements FutureCallback<Object> {

    private final Fan fan;
    private final Duration interval;
    private final ListeningScheduledExecutorService executor;
    private final Logger log;

    public CruiseInstance(Fan fan, Duration interval, ListeningScheduledExecutorService executor, Logger log) {
        this.fan = fan;
        this.interval = interval;
        this.executor = executor;
        this.log = log;
    }

    public void schedule() {
        var cruiseHandle = executor.scheduleAtFixedRate(
            new CruiseCommand.FanCruiseRunnable(fan, log),
            interval.toMillis(),
            interval.toMillis(),
            TimeUnit.MILLISECONDS);

        Futures.addCallback(cruiseHandle, this, Executors.newSingleThreadScheduledExecutor());
    }

    @Override
    public void onSuccess(Object result) {

    }

    @Override
    public void onFailure(@Nullable Throwable t) {
        log.error("Cruise command encountered error", t);
        schedule();
    }
}
