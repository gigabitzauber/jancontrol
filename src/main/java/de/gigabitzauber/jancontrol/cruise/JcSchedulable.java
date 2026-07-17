package de.gigabitzauber.jancontrol.cruise;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.gigabitzauber.jancontrol.error.JcSchedulableException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

import static java.util.Objects.requireNonNull;

public abstract class JcSchedulable {

    protected final RandomGenerator rnd = RandomGenerator.getDefault();

    private final Runnable op;
    private final String opName;
    private final Duration initialDelay;
    private final Duration interval;

    protected JcSchedulable(Runnable op, String opName, Duration initialMaxDelay, Duration interval) {
        this.op = op;
        this.opName = opName;
        this.initialDelay = initialMaxDelay;
        this.interval = interval;
    }

    public final void schedule(ListeningScheduledExecutorService executor, FutureCallback<Object> callback) {
        requireNonNull(executor, "executor must not be null");
        requireNonNull(callback, "callback must not be null");

        var initialDelayMillis = rnd.nextLong(initialDelay.toMillis());
        var future = executor.scheduleAtFixedRate(
            () -> {
                try {
                    op.run();
                } catch (Exception e) {
                    throw new JcSchedulableException(this.opName + " ran into error", this, e);
                }
            },
            initialDelayMillis,
            interval.toMillis(),
            TimeUnit.MILLISECONDS);

        Futures.addCallback(future, callback, executor);
    }
}
