package de.mosig.gigabitzauber.jancontrol.cruise;

import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;

public final class SimpleCruiseAlgorithm implements Runnable {

    private final Fan fan;
    private final Logger log;

    public SimpleCruiseAlgorithm(Fan fan, Logger logger) {
        this.fan = fan;
        this.log = logger;
    }

    @Override
    public void run() {
        var rpmCandidates = new ArrayList<Integer>();
        var dependants = fan.dependsOn();
        for (var i = 0; i < dependants.size() || Thread.currentThread().isInterrupted(); i++) {
            var dependant = dependants.get(i);
            rpmCandidates.add(fan.curve().getY(dependant.read()));
        }

        if (Thread.currentThread().isInterrupted()) {
            log.info("Cruise command got interrupted. Shutting down..");
        } else {
            var newRpm = Collections.max(rpmCandidates);
            //fan.device().write(Collections.max(rpmCandidates));
            log.info("new rpm: {}", newRpm);
        }
    }
}
