package de.mosig.gigabitzauber.jancontrol;

import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import de.mosig.gigabitzauber.jancontrol.domain.ReadOnlyDevice;
import de.mosig.gigabitzauber.jancontrol.domain.WriteableDevice;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.slf4j.Logger;
import org.springframework.context.Lifecycle;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class JcLifecycle implements Lifecycle {
    private static final int FAN_MODE_MANUAL = 1;

    private final ExecutorService fanCruiseExecutor;
    private final Logger log;

    private final Collection<RegisteredFan> registeredFans = new HashSet<>();

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
            try {
                terminationFailed = !fanCruiseExecutor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new JcException("Interrupted while waiting for fan cruise to stop", e);
            }

            if (terminationFailed) {
                throw new JcException("Fan cruise executor termination timed out");
            }
        } finally {
            for (var registeredFan : this.registeredFans) {
                registeredFan.fan.device().write(Integer.parseInt(registeredFan.origRpm));
                registeredFan.fanMode.write(Integer.parseInt(registeredFan.origFanMode));
            }
        }
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    public void register(Fan fan) {
        String fanModeDeviceRawPath = fan.device().getSysPath() + "_enable";
        var fanModeDeviceRo = new ReadOnlyDevice(fan.device().getName() + " Fan Mode R/O", fanModeDeviceRawPath, 0);
        var fanModeDevice = new WriteableDevice(fan.device().getName() + " Fan Mode", fanModeDeviceRawPath);
        var origFanMode = fanModeDeviceRo.readRaw();
        fanModeDevice.write(FAN_MODE_MANUAL);
        var fanDevice = new ReadOnlyDevice(fan.device().getName() + "R/O", fan.device().getSysPath(), 0);

        var origRpm = fanDevice.readRaw();

        var regFan = new RegisteredFan(fan, origRpm, fanModeDevice, origFanMode);
        log.info("Registered Fan: {}", regFan);
        registeredFans.add(regFan);
    }

    private static record RegisteredFan(Fan fan, String origRpm, WriteableDevice fanMode, String origFanMode) {

    }
}
