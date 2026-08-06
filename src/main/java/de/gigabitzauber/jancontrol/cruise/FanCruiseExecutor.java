package de.gigabitzauber.jancontrol.cruise;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.gigabitzauber.jancontrol.error.JcException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class FanCruiseExecutor {
    private final AtomicReference<ListeningScheduledExecutorService> executorServiceRef = new AtomicReference<>();
    private final Supplier<ListeningScheduledExecutorService> executorServiceSupplier;

    public FanCruiseExecutor(Supplier<ListeningScheduledExecutorService> executorServiceSupplier) {
        this.executorServiceSupplier = requireNonNull(executorServiceSupplier, "executorServiceSupplier must not be null");
        this.executorServiceRef.set(executorServiceSupplier.get());
    }

    public void scheduleAtFixedRate(
        Runnable command,
        Duration initialDelay,
        Duration period,
        FutureCallback<Object> callback) {
        var localExecutor = this.executorServiceRef.get();

        var future = localExecutor.scheduleAtFixedRate(command, initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
        Futures.addCallback(future, callback, localExecutor);
    }

    public synchronized void terminate() {
        internalTerminate(this.executorServiceRef.get());
    }

    private void internalTerminate(ListeningScheduledExecutorService localExecutor) {
        localExecutor.shutdownNow();
        
        var terminationFailed = true;
        try {
            terminationFailed = !localExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new JcException("Interrupted while waiting for fan cruise to stop", e);
        }

        if (terminationFailed) {
            throw new JcException("Fan cruise executor termination timed out");
        }
    }

    public synchronized void reInitialize() {
        var oldExecutor = executorServiceRef.getAndSet(executorServiceSupplier.get());
        internalTerminate(oldExecutor);
    }
}
