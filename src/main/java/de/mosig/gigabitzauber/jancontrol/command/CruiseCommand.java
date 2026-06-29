package de.mosig.gigabitzauber.jancontrol.command;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import de.mosig.gigabitzauber.jancontrol.domain.JcConfig;
import de.mosig.gigabitzauber.jancontrol.util.CruiseInstance;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;

@Component
public class CruiseCommand {

    private final ListeningScheduledExecutorService fanCruiseExecutor;
    private final Logger log;

    public CruiseCommand(ListeningScheduledExecutorService fanCruiseExecutor, Logger logger) {
        this.fanCruiseExecutor = fanCruiseExecutor;
        this.log = logger;
    }

    public void execute(JcConfig config) {
        var fan = config.fans().stream().findFirst().get();
        var interval = fan.interval();

        new CruiseInstance(fan, interval, fanCruiseExecutor, log).schedule();
    }

    public static final class FanCruiseRunnable implements Runnable {

        private final Fan fan;
        private final Logger log;

        public FanCruiseRunnable(Fan fan, Logger logger) {
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
}
