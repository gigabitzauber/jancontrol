package de.mosig.gigabitzauber.jancontrol.cruise;

import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;

public final class SimpleCruiseAlgorithm implements Runnable {

    private final Fan fan;
    private final Logger log;

    public SimpleCruiseAlgorithm(Fan fan, Logger log) {
        this.fan = fan;
        this.log = log;
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
            var targetRpmValue = newRpm.targetRpm;
            if (targetRpmValue < 20) {
                targetRpmValue = 20;
            }
            fan.device().write(targetRpmValue);
            log.debug(newRpm.toString());
        }
    }

    private static record RpmCandidate(
        String dependantName, int measurement, int targetRpm, String targetDeviceName) implements Comparable<RpmCandidate> {
        @Override
        public int compareTo(RpmCandidate other) {
            return this.targetRpm - other.targetRpm;
        }
    }
}
