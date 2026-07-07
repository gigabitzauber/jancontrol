package de.mosig.gigabitzauber.jancontrol;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.mosig.gigabitzauber.jancontrol.cruise.CruiseInstance;
import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import de.mosig.gigabitzauber.jancontrol.domain.RwSysFile;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.slf4j.Logger;
import org.springframework.context.Lifecycle;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JcLifecycle implements Lifecycle {
    static final String FAN_MODE_MANUAL = "1";

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
        for (var registeredFan : this.registeredFans) {
            registeredFan.fan.device().write(registeredFan.origRpm);
            registeredFan.fanModeDevice.writeRaw(registeredFan.origFanMode);
        }
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
        String fanModeFileRawPath = fan.device().getSysPath() + "_enable";
        var fanModeDevice = new RwSysFile(fanModeFileRawPath);
        var origFanMode = fanModeDevice.readRaw().strip();
        fanModeDevice.writeRaw(FAN_MODE_MANUAL);
        var origRpm = fan.device().read();

        var regFan = new RegisteredFan(fan, origRpm, fanModeDevice, origFanMode);
        registeredFans.add(regFan);

        CruiseInstance.create(fan, this, fanCruiseExecutor, log).schedule();
    }

    public synchronized void record(String dependantName, int measurement) {
        measurementRecord.compute(dependantName, (_, nullableOldMeasurement) -> {
            var oldMeasurement = nullableOldMeasurement == null ? 0 : nullableOldMeasurement;
            return Math.max(oldMeasurement, measurement);
        });
    }

    private static record RegisteredFan(Fan fan, int origRpm, RwSysFile fanModeDevice, String origFanMode) {

    }
}
