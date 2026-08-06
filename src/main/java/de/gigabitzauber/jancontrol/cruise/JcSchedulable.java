package de.gigabitzauber.jancontrol.cruise;

import com.google.common.util.concurrent.FutureCallback;
import de.gigabitzauber.jancontrol.error.JcSchedulableException;

import java.time.Duration;
import java.util.UUID;
import java.util.random.RandomGenerator;

import static de.gigabitzauber.jancontrol.util.JcErrorUtil.safeGetMessage;
import static java.util.Objects.requireNonNull;

public abstract class JcSchedulable {

    protected final RandomGenerator rnd = RandomGenerator.getDefault();

    private final Runnable op;
    private final String opName;
    private final Duration initialMaxDelay;
    private final Duration interval;
    private final String id;

    protected JcSchedulable(Runnable op, String opName, Duration initialMaxDelay, Duration interval) {
        this.op = op;
        this.opName = opName;
        this.initialMaxDelay = initialMaxDelay;
        this.interval = interval;

        this.id = "%s@%s".formatted(this.opName, UUID.randomUUID().toString());
    }

    public final String id() {
        return id;
    }

    public final void schedule(FanCruiseExecutor executor, FutureCallback<Object> callback) {
        var initialDelay = Duration.ofMillis(randomizeInitialDelay());
        internalSchedule(executor, callback, initialDelay);
    }

    public final void reSchedule(FanCruiseExecutor executor, FutureCallback<Object> callback) {
        var initialDelayMillis = randomizeInitialDelay();
        internalSchedule(executor, callback, this.interval.plusMillis(initialDelayMillis));
    }

    private void internalSchedule(FanCruiseExecutor executor, FutureCallback<Object> callback, Duration initialDelay) {
        requireNonNull(executor, "executor must not be null");
        requireNonNull(callback, "callback must not be null");

        executor.scheduleAtFixedRate(
            () -> {
                try {
                    op.run();
                } catch (Exception e) {
                    var errMsg = this.opName + " ran into error: " + safeGetMessage(e);
                    throw new JcSchedulableException(errMsg, this, e);
                }
            },
            initialDelay,
            interval,
            callback);
    }

    private long randomizeInitialDelay() {
        return rnd.nextLong(initialMaxDelay.toMillis());
    }
}
