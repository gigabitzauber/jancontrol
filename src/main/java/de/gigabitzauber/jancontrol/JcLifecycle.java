package de.gigabitzauber.jancontrol;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.gigabitzauber.jancontrol.cruise.CruiseInstance;
import de.gigabitzauber.jancontrol.cruise.ModeEnforcer;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.FanModes;
import de.gigabitzauber.jancontrol.domain.RegisteredFan;
import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.error.JcSchedulableException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.springframework.context.Lifecycle;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JcLifecycle implements Lifecycle, FutureCallback<Object> {
    private final ListeningScheduledExecutorService fanCruiseExecutor;
    private final Logger log;

    private final Collection<RegisteredFan> registeredFans = new HashSet<>();
    private final Map<String, Integer> measurementRecord = new HashMap<>();

    public JcLifecycle(ListeningScheduledExecutorService fanCruiseExecutor, Logger log) {
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
            try {
                terminationFailed = !fanCruiseExecutor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new JcException("Interrupted while waiting for fan cruise to stop", e);
            }

            if (terminationFailed) {
                throw new JcException("Fan cruise executor termination timed out");
            }
        } finally {
            restoreOldFanConfig();
            printStats();
        }
    }

    private void restoreOldFanConfig() {
        registeredFans.forEach(RegisteredFan::restoreOrigSettings);
    }

    private void printStats() {
        log.info("=== Stats ===");
        for (var recordedMeasurement : this.measurementRecord.entrySet()) {
            log.info("Highest measurement for {}: {}", recordedMeasurement.getKey(), recordedMeasurement.getValue());
        }
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    public void register(Fan fan) {
        registeredFans.add(new RegisteredFan(fan));

        fan.setMode(FanModes.MANUAL);

        CruiseInstance.create(fan, this, log).schedule(fanCruiseExecutor, this);
        ModeEnforcer.create(fan, FanModes.MANUAL, log).schedule(fanCruiseExecutor, this);
    }

    public synchronized void record(String dependencyName, int measurement) {
        measurementRecord.compute(dependencyName, (_, nullableOldMeasurement) -> {
            var oldMeasurement = nullableOldMeasurement == null ? 0 : nullableOldMeasurement;
            return Math.max(oldMeasurement, measurement);
        });
    }

    @Override
    public void onSuccess(Object result) {

    }

    @Override
    public void onFailure(@NonNull Throwable t) {
        if (t instanceof JcSchedulableException e) {
            e.getParent().schedule(fanCruiseExecutor, this);
            log.error(e.getMessage(), e);
        } else {
            log.error("Encountered unexpected error", t);
        }
    }
}
