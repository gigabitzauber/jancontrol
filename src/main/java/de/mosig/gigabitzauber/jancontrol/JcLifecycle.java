package de.mosig.gigabitzauber.jancontrol;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.slf4j.Logger;
import org.springframework.context.Lifecycle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class JcLifecycle implements Lifecycle {
    private final ExecutorService fanCruiseExecutor;
    private final Logger log;

    public JcLifecycle(ExecutorService fanCruiseExecutor, Logger log) {
        this.fanCruiseExecutor = fanCruiseExecutor;
        this.log = log;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        log.info("Caught shutdown request. Shutting down..");
        fanCruiseExecutor.shutdownNow();
        var terminationFailed = true;
        try {
            terminationFailed = !fanCruiseExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new JcException("Interrupted while waiting for fan cruise to stop", e);
        }

        if (terminationFailed) {
            throw new JcException("Fan cruise executor termination timed out");
        }
    }

    @Override
    public boolean isRunning() {
        return true;
    }
}
