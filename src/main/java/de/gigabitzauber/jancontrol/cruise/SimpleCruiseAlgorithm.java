package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.Fan;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

public final class SimpleCruiseAlgorithm implements Runnable {

    private final Fan fan;
    private final JcLifecycle lifecycle;
    private final Logger log;
    private int downStepCount = 0;
    private boolean firstRun = true;

    public SimpleCruiseAlgorithm(Fan fan, JcLifecycle lifecycle, Logger log) {
        this.fan = requireNonNull(fan, "fan must not be null");
        this.lifecycle = requireNonNull(lifecycle, "lifecycle must not be null");
        this.log = requireNonNull(log, "log must not be null");
    }

    @Override
    public void run() {
        var rpmCandidates = new ArrayList<RpmCandidate>();
        var dependencies = fan.dependsOn();
        var curves = fan.curves();
        var targetDeviceName = fan.device().getName();
        var currentRpm = fan.device().read();
        for (var i = 0; i < dependencies.size() || Thread.currentThread().isInterrupted(); i++) {
            var dependency = dependencies.get(i);
            curves.stream().filter(curve -> curve.ref().equals(dependency.getName()))
                .findFirst()
                .ifPresent(curve -> {
                    int measurement = dependency.read();
                    lifecycle.record(dependency.getName(), measurement);
                    var targetRpm = curve.getY(measurement);
                    rpmCandidates.add(new RpmCandidate(dependency.getName(), measurement, currentRpm, targetRpm, targetDeviceName));
                });
        }

        if (Thread.currentThread().isInterrupted()) {
            log.info("Cruise command got interrupted. Shutting down..");
        } else if (!rpmCandidates.isEmpty()) {
            var rawNewRpm = Collections.max(rpmCandidates);
            var safeNewRpm = safeGetTargetRpm(fan, rawNewRpm);
            var rpmPercentageToSet = safeNewRpm.targetRpm;
            var downSkip = fan.downSkip();
            if (firstRun || rpmPercentageToSet >= currentRpm || downStepCount == downSkip) {
                firstRun = false;
                downStepCount = 0;
                var actuallyWrittenValue = fan.device().write(rpmPercentageToSet);
                log.debug(new RpmCandidate(safeNewRpm, actuallyWrittenValue).toString());
            } else {
                log.debug("Skipped, because downSkip: {} vs. downStepCount: {} - {}", downSkip, downStepCount, safeNewRpm);
                downStepCount++;
                fan.device().write(currentRpm);
            }
        }
    }

    private RpmCandidate safeGetTargetRpm(Fan targetFan, RpmCandidate newRpm) {
        var targetRpmValue = newRpm.targetRpm;
        var rpmSafetyMargin = targetFan.device().safetyMargin();
        var lowestSafeRpmValue = rpmSafetyMargin.lowerEndpoint();
        var highestSafeRpmValue = rpmSafetyMargin.upperEndpoint();

        if (!targetFan.device().allowIdle() && targetRpmValue < lowestSafeRpmValue) {
            logRpmLimitSafetyWarning(newRpm.targetDeviceName);
            targetRpmValue = lowestSafeRpmValue;
            log.warn("Setting RPM value for {} to lowest allowed value: {}", newRpm.targetDeviceName, lowestSafeRpmValue);
        }

        if (targetRpmValue > highestSafeRpmValue) {
            logRpmLimitSafetyWarning(newRpm.targetDeviceName);
            targetRpmValue = highestSafeRpmValue;
            log.warn("Setting RPM value for {} to highest allowed value: {}", newRpm.targetDeviceName, highestSafeRpmValue);
        }

        return new RpmCandidate(newRpm, targetRpmValue);
    }

    public static record RpmCandidate(
        String dependencyName, int measurement, int currentRpm, int targetRpm, String targetDeviceName) implements Comparable<RpmCandidate> {

        public RpmCandidate(RpmCandidate other, int targetRpmOverride) {
            this(other.dependencyName, other.measurement, other.currentRpm, targetRpmOverride, other.targetDeviceName);
        }

        @Override
        public int compareTo(RpmCandidate other) {
            return this.targetRpm - other.targetRpm;
        }

        @Override
        @Nonnull
        public String toString() {
            return "Setting %s = %d%% (was: %d%%) | Reason: %s: %d°".formatted(targetDeviceName, targetRpm, currentRpm, dependencyName, measurement);
        }
    }

    private void logRpmLimitSafetyWarning(String targetDeviceName) {
        log.warn("Calculated RPM value for {} exceeds safe limits.", targetDeviceName);
    }
}
