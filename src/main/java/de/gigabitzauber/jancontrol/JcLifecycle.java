package de.gigabitzauber.jancontrol;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.gigabitzauber.jancontrol.cruise.CruiseInstance;
import de.gigabitzauber.jancontrol.cruise.JcSchedulable;
import de.gigabitzauber.jancontrol.cruise.ModeEnforcer;
import de.gigabitzauber.jancontrol.cruise.NopCruise;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.RegisteredFan;
import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.error.JcSchedulableException;
import de.gigabitzauber.jancontrol.util.JcTime;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.springframework.context.Lifecycle;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JcLifecycle implements Lifecycle, FutureCallback<Object> {
    static final int ERROR_THRESHOLD = 3;
    static final int ERROR_COOL_OFF_MILLIS = 10000;

    private final ListeningScheduledExecutorService fanCruiseExecutor;
    private final JcTime time;
    private final Logger log;

    private final Collection<RegisteredFan> registeredFans = new HashSet<>();
    private final Map<String, Integer> measurementRecord = new HashMap<>();
    private final Map<String, JcErrorRecord> schedulableErrorRecord = new HashMap<>();

    public JcLifecycle(ListeningScheduledExecutorService fanCruiseExecutor, JcTime time, Logger log) {
        this.fanCruiseExecutor = fanCruiseExecutor;
        this.time = time;
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

    public void nop() {
        NopCruise.create().schedule(fanCruiseExecutor, this);
    }

    public void register(Fan fan) {
        var fanDevice = fan.device();
        log.info("Registering fan '{}' with allowIdle: {} and activation threshold: {}%",
            fanDevice.getName(), fan.allowIdle(), fanDevice.getActivationThreshold());
        registeredFans.add(new RegisteredFan(fan));

        var manualMode = fan.activateManualMode();

        CruiseInstance.create(fan, this, log).schedule(fanCruiseExecutor, this);
        ModeEnforcer.create(fan, manualMode, log).schedule(fanCruiseExecutor, this);
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
            var failedSchedulable = e.getParent();
            var newErrorCount = -1;
            synchronized (schedulableErrorRecord) {
                cleanupOldErrors();
                newErrorCount = computeNewSchedulableErrorCount(failedSchedulable, e);
            }
            if (newErrorCount > ERROR_THRESHOLD) {
                log.error("Schedulable {} exhausted error threshold of {} for error: {}", failedSchedulable.id(), ERROR_THRESHOLD, e.getMessage(), e);
            } else {
                log.debug("Schedulable {} encountered error #{}: {}", failedSchedulable.id(), newErrorCount, t.getMessage());
            }
            e.getParent().schedule(fanCruiseExecutor, this);
        } else {
            log.error("Encountered unexpected error", t);
        }
    }

    private int computeNewSchedulableErrorCount(JcSchedulable failedSchedulable, JcSchedulableException error) {
        var errorHash = error.getMessage().hashCode();
        var key = failedSchedulable.id() + ":" + errorHash;

        if (schedulableErrorRecord.containsKey(key)) {
            var errorRecord = schedulableErrorRecord.get(key);
            errorRecord.increaseCount();
        } else {
            schedulableErrorRecord.put(key, new JcErrorRecord(time.currentTimestampMillis()));
        }

        return schedulableErrorRecord.get(key).getCount();
    }

    private void cleanupOldErrors() {
        var oldErrors = schedulableErrorRecord.entrySet().stream()
            .filter(entry -> entry.getValue().isOutdated(time.currentTimestampMillis()))
            .map(Map.Entry::getKey)
            .toList();
        oldErrors.forEach(schedulableErrorRecord::remove);
    }

    private static final class JcErrorRecord {
        private int count = 1;
        private final long recordMillis;

        JcErrorRecord(long recordTime) {
            this.recordMillis = recordTime;
        }

        boolean isOutdated(long currentTimeMillis) {
            return currentTimeMillis - recordMillis > ERROR_COOL_OFF_MILLIS;
        }

        void increaseCount() {
            count++;
        }

        int getCount() {
            return count;
        }
    }
}
