package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.util.JcCruiseUtil;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

public final class CruiseAlgorithm implements Runnable {

    private final Fan fan;
    private final JcLifecycle lifecycle;
    private final Logger log;
    private int downStepCount = 0;
    private boolean firstRun = true;

    public CruiseAlgorithm(Fan fan, JcLifecycle lifecycle, Logger log) {
        this.fan = requireNonNull(fan, "fan must not be null");
        this.lifecycle = requireNonNull(lifecycle, "lifecycle must not be null");
        this.log = requireNonNull(log, "log must not be null");
    }

    @Override
    public void run() {
        var dependencies = fan.dependsOn();
        var curves = fan.curves();
        var targetDeviceRef = fan.device().getRef();
        var currentRpm = fan.device().read();
        var rpmCandidates = new ArrayList<RpmCandidate>();
        for (var i = 0; i < dependencies.size() || Thread.currentThread().isInterrupted(); i++) {
            var dependency = dependencies.get(i);
            curves.stream().filter(curve -> curve.ref().equals(dependency.getRef()))
                .findFirst()
                .ifPresent(curve -> {
                    int measurement = dependency.read();
                    lifecycle.record(dependency.getRef(), measurement);
                    var targetRpm = curve.getY(measurement);
                    rpmCandidates.add(new RpmCandidate(dependency.getRef(), measurement, currentRpm, targetRpm, targetDeviceRef));
                });
        }

        if (Thread.currentThread().isInterrupted()) {
            log.info("Cruise got interrupted. Shutting down..");
        } else if (!rpmCandidates.isEmpty()) {
            var rpmCand = postProcess(rpmCandidates);
            var rpmToSet = rpmCand.targetRpm;

            var downSkip = fan.downSkip();
            if (firstRun || rpmToSet >= currentRpm || downStepCount == downSkip) {
                firstRun = false;
                downStepCount = 0;
                var actuallyWrittenValue = fan.device().write(rpmToSet);
                log.debug(new RpmCandidate(rpmCand, actuallyWrittenValue).toString());
            } else {
                log.debug("Skipped, because downStepCount: {} vs. downSkip: {} - {}", downStepCount, downSkip, rpmCand);
                downStepCount++;
                fan.device().write(currentRpm);
            }
        }
    }

    private RpmCandidate postProcess(Collection<RpmCandidate> rpmCandidates) {
        var rawNewRpmCand = Collections.max(rpmCandidates);
        var safeNewRpmCand = safeGetTargetRpmCand(fan, rawNewRpmCand);
        return smoothRpmCand(fan, safeNewRpmCand);
    }

    private RpmCandidate smoothRpmCand(Fan fan, RpmCandidate rawRpmCand) {
        var smoothRpmValue = JcCruiseUtil.getNearestMultiple(rawRpmCand.targetRpm, fan.n());

        return new RpmCandidate(rawRpmCand, smoothRpmValue);
    }

    private RpmCandidate safeGetTargetRpmCand(Fan targetFan, RpmCandidate rawRpmCand) {
        var targetRpmValue = rawRpmCand.targetRpm;
        var rpmSafetyMargin = targetFan.device().safetyMargin();
        var lowestSafeRpmValue = rpmSafetyMargin.lowerEndpoint();
        var highestSafeRpmValue = rpmSafetyMargin.upperEndpoint();
        var targetDeviceName = rawRpmCand.targetDeviceName;

        if (!targetFan.device().allowIdle() && targetRpmValue < lowestSafeRpmValue) {
            logRpmLimitSafetyWarning(targetDeviceName);
            targetRpmValue = lowestSafeRpmValue;
            log.warn("Setting RPM value for {} to lowest allowed value: {}", targetDeviceName, lowestSafeRpmValue);
        }

        if (targetRpmValue > highestSafeRpmValue) {
            logRpmLimitSafetyWarning(targetDeviceName);
            targetRpmValue = highestSafeRpmValue;
            log.warn("Setting RPM value for {} to highest allowed value: {}", targetDeviceName, highestSafeRpmValue);
        }

        return new RpmCandidate(rawRpmCand, targetRpmValue);
    }

    public static record RpmCandidate(
        String dependencyRef, int measurement, int currentRpm, int targetRpm, String targetDeviceName) implements Comparable<RpmCandidate> {

        public RpmCandidate(RpmCandidate other, int targetRpmOverride) {
            this(other.dependencyRef, other.measurement, other.currentRpm, targetRpmOverride, other.targetDeviceName);
        }

        @Override
        public int compareTo(RpmCandidate other) {
            return this.targetRpm - other.targetRpm;
        }

        @Override
        @Nonnull
        public String toString() {
            return "Setting %s = %d%% (was: %d%%) | Reason: %s: %d°".formatted(targetDeviceName, targetRpm, currentRpm, dependencyRef, measurement);
        }
    }

    private void logRpmLimitSafetyWarning(String targetDeviceName) {
        log.warn("Calculated RPM value for {} exceeds safe limits.", targetDeviceName);
    }
}
