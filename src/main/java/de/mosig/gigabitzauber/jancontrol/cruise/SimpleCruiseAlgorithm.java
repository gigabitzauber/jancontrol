package de.mosig.gigabitzauber.jancontrol.cruise;

import com.google.common.collect.Range;
import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

public final class SimpleCruiseAlgorithm implements Runnable {

    private static final Range<Integer> VALID_WRITE_RANGE = Range.closed(20, 100);

    private final Fan fan;
    private final Logger log;

    public SimpleCruiseAlgorithm(Fan fan, Logger log) {
        this.fan = requireNonNull(fan, "fan must not be null");
        this.log = requireNonNull(log, "log must not be null");
    }

    @Override
    public void run() {
        var rpmCandidates = new ArrayList<RpmCandidate>();
        var dependants = fan.dependsOn();
        var curves = fan.curves();
        var targetDeviceName = fan.device().getName();
        for (var i = 0; i < dependants.size() || Thread.currentThread().isInterrupted(); i++) {
            var dependant = dependants.get(i);
            curves.stream().filter(curve -> curve.ref().equals(dependant.getName()))
                .findFirst()
                .ifPresent(curve -> {
                    int measurement = dependant.read();
                    var targetRpm = curve.getY(measurement);
                    rpmCandidates.add(new RpmCandidate(dependant.getName(), measurement, targetRpm, targetDeviceName));
                });
        }

        if (Thread.currentThread().isInterrupted()) {
            log.info("Cruise command got interrupted. Shutting down..");
        } else {
            var newRpm = Collections.max(rpmCandidates);
            var targetRpmValue = safeGetTargetRpm(newRpm);
            fan.device().write(targetRpmValue);
            log.debug(newRpm.toString());
        }
    }

    private int safeGetTargetRpm(RpmCandidate newRpm) {
        var targetRpmValue = newRpm.targetRpm;
        if (!VALID_WRITE_RANGE.contains(targetRpmValue)) {
            log.warn("Calculated RPM value for {} exceeds safe limits.", newRpm.targetDeviceName);
            var lowestSafeRpmValue = VALID_WRITE_RANGE.lowerEndpoint();
            if (targetRpmValue < lowestSafeRpmValue) {
                targetRpmValue = lowestSafeRpmValue;
                log.warn("Setting RPM value for {} to lowest allowed value: {}", newRpm.targetDeviceName, lowestSafeRpmValue);
            } else {
                var highestSafeRpmValue = VALID_WRITE_RANGE.upperEndpoint();
                if (targetRpmValue > highestSafeRpmValue) {
                    targetRpmValue = highestSafeRpmValue;
                    log.warn("Setting RPM value for {} to highest allowed value: {}", newRpm.targetDeviceName, highestSafeRpmValue);
                }
            }
        }

        return targetRpmValue;
    }

    private static record RpmCandidate(
        String dependantName, int measurement, int targetRpm, String targetDeviceName) implements Comparable<RpmCandidate> {
        @Override
        public int compareTo(RpmCandidate other) {
            return this.targetRpm - other.targetRpm;
        }
    }
}
